package libbun.lang.konoha;

import libbun.ast.BNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class VarDeclPatternFunction extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode VarNode = new BunLetVarNode(ParentNode, 0, null, null);
		VarNode = TokenContext.MatchPattern(VarNode, BunLetVarNode._TypeInfo, "$OpenType$", BTokenContext._Required);
		VarNode = TokenContext.MatchPattern(VarNode, BunLetVarNode._NameInfo, "$Name$", BTokenContext._Required);
		VarNode = TokenContext.MatchToken(VarNode, "=", BTokenContext._Required);
		VarNode = TokenContext.MatchPattern(VarNode, BunLetVarNode._InitValue, "$Expression$", BTokenContext._Required);
		return VarNode;
	}

}
