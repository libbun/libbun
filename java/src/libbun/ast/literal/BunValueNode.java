package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.ast.BunNode2;


public abstract class BunValueNode extends BunNode2 {

	public BunValueNode(BNode parentNode) {
		super(parentNode, 0);
	}

}
