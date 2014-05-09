// ***************************************************************************
// Copyright (c) 2013-2014, Libbun project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************


package libbun.type;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.BToken;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;
import libbun.util.ZenMethod;

public class BType  {
	public final static int     UniqueTypeFlag         = 1 << 16;
	public final static int     OpenTypeFlag           = 1 << 9;  // @Open for the future

	public final static BType	VarType = new BType(BType.UniqueTypeFlag, "var", null);
	public final static BType	VoidType = new BType(BType.UniqueTypeFlag, "void", null);
	public final static BType	BooleanType = new BType(BType.UniqueTypeFlag, "boolean", BType.VarType);
	public final static BType	IntType = new BType(BType.UniqueTypeFlag, "int", BType.VarType);
	public final static BType   FloatType = new BType(BType.UniqueTypeFlag, "float", BType.VarType);
	public final static BType	StringType = new BType(BType.UniqueTypeFlag, "String", BType.VarType);
	public final static BType   TypeType = new BType(BType.UniqueTypeFlag, "Type", BType.VarType);

	@BField public int		   TypeFlag = 0;
	@BField public int         TypeId = 0;
	@BField public String      ShortName = null;
	@BField public BType	   RefType = null;

	public BType(int TypeFlag, String ShortName, BType RefType) {
		this.TypeFlag = TypeFlag;
		this.ShortName = ShortName;
		this.RefType = RefType;
		if(LibBunSystem._IsFlag(TypeFlag, BType.UniqueTypeFlag)) {
			this.TypeId = BTypePool._NewTypeId(this);
		}
	}

	@Override public String toString() {
		return this.GetName();
	}

	@ZenMethod public String GetName() {
		return this.ShortName;
	}

	@ZenMethod public BType GetRealType() {
		return this;
	}

	@ZenMethod public BType GetSuperType() {
		return this.RefType;
	}

	@ZenMethod public BType GetBaseType() {
		return this;
	}

	@ZenMethod public int GetParamSize() {
		return 0;
	}

	@ZenMethod public BType GetParamType(int Index) {
		return BType.VarType;  // for safety, it is used in Array
	}

	public final boolean Equals(BType Type) {
		return (this.GetRealType() == Type.GetRealType());
	}

	public final boolean Accept(BType Type) {
		@Var BType ThisType = this.GetRealType();
		if(ThisType == Type.GetRealType()) {
			return true;
		}
		if(Type.GetParamSize() == 0) {
			@Var BType SuperClass = Type.GetSuperType();
			while(SuperClass != null) {
				if(SuperClass == ThisType) {
					return true;
				}
				SuperClass = SuperClass.GetSuperType();
			}
		}
		return false;
	}

	@ZenMethod public boolean IsGreekType() {
		return false;
	}

	@ZenMethod public BType GetGreekRealType(BType[] Greek) {
		return this.GetRealType();
	}

	@ZenMethod public boolean AcceptValueType(BType ValueType, boolean ExactMatch, BType[] Greek) {
		if(this.GetRealType() != ValueType && !ValueType.IsVarType()) {
			if(ExactMatch && !this.Accept(ValueType)) {
				return false;
			}
		}
		return true;
	}

	public final boolean IsVoidType() {
		return (this.GetRealType() == BType.VoidType);
	}

	@ZenMethod public boolean IsVarType() {
		return (this.GetRealType() == BType.VarType);
	}

	public final boolean IsInferrableType() {
		return (!this.IsVarType() && !this.IsVoidType());
	}

	public final boolean IsTypeType() {
		return (this.GetRealType() == BType.TypeType);
	}

	public final boolean IsBooleanType() {
		return (this.GetRealType() == BType.BooleanType);
	}

	public final boolean IsIntType() {
		return (this.GetRealType() == BType.IntType);
	}

	public final boolean IsFloatType() {
		return (this.GetRealType() == BType.FloatType);
	}

	public final boolean IsNumberType() {
		return (this.IsIntType() || this.IsFloatType());
	}

	public final boolean IsStringType() {
		return (this.GetRealType() == BType.StringType);
	}

	public final boolean IsArrayType() {
		return (this.GetBaseType() == BGenericType._ArrayType);
	}

	public final boolean IsMapType() {
		return (this.GetBaseType() == BGenericType._MapType);
	}

	public final boolean IsOpenType() {
		return LibBunSystem._IsFlag(this.TypeFlag, BType.OpenTypeFlag);
	}

	@ZenMethod public boolean IsMutableType(LibBunTypeChecker Gamma) {
		//		if(Gamma.IsSupportMutable) {
		//			return false;
		//		}
		//		return true;
		return !Gamma.IsSupportMutable;
	}

	@ZenMethod public boolean IsNullableType(LibBunTypeChecker Gamma) {
		//		if(Gamma.IsSupportMutable) {
		//			return false;
		//		}
		//		return true;
		return !Gamma.IsSupportNullable;
	}

	public final String StringfyClassMember(String Name) {
		return Name + " of " + this.ShortName;
	}

	public final String GetUniqueName() {
		return LibBunSystem._Stringfy(this.TypeId);
	}

	//	public final boolean AcceptValue(Object Value) {
	//		return (Value != null) ? this.Accept(ZSystem.GuessType(Value)) : true;
	//	}

	public boolean IsFuncType() {
		return (this.GetRealType() instanceof BFuncType);
	}

	public String StringfySignature(String FuncName) {
		return FuncName;
	}

	public void Maybe(BType T, BToken SourceToken) {
	}

	public void bunfy(CommonStringBuilder builder) {
		builder.Append(this.GetName());
	}


}