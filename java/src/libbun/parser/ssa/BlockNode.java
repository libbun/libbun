package libbun.parser.ssa;

import java.util.HashMap;
import java.util.HashSet;

import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZNode;
import libbun.parser.ast.ZNullNode;
import libbun.parser.ast.ZSetNameNode;
import libbun.parser.ast.ZVarBlockNode;
import libbun.util.Var;
import libbun.util.ZArray;

class BlockNode {
	ZBlockNode			block;
	int					preorderId;	/* Depth First Number, Preorder */
	int					rpostoderId;	/* Depth First Number, Reverse Postorder */
	ZArray<BlockNode>	preds;			/* List of predecessor nodes */
	ZArray<BlockNode>	succs;			/* List of successor nodes */
	ZArray<BlockNode>	children;		/* Subnodes in the Dominator Tree */
	HashSet<BlockNode>		dfront;		/* Dominance Frontier */
	BlockNode			idom;			/* Immediate Dominator */
	BitMap			domset;		/* Dominator Set Bit Vector */
	ZArray<PHINode>		phis;

	public BlockNode(ZBlockNode block) {
		this.idom = null;
		this.block = block;
		this.preds = new ZArray<BlockNode>(new BlockNode[0]);
		this.succs = new ZArray<BlockNode>(new BlockNode[0]);
		this.phis = new ZArray<PHINode>(new PHINode[0]);
	}

	void addedge(BlockNode ToNode) {
		this.succs.add(ToNode);
		ToNode.preds.add(this);
	}

	/* Compute Depth First Spanning Tree */
	int dfst(int preorderId, int rpostoderId) {
		int n = 1;
		this.preorderId = preorderId;

		@Var int i = 0;
		while(i < this.succs.size()) {
			BlockNode x = ZArray.GetIndex(this.succs, i);
			if(x.preorderId == 0) {
				n += x.dfst(preorderId + n, rpostoderId - n + 1);
			}
			i = i + 1;
		}
		this.rpostoderId = rpostoderId - n + 1;
		return n;
	}

	/* Print Dominator Tree */
	void printdomtree(int level) {
		if(SSAConveter.DEBUG_DOMTREE) {
			@Var int i = 0;
			for(i = 0; i < level; i++) {
				System.out.print("    ");
			}
			System.out.print(this.block + " (");
			Object[] dfronts = this.dfront.toArray();
			i = 0;
			while(i < dfronts.length) {
				System.out.print(((BlockNode)dfronts[i]).block + " ");
			}
			System.out.println(")");

			i = 0;
			while(i < this.children.size()) {
				BlockNode x = ZArray.GetIndex(this.children, i);
				x.printdomtree(level + 1);
				i = i + 1;
			}
		}
	}

	/* Compute Dominance Frontier */
	void domfront() {
		@Var int i = 0;
		while(i < this.children.size()) {
			BlockNode x = ZArray.GetIndex(this.children, i);
			x.domfront();
			i = i + 1;
		}
		this.dfront = new HashSet<BlockNode>();

		while(i < this.succs.size()) {
			BlockNode x = ZArray.GetIndex(this.succs, i);
			if((x).idom != this) {
				this.dfront.add(x);
			}
			i = i + 1;
		}
		while(i < this.children.size()) {
			BlockNode x = ZArray.GetIndex(this.children, i);
			@Var int j = 0;
			while(j < x.dfront.size()) {
				BlockNode y = ZArray.GetIndex(this.children, j);
				if(y.idom != this) {
					this.dfront.add(y);
				}
				j = j + 1;
			}
			i = i + 1;
		}
	}

	void addPHI(ZNode Node) {
		@Var int i = 0;
		while(i < this.phis.size()) {
			PHINode phi = ZArray.GetIndex(this.phis, i);
			@Var int j = 0;
			while(j < phi.Args.size()) {
				ZNode Inst = ZArray.GetIndex(phi.Args, j);
				if(Inst == Node) {
					return;
				}
				j = j + 1;
			}
			i = i + 1;
		}

		PHINode phi = new PHINode(Node);
		phi.AddIncoming(this.block, Node);
		this.phis.add(phi);
	}

	void InsertPHI(SSAConveter conveter) {
		@Var int i = 0;
		while(i < conveter.LocalVars.size()) {
			ZVarBlockNode Inst = ZArray.GetIndex(conveter.LocalVars, i);
			Object[] dfronts = this.dfront.toArray();
			i = 0;
			while(i < dfronts.length) {
				BlockNode x = (BlockNode)(dfronts[i]);
				x.addPHI(Inst);
			}
			i = i + 1;
		}
		while(i < this.children.size()) {
			BlockNode Node = ZArray.GetIndex(this.children, i);
			Node.InsertPHI(conveter);
			i = i + 1;
		}
	}

	void InsertUndefinedVariables(SSAConveter conveter) {
		int size = conveter.LocalVars.size();
		/* create undefined local variables at entryblock */
		HashMap<String, Integer> local = new HashMap<String, Integer>();
		int i = 0;
		for(i = 0; i < size; i++) {
			local.put(ZArray.GetIndex(conveter.LocalVars, i).VarDeclNode().GetName(), -1);
		}

		while(i < this.block.GetListSize()) {
			ZNode x = this.block.GetListAt(i);
			if(x instanceof ZSetNameNode) {
				ZSetNameNode Inst = (ZSetNameNode) x;
				local.put(Inst.GetName(), Inst.VarIndex);
			}
			i = i + 1;
		}

		ZBlockNode EntryBB = this.block;

		int change = 0;
		int TailIndex = EntryBB.GetListSize();
		for(i = 0; i < size; i++) {
			if(local.get(ZArray.GetIndex(conveter.LocalVars, i).VarDeclNode().GetName()) == -1) {
				ZNode LHS = ZArray.GetIndex(conveter.LocalVars, i);
				ZNode RHS = new ZNullNode(null, null);
				RHS.Type = LHS.Type;
				System.err.println("FIXME(000) support undefined local variables"); // FIXME
				//builder.CreateUpdate(LHS, RHS);
				change += 1;
			}
		}
		if(change > 0) {
			int CurrentSize = EntryBB.GetListSize();
			ZNode Terminator = EntryBB.GetListAt(TailIndex - 1);
			for(i = 0; i < CurrentSize - TailIndex; i++) {
				EntryBB.SetListAt(TailIndex - 1 + i, EntryBB.GetListAt(TailIndex + i));
			}
			EntryBB.SetListAt(CurrentSize - 1, Terminator);
		}
	}
}