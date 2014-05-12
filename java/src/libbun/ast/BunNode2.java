package libbun.ast;

import libbun.parser.common.BunVisitor2;

public abstract class BunNode2 extends BNode {

	public BunNode2(BNode parentNode, int size) {
		super(parentNode, size);
	}

	public abstract void accept2(BunVisitor2 visitor);

}
