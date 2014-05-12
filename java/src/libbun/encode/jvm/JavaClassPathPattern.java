package libbun.encode.jvm;

import libbun.ast.AstNode;
import libbun.ast.expression.GetNameNode;
import libbun.parser.classic.BToken;
import libbun.parser.classic.BTokenContext;
import libbun.util.BMatchFunction;
import libbun.util.Var;

public class JavaClassPathPattern extends BMatchFunction {

	@Override public AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode) {
		@Var BToken Token = TokenContext.ParseLargeToken();
		@Var GetNameNode NameNode = new GetNameNode(ParentNode, Token.GetText());
		NameNode.SourceToken = Token;
		return NameNode;
	}

}
