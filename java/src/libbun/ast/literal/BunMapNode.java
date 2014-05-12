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

package libbun.ast.literal;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.util.Var;

public final class BunMapNode extends AbstractListNode {
	public BunMapNode(BNode ParentNode) {
		super(ParentNode, 0);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunMapNode(ParentNode));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(map", this.vargStartIndex, ")");
	}
	public final BunMapEntryNode GetMapEntryNode(int Index) {
		@Var BNode Node = this.GetListAt(Index);
		if(Node instanceof BunMapEntryNode) {
			return (BunMapEntryNode)Node;
		}
		return null;
	}
	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitMapLiteralNode(this);
	}
}