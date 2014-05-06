package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunSubNode extends ArithmeticOperatorNode {
	public BunSubNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleADD);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunSubNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "-";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitSubNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
