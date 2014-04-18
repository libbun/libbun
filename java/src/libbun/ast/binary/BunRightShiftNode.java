package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.BunVisitor;
import libbun.parser.LibBunVisitor;

public class BunRightShiftNode extends BitwiseOperatorNode {
	public BunRightShiftNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleSHIFT);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunRightShiftNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return ">>";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitRightShiftNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}

}
