package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.common.BunModelVisitor;

public final class BunTupleNode extends BunNode {
	public BunTupleNode(AstNode ParentNode) {
		super(ParentNode, 0);
	}
	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunArrayNode(ParentNode));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(tuple", 0, ")");
	}
	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitTupleNode(this);
	}

}