package libbun.lang.python;

import libbun.lang.bun.ExpressionPatternFunction;
import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZNode;
import libbun.util.Var;
import libbun.util.ZMatchFunction;

public class PythonStatementPatternFunction extends ZMatchFunction {

	@Override
	public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext,
			ZNode LeftNode) {
		@Var boolean Remembered = TokenContext.SetParseFlag(ZTokenContext._AllowSkipIndent);
		TokenContext.SetParseFlag(ZTokenContext._NotAllowSkipIndent);
		@Var ZNode StmtNode = ExpressionPatternFunction._DispatchPattern(ParentNode, TokenContext, null, true, true);
		TokenContext.SetParseFlag(Remembered);
		return StmtNode;
	}

}
