package libbun.type;

import libbun.parser.ZToken;
import libbun.util.BField;

public class ZPrototype extends ZFunc {
	@BField public int DefinedCount = 0;
	@BField public int UsedCount = 0;

	public ZPrototype(int FuncFlag, String FuncName, ZFuncType FuncType, ZToken SourceToken) {
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
