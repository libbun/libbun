package libbun.encode.jvm;

import libbun.ast.AstNode;
import libbun.ast.expression.GetNameNode;
import libbun.parser.classic.BToken;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class JavaClassPathPattern extends BMatchFunction {

	@Override public AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode) {
		@Var BToken Token = TokenContext.ParseLargeToken();
		return new GetNameNode(ParentNode, Token, Token.GetText());
	}

}
