package libbun.util;

import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZNode;

public abstract class ZMatchFunction extends ZFunction {
	public ZMatchFunction(int TypeId, String Name) {
		super(TypeId, Name);
	}
	protected ZMatchFunction() {
		super(0, null);
	}
	public abstract ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode);
}

