package libbun.ast.binary;

import libbun.ast.AstNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunEqualsNode extends ComparatorNode {

	public BunEqualsNode(AstNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleEquals);
	}
	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunEqualsNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "==";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitEqualsNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}

}
