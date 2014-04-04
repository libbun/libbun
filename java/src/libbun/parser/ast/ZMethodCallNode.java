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

import libbun.parser.BTypeChecker;
import libbun.parser.BVisitor;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public final class ZMethodCallNode extends ZListNode {
	public final static int _Recv = 0;
	public static final int _NameInfo = 1;

	@BField public String  GivenName = null;

	public ZMethodCallNode(BNode ParentNode, BNode RecvNode) {
		super(ParentNode, null, 2);
		this.SetNode(ZMethodCallNode._Recv, RecvNode);
	}

	public final BNode RecvNode() {
		return this.AST[ZMethodCallNode._Recv ];
	}

	public final String MethodName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[ZMethodCallNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	@Override public void Accept(BVisitor Visitor) {
		Visitor.VisitMethodCallNode(this);
	}

	public final ZFuncCallNode ToGetterFuncCall(BFuncType FuncType) {
		@Var ZGetterNode Getter = new ZGetterNode(null, this.RecvNode());
		if(this.AST[ZMethodCallNode._NameInfo] != null) {
			Getter.SetNode(ZGetterNode._NameInfo, this.AST[ZMethodCallNode._NameInfo]);
		}
		Getter.GivenName = this.GivenName;
		Getter.Type = FuncType;

		@Var ZFuncCallNode FuncNode = new ZFuncCallNode(this.ParentNode, Getter);
		FuncNode.SourceToken = this.SourceToken;
		if(FuncType.GetFuncParamSize() == this.GetListSize() + 1) {
			FuncNode.Append(this.RecvNode());
		}
		@Var int i = 0;
		while(i < this.GetListSize()) {
			FuncNode.Append(this.GetListAt(i));
			i = i + 1;
		}
		return FuncNode;
	}

	public final ZListNode ToFuncCallNode(BTypeChecker Gamma, BFunc Func, @Nullable BNode RecvNode) {
		@Var ZListNode FuncNode = Gamma.CreateDefinedFuncCallNode(this.ParentNode, this.GetAstToken(ZMethodCallNode._NameInfo), Func);
		FuncNode.SourceToken = this.GetAstToken(ZMethodCallNode._NameInfo);
		if(RecvNode != null) {
			FuncNode.Append(RecvNode);
		}
		@Var int i = 0;
		while(i < this.GetListSize()) {
			FuncNode.Append(this.GetListAt(i));
			i = i + 1;
		}
		return FuncNode;
	}

}