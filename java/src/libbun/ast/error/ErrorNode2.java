package libbun.ast.error;

import libbun.ast.AstNode;
import libbun.parser.common.BunModelVisitor;

public class ErrorNode2 extends VerboseNode {
	public ErrorNode2(String message, AstNode innerNode) {
		super(message, innerNode);
	}

	@Override
	public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new ErrorNode2(this.message, this.innerNode));
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitErrorNode(this);
	}

}
