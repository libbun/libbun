package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;

public class BunBitwiseXorNode extends BitwiseOperatorNode {
	public BunBitwiseXorNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleBITXOR);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunBitwiseXorNode(ParentNode));
	}
	@Override public final String GetOperator() {
		return "^";
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitBitwiseXorNode(this);
		}
		else {
			Visitor.VisitBinaryNode(this);
		}
	}

}
