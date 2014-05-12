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

package libbun.ast.unary;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BFunc;
import libbun.type.BType;
import libbun.util.Var;

public class BunCastNode extends AstNode {
	public final static int _Expr = 0;
	public final static int _TypeInfo = 1;

	public BunCastNode(AstNode ParentNode, BType CastType, AstNode Node) {
		super(ParentNode, 2);
		this.Type = CastType;
		if(Node != null) {
			this.SetNode(BunCastNode._Expr, Node);
		}
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new BunCastNode(ParentNode, this.Type, null));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(to ");
		this.CastType().bunfy(builder);
		builder.Append(" ");
		this.ExprNode().bunfy(builder);
		builder.Append(")");
	}

	public final AstNode ExprNode() {
		return this.AST[BunCastNode._Expr ];
	}

	public final BType CastType() {
		if(this.AST[BunCastNode._TypeInfo ] != null) {
			this.Type = this.AST[BunCastNode._TypeInfo ].Type;
		}
		return this.Type;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitCastNode(this);
	}

	public final AbstractListNode ToFuncCallNode(LibBunTypeChecker TypeChecker, BFunc ConverterFunc) {
		@Var AbstractListNode FuncNode = TypeChecker.CreateDefinedFuncCallNode(this.ParentNode, this.SourceToken, ConverterFunc);
		FuncNode.appendNode(this.ExprNode());
		return FuncNode;
	}

}