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

package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.ast.BlockNode;
import libbun.common.CommonArray;
import libbun.common.CommonStringBuilder;
import libbun.encode.LibBunGenerator;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.type.BTypePool;
import libbun.util.Var;

public class BunFunctionNode extends DefSymbolNode {
	public static final int _NameInfo = 0;
	//	public static final int _TypeInfo = 1;  // unused
	public final static int _Block    = 2;
	public final static int _Params   = 3;
	public final static int _ReturnTypeInfo = 4;

	public BType ReturnType = null;

	public BunFunctionNode(AstNode ParentNode, int symbolFlag) {
		super(ParentNode, 5, symbolFlag);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		@Var BunFunctionNode NewNode = new BunFunctionNode(ParentNode, this.symbolFlag);
		NewNode.GivenType = this.GivenType;
		NewNode.GivenName = this.GivenName;
		NewNode.ParentFunctionNode = this.ParentFunctionNode;
		NewNode.ReturnType = this.ReturnType;
		return this.dupField(typedClone, NewNode);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(function ", this.FuncName(), " ");
		this.ReturnType().bunfy(builder);
		this.ParamNode().bunfyAST(builder, " (", 0, ") ");
		this.blockNode().bunfy(builder);
		builder.Append(")");
	}

	public final BType ReturnType() {
		if(this.ReturnType == null) {
			if(this.AST[BunFunctionNode._ReturnTypeInfo] != null) {
				this.ReturnType = this.AST[BunFunctionNode._ReturnTypeInfo].Type;
			}
			else {
				this.ReturnType = BType.VarType;
			}
		}
		return this.ReturnType;
	}

	public final void SetReturnType(BType Type) {
		this.ReturnType = Type;
	}

	public final String FuncName() {
		if(this.GivenName == null && this.AST[BunFunctionNode._NameInfo] != null) {
			this.GivenName = this.AST[BunFunctionNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	@Override
	public final String GetUniqueName(LibBunGenerator Generator) {
		@Var String FuncName = this.FuncName();
		if(FuncName == null) {
			this.GivenName = Generator.NameUniqueSymbol("f");
			FuncName = this.GivenName;
		}
		return FuncName;
	}

	public final BlockNode blockNode() {
		@Var AstNode blockNode = this.AST[BunFunctionNode._Block];
		if(blockNode instanceof BlockNode) {
			return (BlockNode)blockNode;
		}
		assert(blockNode == null); // this must not happen
		return null;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitFunctionNode(this);
	}

	public final int getParamSize() {
		if(this.AST[BunFunctionNode._Params] != null) {
			return this.AST[BunFunctionNode._Params].size();
		}
		return 0;
	}

	public final BunLetVarNode GetParamNode(int Index) {
		@Var AstNode Node = this.AST[BunFunctionNode._Params].AST[Index];
		if(Node instanceof BunLetVarNode) {
			return (BunLetVarNode)Node;
		}
		return null;
	}

	public final BlockNode ParamNode() {
		@Var AstNode blockNode = this.AST[BunFunctionNode._Params];
		if(blockNode instanceof BlockNode) {
			return (BlockNode)blockNode;
		}
		assert(blockNode == null); // this must not happen
		return null;
	}


	public final BFuncType GetFuncType() {
		if(this.GivenType == null) {
			@Var CommonArray<BType> TypeList = new CommonArray<BType>(new BType[this.getParamSize()+1]);
			@Var int i = 0;
			while(i < this.getParamSize()) {
				@Var BunLetVarNode Node = this.GetParamNode(i);
				@Var BType ParamType = Node.DeclType().GetRealType();
				TypeList.add(ParamType);
				i = i + 1;
			}
			TypeList.add(this.ReturnType().GetRealType());
			@Var BFuncType FuncType = BTypePool._LookupFuncType2(TypeList);
			if(!FuncType.IsVarType()) {
				this.GivenType = FuncType;
			}
			return FuncType;
		}
		return (BFuncType)this.GivenType;
	}

	public final String GetSignature() {
		@Var BFuncType FuncType = this.GetFuncType();
		return FuncType.StringfySignature(this.FuncName());
	}

	//	@BField public BFuncType       ResolvedFuncType = null;

	public BunFunctionNode ParentFunctionNode = null;

	public final BunFunctionNode Push(BunFunctionNode Parent) {
		this.ParentFunctionNode = Parent;
		return this;
	}

	public final BunFunctionNode Pop() {
		return this.ParentFunctionNode;
	}

	public final boolean IsTopLevelDefineFunction() {
		return (this.Type.IsVoidType());
	}


}