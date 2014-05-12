package libbun.ast;

import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;

public abstract class BunNode extends AstNode {

	public BunNode(AstNode parentNode, int size) {
		super(parentNode, size);
	}

	@Override
	public void Accept(LibBunVisitor Visitor) {
		// TODO Auto-generated method stub

	}

	public abstract void acceptBunModel(BunModelVisitor visitor);

}
