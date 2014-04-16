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

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.parser.BVisitor;
import libbun.type.BType;
import libbun.util.Var;

public final class BunWhileNode extends BNode {
	public final static int _Cond  = 0;
	public final static int _Block = 1;
	public final static int _Next  = 2;   // optional iteration statement

	public BunWhileNode(BNode ParentNode) {
		super(ParentNode, null, 3);
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunWhileNode(ParentNode));
	}

	public BunWhileNode(BNode CondNode, BunBlockNode BlockNode) {
		super(null, null, 3);
		this.SetNode(BunWhileNode._Cond, CondNode);
		this.SetNode(BunWhileNode._Block, BlockNode);
		this.Type = BType.VoidType;
	}

	@Override public void Accept(BVisitor Visitor) {
		Visitor.VisitWhileNode(this);
	}

	public final BNode CondNode() {
		return this.AST[BunWhileNode._Cond];
	}

	public final BunBlockNode BlockNode() {
		@Var BNode BlockNode = this.AST[BunWhileNode._Block];
		if(BlockNode instanceof BunBlockNode) {
			return (BunBlockNode)BlockNode;
		}
		assert(BlockNode == null); // this must not happen
		return null;
	}

	public final boolean HasNextNode() {
		return (this.AST[BunWhileNode._Next] != null);
	}

	public final BNode NextNode() {
		return this.AST[BunWhileNode._Next];
	}


}