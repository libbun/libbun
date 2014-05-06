package libbun.ast;

import libbun.parser.classic.LibBunVisitor;

public class ContainerNode extends AbstractListNode {

	public ContainerNode(BNode Node1, BNode Node2) {
		super(null, 0);
		this.Append(Node1);
		this.Append(Node2);
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		// TODO Auto-generated method stub

	}

}
