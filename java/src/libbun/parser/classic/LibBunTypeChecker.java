//***************************************************************************
//Copyright (c) 2013-2014, Libbun project authors. All rights reserved.
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are met:
//
//*  Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//*  Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
//TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
//PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
//CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
//EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
//PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
//OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
//OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
//ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//**************************************************************************

package libbun.parser.classic;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.TopLevelNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.literal.CodeNode;
import libbun.encode.LibBunGenerator;
import libbun.parser.common.BunLogger;
import libbun.parser.common.BunToken;
import libbun.parser.common.TypeChecker;
import libbun.type.BFormFunc;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BGreekType;
import libbun.type.BType;
import libbun.type.BVarScope;
import libbun.util.BField;
import libbun.util.Var;

public abstract class LibBunTypeChecker extends TypeChecker {
	public final static int _DefaultTypeCheckPolicy			= 0;
	public final static int _NoCheckPolicy                  = 1;

	@BField public LibBunGenerator  Generator;
	@BField public BunLogger     Logger;
	@BField public BVarScope   VarScope;
	@BField public boolean     IsSupportNullable = false;
	@BField public boolean     IsSupportMutable  = false;

	public LibBunTypeChecker(LibBunGenerator Generator) {
		this.Generator = Generator;
		this.Logger = Generator.Logger;
		this.VarScope = new BVarScope(null, this.Logger, null);
	}

	@Override
	public final void typeNode(AstNode Node, BType Type) {
		this.VarScope.TypeNode(Node, Type);
	}

	public final void ReturnBinaryTypeNode(BinaryOperatorNode Node, BType Type) {
		if(!Node.getTypeAt(BinaryOperatorNode._Left).IsVarType() && !Node.getTypeAt(BinaryOperatorNode._Right).IsVarType()) {
			@Var String Op = Node.GetOperator();
			@Var BFunc Func = this.Generator.GetDefinedFunc(Op, Node.getTypeAt(BinaryOperatorNode._Left), 2);
			if(Func instanceof BFormFunc) {
				@Var ApplyMacroNode NewNode = new ApplyMacroNode(Node.ParentNode, Node.SourceToken, (BFormFunc)Func);
				NewNode.appendNode(Node.LeftNode());
				NewNode.appendNode(Node.RightNode());
				this.returnTypeNode(NewNode, Type);
				return;
			}
		}
		this.returnTypeNode(Node, Type);
	}

	@Override
	public final AstNode EnforceNodeType(AstNode Node, BType EnforcedType) {
		@Var BFunc Func = this.Generator.LookupConverterFunc(Node.Type, EnforcedType);
		if(Func == null && EnforcedType.IsStringType()) {
			Func = this.Generator.LookupFunc("toString", Node.Type, 1);
		}
		if(Func != null) {
			@Var AstNode FuncNode = this.CreateDefinedFuncCallNode(Node.ParentNode, null, Func);
			FuncNode.appendNode(Node);
			return this.TypeListNodeAsFuncCall(FuncNode, Func.GetFuncType());
		}
		return this.CreateStupidCastNode(EnforcedType, Node);
	}

	@Override
	public final void inferType(BType contextType, AstNode bnode) {
		this.VarScope.InferType(contextType, bnode);
	}

