package libbun.ast.error;

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.parser.common.BunModelVisitor;

public class VerboseNode extends BunNode {
	public AstNode innerNode;
	public String message;
	public VerboseNode(String message, AstNode innerNode) {
		super(innerNode.ParentNode, 0);
		this.innerNode = innerNode;
		this.message = message;
	}

	@Override
	public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new VerboseNode(this.message, this.innerNode));
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitVerboseNode(this);
	}
}
