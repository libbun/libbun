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
import libbun.ast.LegacyBlockNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BType;
import libbun.util.Var;

public final class BunWhileNode extends AstNode {
	public final static int _Cond  = 0;
	public final static int _Block = 1;
	public final static int _Next  = 2;   // optional iteration statement

	public BunWhileNode(AstNode ParentNode) {
		super(ParentNode, 3);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunWhileNode(ParentNode));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(while", 0, ")");
	}

	public BunWhileNode(AstNode CondNode, LegacyBlockNode blockNode) {
		super(null, 3);
		this.SetNode(BunWhileNode._Cond, CondNode);
		this.SetNode(BunWhileNode._Block, blockNode);
		this.Type = BType.VoidType;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitWhileNode(this);
	}

	public final AstNode CondNode() {
		return this.AST[BunWhileNode._Cond];
	}

	public final LegacyBlockNode blockNode() {
		@Var AstNode blockNode = this.AST[BunWhileNode._Block];
		if(blockNode instanceof LegacyBlockNode) {
			return (LegacyBlockNode)blockNode;
		}
		assert(blockNode == null); // this must not happen
		return null;
	}

	public final boolean HasNextNode() {
		return (this.AST[BunWhileNode._Next] != null);
	}

	public final AstNode NextNode() {
		return this.AST[BunWhileNode._Next];
	}


}