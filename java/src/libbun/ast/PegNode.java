package libbun.ast;

import libbun.parser.classic.LibBunVisitor;

public class PegNode extends AstNode {

	public PegNode(AstNode parentNode, int size) {
		super(parentNode, size);
	}

	@Override public void Accept(LibBunVisitor Visitor) {
	}

}
