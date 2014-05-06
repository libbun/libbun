package libbun.lang.konoha;

import libbun.ast.BNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.BMatchFunction;

public class ClassMemberPatternFunction extends BMatchFunction {
	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		TokenContext.MatchToken("public");
		TokenContext.MatchToken("final");
		//		@Var ZNode ClassNode = new ZClassNode(ParentNode);
		//		ClassNode = TokenContext.MatchToken(ClassNode, "class", ZTokenContext._Required);
		//		ClassNode = TokenContext.MatchPattern(ClassNode, ZClassNode._NameInfo, "$Name$", ZTokenContext._Required);
		//		if(TokenContext.MatchNewLineToken("extends")) {
		//			ClassNode = TokenContext.MatchPattern(ClassNode, ZClassNode._TypeInfo, "$OpenType$", ZTokenContext._Required);
		//		}
		//		ClassNode = TokenContext.MatchNtimes(ClassNode, "{", "$ClassMember$", null, "}");
		//		return ClassNode;
		return null;
	}
}
