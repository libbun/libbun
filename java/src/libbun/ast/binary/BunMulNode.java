package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunMulNode extends ArithmeticOperatorNode {
	public BunMulNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleMUL);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunMulNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "*";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitMulNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
