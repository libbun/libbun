package libbun.type;

import libbun.parser.common.BToken;
import libbun.util.BField;

public class BPrototype extends BFunc {
	@BField public int DefinedCount = 0;
	@BField public int UsedCount = 0;

	public BPrototype(int FuncFlag, String FuncName, BFuncType FuncType, BToken SourceToken) {
		super(FuncFlag, FuncName, FuncType);
		this.DefinedCount = 0;
		this.UsedCount = 0;
	}

	public final void Used() {
		this.UsedCount = this.UsedCount + 1;
	}

	public final void Defined() {
		this.DefinedCount = this.DefinedCount + 1;
	}


}
