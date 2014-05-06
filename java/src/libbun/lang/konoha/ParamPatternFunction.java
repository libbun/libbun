package libbun.lang.konoha;

import libbun.ast.BNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class ParamPatternFunction extends BMatchFunction {

	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var BNode ParamNode = new BunLetVarNode(ParentNode, BunLetVarNode._IsReadOnly, null, null);
		ParamNode = TokenContext.MatchPattern(ParamNode, BunLetVarNode._TypeInfo, "$OpenType$", BTokenContext._Required);
		ParamNode = TokenContext.MatchPattern(ParamNode, BunLetVarNode._NameInfo, "$Name$", BTokenContext._Required);
		return ParamNode;
	}

}
