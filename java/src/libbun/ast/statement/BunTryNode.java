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
import libbun.parser.classic.LibBunVisitor;
import libbun.util.Var;

public final class BunTryNode extends BNode {
	public final static int _Try      = 0;
	public static final int _NameInfo = 1;
	public final static int _Catch    = 2;
	public final static int _Finally  = 3;

	public BunTryNode(BNode ParentNode) {
		super(ParentNode, 4);
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunTryNode(ParentNode));
	}

	public final BunBlockNode TryBlockNode() {
		@Var BNode BlockNode = this.AST[BunTryNode._Try ];
		if(BlockNode instanceof BunBlockNode) {
			return (BunBlockNode)BlockNode;
		}
		assert(BlockNode == null); // this must not happen
		return null;
	}

	public final String ExceptionName() {
		return this.AST[BunTryNode._NameInfo].SourceToken.GetText();
	}

	public final boolean HasCatchBlockNode() {
		return (this.AST[BunTryNode._NameInfo] != null && this.AST[BunTryNode._Catch ] != null);
	}

	public final BunBlockNode CatchBlockNode() {
		@Var BNode BlockNode = this.AST[BunTryNode._Catch ];
		if(BlockNode instanceof BunBlockNode) {
			return (BunBlockNode)BlockNode;
		}
		assert(BlockNode == null); // this must not happen
		return null;
	}

	public final boolean HasFinallyBlockNode() {
		return (this.AST[BunTryNode._Finally ] != null);
	}

	public final BunBlockNode FinallyBlockNode() {
		@Var BNode BlockNode = this.AST[BunTryNode._Finally ];
		if(BlockNode instanceof BunBlockNode) {
			return (BunBlockNode)BlockNode;
		}
		assert(BlockNode == null); // this must not happen
		return null;
	}


	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitTryNode(this);
	}




}