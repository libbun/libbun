package libbun.encode.jvm;

import libbun.ast.AstNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class JavaImportPattern extends BMatchFunction {

	@Override public AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode) {
		@Var AstNode ImportNode = new JavaImportNode(ParentNode);
		ImportNode = TokenContext.MatchToken(ImportNode, "import", BTokenContext._Required);
		ImportNode = TokenContext.MatchPattern(ImportNode, JavaImportNode._Path, "$JavaClassPath$", BTokenContext._Required);
		return ImportNode;
	}

}