	//	private final BNode TypeCheckImpl(BNode Node, BType ContextType, int TypeCheckPolicy) {
	//		if(Node.IsErrorNode()) {
	//			if(!ContextType.IsVarType()) {
	//				this.VarScope.TypeNode(Node, ContextType);
	//			}
	//			return Node;
	//		}
	//		if(Node.IsUntyped() || ContextType.IsVarType() || LibBunSystem._IsFlag(TypeCheckPolicy, LibBunTypeChecker._NoCheckPolicy)) {
	//			return Node;
	//		}
	//		if(Node.Type == ContextType || ContextType.Accept(Node.Type)) {
	//			return Node;
	//		}
	//		if(ContextType.IsVoidType() && !Node.Type.IsVoidType()) {
	//			return new BunCastNode(Node.ParentNode, BType.VoidType, Node);
	//		}
	//		if(ContextType.IsFloatType() && Node.Type.IsIntType()) {
	//			return this.EnforceNodeType(Node, ContextType);
	//		}
	//		if(ContextType.IsIntType() && Node.Type.IsFloatType()) {
	//			return this.EnforceNodeType(Node, ContextType);
	//		}
	//		return this.CreateStupidCastNode(ContextType, Node);
	//	}
	//
	//	private BNode VisitNode(BNode Node, BType ContextType) {
	//		@Var BNode ParentNode = Node.ParentNode;
	//		this.StackedContextType = ContextType;
	//		this.ReturnedNode = null;
	//		Node.Accept(this);
	//		if(this.ReturnedNode == null) {  /* debug check */
	//			LibBunSystem._PrintDebug("!! returns no value: " + Node);
	//		}
	//		else {
	//			Node = this.ReturnedNode;
	//		}
	//		if(ParentNode != Node.ParentNode && ParentNode != null) {
	//			if(Node.ParentNode != null) {
	//				LibBunSystem._PrintDebug("Preserving parent of typed new node: " + Node);
	//			}
	//			ParentNode.SetChild(Node, BNode._PreservedParent);
	//		}
	//		return Node;
	//	}
	//
	//	private final BNode TypeCheck(BNode Node, BType ContextType, int TypeCheckPolicy) {
	//		if(this.IsVisitable() && Node != null) {
	//			if(Node.HasUntypedNode()) {
	//				Node = this.VisitNode(Node, ContextType);
	//				this.VarScope.InferType(ContextType, Node);
	//			}
	//			Node = this.TypeCheckImpl(Node, ContextType, TypeCheckPolicy);
	//			this.VarScope.InferType(ContextType, Node);
	//		}
	//		this.ReturnedNode = null;
	//		return Node;
	//	}

	//	@Override
	//	public final BNode TryType(BNode Node, BType ContextType) {
	//		return this.TypeCheck(Node, ContextType, LibBunTypeChecker._NoCheckPolicy);
	//	}
	//
	//	@Override
	//	public final void TryTypeAt(BNode Node, int Index, BType ContextType) {
	//		//		@Var ZNode N = Node.AST[Index];
	//		Node.SetNode(Index, this.TypeCheck(Node.AST[Index], ContextType, LibBunTypeChecker._NoCheckPolicy));
	//		//		if(N != Node.AST[Index]) {
	//		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
	//		//		}
	//	}
	//
	//	@Override
	//	public final BNode CheckType(BNode Node, BType ContextType) {
	//		return this.TypeCheck(Node, ContextType, LibBunTypeChecker._DefaultTypeCheckPolicy);
	//	}
	//
	//	@Override
	//	public final void CheckTypeAt(BNode Node, int Index, BType ContextType) {
	//		//		@Var ZNode N = Node.AST[Index];
	//		Node.SetNode(Index, this.TypeCheck(Node.AST[Index], ContextType, LibBunTypeChecker._DefaultTypeCheckPolicy));
	//		//		if(N != Node.AST[Index]) {
	//		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
	//		//		}
	//	}

	//	@Override
	//	public final void TypeCheckNodeList(AbstractListNode List) {
	//		@Var int i = 0;
	//		while(i < List.GetListSize()) {
	//			@Var BNode SubNode = List.GetListAt(i);
	//			SubNode = this.CheckType(SubNode, BType.VarType);
	//			List.SetListAt(i, SubNode);
	//			i = i + 1;
	//		}
	//	}

