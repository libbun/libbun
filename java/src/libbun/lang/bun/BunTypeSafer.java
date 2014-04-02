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

package libbun.lang.bun;

import libbun.parser.ZGenerator;
import libbun.parser.ZLogger;
import libbun.parser.ZMacroFunc;
import libbun.parser.ZNameSpace;
import libbun.parser.ZNodeUtils;
import libbun.parser.ZToken;
import libbun.parser.ZTypeChecker;
import libbun.parser.ast.ZAndNode;
import libbun.parser.ast.ZArrayLiteralNode;
import libbun.parser.ast.ZAsmNode;
import libbun.parser.ast.ZBinaryNode;
import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZBooleanNode;
import libbun.parser.ast.ZBreakNode;
import libbun.parser.ast.ZCastNode;
import libbun.parser.ast.ZClassNode;
import libbun.parser.ast.ZComparatorNode;
import libbun.parser.ast.ZDefaultValueNode;
import libbun.parser.ast.ZErrorNode;
import libbun.parser.ast.ZFloatNode;
import libbun.parser.ast.ZFuncCallNode;
import libbun.parser.ast.ZFuncNameNode;
import libbun.parser.ast.ZFunctionNode;
import libbun.parser.ast.ZGetIndexNode;
import libbun.parser.ast.ZGetNameNode;
import libbun.parser.ast.ZGetterNode;
import libbun.parser.ast.ZGroupNode;
import libbun.parser.ast.ZIfNode;
import libbun.parser.ast.ZInstanceOfNode;
import libbun.parser.ast.ZIntNode;
import libbun.parser.ast.ZLetVarNode;
import libbun.parser.ast.ZListNode;
import libbun.parser.ast.ZLocalDefinedNode;
import libbun.parser.ast.ZMacroNode;
import libbun.parser.ast.ZMapEntryNode;
import libbun.parser.ast.ZMapLiteralNode;
import libbun.parser.ast.ZMethodCallNode;
import libbun.parser.ast.ZNewObjectNode;
import libbun.parser.ast.ZNode;
import libbun.parser.ast.ZNotNode;
import libbun.parser.ast.ZNullNode;
import libbun.parser.ast.ZOrNode;
import libbun.parser.ast.ZReturnNode;
import libbun.parser.ast.ZSetIndexNode;
import libbun.parser.ast.ZSetNameNode;
import libbun.parser.ast.ZSetterNode;
import libbun.parser.ast.ZStringNode;
import libbun.parser.ast.ZThrowNode;
import libbun.parser.ast.ZTopLevelNode;
import libbun.parser.ast.ZTryNode;
import libbun.parser.ast.ZTypeNode;
import libbun.parser.ast.ZUnaryNode;
import libbun.parser.ast.ZVarBlockNode;
import libbun.parser.ast.ZWhileNode;
import libbun.type.ZClassType;
import libbun.type.ZFunc;
import libbun.type.ZFuncType;
import libbun.type.ZGenericType;
import libbun.type.ZPrototype;
import libbun.type.ZType;
import libbun.type.ZTypePool;
import libbun.type.ZVarScope;
import libbun.type.ZVarType;
import libbun.util.Field;
import libbun.util.LibZen;
import libbun.util.Nullable;
import libbun.util.Var;

public class BunTypeSafer extends ZTypeChecker {

	@Field protected ZFunctionNode CurrentFunctionNode = null;

	public BunTypeSafer(ZGenerator Generator) {
		super(Generator);
	}

	public final boolean IsTopLevel() {
		return (this.CurrentFunctionNode == null);
	}

	@Override public void VisitDefaultValueNode(ZDefaultValueNode Node) {
		@Var ZType Type = this.GetContextType();
		if(Type.IsIntType()) {
			this.ReturnTypeNode(new ZIntNode(Node.ParentNode, null, 0), Type);
		}
		else if(Type.IsBooleanType()) {
			this.ReturnTypeNode(new ZBooleanNode(Node.ParentNode, null, false), Type);
		}
		else if(Type.IsFloatType()) {
			this.ReturnTypeNode(new ZFloatNode(Node.ParentNode, null, 0.0), Type);
		}
		else if(!Type.IsVarType()) {
			this.ReturnTypeNode(new ZNullNode(Node.ParentNode, null), Type);
		}
		else {
			this.ReturnTypeNode(Node, Type);
		}
	}

	@Override public void VisitNullNode(ZNullNode Node) {
		@Var ZType Type = this.GetContextType();
		this.ReturnTypeNode(Node, Type);
	}

