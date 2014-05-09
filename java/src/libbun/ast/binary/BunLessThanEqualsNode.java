package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunLessThanEqualsNode extends ComparatorNode {

	public BunLessThanEqualsNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleCOMPARE);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunLessThanEqualsNode(ParentNode));
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
