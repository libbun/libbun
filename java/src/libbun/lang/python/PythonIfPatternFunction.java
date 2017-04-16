package libbun.lang.python;

import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZIfNode;
import libbun.parser.ast.ZNode;
import libbun.util.Var;
import libbun.util.ZMatchFunction;

public class PythonIfPatternFunction extends ZMatchFunction{
	@Override
	public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
		@Var ZNode IfNode = new ZIfNode(ParentNode);
		IfNode = TokenContext.MatchToken(IfNode, "if", ZTokenContext._Required);
		IfNode = TokenContext.MatchPattern(IfNode, ZIfNode._Cond, "$Expression$", ZTokenContext._Required, ZTokenContext._AllowNewLine);
		IfNode = TokenContext.MatchPattern(IfNode, ZIfNode._Then, "$Block$", ZTokenContext._Required);
		//FIXME elif
		if(TokenContext.MatchNewLineToken("else")) {
			IfNode = TokenContext.MatchPattern(IfNode, ZIfNode._Else, "$Block$", ZTokenContext._Required);
		}
		return IfNode;
	}
}
