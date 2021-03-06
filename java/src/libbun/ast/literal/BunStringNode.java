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

import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunToken;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;

public final class BunStringNode extends BunValueNode {
	@BField public String	StringValue;
	public BunStringNode(AstNode ParentNode, String Value) {
		super(ParentNode);
		this.Type = BType.StringType;
		this.StringValue = Value;
	}
	public BunStringNode(AstNode ParentNode, BunToken Source, String Value) {
		super(ParentNode);
		this.Type = BType.StringType;
		this.StringValue = Value;
		this.SourceToken = Source;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunStringNode(ParentNode, this.StringValue));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append(LibBunSystem._QuoteString("\"", this.StringValue, "\""));
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitStringNode(this);
		}
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitStringNode(this);
	}

}