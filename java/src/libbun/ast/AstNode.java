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

import libbun.ast.decl.BunFunctionNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.statement.BunIfNode;
import libbun.common.CommonStringBuilder;
import libbun.encode.LibBunGenerator;
import libbun.parser.classic.BTokenContext;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;
import libbun.parser.common.SymbolTable;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;

public abstract class AstNode {
	public final static int _AppendIndex = -1;
	public final static int _Nop =      -3;
	public final static boolean _EnforcedParent = true;
	public final static boolean _PreservedParent = false;

	@BField public AstNode    ParentNode;
	@BField public BunToken   SourceToken;
	@BField public AstNode    AST[] = null;
	@BField public BType      Type = BType.VarType;
	@BField public boolean    HasUntyped = true;

	public AstNode(@Nullable AstNode ParentNode, int Size) {
		assert(this != ParentNode);
		this.ParentNode = ParentNode;
		if(Size > 0) {
			this.AST = LibBunSystem._NewNodeArray(Size);
		}
	}

	protected AstNode dupField(boolean typedClone, AstNode NewNode) {
		@Var int i = 0;
		while(i < this.AST.length) {
			if(this.AST[i] != null) {
				NewNode.AST[i] = this.AST[i].dup(typedClone, NewNode);
				assert(NewNode.AST[i].getClass() == this.AST[i].getClass());
			}
			else {
				NewNode.AST[i] = null;
			}
			i = i + 1;
		}
		NewNode.SourceToken = this.SourceToken;
		if(typedClone) {
			NewNode.Type = this.Type;
			NewNode.HasUntyped = this.HasUntyped;
		}
		if(NewNode instanceof AbstractListNode) {
			((AbstractListNode)NewNode).vargStartIndex = ((AbstractListNode)this).vargStartIndex;
		}
		return NewNode;
	}

	//	public abstract BNode Dup(boolean typedClone, BNode ParentNode);
	public AstNode dup(boolean typedClone, AstNode ParentNode) {
		throw new RuntimeException("TODO: Implement Dup method for " + this.getClass());
	}

	public final void bunfyAST(CommonStringBuilder builder, String openClause, int startIdx, String closeClause) {
		builder.Append(openClause);
		for(int i = startIdx; i < this.size(); i++) {
			if(this.AST[i] != null) {
				builder.Append(" ");
				this.AST[i].bunfy(builder);
			}
		}
		builder.Append(closeClause);
	}

