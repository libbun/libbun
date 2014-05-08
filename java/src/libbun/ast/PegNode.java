package libbun.ast;

import libbun.parser.classic.LibBunVisitor;

public class PegNode extends BNode {

	public PegNode(BNode parentNode, int size) {
		super(parentNode, size);
	}

	@Override public void Accept(LibBunVisitor Visitor) {

	}

}
