package libbun.lang.bun.regexp;

import libbun.ast.AstNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.literal.BunStringNode;
import libbun.parser.classic.BSourceContext;
import libbun.parser.classic.BToken;
import libbun.parser.classic.BTokenContext;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BFormFunc;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.BMatchFunction;
import libbun.util.BTokenFunction;
import libbun.util.LibBunSystem;
import libbun.util.Var;

class BackSlashTokenFunction extends BTokenFunction {
	@Override public boolean Invoke(BSourceContext SourceContext) {
		@Var int StartIndex = SourceContext.GetPosition();
		SourceContext.Tokenize(StartIndex, StartIndex+2);
		return false;
	}
}

class RegExpPatternFunction extends BMatchFunction {
	@Override public AstNode Invoke(AstNode ParentNode, BTokenContext TokenContext, AstNode LeftNode) {
		@Var BToken BeginToken = TokenContext.GetToken(BTokenContext._MoveNext);
		while(TokenContext.HasNext()) {
			@Var BToken Token = TokenContext.GetToken(BTokenContext._MoveNext);
			if(Token.EqualsText('/')) {
				BeginToken.endIndex = Token.endIndex;
				return new BunRegExpNode(ParentNode, BeginToken);
			}
			if(Token.IsIndent()) {
				break;
			}
		}
		return null;
	}
}

class BunRegExpNode extends SyntaxSugarNode {
	public final static BType _RegExpType = new BType(BType.UniqueTypeFlag, "RegExp", BType.VarType);

	@BField public final String PatternValue;
	@BField public String PatternOption = "";
	public BunRegExpNode(AstNode ParentNode, BToken RegExpToken) {
		super(ParentNode, 0);
		this.SourceToken = RegExpToken;
		this.PatternValue = LibBunSystem._UnquoteString(RegExpToken.GetText());
		this.Type = BunRegExpNode._RegExpType;
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {

	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChecker) {
		@Var BFormFunc Func = TypeChecker.Generator.GetFormFunc("Bun::NewRegExp", BType.StringType, 2);
		if(Func != null) {
			@Var AstNode FuncNode = TypeChecker.CreateDefinedFuncCallNode(this.ParentNode, this.SourceToken, Func);
			FuncNode.appendNode(new BunStringNode(FuncNode, null, this.PatternValue));
			FuncNode.appendNode(new BunStringNode(FuncNode, null, this.PatternOption));
			return new DesugarNode(this, FuncNode);
		}
		//System.out.println("debug: " + this.PatternValue);
		return new DesugarNode(this, new LegacyErrorNode(this, "unsupported regular expression"));
	}
}

public class RegExpGrammar {
	public final static BTokenFunction BackSlashToken =  new BackSlashTokenFunction();
	public final static BMatchFunction RegExpPattern = new RegExpPatternFunction();
	public static void LoadGrammar(LibBunGamma Gamma) {
		Gamma.SetTypeName(BunRegExpNode._RegExpType, null);
		Gamma.Generator.RequireLibrary("regex", null);
		Gamma.DefineToken("\\", BackSlashToken);
		Gamma.DefineExpression("/", RegExpPattern);
	}
}