package libbun.util;

import libbun.ast.AstNode;
import libbun.parser.classic.BTokenContext;

public abstract class BMatchFunction extends BFunction {
	public BMatchFunction(int TypeId, String Name) {
		super(TypeId, Name);
	}
	protected BMatchFunction() {
		super(0, null);
	}
	public abstract AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode);
}

