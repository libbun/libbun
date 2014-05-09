package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunLessThanNode extends ComparatorNode {
	public BunLessThanNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleCOMPARE);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunLessThanNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "<";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitLessThanNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
