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
import libbun.ast.BNode;
import libbun.ast.BlockNode;
import libbun.parser.classic.BNodeUtils;
import libbun.parser.classic.LibBunVisitor;
import libbun.util.Var;

public class BunVarBlockNode extends BlockNode {
	public static final int _VarDecl = 0;

	public BunVarBlockNode(BNode ParentNode, BunLetVarNode VarNode) {
		super(ParentNode, null, 1);
		this.SetNullableNode(BunVarBlockNode._VarDecl, VarNode);
	}

	public BunVarBlockNode(BNode ParentNode, BunLetVarNode VarNode, BlockNode ParentblockNode) {
		super(ParentNode, null, 1);
		this.SetNode(BunVarBlockNode._VarDecl, VarNode);
		@Var int Index = BNodeUtils._AstListIndexOf(ParentblockNode, VarNode);
		assert(Index >= 0);
		// before: ParentblockNode = [NodeA, NodeB, ..., VarNode, NodeC, ..., NodeZ], this = []
		// after : ParentblockNode = [NodeA, NodeB, ..., this],  this = [NodeC, ..., NodeZ]
		BNodeUtils._MoveAstList(ParentblockNode, Index + 1, this);
		ParentblockNode.SetListAt(Index, this);
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunVarBlockNode(ParentNode, null));
	}

	public final BunLetVarNode VarDeclNode() {
		@Var BNode VarNode = this.AST[BunVarBlockNode._VarDecl];
		if(VarNode instanceof BunLetVarNode) {
			return (BunLetVarNode)VarNode;
		}
		return null;
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitVarblockNode(this);
	}


}