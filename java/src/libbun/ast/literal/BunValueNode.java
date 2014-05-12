package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.ast.BunNode;


public abstract class BunValueNode extends BunNode {

	public BunValueNode(AstNode parentNode) {
		super(parentNode, 0);
	}

}
