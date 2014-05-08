package libbun.encode.jvm;

import libbun.ast.BNode;
import libbun.ast.expression.GetNameNode;
import libbun.parser.classic.BTokenContext;
import libbun.parser.common.BToken;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class JavaClassPathPattern extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BToken Token = TokenContext.ParseLargeToken();
		return new GetNameNode(ParentNode, Token, Token.GetText());
	}

}
