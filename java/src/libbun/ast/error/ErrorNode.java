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

package libbun.ast.error;

import libbun.ast.BNode;
import libbun.ast.literal.ConstNode;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BToken;
import libbun.util.BField;
import libbun.util.BIgnored;

public class ErrorNode extends ConstNode {
	@BField public String ErrorMessage;
	public ErrorNode(BNode ParentNode, BToken SourceToken, String ErrorMessage) {
		super(ParentNode, SourceToken);
		this.ErrorMessage = ErrorMessage;
	}
	public ErrorNode(BNode Node, String ErrorMessage) {
		super(Node.ParentNode, Node.SourceToken);
		this.ErrorMessage = ErrorMessage;
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new ErrorNode(ParentNode, null, this.ErrorMessage));
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitErrorNode(this);
	}

	@BIgnored @Override public String toString() {
		return "ErrorNode '" + this.ErrorMessage + "'";
	}
}