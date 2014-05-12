package libbun.ast;

import libbun.parser.common.BunModelVisitor;

public class BlockNode extends BunNode {

	public BlockNode(AstNode parentNode, int size) {
		super(parentNode, size);
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitBlockNode(this);
	}


}
