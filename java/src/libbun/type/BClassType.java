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

import libbun.parser.classic.BToken;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;
import libbun.util.BArray;

public class BClassType extends BType {
	public final static BClassType _ObjectType = new BClassType("Object");

	@BField BArray<BClassField> FieldList = null;

	private BClassType(String ShortName) {
		super(BType.OpenTypeFlag|BType.UniqueTypeFlag, ShortName, BType.VarType);
		this.TypeFlag = LibBunSystem._UnsetFlag(this.TypeFlag, BType.OpenTypeFlag);
	}

	public BClassType(String ShortName, BType RefType) {
		super(BType.OpenTypeFlag|BType.UniqueTypeFlag, ShortName, RefType);
		if(RefType instanceof BClassType) {
			this.EnforceSuperClass((BClassType)RefType);
		}
	}

	public final void EnforceSuperClass(BClassType SuperClass) {
		this.RefType = SuperClass;
		if(SuperClass.FieldList != null) {
			this.FieldList = new BArray<BClassField>(new BClassField[10]);
			@Var int i = 0;
			while(i < SuperClass.FieldList.size()) {
				@Var BClassField Field = SuperClass.FieldList.ArrayValues[i];
				this.FieldList.add(Field);
				i = i + 1;
			}
		}
	}

	public final int GetFieldSize() {
		if(this.FieldList != null) {
			return this.FieldList.size();
		}
		return 0;
	}

	public final BClassField GetFieldAt(int Index) {
		return this.FieldList.ArrayValues[Index];
	}

	public boolean HasField(String FieldName) {
		if(this.FieldList != null) {
			@Var int i = 0;
			while(i < this.FieldList.size()) {
				if(FieldName.equals(this.FieldList.ArrayValues[i].FieldName)) {
					return true;
				}
				i = i + 1;
			}
		}
		return false;
	}

	public BType GetFieldType(String FieldName, BType DefaultType) {
		if(this.FieldList != null) {
			@Var int i = 0;
			//			System.out.println("FieldSize = " + this.FieldList.size() + " by " + FieldName);
			while(i < this.FieldList.size()) {
				@Var BClassField Field = this.FieldList.ArrayValues[i];
				//				System.out.println("Looking FieldName = " + Field.FieldName + ", " + Field.FieldType);
				if(FieldName.equals(Field.FieldName)) {
					return Field.FieldType;
				}
				i = i + 1;
			}
		}
		return DefaultType;
	}

	public BClassField AppendField(BType FieldType, String FieldName, BToken SourceToken) {
		assert(!FieldType.IsVarType());
		if(this.FieldList == null) {
			this.FieldList = new BArray<BClassField>(new BClassField[4]);
		}
		@Var BClassField ClassField = new BClassField(this, FieldName, FieldType, SourceToken);
		//		System.out.println("Append FieldName = " + ClassField.FieldName + ", " + ClassField.FieldType);
		assert(ClassField.FieldType != null);
		this.FieldList.add(ClassField);
		return ClassField;
	}

	//	public ZNode CheckAllFields(ZGamma Gamma) {
	//		// TODO Auto-generated method stub
	//
	//		return null;  // if no error
	//	}


}
