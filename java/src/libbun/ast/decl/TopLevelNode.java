package libbun.ast.decl;

import libbun.ast.BNode;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunVisitor;

public abstract class TopLevelNode extends BNode {

	public TopLevelNode(BNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitTopLevelNode(this);
	}

	public abstract void Perform(LibBunGamma Gamma);

}
