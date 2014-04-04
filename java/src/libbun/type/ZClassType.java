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

import libbun.parser.ZToken;
import libbun.util.BField;
import libbun.util.BLib;
import libbun.util.Var;
import libbun.util.BArray;

public class ZClassType extends ZType {
	public final static ZClassType _ObjectType = new ZClassType("Object");

	@BField BArray<ZClassField> FieldList = null;

	private ZClassType(String ShortName) {
		super(ZType.OpenTypeFlag|ZType.UniqueTypeFlag, ShortName, ZType.VarType);
		this.TypeFlag = BLib._UnsetFlag(this.TypeFlag, ZType.OpenTypeFlag);
	}

	public ZClassType(String ShortName, ZType RefType) {
		super(ZType.OpenTypeFlag|ZType.UniqueTypeFlag, ShortName, RefType);
		if(RefType instanceof ZClassType) {
			this.EnforceSuperClass((ZClassType)RefType);
		}
	}

	public final void EnforceSuperClass(ZClassType SuperClass) {
		this.RefType = SuperClass;
		if(SuperClass.FieldList != null) {
			this.FieldList = new BArray<ZClassField>(new ZClassField[10]);
			@Var int i = 0;
			while(i < SuperClass.FieldList.size()) {
				@Var ZClassField Field = SuperClass.FieldList.ArrayValues[i];
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

	public final ZClassField GetFieldAt(int Index) {
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

	public ZType GetFieldType(String FieldName, ZType DefaultType) {
		if(this.FieldList != null) {
			@Var int i = 0;
			//			System.out.println("FieldSize = " + this.FieldList.size() + " by " + FieldName);
			while(i < this.FieldList.size()) {
				@Var ZClassField Field = this.FieldList.ArrayValues[i];
				//				System.out.println("Looking FieldName = " + Field.FieldName + ", " + Field.FieldType);
				if(FieldName.equals(Field.FieldName)) {
					return Field.FieldType;
				}
				i = i + 1;
			}
		}
		return DefaultType;
	}

	public ZClassField AppendField(ZType FieldType, String FieldName, ZToken SourceToken) {
		assert(!FieldType.IsVarType());
		if(this.FieldList == null) {
			this.FieldList = new BArray<ZClassField>(new ZClassField[4]);
		}
		@Var ZClassField ClassField = new ZClassField(this, FieldName, FieldType, SourceToken);
		//		System.out.println("Append FieldName = " + ClassField.FieldName + ", " + ClassField.FieldType);
		assert(ClassField.FieldType != null);
		this.FieldList.add(ClassField);
		return ClassField;
	}

	//	public ZNode CheckAllFields(ZNameSpace NameSpace) {
	//		// TODO Auto-generated method stub
	//
	//		return null;  // if no error
	//	}


}
