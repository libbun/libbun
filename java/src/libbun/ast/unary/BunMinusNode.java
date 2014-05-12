package libbun.ast.unary;

import libbun.ast.AstNode;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunMinusNode extends UnaryOperatorNode {
	public BunMinusNode(AstNode ParentNode) {
		super(ParentNode);
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new BunMinusNode(ParentNode));
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
