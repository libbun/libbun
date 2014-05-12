package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunVisitor;

public abstract class TopLevelNode extends AstNode {

	public TopLevelNode(AstNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitTopLevelNode(this);
	}

	public abstract void Perform(LibBunGamma Gamma);

}
