package libbun.ast.unary;

import libbun.ast.BNode;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunPlusNode extends UnaryOperatorNode {
	public BunPlusNode(BNode ParentNode) {
		super(ParentNode);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunPlusNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "+";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitPlusNode(this);
		}
		else {
			Visitor.VisitUnaryNode(this);
		}
	}
}