	//	@Override
	//	public final BNode TypeListNodeAsFuncCall(AbstractListNode FuncNode, BFuncType FuncType) {
	//		@Var int i = 0;
	//		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
	//		//		if(FuncNode.GetListSize() != FuncType.GetFuncParamSize()) {
	//		//			System.err.println(ZLogger._LogError(FuncNode.SourceToken, "mismatch " + FuncType + ", " + FuncNode.GetListSize()+": " + FuncNode));
	//		//		}
	//		while(i < FuncNode.GetListSize()) {
	//			@Var BNode SubNode = FuncNode.GetListAt(i);
	//			@Var BType ParamType =  FuncType.GetFuncParamType(i);
	//			SubNode = this.TryType(SubNode, ParamType);
	//			if(!SubNode.IsUntyped() || !ParamType.IsVarType()) {
	//				if(!ParamType.AcceptValueType(SubNode.Type, false, Greek)) {
	//					SubNode = this.CreateStupidCastNode(ParamType.GetGreekRealType(Greek), SubNode);
	//				}
	//			}
	//			FuncNode.SetListAt(i, SubNode);
	//			i = i + 1;
	//		}
	//		this.VarScope.TypeNode(FuncNode, FuncType.GetReturnType().GetGreekRealType(Greek));
	//		return FuncNode;
	//	}

	public abstract void DefineFunction(BunFunctionNode FunctionNode, boolean Enforced);

	@Override public final void VisitAsmNode(CodeNode Node) {
		this.returnTypeNode(Node, Node.FormType());
	}

	@Override public final void VisitTopLevelNode(TopLevelNode Node) {
		Node.Perform(Node.GetGamma());
		this.returnTypeNode(Node, BType.VoidType);
	}

	@Override public final void VisitErrorNode(LegacyErrorNode Node) {
		@Var BType ContextType = this.getContextType();
		if(!ContextType.IsVarType()) {
			this.returnTypeNode(Node, ContextType);
		}
		else {
			this.returnNode(Node);
		}
	}

	@Override public void VisitSyntaxSugarNode(SyntaxSugarNode Node) {
		@Var BType ContextType = this.getContextType();
		Node.PerformTyping(this, ContextType);
		this.VisitDesugarNode(Node.PerformDesugar(this));
	}

	@Override public void VisitDesugarNode(DesugarNode Node) {
		@Var int i = 0;
		while(i < Node.size() - 1 ) {
			this.CheckTypeAt(Node, i, BType.VoidType);
			i = i + 1;
		}
		this.CheckTypeAt(Node, i, BType.VarType);  // i == Node.GetAstSize() - 1
		this.returnTypeNode(Node, Node.getTypeAt(i));
	}


	// ----------------------------------------------------------------------
	/* Note : the CreateNode serise are designed to treat typed node */

	//	public BunFunctionNode CreateFunctionNode(BNode ParentNode, String FuncName, BNode Node) {
	//		@Var BunFunctionNode FuncNode = new BunFunctionNode(ParentNode);
	//		FuncNode.GivenName = FuncName;
	//		FuncNode.GivenType = Node.Type;
	//		@Var BlockNode blockNode = this.CreateblockNode(FuncNode);
	//		FuncNode.SetNode(BunFunctionNode._Block, blockNode);
	//		if(Node.Type.IsVoidType()) {
	//			blockNode.Append(Node);
	//			blockNode.Append(this.CreateReturnNode(blockNode));
	//		}
	//		else {
	//			blockNode.Append(this.CreateReturnNode(blockNode, Node));
	//		}
	//		FuncNode.Type = BType.VoidType;
	//		return FuncNode;
	//	}
	//
	//	public BlockNode CreateblockNode(BNode ParentNode) {
	//		@Var BlockNode blockNode = new BlockNode(ParentNode, null);
	//		blockNode.Type = BType.VoidType;
	//		return blockNode;
	//	}
	//
	//	public BunReturnNode CreateReturnNode(BNode ParentNode) {
	//		@Var BunReturnNode ReturnNode = new BunReturnNode(ParentNode);
	//		ReturnNode.Type = BType.VoidType;
	//		return ReturnNode;
	//	}
	//
	//	public BunReturnNode CreateReturnNode(BNode ParentNode, BNode ExprNode) {
	//		@Var BunReturnNode ReturnNode = new BunReturnNode(ParentNode);
	//		ReturnNode.SetNode(BunReturnNode._Expr, ExprNode);
	//		ReturnNode.Type = BType.VoidType;
	//		return ReturnNode;
	//	}
	//
	//	public BunVarBlockNode CreateVarNode(BNode ParentNode, String Name, BType DeclType, BNode InitNode) {
	//		@Var BunLetVarNode VarNode = new BunLetVarNode(null, 0, null, null);
	//		VarNode.GivenName   = Name;
	//		VarNode.GivenType   = DeclType;
	//		VarNode.SetNode(BunLetVarNode._InitValue, InitNode);
	//		VarNode.Type = BType.VoidType;
	//		return new BunVarBlockNode(ParentNode, VarNode);
	//	}
	//
	//	public GetNameNode CreateGetNameNode(BNode ParentNode, String Name, BType Type) {
	//		@Var GetNameNode NameNode = new GetNameNode(ParentNode, null, Name);
	//		NameNode.Type = Type;
	//		return NameNode;
	//	}

