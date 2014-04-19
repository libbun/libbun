package libbun.ast.decl;

import libbun.ast.BNode;
import libbun.parser.BToken;
import libbun.parser.LibBunGamma;
import libbun.parser.LibBunLogger;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BunRequireNode extends TopLevelNode {
	public final static int _Path = 0;

	public BunRequireNode(BNode ParentNode) {
		super(ParentNode, 1);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunRequireNode(ParentNode));
	}
	@Override public final void Perform(LibBunGamma Gamma) {
		@Var BToken SourceToken = this.AST[BunRequireNode._Path].SourceToken;
		@Var String Path = SourceToken.GetTextAsName();
		if(Path.startsWith("syntax::")) {
			if(!LibBunSystem._ImportGrammar(Gamma, Path)) {
				LibBunLogger._LogErrorExit(SourceToken, "unknown syntax: " + Path.substring(8));
			}
		}
		else {
			Gamma.Generator.RequireLibrary(Path, SourceToken);
		}
	}
}
