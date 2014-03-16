package libbun.lang.bun;

import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZClassNode;
import libbun.parser.ast.ZNode;
import libbun.util.Var;
import libbun.util.ZMatchFunction;

public class ClassPatternFunction extends ZMatchFunction {

	@Override public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
		@Var ZNode ClassNode = new ZClassNode(ParentNode);
		ClassNode = TokenContext.MatchToken(ClassNode, "class", ZTokenContext._Required);
		ClassNode = TokenContext.MatchPattern(ClassNode, ZClassNode._NameInfo, "$Name$", ZTokenContext._Required);
		if(TokenContext.MatchNewLineToken("extends")) {
			ClassNode = TokenContext.MatchPattern(ClassNode, ZClassNode._TypeInfo, "$OpenType$", ZTokenContext._Required);
		}
		ClassNode = TokenContext.MatchNtimes(ClassNode, "{", "$FieldDecl$", null, "}");
		return ClassNode;
	}

}
