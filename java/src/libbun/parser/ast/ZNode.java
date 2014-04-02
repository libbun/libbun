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

import libbun.parser.ZNameSpace;
import libbun.parser.ZToken;
import libbun.parser.ZTypeChecker;
import libbun.parser.ZVisitor;
import libbun.type.ZFuncType;
import libbun.type.ZType;
import libbun.util.Field;
import libbun.util.LibZen;
import libbun.util.Nullable;
import libbun.util.Var;

public abstract class ZNode {
	public final static int _AppendIndex = -1;
	public final static int _NestedAppendIndex = -2;
	public final static int _Nop =      -3;
	public final static boolean _EnforcedParent = true;
	public final static boolean _PreservedParent = false;

	@Field public ZNode ParentNode;
	@Field public ZToken SourceToken;
	@Field public ZNode  AST[];
	@Field public ZType	Type = ZType.VarType;
	@Field public boolean HasUntyped = true;

	public ZNode(ZNode ParentNode, ZToken SourceToken, int Size) {
		assert(this != ParentNode);
		this.ParentNode = ParentNode;
		this.SourceToken = SourceToken;
		if(Size > 0) {
			this.AST = LibZen._NewNodeArray(Size);
		}
		else {
			this.AST = null;
		}
	}

	public final String GetSourceLocation() {
		if(this.SourceToken != null) {
			return "(" + this.SourceToken.GetFileName() + ":" + this.SourceToken.GetLineNumber() + ")";
		}
		return null;
	}

	@Override public String toString() {
		@Var String Self = "#" + LibZen._GetClassName(this);
		if(!this.Type.IsVarType()) {
			Self = Self + ":" + this.Type;
		}
		else {
			Self = Self + ":?";
		}
		if(this.AST != null) {
			@Var int i = 0;
			Self = Self + "[";
			while(i < this.AST.length) {
				if(i > 0) {
					Self = Self + ",";
				}
				if(this.AST[i] == null) {
					Self = Self + "null";
				}
				else {
					if(this.AST[i].ParentNode == this) {
						Self = Self + this.AST[i].toString();
					}
					else {
						Self = Self + "*" + LibZen._GetClassName(this.AST[i])+"*";
					}
				}
				i = i + 1;
			}
			Self = Self + "]";
		}
		return Self;
	}

	public final ZNode SetChild(ZNode Node, boolean EnforcedParent) {
		assert(this != Node);
		if(EnforcedParent || Node.ParentNode == null) {
			Node.ParentNode = this;
		}
		return Node;
	}

	public final ZNode SetNode(int Index, ZNode Node, boolean EnforcedParent) {
		if(Index >= 0) {
			this.AST[Index] = this.SetChild(Node, EnforcedParent);
		}
		else if(Index == ZNode._AppendIndex) {
			@Var ZNode ListNode = this;
			if(ListNode instanceof ZListNode) {
				((ZListNode)ListNode).Append(Node);
			}
			else {
				assert(ListNode instanceof ZListNode);
			}
		}
		return Node;
	}

	public final ZNode SetNode(int Index, ZNode Node) {
		return this.SetNode(Index, Node, ZNode._EnforcedParent);
	}

	public final int GetAstSize() {
		if(this.AST == null) {
			return 0;
		}
		return this.AST.length;
	}

	public final ZType GetAstType(int Index) {
		if(Index < this.AST.length) {
			return this.AST[Index].Type.GetRealType();
		}
		return ZType.VoidType;  // to retrieve RecvType
	}

	public final void SetAstType(int Index, ZType Type) {
		if(this.AST[Index] != null) {
			this.AST[Index].Type = Type;
		}
	}

	public final ZToken GetAstToken(int TokenIndex) {
		if(TokenIndex >= 0 && this.AST[TokenIndex] != null) {
			return this.AST[TokenIndex].SourceToken;
		}
		return this.SourceToken;
	}

	public final boolean IsTopLevel() {
		@Var @Nullable ZNode Cur = this.ParentNode;
		while(Cur != null) {
			if(Cur instanceof ZFunctionNode) {
				return false;
			}
			Cur = Cur.ParentNode;
		}
		return true;
	}

	@Nullable public final ZFunctionNode GetDefiningFunctionNode() {
		@Var @Nullable ZNode Cur = this;
		while(Cur != null) {
			if(Cur instanceof ZFunctionNode) {
				return (ZFunctionNode)Cur;
			}
			Cur = Cur.ParentNode;
		}
		return null;
	}

	@Nullable public final ZBlockNode GetScopeBlockNode() {
		@Var int SafeCount = 0;
		@Var ZNode Node = this;
		while(Node != null) {
			if(Node instanceof ZBlockNode) {
				return (ZBlockNode)Node;
			}
			assert(!(Node == Node.ParentNode));
			//System.out.println("node: " + Node.getClass() + ", " + Node.hashCode() + ", " + SafeCount);
			Node = Node.ParentNode;
			SafeCount = SafeCount + 1;
			assert(SafeCount < 100);
		}
		return null;
	}

	public final ZNameSpace GetNameSpace() {
		@Var int SafeCount = 0;
		@Var ZBlockNode BlockNode = this.GetScopeBlockNode();
		while(BlockNode.NullableNameSpace == null) {
			@Var ZBlockNode ParentBlockNode = BlockNode.ParentNode.GetScopeBlockNode();
			assert(!(BlockNode == ParentBlockNode));
			BlockNode = ParentBlockNode;
			SafeCount = SafeCount + 1;
			assert(SafeCount < 100);
		}
		return BlockNode.NullableNameSpace;
	}

	public final boolean IsErrorNode() {
		return (this instanceof ZErrorNode);
	}

	public abstract void Accept(ZVisitor Visitor);

	public final boolean IsUntyped() {
		return !(this.Type instanceof ZFuncType) && this.Type.IsVarType();
	}

	public final boolean HasUntypedNode() {
		if(this.HasUntyped) {
			if(!this.IsUntyped()) {
				@Var int i = 0;
				while(i < this.GetAstSize()) {
					if(this.AST[i] != null && this.AST[i].HasUntypedNode()) {
						//LibZen._PrintLine("@Untyped " + LibZen._GetClassName(this) + "[" + i + "] " + this.AST[i]);
						return true;
					}
					i = i + 1;
				}
				this.HasUntyped = false;
			}
		}
		return this.HasUntyped;
	}

	// convinient interface
	public final ZGetNameNode SetNewGetNameNode(int Index, ZTypeChecker Typer, String Name, ZType Type) {
		@Var ZGetNameNode Node = Typer.CreateGetNameNode(null, Name, Type);
		this.SetNode(Index, Node);
		return Node;
	}

	public final ZBlockNode SetNewBlockNode(int Index, ZTypeChecker Typer) {
		@Var ZBlockNode Node = Typer.CreateBlockNode(null);
		this.SetNode(Index, Node);
		return Node;
	}

	public final ZWhileNode SetNewWhileNode(int Index, ZTypeChecker Typer) {
		@Var ZWhileNode Node = new ZWhileNode(null);
		this.SetNode(Index, Node);
		return Node;

	}

}

