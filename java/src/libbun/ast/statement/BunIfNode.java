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

package libbun.ast.statement;

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;

public final class BunIfNode extends BunNode {
	public final static int _Cond = 0;
	public final static int _Then = 1;
	public final static int _Else = 2;

	public BunIfNode(AstNode ParentNode) {
		super(ParentNode, 3);
	}
	@Override
	public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunIfNode(ParentNode));
	}
	@Override
	public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(if", 0, ")");
	}
	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitIfNode(this);
	}

	public final AstNode CondNode() {
		return this.AST[BunIfNode._Cond ];
	}

	public final AstNode ThenNode() {
		return this.AST[BunIfNode._Then ];
	}

	public final boolean HasElseNode() {
		return this.AST[BunIfNode._Else ] != null;
	}

	public final AstNode ElseNode() {
		return this.AST[BunIfNode._Else ];
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitIfNode(this);
	}
}