package libbun.parser.classic;

import libbun.util.BField;
import libbun.util.BTokenFunction;

public final class LibBunTokenFuncChain {
	@BField public BTokenFunction   Func;
	@BField public LibBunTokenFuncChain	ParentFunc;

	LibBunTokenFuncChain(BTokenFunction Func, LibBunTokenFuncChain Parent) {
		this.Func = Func;
		this.ParentFunc = Parent;
	}
}