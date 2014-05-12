package libbun.ast.expression;

import libbun.ast.AstNode;
import libbun.ast.BunNode;

public abstract class MutableNode extends BunNode {
	public boolean IsImmutable = false;
	public MutableNode(AstNode parentNode, int size) {
		super(parentNode, size);
	}
}
