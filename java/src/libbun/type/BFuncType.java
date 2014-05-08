package libbun.type;

import libbun.common.CommonArray;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public final class BFuncType extends BType {
	public final static BFuncType _FuncType  = new BFuncType();

	@BField public BType[]  TypeParams;
	@BField private boolean HasUnknownType = false;
	@BField private boolean HasGreekType = false;

	private BFuncType() {
		super(BType.UniqueTypeFlag, "Func", BType.VarType);
		this.TypeParams = LibBunSystem._NewTypeArray(1);
		this.TypeParams[0] = BType.VarType;
		this.HasUnknownType = true;
	}

	public BFuncType(BType[] UniqueTypeParams) {
		super(BType.UniqueTypeFlag, null, BType.VarType);
		this.TypeParams = UniqueTypeParams;
		@Var int i = 0;
		while(i < this.TypeParams.length) {
			if(this.TypeParams[i].IsVarType()) {
				this.HasUnknownType = true;
			}
			if(this.TypeParams[i].IsGreekType()) {
				this.HasGreekType = true;
			}
			i = i + 1;
		}
	}

	@Override public final String GetName() {
		if(this.ShortName == null) {
			@Var String s = "Func<";
			@Var int i = 0;
			while(i < this.TypeParams.length) {
				if(i > 0) {
					s = s + ",";
				}
				s = s + this.TypeParams[i].GetName();
				i = i + 1;
			}
			this.ShortName =  s + ">";
		}
		return this.ShortName;
	}

	@Override public final boolean IsFuncType() {
		return true;
	}

	@Override public final boolean IsVarType() {
		return this.HasUnknownType;
	}

	@Override public final boolean IsGreekType() {
		return this.HasGreekType;
	}

	@Override public final BType GetGreekRealType(BType[] Greek) {
		if(this.HasGreekType) {
			@Var CommonArray<BType> TypeList = new CommonArray<BType>(new BType[this.TypeParams.length]);
			@Var int i = 0;
			while(i < this.TypeParams.length) {
				TypeList.add(this.TypeParams[i].GetGreekRealType(Greek));
				i = i + 1;
			}
			return BTypePool._LookupFuncType2(TypeList);
		}
		return this;
	}

	@Override public final boolean AcceptValueType(BType ValueType, boolean ExactMatch, BType[] Greek) {
		if(ValueType.IsFuncType() && ValueType.GetParamSize() == this.GetParamSize()) {
			@Var int i = 0;
			while(i < this.TypeParams.length) {
				if(!this.TypeParams[i].AcceptValueType(ValueType.GetParamType(i), true, Greek)) {
					return false;
				}
				i = i + 1;
			}
			return true;
		}
		return false;
	}

	@Override public final String StringfySignature(String FuncName) {
		return BFunc._StringfySignature(FuncName, this.GetFuncParamSize(), this.GetRecvType());
	}

	@Override public final BType GetBaseType() {
		return BFuncType._FuncType;
	}

	@Override public final int GetParamSize() {
		return this.TypeParams.length;
	}

	@Override public final BType GetParamType(int Index) {
		return this.TypeParams[Index];
	}

	public final BType GetReturnType() {
		return this.TypeParams[this.TypeParams.length - 1];
	}

	public final BType GetRecvType() {
		if(this.TypeParams.length == 1) {
			return BType.VoidType;
		}
		return this.TypeParams[0];
	}

	public final int GetFuncParamSize() {
		return this.TypeParams.length - 1;
	}


	public final BType GetFuncParamType(int Index) {
		return this.TypeParams[Index];
	}

	public final boolean AcceptAsFieldFunc(BFuncType FuncType) {
		if(FuncType.GetFuncParamSize() == this.GetFuncParamSize() && FuncType.GetReturnType().Equals(this.GetReturnType())) {
			@Var int i = 1;
			while(i < FuncType.GetFuncParamSize()) {
				if(!FuncType.GetFuncParamType(i).Equals(this.GetFuncParamType(i))) {
					return false;
				}
				i = i + 1;
			}
		}
		return true;
	}
}