	@Override public void VisitBooleanNode(ZBooleanNode Node) {
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	@Override public void VisitIntNode(ZIntNode Node) {
		this.ReturnTypeNode(Node, ZType.IntType);
	}

	@Override public void VisitFloatNode(ZFloatNode Node) {
		this.ReturnTypeNode(Node, ZType.FloatType);
	}

	@Override public void VisitStringNode(ZStringNode Node) {
		this.ReturnTypeNode(Node, ZType.StringType);
	}

	@Override public void VisitArrayLiteralNode(ZArrayLiteralNode Node) {
		@Var ZType ArrayType = this.GetContextType();
		if(ArrayType.IsMapType() && Node.GetListSize() == 0) {
			/* this is exceptional treatment for map literal */
			this.ReturnTypeNode(new ZMapLiteralNode(Node.ParentNode), ArrayType);
			return;
		}
		@Var ZType ElementType = ZType.VarType;
		if(ArrayType.IsArrayType()) {
			ElementType = ArrayType.GetParamType(0);
		}
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			@Var ZNode SubNode = Node.GetListAt(i);
			SubNode = this.CheckType(SubNode, ElementType);
			Node.SetListAt(i, SubNode);
			if(ElementType.IsVarType()) {
				ElementType = SubNode.Type;
			}
			i = i + 1;
		}
		if(!ElementType.IsVarType()) {
			this.ReturnTypeNode(Node,ZTypePool._GetGenericType1(ZGenericType._ArrayType, ElementType));
		}
		else {
			this.ReturnTypeNode(Node, ZType.VarType);
		}
	}