	//	public abstract void bunfy(CommonStringBuilder builder);
	public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "/*(", 0, ")*/");
	}

	public String bunfy() {
		CommonStringBuilder builder = new CommonStringBuilder();
		this.bunfy(builder);
		return builder.toString();
	}

	// AST[]

	public final int size() {
		if(this.AST == null) {
			return 0;
		}
		return this.AST.length;
	}

	public final AstNode get(int index) {
		return this.AST[index];
	}

	public final void set(int index, AstNode node) {
		if(index == -1) {
			this.appendNode(node);
			return;
		}
		if(!(index < this.size())){
			this.expandAstToSize(index+1);
		}
		this.AST[index] = node;
		if(node != null) {
			node.ParentNode = this;
		}
	}

	public final void swap(int i, int j) {
		AstNode node = this.AST[i];
		this.AST[i] = this.AST[j];
		this.AST[j] = node;
	}







	public final String GetSourceLocation() {
		if(this.SourceToken != null) {
			return "(" + this.SourceToken.GetFileName() + ":" + this.SourceToken.GetLineNumber() + ")";
		}
		return null;
	}

	@Override public String toString() {
		@Var String Self = "#" + LibBunSystem._GetClassName(this);
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
						Self = Self + "*" + LibBunSystem._GetClassName(this.AST[i])+"*";
					}
				}
				i = i + 1;
			}
			Self = Self + "]";
		}
		return Self;
	}


	public final AstNode SetChild(AstNode Node, boolean EnforcedParent) {
		assert(this != Node);
		if(EnforcedParent || Node.ParentNode == null) {
			Node.ParentNode = this;
		}
		return Node;
	}

	public final AstNode SetParent(AstNode Node, boolean Enforced) {
		if(Enforced || this.ParentNode == null) {
			this.ParentNode = Node;
		}
		return this;
	}

	public final AstNode SetNode(int Index, AstNode Node, boolean EnforcedParent) {
		if(Index >= 0) {
			assert(Index < this.size());
			this.AST[Index] = this.SetChild(Node, EnforcedParent);
		}
		else if(Index == AstNode._AppendIndex) {
			this.appendNode(Node);
		}
		return Node;
	}

	public final void SetNode(int Index, AstNode Node) {
		this.SetNode(Index, Node, AstNode._EnforcedParent);
	}

	public final void SetNullableNode(int Index, @Nullable AstNode Node) {
		if(Node != null) {
			this.SetNode(Index, Node, AstNode._EnforcedParent);
		}
	}

	protected final void resizeAst(int size) {
		if(this.AST == null && size > 0) {
			this.AST = LibBunSystem._NewNodeArray(size);
		}
		else if(this.AST.length != size) {
			@Var AstNode[] newast = LibBunSystem._NewNodeArray(size);
			if(size > this.AST.length) {
				LibBunSystem._ArrayCopy(this.AST, 0, newast, 0, this.AST.length);
			}
			else {
				LibBunSystem._ArrayCopy(this.AST, 0, newast, 0, size);
			}
			this.AST = newast;
		}
	}

	public final void expandAstToSize(int newSize) {
		if(newSize > this.size()) {
			this.resizeAst(newSize);
		}
	}

	public final void copyAstTo(int startIndex, AstNode destNode, int destIndex, int size) {
		for(int i = 0; i < size; i++) {
			destNode.AST[destIndex+i] = this.AST[startIndex+i];
			if(destNode.AST[destIndex+i] != null) {
				destNode.AST[destIndex+i].ParentNode = destNode;
			}
		}
	}



	private final void appendNode(AstNode Node, boolean EnforcedParent) {
		if(this.AST == null) {
			this.AST = LibBunSystem._NewNodeArray(1);
			this.SetNode(0, Node, EnforcedParent);
		}
		else {
			@Var AstNode[] newAST = LibBunSystem._NewNodeArray(this.AST.length+1);
			LibBunSystem._ArrayCopy(this.AST, 0, newAST, 0, this.AST.length);
			this.AST = newAST;
			this.SetNode(this.AST.length - 1, Node, EnforcedParent);
		}
	}

	public final void appendNode(AstNode Node) {
		if(Node instanceof ContainerNode) {
			@Var ContainerNode Container = (ContainerNode)Node;
			@Var int i = 0;
			while(i < Container.AST.length) {
				this.appendNode(Container.AST[i], AstNode._EnforcedParent);
				i = i + 1;
			}
		}
		else {
			this.appendNode(Node, AstNode._EnforcedParent);
		}
	}

	public final BType getTypeAt(int Index) {
		if(Index < this.AST.length) {
			return this.AST[Index].Type.GetRealType();
		}
		return BType.VoidType;  // to retrieve RecvType
	}

	public final void setTypeAt(int Index, BType Type) {
		if(this.AST[Index] != null) {
			this.AST[Index].Type = Type;
		}
	}

	public final BunToken getTokenAt(int TokenIndex) {
		if(TokenIndex >= 0 && this.AST[TokenIndex] != null) {
			return this.AST[TokenIndex].SourceToken;
		}
		return this.SourceToken;
	}

	// ParentNode

	public final boolean isTopLevel() {
		@Var @Nullable AstNode Cur = this.ParentNode;
		while(Cur != null) {
			if(Cur instanceof BunFunctionNode) {
				return false;
			}
			Cur = Cur.ParentNode;
		}
		return true;
	}

	@Nullable public final BunFunctionNode GetDefiningFunctionNode() {
		@Var @Nullable AstNode Cur = this;
		while(Cur != null) {
			if(Cur instanceof BunFunctionNode) {
				return (BunFunctionNode)Cur;
			}
			Cur = Cur.ParentNode;
		}
		return null;
	}

	@Nullable public final LegacyBlockNode GetScopeLegacyBlockNode() {
		@Var int SafeCount = 0;
		@Var AstNode Node = this;
		while(Node != null) {
			if(Node instanceof LegacyBlockNode) {
				return (LegacyBlockNode)Node;
			}
			assert(!(Node == Node.ParentNode));
			//System.out.println("node: " + Node.getClass() + ", " + Node.hashCode() + ", " + SafeCount);
			Node = Node.ParentNode;
			if(LibBunSystem.DebugMode) {
				SafeCount = SafeCount + 1;
				assert(SafeCount < 100);
			}
		}
		return null;
	}

	public final LibBunGamma GetGamma() {
		@Var int SafeCount = 0;
		@Var LegacyBlockNode blockNode = this.GetScopeLegacyBlockNode();
		while(blockNode.NullableGamma == null) {
			@Var LegacyBlockNode ParentblockNode = blockNode.ParentNode.GetScopeLegacyBlockNode();
			blockNode = ParentblockNode;
			if(LibBunSystem.DebugMode) {
				SafeCount = SafeCount + 1;
				assert(SafeCount < 100);
			}
		}
		return (LibBunGamma)blockNode.NullableGamma;
	}

	@Nullable public final BlockNode getScopeBlockNode() {
		@Var int SafeCount = 0;
		@Var AstNode Node = this;
		while(Node != null) {
			if(Node instanceof BlockNode) {
				return (BlockNode)Node;
			}
			assert(!(Node == Node.ParentNode));
			//System.out.println("node: " + Node.getClass() + ", " + Node.hashCode() + ", " + SafeCount);
			Node = Node.ParentNode;
			if(LibBunSystem.DebugMode) {
				SafeCount = SafeCount + 1;
				assert(SafeCount < 100);
			}
		}
		return null;
	}

	public final SymbolTable getSymbolTable() {
		@Var int SafeCount = 0;
		@Var BlockNode blockNode = this.getScopeBlockNode();
		while(blockNode.NullableGamma == null) {
			@Var BlockNode parentBlockNode = blockNode.ParentNode.getScopeBlockNode();
			blockNode = parentBlockNode;
			if(LibBunSystem.DebugMode) {
				SafeCount = SafeCount + 1;
				assert(SafeCount < 100);
			}
		}
		return blockNode.NullableGamma;
	}

	public final boolean IsErrorNode() {
		return (this instanceof ErrorNode || this instanceof LegacyErrorNode);
	}

	public abstract void Accept(LibBunVisitor Visitor);
	//public abstract void accept2(BunVisitor2 visitor);

	public final boolean IsUntyped() {
		return !(this.Type instanceof BFuncType) && this.Type.IsVarType();
	}

	public final boolean HasUntypedNode() {
		if(this.HasUntyped) {
			if(!this.IsUntyped()) {
				@Var int i = 0;
				while(i < this.size()) {
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

	public final AstNode ParseExpression(String SourceText) {
		LibBunGenerator Generator = this.GetGamma().Generator;
		BunSource Source = new BunSource("(sugar)", 1, SourceText, Generator.Logger);
		BTokenContext TokenContext = new BTokenContext(Generator.RootParser, Generator, Source, 0, SourceText.length());
		return TokenContext.ParsePattern(this, "$Expression$", BTokenContext._Required);
	}

	public final void ReplaceNode(String Name, AstNode Node) {
		if(Node instanceof LegacyBlockNode) {
			BunIfNode IfNode = new BunIfNode(null);  // {block} => if(true) {block}
			IfNode.SetNode(BunIfNode._Cond, new BunBooleanNode(null, null, true));
			IfNode.AST[BunIfNode._Then] = Node;
			Node = IfNode;
		}
		@Var int i = 0;
		while(i < this.size()) {
			AstNode SubNode = this.AST[i];
			if(SubNode instanceof GetNameNode) {
				@Var String NodeName = ((GetNameNode)SubNode).GivenName;
				if(NodeName.equals(Name)) {
					this.AST[i] = Node;
				}
			}
			else if(SubNode != null) {
				SubNode.ReplaceNode(Name, Node);
			}
			i = i + 1;
		}
	}

}

