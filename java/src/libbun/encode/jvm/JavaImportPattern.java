package libbun.encode.jvm;

import libbun.ast.BNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class JavaImportPattern extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode ImportNode = new JavaImportNode(ParentNode);
		ImportNode = TokenContext.MatchToken(ImportNode, "import", BTokenContext._Required);
		ImportNode = TokenContext.MatchPattern(ImportNode, JavaImportNode._Path, "$JavaClassPath$", BTokenContext._Required);
		return ImportNode;
	}

}
