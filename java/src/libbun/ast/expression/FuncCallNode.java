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
import libbun.ast.BNode;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.Nullable;
import libbun.util.Var;

public final class FuncCallNode extends AbstractListNode {
	public final static int _Functor = 0;

	public FuncCallNode(BNode ParentNode, BNode FuncNode) {
		super(ParentNode, 1);
		this.SetNullableNode(FuncCallNode._Functor, FuncNode);
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new FuncCallNode(ParentNode, null));
	}

	public final BNode FunctorNode() {
		return this.AST[FuncCallNode._Functor];
	}

	@Nullable public final BunFuncNameNode FuncNameNode() {
		@Var BNode NameNode = this.FunctorNode();
		if(NameNode instanceof BunFuncNameNode) {
			return (BunFuncNameNode)NameNode;
		}
		return null;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitFuncCallNode(this);
	}

	public final BType GetRecvType() {
		if(this.GetListSize() > 0) {
			return this.GetListAt(0).Type.GetRealType();
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

	public BunFormNode ToFormNode(BFormFunc FormFunc) {
		@Var BunFormNode MacroNode = new BunFormNode(this.ParentNode, this.FunctorNode().SourceToken, FormFunc);
		@Var int i = 0;
		while(i < this.GetListSize()) {
			MacroNode.Append(this.GetListAt(i));
			i = i + 1;
		}
		return MacroNode;
	}


}