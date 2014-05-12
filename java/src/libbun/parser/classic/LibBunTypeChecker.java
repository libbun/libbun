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
import libbun.ast.BNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.TopLevelNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.literal.BunAsmNode;
import libbun.encode.LibBunGenerator;
import libbun.parser.common.BunLogger;
import libbun.parser.common.BunToken;
import libbun.parser.common.TypeChecker;
import libbun.type.BFormFunc;
import libbun.type.BFunc;
import libbun.type.BFuncType;
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
	public final void TypeNode(BNode Node, BType Type) {
		this.VarScope.TypeNode(Node, Type);
	}

	public final void ReturnBinaryTypeNode(BinaryOperatorNode Node, BType Type) {
		if(!Node.GetAstType(BinaryOperatorNode._Left).IsVarType() && !Node.GetAstType(BinaryOperatorNode._Right).IsVarType()) {
			@Var String Op = Node.GetOperator();
			@Var BFunc Func = this.Generator.GetDefinedFunc(Op, Node.GetAstType(BinaryOperatorNode._Left), 2);
			if(Func instanceof BFormFunc) {
				@Var ApplyMacroNode NewNode = new ApplyMacroNode(Node.ParentNode, Node.SourceToken, (BFormFunc)Func);
				NewNode.Append(Node.LeftNode());
				NewNode.Append(Node.RightNode());
				this.ReturnTypeNode(NewNode, Type);
				return;
			}
		}
		this.ReturnTypeNode(Node, Type);
	}

	@Override
	public final BNode EnforceNodeType(BNode Node, BType EnforcedType) {
		@Var BFunc Func = this.Generator.LookupConverterFunc(Node.Type, EnforcedType);
		if(Func == null && EnforcedType.IsStringType()) {
			Func = this.Generator.LookupFunc("toString", Node.Type, 1);
		}
		if(Func != null) {
			@Var AbstractListNode FuncNode = this.CreateDefinedFuncCallNode(Node.ParentNode, null, Func);
			FuncNode.Append(Node);
			return this.TypeListNodeAsFuncCall(FuncNode, Func.GetFuncType());
		}
		return this.CreateStupidCastNode(EnforcedType, Node);
	}

	@Override
	public final void InferType(BType contextType, BNode bnode) {
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

	@Override public final void VisitAsmNode(BunAsmNode Node) {
		this.ReturnTypeNode(Node, Node.FormType());
	}

	@Override public final void VisitTopLevelNode(TopLevelNode Node) {
		Node.Perform(Node.GetGamma());
		this.ReturnTypeNode(Node, BType.VoidType);
	}

	@Override public final void VisitErrorNode(ErrorNode Node) {
		@Var BType ContextType = this.GetContextType();
		if(!ContextType.IsVarType()) {
			this.ReturnTypeNode(Node, ContextType);
		}
		else {
			this.ReturnNode(Node);
		}
	}

	@Override public void VisitSyntaxSugarNode(SyntaxSugarNode Node) {
		@Var BType ContextType = this.GetContextType();
		Node.PerformTyping(this, ContextType);
		this.VisitDesugarNode(Node.PerformDesugar(this));
	}

	@Override public void VisitDesugarNode(DesugarNode Node) {
		@Var int i = 0;
		while(i < Node.GetAstSize() - 1 ) {
			this.CheckTypeAt(Node, i, BType.VoidType);
			i = i + 1;
		}
		this.CheckTypeAt(Node, i, BType.VarType);  // i == Node.GetAstSize() - 1
		this.ReturnTypeNode(Node, Node.GetAstType(i));
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

	@Override
	public FuncCallNode CreateFuncCallNode(BNode ParentNode, BunToken sourceToken, String FuncName, BFuncType FuncType) {
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

}

