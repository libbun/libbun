package libbun.lang.bun;

import libbun.ast.BNode;
import libbun.ast.expression.SetFieldNode;
import libbun.parser.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class SetterPatternFunction extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode SetterNode = new SetFieldNode(ParentNode, LeftNode);
		SetterNode = TokenContext.MatchToken(SetterNode, ".", BTokenContext._Required);
		SetterNode = TokenContext.MatchPattern(SetterNode, SetFieldNode._NameInfo, "$Name$", BTokenContext._Required);
		SetterNode = TokenContext.MatchToken(SetterNode, "=", BTokenContext._Required);
		SetterNode = TokenContext.MatchPattern(SetterNode, SetFieldNode._Expr, "$Expression$", BTokenContext._Required);
		return SetterNode;
	}

}
