package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BType;

public class BunTrueNode extends BunValueNode {
	public BunTrueNode(AstNode ParentNode) {
		super(ParentNode);
		this.Type = BType.BooleanType;
	}
	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunTrueNode(ParentNode));
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
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitTrueNode(this);
	}

}
