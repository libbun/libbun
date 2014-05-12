package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BType;

public class BunFalseNode extends AstNode {
	public BunFalseNode(AstNode ParentNode) {
		super(ParentNode,0);
		this.Type = BType.BooleanType;
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
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

	public void accept2(BunModelVisitor visitor) {
		visitor.visitFalseNode(this);
	}

}
