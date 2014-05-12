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

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.Nullable;
import libbun.util.Var;

public final class FuncCallNode extends BunNode {
	public final static int _Functor = 0;

	public FuncCallNode(AstNode ParentNode, AstNode FuncNode) {
		super(ParentNode, 1);
		this.SetNullableNode(FuncCallNode._Functor, FuncNode);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new FuncCallNode(ParentNode, null));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(funccall", 0, ")");
	}

	public final AstNode FunctorNode() {
		return this.AST[FuncCallNode._Functor];
	}

	public final GetNameNode getNameNode() {
		AstNode node = this.AST[FuncCallNode._Functor];
		if(node instanceof GetNameNode) {
			return (GetNameNode)node;
		}
		return null;
	}

	@Nullable public final BunFuncNameNode FuncNameNode() {
		@Var AstNode NameNode = this.FunctorNode();
		if(NameNode instanceof BunFuncNameNode) {
			return (BunFuncNameNode)NameNode;
		}
		return null;
	}

	public final int getFuncParamSize() {
		return this.size() - 1;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitFuncCallNode(this);
	}

	public final BType GetRecvType() {
		if(this.getFuncParamSize() > 0) {
			return this.get(1).Type.GetRealType();
		}
		return BType.VoidType;
	}

	public final BFuncType GetFuncType() {
		@Var BType FType = this.FunctorNode().Type;
		if(FType instanceof BFuncType) {
			return (BFuncType)FType;
		}
		return null;
	}

	public ApplyMacroNode ToFormNode(BFormFunc FormFunc) {
		@Var ApplyMacroNode MacroNode = new ApplyMacroNode(this.ParentNode, this.FunctorNode().SourceToken, FormFunc);
		@Var int i = 1;
		while(i < this.size()) {
			MacroNode.appendNode(this.get(i));
			i = i + 1;
		}
		return MacroNode;
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitFuncCallNode(this);

	}


}