package libbun.lang.lisp;

import libbun.ast.AstNode;
import libbun.ast.GroupNode;
import libbun.parser.classic.BTokenContext;
import libbun.parser.classic.LibBunGamma;
import libbun.util.BMatchFunction;
import libbun.util.Var;

class GroupPatternFunction extends BMatchFunction {
	@Override public AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode) {
		@Var AstNode Node = new SExpressionNode(ParentNode);
		Node = TokenContext.MatchToken(Node, "(", BTokenContext._Required);
		while(TokenContext.HasNext()) {
			if(TokenContext.IsToken(")")) {
				break;
			}
			@Var AstNode ParsedNode = TokenContext.MatchPattern(Node, GroupNode._Expr, "$Expression$", BTokenContext._Required, BTokenContext._AllowSkipIndent);
			if(ParsedNode.IsErrorNode()) {
				return ParsedNode;
			}
			Node = ParsedNode;
		}
		Node = TokenContext.MatchToken(Node, ")", BTokenContext._Required);
		return Node;
	}
}

public class LispGrammar {
	//	public final static BMatchFunction ContinuePattern = new ContinuePatternFunction();

	public static void LoadGrammar(LibBunGamma Gamma) {
		//Gamma.SetTypeName(BType.VoidType,  null);
		//Gamma.AppendTokenFunc(" \t", WhiteSpaceToken);
		//Gamma.DefineExpression("null", NullPattern);
		//Gamma.DefineRightExpression("instanceof", InstanceOfPattern);
		//Gamma.DefineStatement("continue", ContinuePattern);
	}

}
