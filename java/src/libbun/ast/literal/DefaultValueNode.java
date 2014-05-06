package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;


public class DefaultValueNode extends LiteralNode {
	public DefaultValueNode(BNode ParentNode) {
		super(ParentNode, null);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new DefaultValueNode(ParentNode));
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitDefaultValueNode(this);
		}
		else {
			Visitor.VisitLiteralNode(this);
		}
	}

}
