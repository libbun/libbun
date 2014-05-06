package libbun.ast.unary;

import libbun.ast.BNode;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunMinusNode extends UnaryOperatorNode {
	public BunMinusNode(BNode ParentNode) {
		super(ParentNode);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunMinusNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "-";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitMinusNode(this);
		}
		else {
			Visitor.VisitUnaryNode(this);
		}
	}
}
