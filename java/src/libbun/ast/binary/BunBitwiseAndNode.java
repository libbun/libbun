package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunBitwiseAndNode extends BitwiseOperatorNode {
	public BunBitwiseAndNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleBITAND);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunBitwiseAndNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "&";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitBitwiseAndNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}
}