	@Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
		@Var ZType ContextType = this.GetContextType();
		@Var ZType EntryType = ZType.VarType;
		if(ContextType.IsMapType()) {
			EntryType = ContextType.GetParamType(0);
		}
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			@Var ZMapEntryNode EntryNode = Node.GetMapEntryNode(i);
			if(EntryNode.Name == null) {
				EntryNode.Name = EntryNode.KeyNode().SourceToken.GetText();
			}
			if(EntryNode.IsUntyped()) {
				this.CheckTypeAt(EntryNode, ZMapEntryNode._Value, EntryType);
				if(EntryType.IsVarType()) {
					EntryType = EntryNode.GetAstType(ZMapEntryNode._Value);
				}
			}
			i = i + 1;
		}
		if(!EntryType.IsVarType()) {
			this.ReturnTypeNode(Node, ZTypePool._GetGenericType1(ZGenericType._MapType, EntryType));
			return;
		}
		else {
			this.ReturnTypeNode(Node, ZType.VarType);
		}
	}

	@Override public void VisitGetNameNode(ZGetNameNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZNode VarNode = NameSpace.GetSymbol(Node.GetName());
		Node.ResolvedNode = VarNode;
		if(VarNode instanceof ZLetVarNode) {
			this.ReturnTypeNode(Node, ((ZLetVarNode)VarNode).DeclType());
			return;
		}
		if(VarNode != null) {
			this.ReturnTypeNode(Node, VarNode.Type);
			return;
		}
		this.ReturnNode(Node);
	}

	@Override public void VisitSetNameNode(ZSetNameNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZNode SymbolNode = NameSpace.GetSymbol(Node.GetName());
		if(SymbolNode == null) {
			this.ReturnErrorNode(Node, Node.SourceToken, "undefined variable");
			return;
		}
		if(SymbolNode instanceof ZLetVarNode) {
			@Var ZLetVarNode VarNode = (ZLetVarNode)SymbolNode;
			//			if(!VarNode.IsReadOnly) {
			this.CheckTypeAt(Node, ZSetNameNode._Expr, VarNode.DeclType());
			this.ReturnTypeNode(Node, ZType.VoidType);
			return;
			//			}
		}
		this.ReturnErrorNode(Node, Node.SourceToken, "readonly variable");
	}

	private ZType GetIndexType(ZNameSpace NameSpace, ZType RecvType) {
		if(RecvType.IsArrayType() || RecvType.IsStringType()) {
			return ZType.IntType;
		}
		if(RecvType.IsMapType()) {
			return ZType.StringType;
		}
		return ZType.VarType;
	}

	private ZType GetElementType(ZNameSpace NameSpace, ZType RecvType) {
		if(RecvType.IsArrayType() || RecvType.IsMapType()) {
			return RecvType.GetParamType(0);
		}
		if(RecvType.IsStringType()) {
			return ZType.StringType;
		}
		return ZType.VarType;
	}

	@Override public void VisitGetIndexNode(ZGetIndexNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		this.CheckTypeAt(Node, ZGetIndexNode._Recv, ZType.VarType);
		this.CheckTypeAt(Node, ZGetIndexNode._Index, this.GetIndexType(NameSpace, Node.RecvNode().Type));
		this.ReturnTypeNode(Node, this.GetElementType(NameSpace, Node.RecvNode().Type));
	}

	@Override public void VisitSetIndexNode(ZSetIndexNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		this.CheckTypeAt(Node, ZSetIndexNode._Recv, ZType.VarType);
		this.CheckTypeAt(Node, ZSetIndexNode._Index, this.GetIndexType(NameSpace, Node.RecvNode().Type));
		this.CheckTypeAt(Node, ZSetIndexNode._Expr, this.GetElementType(NameSpace, Node.RecvNode().Type));
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitGroupNode(ZGroupNode Node) {
		@Var ZType ContextType = this.GetContextType();
		this.CheckTypeAt(Node, ZGroupNode._Expr, ContextType);
		this.ReturnTypeNode(Node, Node.GetAstType(ZGroupNode._Expr));
	}

	@Override public void VisitMacroNode(ZMacroNode FuncNode) {
		this.ReturnNode(this.TypeListNodeAsFuncCall(FuncNode, FuncNode.GetFuncType()));
	}

	@Override public void VisitFuncCallNode(ZFuncCallNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		this.TypeCheckNodeList(Node);
		this.CheckTypeAt(Node, ZFuncCallNode._Functor, ZType.VarType);
		@Var ZNode FuncNode = Node.FunctorNode();
		@Var ZType FuncNodeType = Node.GetAstType(ZFuncCallNode._Functor);
		if(FuncNodeType instanceof ZFuncType) {
			this.ReturnNode(this.TypeListNodeAsFuncCall(Node, (ZFuncType)FuncNodeType));
			return;
		}
		if(FuncNode instanceof ZTypeNode) {   // TypeName()..;
			@Var String FuncName = FuncNode.Type.GetName();
			FuncNode = new ZFuncNameNode(Node, FuncNode.SourceToken, FuncName, FuncNode.Type, Node.GetListSize());
			Node.SetNode(ZFuncCallNode._Functor, FuncNode);
		}
		if(FuncNode instanceof ZGetNameNode) {
			@Var String FuncName = ((ZGetNameNode)FuncNode).GetName();
			FuncNode = new ZFuncNameNode(Node, FuncNode.SourceToken, FuncName, Node.GetRecvType(), Node.GetListSize());
			Node.SetNode(ZFuncCallNode._Functor, FuncNode);
		}
		if(FuncNode instanceof ZFuncNameNode) {
			ZFuncNameNode FuncNameNode = (ZFuncNameNode)FuncNode;
			@Var ZFunc Func = this.LookupFunc(NameSpace, FuncNameNode.FuncName, FuncNameNode.RecvType, FuncNameNode.FuncParamSize);
			if(Func instanceof ZMacroFunc) {
				@Var ZMacroNode MacroNode = Node.ToMacroNode((ZMacroFunc)Func);
				this.ReturnNode(this.TypeListNodeAsFuncCall(MacroNode, Func.GetFuncType()));
				return;
			}
			if(Func != null) {
				this.ReturnNode(this.TypeListNodeAsFuncCall(Node, Func.GetFuncType()));
				return;
			}
			this.ReturnTypeNode(Node, ZType.VarType);
		}
		else {
			this.ReturnNode(new ZErrorNode(Node, "not function: " + FuncNodeType + " of node " + Node.FunctorNode()));
		}
	}

	private ZType LookupFieldType(ZNameSpace NameSpace, ZType ClassType, String FieldName) {
		ClassType = ClassType.GetRealType();
		if(ClassType instanceof ZClassType) {
			return ((ZClassType)ClassType).GetFieldType(FieldName, ZType.VoidType);
		}
		return NameSpace.Generator.GetFieldType(ClassType, FieldName);
	}

	private ZType LookupSetterType(ZNameSpace NameSpace, ZType ClassType, String FieldName) {
		ClassType = ClassType.GetRealType();
		if(ClassType instanceof ZClassType) {
			return ((ZClassType)ClassType).GetFieldType(FieldName, ZType.VoidType);
		}
		return NameSpace.Generator.GetSetterType(ClassType, FieldName);
	}

	private ZNode UndefinedFieldNode(ZNode Node, String Name) {
		return new ZErrorNode(Node, "undefined field: " + Name + " of " + Node.GetAstType(ZGetterNode._Recv));
	}

	@Override public void VisitGetterNode(ZGetterNode Node) {
		this.CheckTypeAt(Node, ZGetterNode._Recv, ZType.VarType);
		@Var ZNode RecvNode = Node.RecvNode();
		if(!RecvNode.IsUntyped()) {
			@Var ZType FieldType = this.LookupFieldType(Node.GetNameSpace(), Node.GetAstType(ZGetterNode._Recv), Node.GetName());
			if(FieldType.IsVoidType()) {
				this.ReturnNode(this.UndefinedFieldNode(Node, Node.GetName()));
				return;
			}
			this.ReturnTypeNode(Node, FieldType);
			return;
		}
		if(RecvNode instanceof ZGetNameNode) {
			@Var String Symbol = ((ZGetNameNode)RecvNode).GetName() + "." + Node.GetName();
			@Var ZNode VarNode = Node.GetNameSpace().GetSymbol(Symbol);
			if(VarNode instanceof ZAsmNode) {
				((ZGetNameNode) RecvNode).GivenName = Symbol;
				((ZGetNameNode) RecvNode).ResolvedNode = VarNode;
				this.ReturnTypeNode(RecvNode, VarNode.Type);
				return;
			}
		}
		this.ReturnTypeNode(Node, ZType.VarType);
	}

	@Override public void VisitSetterNode(ZSetterNode Node) {
		this.CheckTypeAt(Node, ZSetterNode._Recv, ZType.VarType);
		if(!Node.RecvNode().IsUntyped()) {
			@Var ZNameSpace NameSpace = Node.GetNameSpace();
			@Var ZType FieldType = this.LookupSetterType(NameSpace, Node.GetAstType(ZSetterNode._Recv), Node.GetName());
			if(FieldType.IsVoidType()) {
				this.ReturnNode(this.UndefinedFieldNode(Node, Node.GetName()));
				return;
			}
			this.CheckTypeAt(Node, ZSetterNode._Expr, FieldType);
			this.ReturnTypeNode(Node, ZType.VoidType);
		}
		else {
			/* if Recv is Var, type should not be decided */
			this.ReturnTypeNode(Node, ZType.VarType);
		}
	}

	private void VisitListAsNativeMethod(ZNode Node, ZType RecvType, String MethodName, ZListNode List) {
		@Var ZFuncType FuncType = this.Generator.GetMethodFuncType(RecvType, MethodName, List);
		if(FuncType != null) {
			if(!FuncType.IsVarType()) {
				@Var int i = 0;
				//@Var int StaticShift = FuncType.GetParamSize() - List.GetListSize();
				@Var int StaticShift = FuncType.GetFuncParamSize() - List.GetListSize();
				while(i < List.GetListSize()) {
					@Var ZNode SubNode = List.GetListAt(i);
					SubNode = this.CheckType(SubNode, FuncType.GetFuncParamType(i+StaticShift));
					List.SetListAt(i, SubNode);
					i = i + 1;
				}
			}
			this.ReturnTypeNode(Node, FuncType.GetReturnType());
			return;
		}
		@Var String Message = null;
		if(MethodName == null) {
			Message = "undefined constructor: " + RecvType;
		}
		else {
			Message = "undefined method: " + MethodName + " of " + RecvType;
		}
		this.ReturnErrorNode(Node, null, Message);
	}

	@Override public void VisitMethodCallNode(ZMethodCallNode Node) {
		this.CheckTypeAt(Node, ZMethodCallNode._Recv, ZType.VarType);
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZNode RecvNode = Node.RecvNode();
		if(!RecvNode.IsUntyped()) {
			@Var ZType FieldType = this.LookupFieldType(NameSpace, Node.GetAstType(ZMethodCallNode._Recv), Node.MethodName());
			if(FieldType instanceof ZFuncType) {
				@Var ZFuncType FieldFuncType = (ZFuncType)FieldType;
				@Var ZFuncCallNode FuncCall = Node.ToGetterFuncCall(FieldFuncType);
				this.ReturnNode(this.TypeListNodeAsFuncCall(FuncCall, FieldFuncType));
				return;
			}
			@Var int FuncParamSize = Node.GetListSize() + 1;
			@Var ZFunc Func = this.LookupFunc(NameSpace, Node.MethodName(), Node.GetAstType(ZMethodCallNode._Recv), FuncParamSize);
			if(Func != null) {
				@Var ZListNode FuncCallNode = Node.ToFuncCallNode(this, Func, RecvNode);
				this.ReturnNode(this.TypeListNodeAsFuncCall(FuncCallNode, Func.GetFuncType()));
				return;
			}
			this.VisitListAsNativeMethod(Node, Node.GetAstType(ZMethodCallNode._Recv), Node.MethodName(), Node);
			return;
		}
		this.TypeCheckNodeList(Node);
		if(RecvNode instanceof ZGetNameNode) {
			@Var String Symbol = ((ZGetNameNode)RecvNode).GetName();
			@Var String FuncName = Symbol + "." + Node.MethodName();
			@Var int FuncParamSize = Node.GetListSize();
			@Var ZFunc Func = this.LookupFunc(NameSpace, FuncName, Node.GetAstType(ZMethodCallNode._NameInfo+1), FuncParamSize);
			if(Func != null) {
				@Var ZListNode FuncCallNode = Node.ToFuncCallNode(this, Func, null);
				this.ReturnNode(this.TypeListNodeAsFuncCall(FuncCallNode, Func.GetFuncType()));
				return;
			}
			@Var ZNode VarNode = NameSpace.GetSymbol(Symbol);
			if(VarNode instanceof ZAsmNode) {
				this.ReturnTypeNode(Node, ZType.VarType);
				return;
			}
			//			LibZen._PrintLine("FIXME: undefined function call:" + FuncName);
			//			//TODO: undefined function
		}
		this.ReturnTypeNode(Node, ZType.VarType);
	}

	@Override public void VisitNewObjectNode(ZNewObjectNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ContextType = this.GetContextType();
		this.TypeCheckNodeList(Node);
		if(Node.ClassType().IsVarType()) {
			if(ContextType.IsVarType()) {
				this.ReturnTypeNode(Node, ZType.VarType);
				return;
			}
			Node.GivenType = ContextType;
		}
		@Var int FuncParamSize = Node.GetListSize() + 1;
		@Var ZFunc Func = this.LookupFunc(NameSpace, Node.ClassType().GetName(), Node.ClassType(), FuncParamSize);
		if(Func != null) {
			@Var ZListNode FuncCall = Node.ToFuncCallNode(NameSpace.Generator.TypeChecker, Func);
			this.ReturnNode(this.TypeListNodeAsFuncCall(FuncCall, Func.GetFuncType()));
			return;
		}
		if(FuncParamSize == 1) { /* no argument */
			this.ReturnTypeNode(Node, Node.ClassType());
		}
		else {
			this.VisitListAsNativeMethod(Node, Node.ClassType(), null, Node);
		}
	}

	@Override public void VisitUnaryNode(ZUnaryNode Node) {
		this.CheckTypeAt(Node, ZUnaryNode._Recv, ZType.VarType);
		this.ReturnTypeNode(Node, Node.RecvNode().Type);
	}

	@Override public void VisitNotNode(ZNotNode Node) {
		this.CheckTypeAt(Node, ZNotNode._Recv, ZType.BooleanType);
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	@Override public void VisitCastNode(ZCastNode Node) {
		@Var ZType ContextType = this.GetContextType();
		if(Node.CastType().IsVarType()) {
			Node.Type = ContextType;
		}
		this.TryTypeAt(Node, ZCastNode._Expr, Node.CastType());
		@Var ZType ExprType = Node.ExprNode().Type;
		if(Node.Type.IsVarType() || ExprType.IsVarType()) {
			this.ReturnNode(Node);
			return;
		}
		if(ExprType.Equals(Node.Type) || Node.Type.Accept(ExprType)) {
			this.ReturnNode(Node.ExprNode());
			return;
		}
		if(ExprType.Accept(Node.Type)) {
			this.ReturnNode(this.CreateStupidCastNode(Node.Type, Node.ExprNode(), Node.GetAstToken(ZCastNode._TypeInfo), "unsafe downcast"));
			return;
		}
		else {
			@Var ZFunc Func = this.Generator.LookupConverterFunc(ExprType, Node.Type);
			if(Func != null) {
				this.ReturnTypeNode(Node.ToFuncCallNode(this, Func), Node.Type);
				return;
			}
		}
		this.ReturnNode(this.CreateStupidCastNode(Node.Type, Node.ExprNode(), Node.GetAstToken(ZCastNode._TypeInfo), "undefined converter"));
	}

	@Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
		this.CheckTypeAt(Node, ZBinaryNode._Left, ZType.VarType);
		if(!(Node.TargetType() instanceof ZClassType)) {
			ZLogger._LogWarning(Node.GetAstToken(ZInstanceOfNode._TypeInfo), "instanceof takes a class type; the result is implementation-dependant.");
		}
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	private ZType GuessBinaryLeftType(ZToken Op, ZType ContextType) {
		if(Op.EqualsText('|') || Op.EqualsText('&') || Op.EqualsText("<<") || Op.EqualsText(">>") || Op.EqualsText('^')) {
			return ZType.IntType;
		}
		if(Op.EqualsText('+') || Op.EqualsText('-') || Op.EqualsText('*') || Op.EqualsText('/') || Op.EqualsText('%')) {
			if(ContextType.IsNumberType()) {
				return ContextType;
			}
		}
		return ZType.VarType;
	}

	private void UnifyBinaryNodeType(ZBinaryNode Node, ZType Type) {
		if(Node.GetAstType(ZBinaryNode._Left).Equals(Type)) {
			this.CheckTypeAt(Node, ZBinaryNode._Right, Type);
			return;
		}
		if(Node.GetAstType(ZBinaryNode._Right).Equals(Type)) {
			this.CheckTypeAt(Node, ZBinaryNode._Left, Type);
		}
	}

	private void UnifyBinaryEnforcedType(ZBinaryNode Node, ZType Type) {
		if(Node.GetAstType(ZBinaryNode._Left).Equals(Type)) {
			Node.SetNode(ZBinaryNode._Right, this.EnforceNodeType(Node.RightNode(), Type));
			return;
		}
		if(Node.GetAstType(ZBinaryNode._Right).Equals(Type)) {
			Node.SetNode(ZBinaryNode._Left, this.EnforceNodeType(Node.LeftNode(), Type));
		}
	}

	@Override public void VisitBinaryNode(ZBinaryNode Node) {
		@Var ZType ContextType = this.GetContextType();
		@Var ZType LeftType = this.GuessBinaryLeftType(Node.SourceToken, ContextType);
		@Var ZType RightType = this.GuessBinaryLeftType(Node.SourceToken, ContextType);
		this.CheckTypeAt(Node, ZBinaryNode._Left, LeftType);
		this.CheckTypeAt(Node, ZBinaryNode._Right, RightType);
		if(!Node.GetAstType(ZBinaryNode._Left).Equals(Node.GetAstType(ZBinaryNode._Right))) {
			if(Node.SourceToken.EqualsText('+')) {
				this.UnifyBinaryEnforcedType(Node, ZType.StringType);
			}
			this.UnifyBinaryNodeType(Node, ZType.FloatType);
			this.CheckTypeAt(Node, ZBinaryNode._Left, Node.GetAstType(ZBinaryNode._Right));
		}
		this.ReturnTypeNode(Node.TryMacroNode(this.Generator), Node.GetAstType(ZBinaryNode._Left));
	}

	@Override public void VisitComparatorNode(ZComparatorNode Node) {
		this.CheckTypeAt(Node, ZBinaryNode._Left, ZType.VarType);
		this.TryTypeAt(Node, ZBinaryNode._Right, Node.GetAstType(ZBinaryNode._Left));
		this.UnifyBinaryNodeType(Node, ZType.FloatType);
		//this.CheckTypeAt(Node, ZBinaryNode._Right, Node.GetAstType(ZBinaryNode._Left));
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	@Override public void VisitAndNode(ZAndNode Node) {
		this.CheckTypeAt(Node, ZBinaryNode._Left, ZType.BooleanType);
		this.CheckTypeAt(Node, ZBinaryNode._Right, ZType.BooleanType);
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	@Override public void VisitOrNode(ZOrNode Node) {
		this.CheckTypeAt(Node, ZBinaryNode._Left, ZType.BooleanType);
		this.CheckTypeAt(Node, ZBinaryNode._Right, ZType.BooleanType);
		this.ReturnTypeNode(Node, ZType.BooleanType);
	}

	protected void VisitVarDeclNode(ZNameSpace NameSpace, ZLetVarNode Node1) {
		@Var @Nullable ZLetVarNode CurNode = Node1;
		while(CurNode != null) {
			CurNode.InitValueNode();
			this.CheckTypeAt(CurNode, ZLetVarNode._InitValue, CurNode.DeclType());
			if(CurNode.DeclType().IsVarType()) {
				CurNode.SetDeclType(CurNode.GetAstType(ZLetVarNode._InitValue));
			}
			CurNode.SetDeclType(this.VarScope.NewVarType(CurNode.DeclType(), CurNode.GetName(), CurNode.SourceToken));
			NameSpace.SetSymbol(CurNode.GetName(), CurNode);
			CurNode = CurNode.NextVarNode();
		}
	}

	@Override public void VisitBlockNode(ZBlockNode Node) {
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			@Var ZNode SubNode = Node.GetListAt(i);
			@Var ZNode TypedNode = this.CheckType(SubNode, ZType.VoidType);
			@Var ZNode CheckNode = Node.GetListAt(i);
			while(SubNode != CheckNode) {  // detecting replacement
				SubNode = CheckNode;
				TypedNode = this.CheckType(SubNode, ZType.VoidType);
				CheckNode = Node.GetListAt(i);
			}
			Node.SetListAt(i, TypedNode);
			if(ZNodeUtils._IsBlockBreak(SubNode)) {
				Node.ClearListAfter(i+1);
				break;
			}
			i = i + 1;
		}
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitVarBlockNode(ZVarBlockNode Node) {
		if(this.IsTopLevel()) {
			this.ReturnErrorNode(Node, Node.SourceToken, "only available inside function");
			return;
		}
		this.VisitVarDeclNode(Node.GetBlockNameSpace(), Node.VarDeclNode());
		this.VisitBlockNode(Node);
		if(Node.GetListSize() == 0) {
			ZLogger._LogWarning(Node.SourceToken, "unused variable: " + Node.VarDeclNode().GetName());
		}
	}

	@Override public void VisitIfNode(ZIfNode Node) {
		this.CheckTypeAt(Node, ZIfNode._Cond, ZType.BooleanType);
		this.CheckTypeAt(Node, ZIfNode._Then, ZType.VoidType);
		if(Node.HasElseNode()) {
			this.CheckTypeAt(Node, ZIfNode._Else, ZType.VoidType);
		}
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitReturnNode(ZReturnNode Node) {
		if(this.IsTopLevel()) {
			this.ReturnErrorNode(Node, Node.SourceToken, "only available inside function");
			return;
		}
		@Var ZType ReturnType = this.CurrentFunctionNode.ReturnType();
		if(Node.HasReturnExpr() && ReturnType.IsVoidType()) {
			Node.AST[ZReturnNode._Expr] = null;
		}
		else if(!Node.HasReturnExpr() && !ReturnType.IsVarType() && !ReturnType.IsVoidType()) {
			ZLogger._LogWarning(Node.SourceToken, "returning default value of " + ReturnType);
			Node.SetNode(ZReturnNode._Expr, new ZDefaultValueNode());
		}
		if(Node.HasReturnExpr()) {
			this.CheckTypeAt(Node, ZReturnNode._Expr, ReturnType);
		}
		else {
			if(ReturnType instanceof ZVarType) {
				((ZVarType)ReturnType).Infer(ZType.VoidType, Node.SourceToken);
			}
		}
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitWhileNode(ZWhileNode Node) {
		this.CheckTypeAt(Node, ZWhileNode._Cond, ZType.BooleanType);
		this.CheckTypeAt(Node, ZWhileNode._Block, ZType.VoidType);
		if(Node.HasNextNode()) {
			this.CheckTypeAt(Node, ZWhileNode._Next, ZType.VoidType);
			Node.BlockNode().Append(Node.NextNode());
		}
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitBreakNode(ZBreakNode Node) {
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitThrowNode(ZThrowNode Node) {
		this.CheckTypeAt(Node, ZThrowNode._Expr, ZType.VarType);
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitTryNode(ZTryNode Node) {
		this.CheckTypeAt(Node, ZTryNode._Try, ZType.VoidType);
		if(Node.HasCatchBlockNode()) {
			@Var ZNameSpace NameSpace = Node.CatchBlockNode().GetBlockNameSpace();
			@Var ZLetVarNode VarNode = new ZLetVarNode(Node, ZLetVarNode._IsReadOnly);
			VarNode.GivenName = Node.ExceptionName();
			VarNode.GivenType = ZClassType._ObjectType;
			NameSpace.SetSymbol(VarNode.GetName(), VarNode);
			this.CheckTypeAt(Node, ZTryNode._Catch, ZType.VoidType);
		}
		if(Node.HasFinallyBlockNode()) {
			this.CheckTypeAt(Node, ZTryNode._Finally, ZType.VoidType);
		}
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitLetNode(ZLetVarNode Node) {
		if(Node.IsTopLevel()) {
			@Var ZType DeclType = Node.DeclType();
			this.CheckTypeAt(Node, ZLetVarNode._InitValue, DeclType);
			@Var ZType ConstType = Node.InitValueNode().Type;
			Node.SetAstType(ZLetVarNode._NameInfo, ConstType);
			if(DeclType.IsVarType()) {
				Node.SetDeclType(ConstType);
			}
			Node.GlobalName = this.Generator.NameGlobalSymbol(Node.GetName(), Node.IsExport());
			Node.GetNameSpace().SetDebugSymbol(Node.GetName(), Node);
			this.ReturnTypeNode(Node, ZType.VoidType);
		}
		else {
			this.ReturnNode(new ZVarBlockNode(null, Node, Node.GetScopeBlockNode()));
		}
	}

	@Override public void DefineFunction(ZFunctionNode FunctionNode, boolean Enforced) {
		if(FunctionNode.FuncName() != null && FunctionNode.ResolvedFuncType == null) {
			@Var ZFuncType FuncType = FunctionNode.GetFuncType();
			if(Enforced || !FuncType.IsVarType()) {
				@Var ZNameSpace NameSpace = FunctionNode.GetNameSpace();
				@Var ZPrototype Func = NameSpace.Generator.SetPrototype(FunctionNode, FunctionNode.FuncName(), FuncType);
				if(Func != null) {
					Func.Defined();
					if(Func.DefinedCount > 1) {
						ZLogger._LogError(FunctionNode.SourceToken, "redefinition of function: " + Func);
					}
				}
			}
		}
	}

	private void PushFunctionNode(ZNameSpace NameSpace, ZFunctionNode FunctionNode, ZType ContextType) {
		@Var ZFuncType FuncType = null;
		if(ContextType instanceof ZFuncType) {
			FuncType = (ZFuncType)ContextType;
		}
		this.CurrentFunctionNode = FunctionNode.Push(this.CurrentFunctionNode);
		this.VarScope = new ZVarScope(this.VarScope, this.Logger, null);
		@Var int i = 0;
		while(i < FunctionNode.GetListSize()) {
			@Var ZLetVarNode ParamNode = FunctionNode.GetParamNode(i);
			ParamNode.SetDeclType(this.VarScope.NewVarType(ParamNode.DeclType(), ParamNode.GetName(), ParamNode.GetAstToken(ZLetVarNode._NameInfo)));
			if(FuncType != null) {
				this.VarScope.InferType(FuncType.GetFuncParamType(i), ParamNode);
			}
			NameSpace.SetSymbol(ParamNode.GetName(), ParamNode);
			i = i + 1;
		}
		FunctionNode.SetReturnType(this.VarScope.NewVarType(FunctionNode.ReturnType(), "return", FunctionNode.SourceToken));
		if(FuncType != null) {
			FunctionNode.Type.Maybe(FuncType.GetReturnType(), null);
		}
	}

	private void PopFunctionNode(ZNameSpace NameSpace) {
		this.CurrentFunctionNode = this.CurrentFunctionNode.Pop();
		this.VarScope = this.VarScope.Parent;
	}

	@Override public void VisitFunctionNode(ZFunctionNode Node) {
		//LibZen._PrintDebug("name="+Node.FuncName+ ", Type=" + Node.Type + ", IsTopLevel=" + this.IsTopLevel());
		@Var ZType ContextType = this.GetContextType();
		if(Node.IsUntyped()) {
			Node.Type = ContextType;  // funcdecl is requested with VoidType
		}
		if(Node.Type.IsVoidType()) {
			if(Node.FuncName() == null) {   // function() object
				Node.Type = ZType.VarType;
			}
			//			if(!this.IsTopLevel()) {
			//				/* function f() {} ==> var f = function() {} */
			//				@Var ZVarNode VarNode = new ZVarNode(Node.ParentNode);
			//				VarNode.SetNode(ZLetVarNode._NameInfo, Node.AST[ZFunctionNode._NameInfo]);
			//				VarNode.SetNode(ZLetVarNode._InitValue, Node);
			//				@Var ZBlockNode Block = Node.GetScopeBlockNode();
			//				@Var int Index = Block.IndexOf(Node);
			//				Block.CopyTo(Index+1, VarNode);
			//				Block.ClearListAfter(Index+1);   // Block[Index] is set to VarNode
			//				this.VisitVarNode(VarNode);
			//				return;
			//			}
		}
		if(!ZNodeUtils._HasFunctionBreak(Node.BlockNode())) {
			//System.out.println("adding return.. ");
			Node.BlockNode().SetNode(ZNode._AppendIndex, new ZReturnNode(Node));
		}
		@Var ZNameSpace NameSpace = Node.BlockNode().GetBlockNameSpace();
		this.PushFunctionNode(NameSpace, Node, ContextType);
		this.VarScope.TypeCheckFuncBlock(this, Node);
		this.PopFunctionNode(NameSpace);
		if(!Node.Type.IsVoidType()) {
			Node.Type = Node.GetFuncType();
		}
		this.ReturnNode(Node);
	}

	@Override public void VisitClassNode(ZClassNode Node) {
		@Var ZNameSpace NameSpace = Node.GetNameSpace();
		@Var ZType ClassType = NameSpace.GetType(Node.ClassName(), Node.SourceToken, true/*IsCreation*/);
		if(ClassType instanceof ZClassType) {
			if(!ClassType.IsOpenType()) {
				this.ReturnNode(new ZErrorNode(Node, Node.ClassName() + " has been defined."));
				return;
			}
			Node.ClassType = (ZClassType)ClassType;
		}
		else {
			this.ReturnNode(new ZErrorNode(Node, Node.ClassName() + " is not a Zen class."));
			return;
		}
		//System.out.println(" B NodeClass.ToOpen="+Node.ClassType+", IsOpenType="+Node.ClassType.IsOpenType());
		if(Node.SuperType() != null) {
			if(Node.SuperType() instanceof ZClassType && !Node.SuperType().IsOpenType()) {
				Node.ClassType.EnforceSuperClass((ZClassType)Node.SuperType());
			}
			else {
				this.ReturnNode(new ZErrorNode(Node.ParentNode, Node.GetAstToken(ZClassNode._TypeInfo), "" + Node.SuperType() + " cannot be extended."));
				return;
			}
		}
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			@Var ZLetVarNode FieldNode = Node.GetFieldNode(i);
			if(!Node.ClassType.HasField(FieldNode.GetName())) {
				FieldNode.InitValueNode();// creation of default value if not given;
				this.CheckTypeAt(FieldNode, ZLetVarNode._InitValue, FieldNode.DeclType());
				if(FieldNode.DeclType().IsVarType()) {
					FieldNode.SetDeclType(FieldNode.InitValueNode().Type);
				}
				if(FieldNode.DeclType().IsVarType()) {
					ZLogger._LogError(FieldNode.SourceToken, "type of " + FieldNode.GetName() + " is unspecific");
				}
				else {
					Node.ClassType.AppendField(FieldNode.DeclType(), FieldNode.GetName(), FieldNode.SourceToken);
				}
			}
			else {
				ZLogger._LogError(FieldNode.SourceToken, "duplicated field: " + FieldNode.GetName());
			}
			FieldNode.Type = ZType.VoidType;
			i = i + 1;
		}
		Node.ClassType.TypeFlag = LibZen._UnsetFlag(Node.ClassType.TypeFlag, ZType.OpenTypeFlag);
		//System.out.println(" E NodeClass.ToOpen="+Node.ClassType+", IsOpenType="+Node.ClassType.IsOpenType());
		this.ReturnTypeNode(Node, ZType.VoidType);
	}

	@Override public void VisitTopLevelNode(ZTopLevelNode Node) {
		// TODO Auto-generated method stub
		System.out.println("FIXME: " + Node);
	}

	@Override public void VisitLocalDefinedNode(ZLocalDefinedNode Node) {
		// TODO Auto-generated method stub
		System.out.println("FIXME: " + Node);
	}


	// utils

	private ZFunc LookupFunc(ZNameSpace NameSpace, String FuncName, ZType RecvType, int FuncParamSize) {
		@Var String Signature = ZFunc._StringfySignature(FuncName, FuncParamSize, RecvType);
		@Var ZFunc Func = this.Generator.GetDefinedFunc(Signature);
		//System.out.println("LookupFunc: " + Func + " by " + Signature);
		if(Func != null) {
			return Func;
		}
		RecvType = RecvType.GetSuperType();
		while(RecvType != null) {
			Signature = ZFunc._StringfySignature(FuncName, FuncParamSize, RecvType);
			Func = this.Generator.GetDefinedFunc(Signature);
			if(Func != null) {
				return Func;
			}
			if(RecvType.IsVarType()) {
				break;
			}
			RecvType = RecvType.GetSuperType();
		}
		//		if(Func == null) {
		//			System.err.println("Unfound: " + FuncName + ", " + RecvType + ", " + FuncParamSize);
		//		}
		return null;
	}

	//	private ZFunc LookupFunc2(ZNameSpace NameSpace, String FuncName, ZType RecvType, int FuncParamSize) {
	//		@Var ZFunc Func = this.Generator.LookupFunc(FuncName, RecvType, FuncParamSize);
	//		if(Func == null && RecvType.IsIntType()) {
	//			Func = this.Generator.GetDefinedFunc(FuncName, ZType.FloatType, FuncParamSize);
	//		}
	//		if(Func == null && RecvType.IsFloatType()) {
	//			Func = this.Generator.GetDefinedFunc(FuncName, ZType.IntType, FuncParamSize);
	//		}
	//		if(Func == null) {
	//			System.err.println("Unfound: " + FuncName + ", " + RecvType + ", " + FuncParamSize);
	//		}
	//		return null;
	//	}

}

