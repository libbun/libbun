package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.BunVisitor;
import libbun.parser.BToken;
import libbun.parser.LibBunVisitor;

public class BunLessThanEqualsNode extends ComparatorNode {

	public BunLessThanEqualsNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleCOMPARE);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunLessThanEqualsNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "<=";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitLessThanEqualsNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}

}
