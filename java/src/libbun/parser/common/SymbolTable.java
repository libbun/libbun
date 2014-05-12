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


package libbun.parser.common;
import libbun.ast.BNode;
import libbun.ast.BlockNode;
import libbun.ast.decl.DefSymbolNode;
import libbun.common.CommonMap;
import libbun.type.BFuncType;
import libbun.util.BField;
import libbun.util.Var;

public class SymbolTable {
	@BField public Namespace          namespace = null;
	@BField public BlockNode       blockNode = null;
	@BField public CommonMap<DefSymbolNode>  symbolTable = null;

	public SymbolTable(Namespace namespace) {
		this.namespace = namespace;
	}

	@Override public String toString() {
		return "NS[" + this.blockNode + "]";
	}

	public final Namespace getNamespace() {
		return this.namespace;
	}

	public final SymbolTable getParentTable() {
		if(this.blockNode != null) {
			@Var BNode Node = this.blockNode.ParentNode;
			while(Node != null) {
				if(Node instanceof BlockNode) {
					@Var BlockNode blockNode = (BlockNode)Node;
					if(blockNode.NullableGamma != null) {
						return blockNode.NullableGamma;
					}
				}
				Node = Node.ParentNode;
			}
		}
		return null;
	}

	public final void SetSymbol(String Symbol, DefSymbolNode EntryNode) {
		if(this.symbolTable == null) {
			this.symbolTable = new CommonMap<DefSymbolNode>(null);
		}
		this.symbolTable.put(Symbol, EntryNode);
	}

	public final DefSymbolNode GetSymbol(String Symbol) {
		@Var SymbolTable table = this;
		while(table != null) {
			if(table.symbolTable != null) {
				@Var DefSymbolNode EntryNode = table.symbolTable.GetValue(Symbol, null);
				if(EntryNode != null) {
					return EntryNode;
				}
			}
			table = table.getParentTable();
		}
		return null;
	}

	public DefSymbolNode getSymbol(String funcName, BFuncType funcType) {
		// TODO Auto-generated method stub
		return null;
	}

	public final void setSymbol(String Symbol, BFuncType funcType, DefSymbolNode EntryNode) {
		if(this.symbolTable == null) {
			this.symbolTable = new CommonMap<DefSymbolNode>(null);
		}
		this.symbolTable.put(Symbol, EntryNode);
	}



	//	public final void SetDebugSymbol(String Symbol, DefSymbolNode EntryNode) {
	//		this.SetSymbol(Symbol, EntryNode);
	//		LibBunSystem._PrintLine("SetSymbol: " + Symbol + " @" + this);
	//	}
	//
	//	public final DefSymbolNode GetDebugSymbol(String Symbol) {
	//		@Var DefSymbolNode Node = this.GetSymbol(Symbol);
	//		LibBunSystem._PrintLine("GetSymbol: " + Symbol + " => " + Node);
	//		return Node;
	//	}
	//
	//	public final int GetNameIndex(String Name) {
	//		@Var int NameIndex = -1;
	//		@Var SymbolTable Gamma = this;
	//		while(Gamma != null) {
	//			if(Gamma.SymbolTable != null) {
	//				@Var BNode EntryNode = Gamma.SymbolTable.GetOrNull(Name);
	//				if(EntryNode != null) {
	//					NameIndex = NameIndex + 1;
	//				}
	//			}
	//			Gamma = Gamma.GetParentGamma();
	//		}
	//		return NameIndex;
	//	}
	//
	//	public final void SetRootSymbol(String Symbol, DefSymbolNode EntryNode) {
	//		this.GetRootGamma().SetSymbol(Symbol, EntryNode);
	//	}
	//
	//	public final DefSymbolNode GetLocalVariable(String Name) {
	//		@Var BNode EntryNode = this.GetSymbol(Name);
	//		//System.out.println("var " + VarName + ", entry=" + Entry + ", Gamma=" + this);
	//		if(EntryNode instanceof DefSymbolNode) {
	//			return (DefSymbolNode)EntryNode;
	//		}
	//		return null;
	//	}
	//
	//	// Type
	//	public final void SetTypeName(String Name, BType Type, @Nullable BunToken sourceToken) {
	//		//@Var ZTypeNode Node = new ZTypeNode(null, SourceToken, Type);
	//		@Var DefSymbolNode Node = new DefSymbolNode(null, DefSymbolNode._IsReadOnly, Type, Name);
	//		Node.SourceToken = sourceToken;
	//		this.SetSymbol(Name, Node);
	//	}
	//
	//	public final void SetTypeName(BType Type, @Nullable BToken SourceToken) {
	//		this.SetTypeName(Type.ShortName, Type, SourceToken);
	//	}
	//
	//	public final BType GetType(String TypeName, BunToken sourceToken, boolean IsCreation) {
	//		@Var DefSymbolNode Node = this.GetSymbol(TypeName);
	//		if(Node != null) {
	//			return Node.DeclType();
	//		}
	//		if(IsCreation && LibBunSystem._IsSymbol(LibBunSystem._GetChar(TypeName, 0))) {
	//			@Var BType Type = new BClassType(TypeName, BType.VarType);
	//			this.GetRootGamma().SetTypeName(TypeName, Type, sourceToken);
	//			return Type;
	//		}
	//		return null;
	//	}
	//

}

