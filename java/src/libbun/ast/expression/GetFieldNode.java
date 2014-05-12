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
import libbun.ast.literal.BunTypeNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.util.BField;
import libbun.util.Var;

public class GetFieldNode extends MutableNode {
	public static final int _NameInfo = 0;
	public final static int _Recv     = 1;

	@BField public String  GivenName = null;

	protected GetFieldNode(AstNode ParentNode, AstNode RecvNode, int Size) {  //
		super(ParentNode, Size);
		this.SetNullableNode(GetFieldNode._Recv, RecvNode);
	}

	public GetFieldNode(AstNode ParentNode, AstNode RecvNode) {
		this(ParentNode, RecvNode, 2);
	}

	public GetFieldNode(AstNode ParentNode) {
		super(ParentNode, 2);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.swap(0, 1);
		this.bunfyAST(builder, "(field", 0, ")");
		this.swap(0, 1);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		@Var GetFieldNode NewNode = new GetFieldNode(ParentNode, null);
		if(typedClone) {
			NewNode.IsImmutable = this.IsImmutable;
		}
		NewNode.GivenName = this.GivenName;
		return this.dupField(typedClone, NewNode);
	}

	public final AstNode RecvNode() {
		return this.AST[GetFieldNode._Recv ];
	}

	public final String GetName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[GetFieldNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitGetFieldNode(this);
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitGetFieldNode(this);
	}

	public final boolean IsStaticField() {
		return this.RecvNode() instanceof BunTypeNode;
	}

}