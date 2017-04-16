package libbun.lang.python;

import libbun.parser.ZTokenContext;
import libbun.parser.ast.ZFunctionNode;
import libbun.parser.ast.ZNode;
import libbun.util.Var;
import libbun.util.ZMatchFunction;

public class PythonFunctionPatternFunction extends ZMatchFunction {
    @Override
    public ZNode Invoke(ZNode ParentNode, ZTokenContext TokenContext, ZNode LeftNode) {
        @Var ZNode FuncNode = new ZFunctionNode(ParentNode);
        FuncNode = TokenContext.MatchToken(FuncNode, "def", ZTokenContext._Required);
        FuncNode = TokenContext.MatchPattern(FuncNode, ZFunctionNode._NameInfo, "$Name$", ZTokenContext._Optional);
        FuncNode = TokenContext.MatchNtimes(FuncNode, "(", "$Param$", ",", ")");
        FuncNode = TokenContext.MatchPattern(FuncNode, ZFunctionNode._TypeInfo, "$TypeAnnotation$", ZTokenContext._Optional);
        FuncNode = TokenContext.MatchPattern(FuncNode, ZFunctionNode._Block, "$Block$", ZTokenContext._Required);
        return FuncNode;
    }
}
