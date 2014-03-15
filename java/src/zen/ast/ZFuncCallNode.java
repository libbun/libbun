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

package zen.ast;

import zen.parser.ZMacroFunc;
import zen.parser.ZVisitor;
import zen.type.ZFuncType;
import zen.type.ZType;
import zen.util.Var;

public final class ZFuncCallNode extends ZListNode {
	public final static int _Func = 0;

	public ZFuncCallNode(ZNode ParentNode, ZNode FuncNode) {
		super(ParentNode, null, 1);
		this.SetNode(ZFuncCallNode._Func, FuncNode);
	}

	@Deprecated public ZFuncCallNode(ZNode ParentNode, String FuncName, ZFuncType FuncType) {
		super(ParentNode, null, 1);
		this.SetNode(ZFuncCallNode._Func, new ZGlobalNameNode(this, null, FuncName, FuncType));
	}

	public final ZNode FunctionNode() {
		return this.AST[ZFuncCallNode._Func ];
	}

	public final ZGlobalNameNode FuncNameNode() {
		@Var ZNode NameNode = this.FunctionNode();
		if(NameNode instanceof ZGlobalNameNode) {
			return (ZGlobalNameNode)NameNode;
		}
		return null;
	}

	@Override public void Accept(ZVisitor Visitor) {
		Visitor.VisitFuncCallNode(this);
	}

	public final ZType GetRecvType() {
		if(this.GetListSize() > 0) {
			return this.GetListAt(0).Type.GetRealType();
		}
		return ZType.VoidType;
	}

	public final boolean IsStaticFuncCall() {
		@Var ZNode FNode = this.FunctionNode();
		if(FNode instanceof ZGlobalNameNode) {
			return ((ZGlobalNameNode)FNode).IsFuncNameNode();
		}
		return false;
	}

	public final String GetStaticFuncName() {
		@Var ZNode FNode = this.FunctionNode();
		if(FNode instanceof ZGlobalNameNode) {
			return ((ZGlobalNameNode)FNode).GlobalName;
		}
		return null;
	}

	public final ZFuncType GetFuncType() {
		@Var ZType FType = this.FunctionNode().Type;
		if(FType instanceof ZFuncType) {
			return (ZFuncType)FType;
		}
		return null;
	}

	public ZMacroNode ToMacroNode(ZMacroFunc MacroFunc) {
		@Var ZMacroNode MacroNode = new ZMacroNode(this.ParentNode, this.FunctionNode().SourceToken, MacroFunc);
		@Var int i = 0;
		while(i < this.GetListSize()) {
			MacroNode.Append(this.GetListAt(i));
			i = i + 1;
		}
		return MacroNode;
	}


}