	public FuncCallNode CreateFuncCallNode(AstNode ParentNode, BunToken sourceToken, String FuncName, BFuncType FuncType) {
		@Var FuncCallNode FuncNode = new FuncCallNode(ParentNode, new BunFuncNameNode(null, sourceToken, FuncName, FuncType));
		FuncNode.Type = FuncType.GetReturnType();
		return FuncNode;
	}

	//	@Override
	//	public final AbstractListNode CreateDefinedFuncCallNode(BNode ParentNode, BunToken sourceToken, BFunc Func) {
	//		@Var AbstractListNode FuncNode = null;
	//		if(Func instanceof BFormFunc) {
	//			FuncNode = new BunFormNode(ParentNode, sourceToken, (BFormFunc)Func);
	//		}
	//		else {
	//			FuncNode = this.CreateFuncCallNode(ParentNode, sourceToken, Func.FuncName, Func.GetFuncType());
	//		}
	//		//		FuncNode.Type = Func.GetFuncType().GetRealType();
	//		return FuncNode;
	//	}

	public final void TypeCheckNodeList(int startIndex, AstNode List) {
		@Var int i = startIndex;
		while(i < List.size()) {
			this.CheckTypeAt(List, i, BType.VarType);
			i = i + 1;
		}
	}

	public final void TypeCheckNodeList(AbstractListNode List) {
		@Var int i = 0;
		while(i < List.GetListSize()) {
			@Var AstNode SubNode = List.GetListAt(i);
			SubNode = this.checkType(SubNode, BType.VarType);
			List.SetListAt(i, SubNode);
			i = i + 1;
		}
	}

	public final AstNode TypeListNodeAsFuncCall(AstNode FuncNode, BFuncType FuncType) {
		int startIndex = 0;
		if(FuncNode instanceof FuncCallNode) {
			startIndex = 1;
		}
		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
		//		if(FuncNode.GetListSize() != FuncType.GetFuncParamSize()) {
		//			System.err.println(ZLogger._LogError(FuncNode.SourceToken, "mismatch " + FuncType + ", " + FuncNode.GetListSize()+": " + FuncNode));
		//		}
		@Var int i = startIndex;
		while(i < FuncNode.size()) {
			@Var AstNode SubNode = FuncNode.get(i);
			@Var BType ParamType =  FuncType.GetFuncParamType(i - startIndex);
			SubNode = this.tryType(SubNode, ParamType);
			if(!SubNode.IsUntyped() || !ParamType.IsVarType()) {
				if(!ParamType.AcceptValueType(SubNode.Type, false, Greek)) {
					SubNode = this.CreateStupidCastNode(ParamType.GetGreekRealType(Greek), SubNode);
				}
			}
			FuncNode.set(i, SubNode);
			i = i + 1;
		}
		this.typeNode(FuncNode, FuncType.GetReturnType().GetGreekRealType(Greek));
		return FuncNode;
	}


	public final AstNode CreateDefinedFuncCallNode(AstNode ParentNode, BunToken sourceToken, BFunc Func) {
		@Var AstNode FuncNode = null;
		if(Func instanceof BFormFunc) {
			FuncNode = new ApplyMacroNode(ParentNode, sourceToken, (BFormFunc)Func);
		}
		else {
			FuncNode = this.CreateFuncCallNode(ParentNode, sourceToken, Func.FuncName, Func.GetFuncType());
		}
		//	FuncNode.Type = Func.GetFuncType().GetRealType();
		return FuncNode;
	}

}

