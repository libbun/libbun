package libbun.ast;

import libbun.parser.classic.LibBunVisitor;


public abstract class LocalDefinedNode extends AstNode {

	public LocalDefinedNode(AstNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitLocalDefinedNode(this);
	}

}
