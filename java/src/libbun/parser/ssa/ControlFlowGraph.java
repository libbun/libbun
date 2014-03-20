package libbun.parser.ssa;

import java.util.HashMap;
import java.util.Stack;

import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZGetNameNode;
import libbun.parser.ast.ZNode;
import libbun.parser.ast.ZSetNameNode;
import libbun.parser.ast.ZVarBlockNode;
import libbun.util.Var;
import libbun.util.ZArray;

public class ControlFlowGraph {
	ZArray<BlockNode>	BlockNodes;
	BlockNode			EntryBlock;

	public ControlFlowGraph(ZBlockNode TopLevelBlock) {
		this.BlockNodes = new ZArray<BlockNode>(new BlockNode[TopLevelBlock.GetListSize()]);
		for (int i = 0; i < TopLevelBlock.GetListSize(); i++) {
			ZNode x = TopLevelBlock.GetListAt(i);
			this.CreateBlockNode(x);
		}

		@Var int i = 0;
		while(i < this.BlockNodes.size()) {
			BlockNode node = ZArray.GetIndex(this.BlockNodes, i);
			@Var int j = 0;
			while(j < node.succs.size()) {
				BlockNode x = ZArray.GetIndex(node.succs, j);
				node.addedge(x);
				j += 1;
			}
			i += 1;
		}
		this.EntryBlock = ZArray.GetIndex(this.BlockNodes, 0);
	}
	BlockNode CreateBlockNode(ZNode block) {
		if(block instanceof ZBlockNode) {
		}
		else {
			ZBlockNode NewBlock = new ZBlockNode(null, block.GetNameSpace());
			NewBlock.Append(block);
			block = NewBlock;
		}
		BlockNode p = new BlockNode((ZBlockNode)block);
		this.BlockNodes.add(p);
		return p;
	}

	/* Compute Dominators by Aho-Ullman's bit vector algorithm */
	void ComputeDominator() {
		int BlockNodeSize = this.BlockNodes.size();
		int w = (BlockNodeSize + BitMap.BITS - 1) / BitMap.BITS;
		BitMap work = new BitMap(w);

		BlockNode[] nodes = new BlockNode[BlockNodeSize + 1];
		@Var int Index = 0;
		while(Index < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, Index);
			x.preorderId = 0;
			x.domset = new BitMap(w);
			x.domset.clear(~0);
			Index = Index + 1;
		}

		this.EntryBlock.domset.clear(0);
		this.EntryBlock.dfst(1, BlockNodeSize);
		this.EntryBlock.domset.add(1);

		nodes[0] = null;

