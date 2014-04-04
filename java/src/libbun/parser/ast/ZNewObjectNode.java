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

package libbun.parser.ast;

import libbun.parser.ZTypeChecker;
import libbun.parser.ZVisitor;
import libbun.type.ZFunc;
import libbun.type.ZType;
import libbun.util.BField;
import libbun.util.Var;

public final class ZNewObjectNode extends ZListNode {
	public static final int _TypeInfo = 0;

	@BField public ZType GivenType = null;

	public ZNewObjectNode(BNode ParentNode) {
		super(ParentNode, null, 1);
	}

	public final ZType ClassType() {
		if(this.GivenType == null) {
			if(this.AST[ZNewObjectNode._TypeInfo] != null) {
				return this.AST[ZNewObjectNode._TypeInfo].Type;
			}
			else {
				return ZType.VarType;
			}
		}
		return this.GivenType;
	}

	@Override public void Accept(ZVisitor Visitor) {
		Visitor.VisitNewObjectNode(this);
	}

	public final ZListNode ToFuncCallNode(ZTypeChecker Gamma, ZFunc Func) {
		@Var ZListNode FuncNode = Gamma.CreateDefinedFuncCallNode(this.ParentNode, this.SourceToken, Func);
		FuncNode.Append(this);
		@Var int i = 0;
		while(i < this.GetListSize()) {
			FuncNode.Append(this.GetListAt(i));
			i = i + 1;
		}
		this.Type = this.ClassType();
		this.ClearListToSize(0);
		return FuncNode;
	}
}