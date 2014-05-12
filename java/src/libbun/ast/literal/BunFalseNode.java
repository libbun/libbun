package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunVisitor2;
import libbun.type.BType;

public class BunFalseNode extends BNode {
	public BunFalseNode(BNode ParentNode) {
		super(ParentNode,0);
		this.Type = BType.BooleanType;
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunFalseNode(ParentNode));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("false");
	}

	@Override public final String toString() {
		return "false";
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
	}

	public void accept2(BunVisitor2 visitor) {
		visitor.visitFalseNode(this);
	}

}
