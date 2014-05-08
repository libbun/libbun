package zen.obsolete;

import libbun.common.CommonArray;
import libbun.type.BType;
import libbun.util.BField;

public class ZUnionType extends BType {

	@BField public final CommonArray<BType> UnionList = null;

	public ZUnionType() {
		super(0, "union", BType.VarType);
		this.TypeId = this.RefType.TypeId;
	}

}