		Index = 0;
		while(Index < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, Index);
			nodes[x.rpostoderId] = x;
			Index = Index + 1;
		}

		boolean change;
		do {
			change = false;
			for(int i = 2; i <= BlockNodeSize; i++) {
				work.clear(~0);
				int k = 0;
				while(k < nodes[i].preds.size()) {
					BlockNode x = ZArray.GetIndex(nodes[i].preds, k);
					for(int j = 0; j < w; ++j) {
						work.SetAll(j, work.GetAll(j) & x.domset.GetAll(j));
					}
					k = k + 1;
				}
				work.add(i);
				for(int j = 0; j < w; ++j) {
					if(work.get(j) != nodes[i].domset.get(j)) {
						nodes[i].domset.SetAll(j, work.GetAll(j));
						change = true;
					}
				}

			}
		} while(change);

		/* Convert dominator sets to dominator tree */
		this.EntryBlock.idom = null;
		int l = 0;
		while(l < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, l);
			x.domset.Remove(x.rpostoderId);
			for(int i = w - 1; i >= 0; --i) {
				if(x.domset.get(i) != 0) {
					int bit = BitMap.highestbit(x.domset.get(i));
					//System.out.println(x.block + ":" + i + "," + bit);
					x.idom = nodes[(i * BitMap.BITS) + bit];
					break;
				}
			}
			x.domset = null;
			l = l + 1;
		}
	}

	/* Set up Subnode Pointers in the Dominator Tree */
	void SetupDomtree() {
		@Var int i = 0;
		while(i < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, i);
			x.children = new ZArray<BlockNode>(new BlockNode[0]);
			i = i + 1;
		}
		i = 0;
		while(i < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, i);
			if(x.idom != null) {
				x.idom.children.add(x);
			}
			i = i + 1;
		}
	}

	BlockNode GetBlockNode(ZNode block) {
		@Var int i = 0;
		while(i < this.BlockNodes.size()) {
			BlockNode x = ZArray.GetIndex(this.BlockNodes, i);
			if(x.block == block) {
				return x;
			}
			i = i + 1;
		}
		assert (false); // unreachable;
		return null;
	}

	void ComputeDominanceFrontier() {
		this.ComputeDominator();
		/* Compute Dominance Frontier */
		this.SetupDomtree();
		this.EntryBlock.domfront();
		/* Print Dominace Frontiers */
		this.EntryBlock.printdomtree(0);
	}

	void RenamingVariables(SSAConveter conveter) {
		int i = 0, size = conveter.LocalVars.size();
		HashMap<String, Stack<ZNode>> stack = new HashMap<String, Stack<ZNode>>();
		for(i = 0; i < size; i++) {
			stack.put(ZArray.GetIndex(conveter.LocalVars, i).VarDeclNode().GetName(), new Stack<ZNode>());
		}
		this.Rename(this.EntryBlock, stack);
	}

	private void NewName(ZNode NewVal, ZNode Node, HashMap<String, Stack<ZNode>> stack) {
		String n = null;
		if(Node instanceof ZGetNameNode) {
			n = ((ZGetNameNode)Node).GetName();
		}
		else {
			assert(Node instanceof ZVarBlockNode);
			n = ((ZVarBlockNode)Node).VarDeclNode().GetName();
		}
		stack.get(n).push(NewVal);
	}

	private ZNode TopStack(ZNode Node, HashMap<String, Stack<ZNode>> stack) {
		if(Node instanceof ZGetNameNode) {
			String n = ((ZGetNameNode)Node).GetName();
			return stack.get(n).peek();
		}
		else {
			assert(Node instanceof ZVarBlockNode);
			String n = ((ZVarBlockNode)Node).VarDeclNode().GetName();
			return stack.get(n).peek();
		}
	}

	private ZNode PopStack(ZNode Node, HashMap<String, Stack<ZNode>> stack) {
		if(Node instanceof ZGetNameNode) {
			String n = ((ZGetNameNode)Node).GetName();
			return stack.get(n).peek();
		}
		else {
			assert(Node instanceof ZSetNameNode);
			String n = ((ZVarBlockNode)Node).VarDeclNode().GetName();
			return stack.get(n).peek();
		}
	}

	private void FillIncoming(ZBlockNode BB, PHINode phi, HashMap<String, Stack<ZNode>> stack) {
		int Index = 0;
		while(Index < phi.Args.size()) {
			ZNode Inst = ZArray.GetIndex(phi.Args, Index);
			ZBlockNode Block = ZArray.GetIndex(phi.Blocks, Index);
			if(Block == BB) {
				ZNode Value = this.TopStack(Inst, stack);
				ZArray.SetIndex(phi.Args, Index, Value);
			}
			Index++;
		}
	}

	<T> void RewriteList(ZArray<T> List, HashMap<String, Stack<ZNode>> stack) {
		for(int i = 0; i < List.size(); i++) {
			ZArray.SetIndex(List, i, this.RewriteInst(ZArray.GetIndex(List, i), stack));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T RewriteInst(T Inst, HashMap<String, Stack<ZNode>> stack) {
		if(Inst instanceof ZVarBlockNode) {
			ZNode Node = this.TopStack((ZVarBlockNode) Inst, stack);
			if(Node == null) {
				throw new RuntimeException("Error");
			}
			return (T) Node;
		}
		return Inst;
	}

	void Rename(BlockNode b, HashMap<String, Stack<ZNode>> stack) {
		int i = 0;
		for (i = 0; i < b.block.GetListSize(); i++) {
			ZNode Inst = b.block.GetListAt(i);
			if(Inst instanceof PHINode) {
				PHINode PHI = (PHINode) Inst;
				this.NewName(PHI, PHI.Value, stack);
			}
		}
		for (i = 0; i < b.block.GetListSize(); i++) {
			ZNode Inst = b.block.GetListAt(i);
			this.RewriteInst(Inst, stack);
		}

		while(i < b.succs.size()) {
			BlockNode x = ZArray.GetIndex(b.succs, i);
			for (int j = 0; j < x.block.GetListSize(); j++) {
				ZNode Inst = x.block.GetListAt(j);
				if(Inst instanceof PHINode) {
					PHINode PHI = (PHINode) Inst;
					this.FillIncoming(b.block, PHI, stack);
				}
			}
			i = i + 1;
		}

		/* Rename each successor s of b in the dominator tree */
		while(i < b.children.size()) {
			BlockNode x = ZArray.GetIndex(b.children, i);
			this.Rename(x, stack);
			i = i + 1;
		}

		/* clear stack */
		for (i = 0; i < b.block.GetListSize(); i++) {
			ZNode Inst = b.block.GetListAt(i);
			if(Inst instanceof PHINode) {
				PHINode phi = (PHINode) Inst;
				this.PopStack(phi.Value, stack);
			}
			if(Inst instanceof ZSetNameNode) {
				ZSetNameNode update = (ZSetNameNode) Inst;
				this.PopStack(update, stack);
			}
		}
	}

}