package libbun.lang.konoha;

import libbun.ast.BNode;
import libbun.ast.sugar.BunContinueNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.BMatchFunction;
import libbun.util.Var;

public class ContinuePatternFunction extends BMatchFunction {
	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode ContinueNode = new BunContinueNode(ParentNode);
		ContinueNode = TokenContext.MatchToken(ContinueNode, "continue", BTokenContext._Required);
		return ContinueNode;
	}
}
