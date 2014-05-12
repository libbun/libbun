package libbun.parser.ssa;

import libbun.ast.AstNode;
import libbun.ast.LocalDefinedNode;
import libbun.common.CommonArray;
import libbun.util.Var;

public class JoinNode extends LocalDefinedNode {
	private final CommonArray<PHINode> PHIs;

	public JoinNode(AstNode Parent) {
		super(null, 0);
		Parent.SetChild(this, true);
		this.PHIs = new CommonArray<PHINode>(new PHINode[0]);
	}

	public final void Append(PHINode Node) {
		this.PHIs.add(Node);
	}

	public final int getPhiSize() {
		return this.PHIs.size();
	}

	public final PHINode ListAt(int Index) {
		return CommonArray.GetIndex(this.PHIs, Index);
	}

	public PHINode FindPHINode(Variable Var) {
		@Var int i = 0;
		while(i < this.getPhiSize()) {
			PHINode Node = this.ListAt(i);
			if(Node.IsSameVariable(Var)) {
				return Node;
			}
			i = i + 1;
		}
		return null;
	}

	public boolean isJoinNodeOfRepeatNode() {
		// FIXME
		return false;
	}
}