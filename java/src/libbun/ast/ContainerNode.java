package libbun.ast;

import libbun.parser.classic.LibBunVisitor;

public class ContainerNode extends AstNode {

	public ContainerNode(AstNode Node1, AstNode Node2) {
		super(null, 0);
		this.appendNode(Node1);
		this.appendNode(Node2);
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		// TODO Auto-generated method stub

	}

}
