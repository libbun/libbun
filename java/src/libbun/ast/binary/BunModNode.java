package libbun.ast.binary;

import libbun.ast.AstNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunModNode extends ArithmeticOperatorNode {
	public BunModNode(AstNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleMUL);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunModNode(ParentNode));
	}

	@Override public final String GetOperator() {
		return "%";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitModNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}

}
