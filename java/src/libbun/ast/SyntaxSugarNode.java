package libbun.ast;

import libbun.parser.BGenerator;
import libbun.parser.BToken;
import libbun.parser.BTypeChecker;
import libbun.parser.BVisitor;


public abstract class SyntaxSugarNode extends BNode {

	public SyntaxSugarNode(BNode ParentNode, BToken SourceToken, int Size) {
		super(ParentNode, SourceToken, Size);
	}

	@Override public final void Accept(BVisitor Visitor) {
		Visitor.VisitSyntaxSugarNode(this);
	}

	public abstract DesugarNode DeSugar(BGenerator Generator, BTypeChecker TypeChekcer);



}