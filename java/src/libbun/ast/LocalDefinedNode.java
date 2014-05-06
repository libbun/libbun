package libbun.ast;

import libbun.parser.classic.LibBunVisitor;


public abstract class LocalDefinedNode extends BNode {

	public LocalDefinedNode(BNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitLocalDefinedNode(this);
	}

}
