package libbun.lang.python;

import libbun.parser.ZToken;
import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZNode;
import libbun.util.Var;
import libbun.util.ZMatchFunction;

public class PythonBlockPatternFunction extends ZMatchFunction {

	@Override
	public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext,
			ZNode LeftNode) {
		@Var ZNode BlockNode =  new ZBlockNode(ParentNode, ParentNode.GetNameSpace());
		@Var ZToken SkipToken = TokenContext.GetToken();
		BlockNode = TokenContext.MatchToken(BlockNode, ":", ZTokenContext._Required);
		if(!BlockNode.IsErrorNode()) {
			@Var boolean Remembered = TokenContext.SetParseFlag(ZTokenContext._AllowSkipIndent); // init
			@Var ZNode NestedBlockNode = BlockNode;
			@Var int IndentSize = 0;
			while(TokenContext.HasNext()) {
				@Var ZToken Token = TokenContext.GetToken();
				if(IndentSize > Token.GetIndentSize()) {
					break;
				}
				IndentSize = Token.GetIndentSize();
				NestedBlockNode = TokenContext.MatchPattern(NestedBlockNode, ZNode._NestedAppendIndex, "$Statement$", ZTokenContext._Required);
				if(NestedBlockNode.IsErrorNode()) {
					TokenContext.SkipError(SkipToken);
					break;
				}
			}
			TokenContext.SetParseFlag(Remembered);
		}
		return BlockNode;
	}

}
