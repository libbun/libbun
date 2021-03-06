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


package libbun.parser.classic;
import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.encode.LibBunGenerator;
import libbun.parser.common.BunToken;
import libbun.parser.common.SymbolTable;
import libbun.type.BClassType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.BMatchFunction;
import libbun.util.BTokenFunction;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;

public final class LibBunGamma extends SymbolTable {
	@BField public final LibBunGenerator   Generator;

	public LibBunGamma(LibBunGenerator Generator, LegacyBlockNode blockNode) {
		super(null);
		this.blockNode = blockNode;   // rootname is null
		this.Generator = Generator;
		assert(this.Generator != null);
	}

	public final LibBunGamma GetRootGamma() {
		return this.Generator.RootGamma;
	}

	public final void SetDebugSymbol(String Symbol, BunLetVarNode EntryNode) {
		this.SetSymbol(Symbol, EntryNode);
		LibBunSystem._PrintLine("SetSymbol: " + Symbol + " @" + this);
	}

	public final BunLetVarNode GetDebugSymbol(String Symbol) {
		@Var BunLetVarNode Node = (BunLetVarNode)this.GetSymbol(Symbol);
		LibBunSystem._PrintLine("GetSymbol: " + Symbol + " => " + Node);
		return Node;
	}

	public final int GetNameIndex(String Name) {
		@Var int NameIndex = -1;
		@Var SymbolTable table = this;
		while(table != null) {
			if(table.symbolTable != null) {
				@Var AstNode EntryNode = table.symbolTable.GetOrNull(Name);
				if(EntryNode != null) {
					NameIndex = NameIndex + 1;
				}
			}
			table = table.getParentTable();
		}
		return NameIndex;
	}

	public final void SetRootSymbol(String Symbol, BunLetVarNode EntryNode) {
		this.GetRootGamma().SetSymbol(Symbol, EntryNode);
	}

	public final BunLetVarNode GetLocalVariable(String Name) {
		@Var AstNode EntryNode = this.GetSymbol(Name);
		//System.out.println("var " + VarName + ", entry=" + Entry + ", Gamma=" + this);
		if(EntryNode instanceof BunLetVarNode) {
			return (BunLetVarNode)EntryNode;
		}
		return null;
	}

	// Type
	public final void SetTypeName(String Name, BType Type, @Nullable BunToken sourceToken) {
		//@Var ZTypeNode Node = new ZTypeNode(null, SourceToken, Type);
		@Var BunLetVarNode Node = new BunLetVarNode(null, BunLetVarNode._IsReadOnly, Type, Name);
		Node.SourceToken = sourceToken;
		this.SetSymbol(Name, Node);
	}

	public final void SetTypeName(BType Type, @Nullable BToken SourceToken) {
		this.SetTypeName(Type.ShortName, Type, SourceToken);
	}

	public final BType GetType(String TypeName, BunToken sourceToken, boolean IsCreation) {
		@Var BunLetVarNode Node = (BunLetVarNode)this.GetSymbol(TypeName);
		if(Node != null) {
			return Node.DeclType();
		}
		if(IsCreation && LibBunSystem._IsSymbol(LibBunSystem._GetChar(TypeName, 0))) {
			@Var BType Type = new BClassType(TypeName, BType.VarType);
			this.GetRootGamma().SetTypeName(TypeName, Type, sourceToken);
			return Type;
		}
		return null;
	}

	public LibBunSyntax GetSyntaxPattern(String PatternName) {
		return this.Generator.RootParser.GetSyntaxPattern(PatternName);
	}

	public LibBunSyntax GetRightSyntaxPattern(String PatternName) {
		return this.Generator.RootParser.GetRightSyntaxPattern(PatternName);
	}

	public final void DefineToken(String Symbol, BTokenFunction Func) {
		this.Generator.RootParser.AppendTokenFunc(Symbol, Func);
	}

	public final void DefineStatement(String Symbol, BMatchFunction Func) {
		this.Generator.RootParser.DefineExpression(Symbol, Func);
	}

	public final void DefineExpression(String Symbol, BMatchFunction Func) {
		this.Generator.RootParser.DefineExpression(Symbol, Func);
	}

	public final void DefineBinaryOperator(String Symbol, BMatchFunction Func) {
		this.Generator.RootParser.DefineBinaryOperator(Symbol, Func);
	}

	public final void DefineExpressionSuffix(String Symbol, BMatchFunction Func) {
		this.Generator.RootParser.DefineExpressionSuffix(Symbol, Func);
	}




}
