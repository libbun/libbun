package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunVisitor2;
import libbun.type.BType;

public class BunTrueNode extends BunValueNode {
	public BunTrueNode(BNode ParentNode) {
		super(ParentNode);
		this.Type = BType.BooleanType;
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunTrueNode(ParentNode));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("true");
	}

	@Override public final String toString() {
		return "true";
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
	}

	@Override
	public void accept2(BunVisitor2 visitor) {
		visitor.visitTrueNode(this);
	}

}
