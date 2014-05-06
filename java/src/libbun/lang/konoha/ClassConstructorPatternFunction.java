package libbun.lang.konoha;

import libbun.ast.BNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.parser.classic.BTokenContext;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class ClassConstructorPatternFunction extends BMatchFunction {
	@Override public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		String Name = "constructor";
		if(ParentNode instanceof BunClassNode) {
			Name = ((BunClassNode)ParentNode).ClassName();
		}
		ClassMemberNode MemberNode = new ClassMemberNode(ParentNode);
		boolean FoundQualifer = true;
		while(FoundQualifer) {
			FoundQualifer = false;
			if(TokenContext.MatchNewLineToken("public")) {
				MemberNode.IsPublic = true;
				FoundQualifer = true;
			}
			if(TokenContext.MatchNewLineToken("protected")) {
				FoundQualifer = true;
			}
			if(TokenContext.MatchNewLineToken("private")) {
				FoundQualifer = true;
			}
		}
		@Var BunFunctionNode FunctionNode = new BunFunctionNode(ParentNode);
		FunctionNode.GivenName = Name;
		@Var BNode FuncNode = FunctionNode;
		FuncNode = TokenContext.MatchToken(ParentNode, Name, BTokenContext._Required);
		FuncNode = TokenContext.MatchNtimes(FuncNode, "(", "$Param$", ",", ")");
		FuncNode = TokenContext.MatchPattern(FuncNode, BunFunctionNode._Block, "$Block$", BTokenContext._Required);
		if(!FuncNode.IsErrorNode()) {
			MemberNode.ConstructorNode = FunctionNode;
			return MemberNode;
		}
		return FuncNode;
	}
}
