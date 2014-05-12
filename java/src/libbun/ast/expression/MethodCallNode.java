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

package libbun.ast.expression;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public final class MethodCallNode extends BunNode {
	public static final int _NameInfo = 0;
	public final static int _Recv = 1;

	@BField public String  GivenName = null;

	public MethodCallNode(AstNode ParentNode, AstNode RecvNode, String MethodName) {
		super(ParentNode, 2);
		this.SetNullableNode(MethodCallNode._Recv, RecvNode);
		this.GivenName = MethodName;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new MethodCallNode(ParentNode, null, this.GivenName));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(methodcall", 0, ")");
	}

	public final AstNode RecvNode() {
		return this.AST[MethodCallNode._Recv ];
	}

	public final String MethodName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[MethodCallNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public final int getFuncParamSize() {
		return this.size() - 1;
	}

	public final int getMethodParamSize() {
		return this.size() - 2;
	}


	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitMethodCallNode(this);
	}

	public final FuncCallNode ToGetterFuncCall(BFuncType FuncType) {
		@Var GetFieldNode Getter = new GetFieldNode(null, this.RecvNode());
		if(this.AST[MethodCallNode._NameInfo] != null) {
			Getter.SetNode(GetFieldNode._NameInfo, this.AST[MethodCallNode._NameInfo]);
		}
		Getter.GivenName = this.GivenName;
		Getter.Type = FuncType;

		@Var FuncCallNode FuncNode = new FuncCallNode(this.ParentNode, Getter);
		FuncNode.SourceToken = this.SourceToken;
		if(FuncType.GetFuncParamSize() == this.getFuncParamSize()) {
			FuncNode.appendNode(this.RecvNode());
		}
		@Var int i = 2;
		while(i < this.size()) {
			FuncNode.appendNode(this.get(i));
			i = i + 1;
		}
		return FuncNode;
	}

	public final AbstractListNode ToFuncCallNode(LibBunTypeChecker Gamma, BFunc Func, @Nullable AstNode RecvNode) {
		@Var AbstractListNode FuncNode = Gamma.CreateDefinedFuncCallNode(this.ParentNode, this.getTokenAt(MethodCallNode._NameInfo), Func);
		FuncNode.SourceToken = this.getTokenAt(MethodCallNode._NameInfo);
		if(RecvNode != null) {
			FuncNode.appendNode(RecvNode);
		}
		@Var int i = 2;
		while(i < this.size()) {
			FuncNode.appendNode(this.get(i));
			i = i + 1;
		}
		return FuncNode;
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitMethodCallNode(this);
	}

}