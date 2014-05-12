package libbun.ast.expression;

import libbun.ast.AstNode;

public abstract class MutableNode extends AstNode {
	public boolean IsImmutable = false;
	public MutableNode(AstNode ParentNode, int Size) {
		super(ParentNode, Size);
	}
}
