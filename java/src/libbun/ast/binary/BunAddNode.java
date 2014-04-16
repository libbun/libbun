package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.BunVisitor;
import libbun.parser.LibBunVisitor;

public class BunAddNode extends ArithmeticOperatorNode {
	public BunAddNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleADD);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunAddNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "+";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitAddNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
