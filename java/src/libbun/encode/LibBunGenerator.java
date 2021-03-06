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

package libbun.encode;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.TopLevelNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.common.CommonMap;
import libbun.parser.classic.BToken;
import libbun.parser.classic.BTokenContext;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunGamma;
import libbun.parser.classic.LibBunLangInfo;
import libbun.parser.classic.LibBunParser;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.common.BunLogger;
import libbun.parser.common.BunToken;
import libbun.type.BClassType;
import libbun.type.BFormFunc;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BPrototype;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.BIgnored;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;
import libbun.util.ZenMethod;

public abstract class LibBunGenerator extends BunVisitor {
	@BField public CommonMap<String>        ImportedLibraryMap = new CommonMap<String>(null);
	@BField public CommonMap<String>        SymbolMap = new CommonMap<String>(null);
	@BField private final CommonMap<BFunc>  DefinedFuncMap = new CommonMap<BFunc>(null);

	@BField public final LibBunGamma      RootGamma;
	@BField public LibBunParser           RootParser;
	@BField public BunLogger           Logger;
	@BField public LibBunTypeChecker      TypeChecker;
	@BField public LibBunLangInfo         LangInfo;
	@BField protected String              TopLevelSymbol = null;
	@BField private int                   UniqueNumber = 0;

	protected LibBunGenerator(LibBunLangInfo LangInfo) {
		this.RootGamma     = new LibBunGamma(this, null);
		this.RootParser    = new LibBunParser(null);
		this.Logger        = new BunLogger();
		this.LangInfo      = LangInfo;
		this.TypeChecker   = null;
	}

	public final void SetTypeChecker(LibBunTypeChecker TypeChecker) {
		this.TypeChecker = TypeChecker;
	}

	// symbol map

	protected void SetReservedName(String Keyword, @Nullable String AnotherName) {
		if(AnotherName == null) {
			AnotherName = "_" + Keyword;
		}
		this.SymbolMap.put(Keyword, AnotherName);
	}

	public final String GetNonKeyword(String Text) {
		return this.SymbolMap.GetValue(Text, Text);
	}

	public final int GetUniqueNumber() {
		@Var int UniqueNumber = this.UniqueNumber;
		this.UniqueNumber = this.UniqueNumber + 1;
		return UniqueNumber;
	}

	public String NameUniqueSymbol(String Symbol, int NameIndex) {
		return Symbol + "__B" + NameIndex;
	}

	public final String NameUniqueSymbol(String Symbol) {
		return this.NameUniqueSymbol(Symbol, this.GetUniqueNumber());
	}

	protected void SetNativeType(BType Type, String TypeName) {
		@Var String Key = "" + Type.TypeId;
		this.SymbolMap.put(Key, TypeName);
	}

	protected String GetNativeTypeName(BType Type) {
		@Var String Key = "" + Type.TypeId;
		@Var String TypeName = this.SymbolMap.GetOrNull(Key);
		if (TypeName == null) {
			return Type.GetName();
		}
		return TypeName;
	}

	public String NameGlobalNameClass(String Name) {
		return "G__" + Name;
	}

	public final String NameClass(BType ClassType) {
		return ClassType.GetName() /*+ "" + ClassType.TypeId*/;
	}

	public final String NameFunctionClass(String FuncName, BFuncType FuncType) {
		return "F__" + FuncType.StringfySignature(FuncName);
	}

	public final String NameFunctionClass(String FuncName, BType RecvType, int FuncParamSize) {
		return "F__" + BFunc._StringfySignature(FuncName, FuncParamSize, RecvType);
	}

	public final String NameType(BType Type) {
		if(Type.IsArrayType()) {
			return "ArrayOf" + this.NameType(Type.GetParamType(0)) + "_";
		}
		if(Type.IsMapType()) {
			return "MapOf" + this.NameType(Type.GetParamType(0)) + "_";
		}
		if(Type instanceof BFuncType) {
			@Var String s = "FuncOf";
			@Var int i = 0;
			while(i < Type.GetParamSize()) {
				s = s +  this.NameType(Type.GetParamType(i));
				i = i + 1;
			}
			return s + "_";
		}
		if(Type instanceof BClassType) {
			return this.NameClass(Type);
		}
		return Type.GetName();
	}

