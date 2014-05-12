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
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.util.Var;

//E.g., $Recv[$Index]
public final class GetIndexNode extends MutableNode {
	public final static int _Recv = 0;
	public final static int _Index = 1;

	public GetIndexNode(AstNode ParentNode, AstNode RecvNode) {
		super(ParentNode, 2);
		this.SetNullableNode(GetIndexNode._Recv, RecvNode);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		@Var GetIndexNode NewNode = new GetIndexNode(ParentNode, null);
		if(typedClone) {
			NewNode.IsImmutable = this.IsImmutable;
		}
		return this.dupField(typedClone, NewNode);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(get", 0, ")");
	}

	public final AstNode RecvNode() {
		return this.AST[GetIndexNode._Recv ];
	}
	public final AstNode IndexNode() {
		return this.AST[GetIndexNode._Index ];
	}
	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitGetIndexNode(this);
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitGetIndexNode(this);
	}

}