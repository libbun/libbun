package libbun.ast;

import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BType;


public abstract class SyntaxSugarNode extends BNode {

	public SyntaxSugarNode(BNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitSyntaxSugarNode(this);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(syntax sugar)");
	}

	public abstract void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType);
	public abstract DesugarNode PerformDesugar(LibBunTypeChecker TypeChekcer);

}
