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

package libbun.parser.ast;

import libbun.parser.ZVisitor;
import libbun.util.Field;

public class ZGetterNode extends BNode {
	public final static int _Recv = 0;
	public static final int _NameInfo = 1;

	@Field public String  GivenName = null;

	protected ZGetterNode(BNode ParentNode, BNode RecvNode, int Size) {
		super(ParentNode, null, Size);
		this.SetNode(ZGetterNode._Recv, RecvNode);
	}

	public ZGetterNode(BNode ParentNode, BNode RecvNode) {
		super(ParentNode, null, 2);
		this.SetNode(ZGetterNode._Recv, RecvNode);
	}

	//	public final ZNameSpace GetPrefixNameSpace() {
	//		@Var BNode Node = this.AST[ZGetterNode._Recv ];
	//		if(Node instanceof BGetNameNode) {
	//			@Var String Name = ((BGetNameNode)Node).GetName();
	//			@Var BNode NameSpaceName = Node.GetNameSpace().GetSymbol(Name);
	//		}
	//		return null;
	//	}

	public final BNode RecvNode() {
		return this.AST[ZGetterNode._Recv ];
	}

	public final String GetName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[ZGetterNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	@Override public void Accept(ZVisitor Visitor) {
		Visitor.VisitGetterNode(this);
	}

	public final boolean IsStaticField() {
		return this.RecvNode() instanceof ZTypeNode;
	}
}