	protected final String NameMethod(BType ClassType, String MethodName) {
		return "_" + this.NameClass(ClassType) + "_" + MethodName;
	}

	protected final boolean IsMethod(String FuncName, BFuncType FuncType) {
		@Var BType RecvType = FuncType.GetRecvType();
		if(RecvType instanceof BClassType && FuncName != null) {
			@Var BClassType ClassType = (BClassType)RecvType;
			@Var BType FieldType = ClassType.GetFieldType(FuncName, null);
			if(FieldType == null || !FieldType.IsFuncType()) {
				FuncName = LibBunSystem._AnotherName(FuncName);
				FieldType = ClassType.GetFieldType(FuncName, null);
				if(FieldType == null || !FieldType.IsFuncType()) {
					return false;
				}
			}
			if(FieldType instanceof BFuncType) {
				if(((BFuncType)FieldType).AcceptAsFieldFunc(FuncType)) {
					return true;
				}
			}
		}
		return false;
	}

	// function map

	public final void SetDefinedFunc(BFunc Func) {
		this.DefinedFuncMap.put(Func.GetSignature(), Func);
	}

	private String NameConverterFunc(BType FromType, BType ToType) {
		return FromType.GetUniqueName() + "T" + ToType.GetUniqueName();
	}

	public final void SetConverterFunc(BType FromType, BType ToType, BFunc Func) {
		this.DefinedFuncMap.put(this.NameConverterFunc(FromType, ToType), Func);
	}

	public final BFunc LookupConverterFunc(BType FromType, BType ToType) {
		while(FromType != null) {
			@Var BFunc Func = this.DefinedFuncMap.GetOrNull(this.NameConverterFunc(FromType, ToType));
			//System.out.println("get " + this.NameConverterFunc(FromType, ToType) + ", func="+ Func);
			if(Func != null) {
				return Func;
			}
			FromType = FromType.GetSuperType();
		}
		return null;
	}

	//

	public final BPrototype SetPrototype(AstNode Node, String FuncName, BFuncType FuncType) {
		@Var BFunc Func = this.GetDefinedFunc(FuncName, FuncType);
		if(Func != null) {
			if(!FuncType.Equals(Func.GetFuncType())) {
				BunLogger._LogError(Node.SourceToken, "function has been defined diffrently: " + Func.GetFuncType());
				return null;
			}
			if(Func instanceof BPrototype) {
				return (BPrototype)Func;
			}
			BunLogger._LogError(Node.SourceToken, "function has been defined as macro" + Func);
			return null;
		}
		@Var BPrototype	Proto= new BPrototype(0, FuncName, FuncType, Node.SourceToken);
		this.DefinedFuncMap.put(Proto.GetSignature(), Proto);
		return Proto;
	}

	public final BFunc GetDefinedFunc(String GlobalName) {
		@Var BFunc Func = this.DefinedFuncMap.GetOrNull(GlobalName);
		if(Func == null && LibBunSystem._IsLetter(LibBunSystem._GetChar(GlobalName, 0))) {
			//			System.out.println("AnotherName = " + GlobalName + ", " + LibZen._AnotherName(GlobalName));
			Func = this.DefinedFuncMap.GetOrNull(LibBunSystem._AnotherName(GlobalName));
		}
		//System.out.println("sinature="+GlobalName+", func="+Func);
		return Func;
	}

	public final BFunc GetDefinedFunc(String FuncName, BFuncType FuncType) {
		return this.GetDefinedFunc(FuncType.StringfySignature(FuncName));
	}

	public final BFunc GetDefinedFunc(String FuncName, BType RecvType, int FuncParamSize) {
		return this.GetDefinedFunc(BFunc._StringfySignature(FuncName, FuncParamSize, RecvType));
	}

	public final BFunc LookupFunc(String FuncName, BType RecvType, int FuncParamSize) {
		@Var BFunc Func = this.GetDefinedFunc(BFunc._StringfySignature(FuncName, FuncParamSize, RecvType));
		while(Func == null) {
			RecvType = RecvType.GetSuperType();
			if(RecvType == null) {
				break;
			}
			Func = this.GetDefinedFunc(BFunc._StringfySignature(FuncName, FuncParamSize, RecvType));
			//			if(RecvType.IsVarType()) {
			//				break;
			//			}
		}
		return Func;
	}

