package libbun.ast.unary;

import libbun.ast.BNode;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunComplementNode extends UnaryOperatorNode {
	public BunComplementNode(BNode ParentNode) {
		super(ParentNode);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunComplementNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "~";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitComplementNode(this);
		}
		else {
			Visitor.VisitUnaryNode(this);
		}
	}
}
