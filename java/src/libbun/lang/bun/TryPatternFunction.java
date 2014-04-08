package libbun.lang.bun;

import libbun.ast.BNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.statement.BunTryNode;
import libbun.parser.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class TryPatternFunction extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode TryNode = new BunTryNode(ParentNode);
		TryNode = TokenContext.MatchToken(TryNode, "try", BTokenContext._Required);
		TryNode = TokenContext.MatchPattern(TryNode, BunTryNode._Try, "$Block$", BTokenContext._Required);
		@Var int count = 0;
		if(TokenContext.MatchNewLineToken("catch")) {
			TryNode = TokenContext.MatchToken(TryNode, "(", BTokenContext._Required);
			TryNode = TokenContext.MatchPattern(TryNode, BunTryNode._NameInfo, "$Name$", BTokenContext._Required);
			TryNode = TokenContext.MatchToken(TryNode, ")", BTokenContext._Required);
			TryNode = TokenContext.MatchPattern(TryNode, BunTryNode._Catch, "$Block$", BTokenContext._Required);
			count = count + 1;
		}
		if(TokenContext.MatchNewLineToken("finally")) {
			TryNode = TokenContext.MatchPattern(TryNode, BunTryNode._Finally, "$Block$", BTokenContext._Required);
			count = count + 1;
		}
		if(count == 0 && !TryNode.IsErrorNode()) {
			TryNode = new ErrorNode(ParentNode, TryNode.SourceToken, "either catch or finally is expected");
		}
		return TryNode;
	}

}