	public final BFormFunc GetFormFunc(String FuncName, BType RecvType, int FuncParamSize) {
		@Var BFunc Func = this.GetDefinedFunc(BFunc._StringfySignature(FuncName, FuncParamSize, RecvType));
		if(Func instanceof BFormFunc) {
			return ((BFormFunc)Func);
		}
		return null;
	}

	@ZenMethod public void WriteTo(@Nullable String FileName) {
		// TODO Stub
	}

	@ZenMethod public String GetSourceText() {
		return null;
	}

	@ZenMethod public BType GetFieldType(BType BaseType, String Name) {
		return BType.VarType;     // undefined
	}

	@ZenMethod public BType GetSetterType(BType BaseType, String Name) {
		return BType.VarType;     // undefined
	}

	@ZenMethod public BFuncType GetConstructorFuncType(BType ClassType, AstNode List, int startIndex) {
		//return null;              // undefined and undefined error
		return BFuncType._FuncType;    // undefined and no error
	}

	@ZenMethod public BFuncType GetMethodFuncType(BType RecvType, String MethodName, AstNode List, int startIndex) {
		//return null;              // undefined and undefined error
		return BFuncType._FuncType;     // undefined and no error
	}

	// Naming


	public final void VisitUndefinedNode(AstNode Node) {
		@Var LegacyErrorNode ErrorNode = new LegacyErrorNode(Node.ParentNode, Node.SourceToken, "undefined node:" + Node.toString());
		this.VisitErrorNode(ErrorNode);
	}

	@Override public final void VisitDefaultValueNode(DefaultValueNode Node) {
		this.VisitNullNode(new BunNullNode(Node.ParentNode));
	}

	protected boolean LocallyGenerated(AstNode Node) {
		return false;
	}

	@Override public final void VisitDesugarNode(DesugarNode Node) {
		if(!this.LocallyGenerated(Node.OriginalNode)) {
			this.GenerateExpression(Node.AST[0]);
			if(Node.size() > 1) {
				@Var int i = 1;
				while(i < Node.size()) {
					this.GenerateStatement(Node.AST[i]);
					i = i + 1;
				}
			}
		}
	}

	@Override public void VisitSyntaxSugarNode(SyntaxSugarNode Node) {
		this.VisitDesugarNode(Node.PerformDesugar(this.TypeChecker));
	}

	@ZenMethod protected void GenerateExpression(AstNode Node) {
		Node.Accept(this);
	}

	@ZenMethod protected void GenerateStatement(AstNode Node) {
		Node.Accept(this);
	}

	private void PreProcess(AstNode Node) {
		if(this.TypeChecker != null) {
			Node = this.TypeChecker.checkType(Node, BType.VoidType);
		}
	}

	private boolean GenerateTopLevelStatement(AstNode Node) {
		this.TopLevelSymbol = null;
		if(Node instanceof BunFunctionNode || Node instanceof BunClassNode || Node instanceof BunLetVarNode) {
			//Node.Type = BType.VoidType;
			this.GenerateStatement(Node);
			return true;
		}
		if(Node instanceof LegacyErrorNode) {
			this.GenerateStatement(Node);
			return false;
		}
		//System.out.println("Node: " + Node);
		if(!this.LangInfo.AllowTopLevelScript) {
			@Var AstNode FuncNode = null;
			if(Node.Type.IsVoidType()) {
				FuncNode = Node.ParseExpression("function () { Bun::X; }");
			}
			else {
				FuncNode = Node.ParseExpression("function () { return Bun::X; }");
			}
			FuncNode.ReplaceNode("Bun::X", Node);
			Node = FuncNode;
			if(FuncNode instanceof BunFunctionNode) {
				this.TopLevelSymbol = ((BunFunctionNode)FuncNode).GetUniqueName(this);
			}
		}
		this.GenerateStatement(Node);
		return true;
	}

