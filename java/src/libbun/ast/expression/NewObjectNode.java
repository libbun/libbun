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

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BFunc;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public final class NewObjectNode extends BunNode {
	public static final int _TypeInfo = 0;

	@BField public BType GivenType = null;

	public NewObjectNode(AstNode ParentNode) {
		super(ParentNode, 1);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new NewObjectNode(ParentNode));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(new ");
		this.ClassType().bunfy(builder);
		this.bunfyAST(builder, "", 1, ")");
	}

	public final BType ClassType() {
		if(this.GivenType == null) {
			if(this.AST[NewObjectNode._TypeInfo] != null) {
				return this.AST[NewObjectNode._TypeInfo].Type;
			}
			else {
				return BType.VarType;
			}
		}
		return this.GivenType;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitNewObjectNode(this);
	}

	public final AbstractListNode ToFuncCallNode(LibBunTypeChecker Gamma, BFunc Func) {
		@Var AbstractListNode FuncNode = Gamma.CreateDefinedFuncCallNode(this.ParentNode, this.SourceToken, Func);
		FuncNode.appendNode(this);
		@Var int i = 1;
		while(i < this.size()) {  // checked
			FuncNode.appendNode(this.get(i));  // checked
			i = i + 1;
		}
		this.Type = this.ClassType();
		this.resizeAst(0);
		return FuncNode;
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		// TODO Auto-generated method stub

	}
}