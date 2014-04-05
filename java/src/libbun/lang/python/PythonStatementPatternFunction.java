package libbun.lang.python;

import libbun.ast.BNode;
import libbun.lang.bun.ExpressionPatternFunction;
import libbun.parser.BTokenContext;
import libbun.util.BMatchFunction;
import libbun.util.Var;

public class PythonStatementPatternFunction extends BMatchFunction {

	@Override
	public BNode Invoke(BNode ParentNode, BTokenContext TokenContext,
			BNode LeftNode) {
		@Var boolean Remembered = TokenContext.SetParseFlag(BTokenContext._AllowSkipIndent);
		TokenContext.SetParseFlag(BTokenContext._NotAllowSkipIndent);
		@Var BNode StmtNode = ExpressionPatternFunction._DispatchPattern(ParentNode, TokenContext, null, true, true);
		TokenContext.SetParseFlag(Remembered);
		return StmtNode;
	}

}
