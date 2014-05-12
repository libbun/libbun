package libbun.ast.binary;

import libbun.ast.AstNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunBitwiseOrNode extends BitwiseOperatorNode {
	public BunBitwiseOrNode(AstNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleBITOR);
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new BunBitwiseOrNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "|";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitBitwiseOrNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
