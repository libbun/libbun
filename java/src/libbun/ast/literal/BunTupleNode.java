package libbun.ast.literal;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;

public final class BunTupleNode extends AbstractListNode {
	public BunTupleNode(BNode ParentNode) {
		super(ParentNode, 0);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunArrayNode(ParentNode));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(tuple", this.vargStartIndex, ")");
	}
	@Override public void Accept(LibBunVisitor Visitor) {
	}
	
}