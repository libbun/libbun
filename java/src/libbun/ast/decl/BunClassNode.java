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

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BClassType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public final class BunClassNode extends AbstractListNode {
	public static final int _NameInfo = 0;
	public static final int _TypeInfo = 1;

	@BField public String  GivenName = null;
	@BField public BClassType ClassType = null;
	@BField public boolean IsExport = false;

	public BunClassNode(AstNode ParentNode) {
		super(ParentNode, 2);
	}

	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		@Var BunClassNode NewNode = new BunClassNode(ParentNode);
		NewNode.GivenName = this.GivenName;
		NewNode.ClassType = this.ClassType;
		NewNode.IsExport  = this.IsExport;
		return this.dupField(TypedClone, NewNode);
	}

	public final String ClassName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[BunLetVarNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public final BType SuperType() {
		if(this.AST[BunLetVarNode._TypeInfo] != null) {
			return this.AST[BunLetVarNode._TypeInfo].Type;
		}
		else {
			return BClassType._ObjectType;
		}
	}

	public final BunLetVarNode GetFieldNode(int Index) {
		@Var AstNode Node = this.GetListAt(Index);
		if(Node instanceof BunLetVarNode) {
			return (BunLetVarNode)Node;
		}
		return null;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitClassNode(this);
	}
}