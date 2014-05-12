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

package libbun.ast;

import libbun.ast.decl.BunLetVarNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.SymbolTable;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public class BunBlockNode extends AbstractListNode {
	@BField public SymbolTable NullableGamma;

	public BunBlockNode(BNode ParentNode, @Nullable SymbolTable Gamma) {
		super(ParentNode, 0);
		this.NullableGamma = Gamma;
	}

	protected BunBlockNode(BNode ParentNode, @Nullable SymbolTable Gamma, int Init) {  // call by ZVarNode
		super(ParentNode, Init);
		this.NullableGamma = Gamma;
	}

	public BunBlockNode(BNode ParentNode, @Nullable SymbolTable Gamma, BunLetVarNode VarNode) {
		super(ParentNode, 1);
		this.NullableGamma = Gamma;
		this.SetNode(0, VarNode);
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunBlockNode(ParentNode, this.NullableGamma));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(block", this.vargStartIndex, ")");
	}

	public final SymbolTable getBlockSymbolTable() {
		if(this.NullableGamma == null) {
			@Var SymbolTable parentTable = this.getSymbolTable();
			this.NullableGamma = new SymbolTable(parentTable.namespace);
		}
		return this.NullableGamma;
	}

	public final LibBunGamma GetBlockGamma() {
		if(this.NullableGamma == null) {
			@Var LibBunGamma Gamma = this.GetGamma();
			this.NullableGamma = new LibBunGamma(Gamma.Generator, this);
		}
		return (LibBunGamma)this.NullableGamma;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitBlockNode(this);
	}

	public final void ReplaceWith(BNode OldNode, BNode NewNode) {
		@Var int i = 0;
		while(i < this.GetAstSize()) {
			if(this.AST[i] == OldNode) {
				this.AST[i] = NewNode;
				this.SetChild(NewNode, BNode._EnforcedParent);
				if(NewNode.HasUntypedNode()) {
					this.HasUntyped = true;
				}
				return;
			}
			i = i + 1;
		}
		//		System.out.println("no replacement");
		assert(OldNode == NewNode);  // this must not happen!!
	}
}