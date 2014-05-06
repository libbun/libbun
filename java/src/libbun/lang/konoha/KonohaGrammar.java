package libbun.lang.konoha;

import libbun.lang.bun.BunGrammar;
import libbun.parser.classic.LibBunGamma;

public class KonohaGrammar {
	public static void LoadGrammar(LibBunGamma Gamma) {
		BunGrammar.LoadGrammar(Gamma);
		Gamma.DefineStatement("continue", new ContinuePatternFunction());
	}
}
