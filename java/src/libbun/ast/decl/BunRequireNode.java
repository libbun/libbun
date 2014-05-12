package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.common.BunLogger;
import libbun.parser.common.BunToken;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BunRequireNode extends TopLevelNode {
	public final static int _Path = 0;

	public BunRequireNode(AstNode ParentNode) {
		super(ParentNode, 1);
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new BunRequireNode(ParentNode));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(require", 0, ")");
	}
	@Override public final void Perform(LibBunGamma Gamma) {
		@Var BunToken SourceToken = this.AST[BunRequireNode._Path].SourceToken;
		@Var String Path = SourceToken.GetTextAsName();
		if(Path.startsWith("syntax::")) {
			if(!LibBunSystem._LoadGrammar(Gamma, Path)) {
				BunLogger._LogErrorExit(SourceToken, "unknown syntax: " + Path.substring(8));
			}
		}
		else {
			Gamma.Generator.RequireLibrary(Path, SourceToken);
		}
	}
}