	public final boolean LoadScript(String ScriptText, String FileName, int LineNumber) {
		@Var boolean AllPassed = true;
		@Var LegacyBlockNode TopblockNode = new LegacyBlockNode(null, this.RootGamma);
		@Var BTokenContext TokenContext = new BTokenContext(this.RootParser, this, FileName, LineNumber, ScriptText);
		TokenContext.SkipEmptyStatement();
		TokenContext.SetParseFlag(BTokenContext._AllowSkipIndent);
		while(TokenContext.HasNext()) {
			@Var AstNode StmtNode = TokenContext.ParsePattern(TopblockNode, "$Statement$", BTokenContext._Required);
			if(StmtNode.IsErrorNode()) {
				@Var boolean SkipLine = false;
				while(TokenContext.HasNext()) {
					@Var BToken SkipToken = TokenContext.GetToken(BTokenContext._MoveNext);
					if(SkipToken.IsIndent()) {
						SkipLine = true;
					}
					if(SkipLine && SkipToken.GetIndentSize() == 0) {
						TokenContext.MatchToken("}");  // eat noizy token
						break;
					}
				}
			}
			this.PreProcess(StmtNode);
			if(!(StmtNode instanceof TopLevelNode)) {
				TopblockNode.appendNode(StmtNode);
			}
			TokenContext.SkipEmptyStatement();
			TokenContext.Vacume();
		}
		this.Logger.OutputErrorsToStdErr();
		@Var int i = 0;
		while(i < TopblockNode.GetListSize()) {
			@Var AstNode StmtNode = TopblockNode.GetListAt(i);
			if(!this.GenerateTopLevelStatement(StmtNode)) {
				AllPassed = false;
			}
			i = i + 1;
		}
		this.Logger.OutputErrorsToStdErr();
		return AllPassed;
	}

	public final boolean LoadFile(String FileName, @Nullable BToken SourceToken) {
		@Var String ScriptText = LibBunSystem._LoadTextFile(FileName);
		if(ScriptText == null) {
			BunLogger._LogErrorExit(SourceToken, "file not found: " + FileName);
			return false;
		}
		if(!this.LoadScript(ScriptText, FileName, 1)) {
			LibBunSystem._Exit(1, "found top level error: " + FileName);
			return false;
		}
		return true;
	}

	public final boolean RequireLibrary(String LibName, @Nullable BunToken sourceToken) {
		@Var String Key = "_Z" + LibName.toLowerCase();
		@Var String Value = this.ImportedLibraryMap.GetOrNull(Key);
		if(Value == null) {
			@Var String Path = this.LangInfo.GetLibPath(LibName);
			@Var String Script = LibBunSystem._LoadTextFile(Path);
			if(Script == null) {
				BunLogger._LogErrorExit(sourceToken, "library not found: " + LibName + " as " + Path);
				return false;
			}
			@Var boolean Result = this.LoadScript(Script, Path, 1);
			this.ImportedLibraryMap.put(Key, Path);
			return Result;
		}
		return true;
	}

	@BIgnored public void Perform() {
		if(this.TopLevelSymbol != null) {
			System.out.println("TODO: " + this.TopLevelSymbol);
		}
	}

	@ZenMethod public void ExecMain() {
		this.Logger.OutputErrorsToStdErr();
	}

	//	public final String Translate(String ScriptText, String FileName, int LineNumber) {
	//		@Var ZblockNode TopblockNode = new ZblockNode(this.Generator.RootGamma);
	//		@Var ZTokenContext TokenContext = new ZTokenContext(this.Generator, this.Generator.RootGamma, FileName, LineNumber, ScriptText);
	//		TokenContext.SkipEmptyStatement();
	//		@Var ZToken SkipToken = TokenContext.GetToken();
	//		while(TokenContext.HasNext()) {
	//			TokenContext.SetParseFlag(ZTokenContext._NotAllowSkipIndent);
	//			TopblockNode.ClearListAfter(0);
	//			SkipToken = TokenContext.GetToken();
	//			@Var ZNode ParsedNode = TokenContext.ParsePattern(TopblockNode, "$Statement$", ZTokenContext._Required);
	//			if(ParsedNode.IsErrorNode()) {
	//				TokenContext.SkipError(SkipToken);
	//			}
	//			this.Exec2(ParsedNode, false);
	//			TokenContext.SkipEmptyStatement();
	//			TokenContext.Vacume();
	//		}
	//		return this.Generator.GetSourceText();
	//	}



}
