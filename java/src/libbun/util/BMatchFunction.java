package libbun.util;

import libbun.ast.BNode;
import libbun.parser.classic.BTokenContext;

public abstract class BMatchFunction extends BFunction {
	public BMatchFunction(int TypeId, String Name) {
		super(TypeId, Name);
	}
	protected BMatchFunction() {
		super(0, null);
	}
	public abstract BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode);
}

