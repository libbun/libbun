package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunDivNode extends ArithmeticOperatorNode {
	public BunDivNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleMUL);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunDivNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "/";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitDivNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
