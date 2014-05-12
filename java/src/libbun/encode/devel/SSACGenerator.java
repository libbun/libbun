// ***************************************************************************
// Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
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


package libbun.encode.devel;

import libbun.ast.AstNode;
import libbun.ast.LocalDefinedNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunMapNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.common.CommonArray;
import libbun.encode.obsolete.OldSourceGenerator;
import libbun.parser.common.BunLogger;
import libbun.parser.ssa.NodeLib;
import libbun.parser.ssa.PHINode;
import libbun.parser.ssa.SSAConverter;
import libbun.type.BClassField;
import libbun.type.BClassType;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.Var;

public class SSACGenerator extends OldSourceGenerator {

	public SSACGenerator() {
		super("c", "C99");
		this.TrueLiteral  = "1/*true*/";
		this.FalseLiteral = "0/*false*/";
		this.NullLiteral  = "NULL";

		this.TopType = "ZObject *";
		this.SetNativeType(BType.BooleanType, "int");
		//		this.SetNativeType(ZType.IntType, "long long int");
		this.SetNativeType(BType.IntType, "long");
		this.SetNativeType(BType.FloatType, "double");
		this.SetNativeType(BType.StringType, "const char *");

		this.Header.AppendNewLine("/* end of header */", this.LineFeed);
	}

	@Override protected void GenerateImportLibrary(String LibName) {
		this.Header.AppendNewLine("#include<", LibName, ">");
	}

	@Override protected void GenerateExpression(AstNode Node) {
		if(Node.IsUntyped() && !Node.IsErrorNode() && !(Node instanceof BunFuncNameNode)) {
			this.Source.Append("/*untyped*/" + this.NullLiteral);
			BunLogger._LogError(Node.SourceToken, "untyped error: " + Node);
		}
		else {
			//			if(ContextType != null && Node.Type != ContextType) {
			//				this.Source.Append("(");
			//				this.GenerateTypeName(ContextType);
			//				this.Source.Append(")");
			//			}
			Node.Accept(this);
		}
	}

	@Override public void VisitGetNameNode(GetNameNode Node) {
		if(Node.ResolvedNode == null && !this.LangInfo.AllowUndefinedSymbol) {
			BunLogger._LogError(Node.SourceToken, "undefined symbol: " + Node.GetUniqueName(this));
		}
		this.Source.Append(Node.GetUniqueName(this) + Node.VarIndex);
	}

	@Override public void VisitAssignNode(AssignNode Node) {
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" = ");
		this.GenerateExpression(Node.RightNode());
	}

	@Override public void VisitArrayLiteralNode(BunArrayNode Node) {
		@Var BType ParamType = Node.Type.GetParamType(0);
		if(ParamType.IsIntType() || ParamType.IsBooleanType()) {
			this.Source.Append("LibZen_NewIntArray(");
		}
		else if(ParamType.IsFloatType()) {
			this.Source.Append("LibZen_NewFloatArray(");
		}
		else if(ParamType.IsStringType()) {
			this.Source.Append("LibZen_NewStringArray(");
		}
		else {
			this.Source.Append("LibZen_NewArray(");
		}
		this.Source.Append(String.valueOf(Node.size()));
		if(Node.size() > 0) {
			this.Source.Append(this.Camma);
		}
		this.GenerateListNode("", Node, 0, ", ", ")");
	}

	@Override public void VisitMapLiteralNode(BunMapNode Node) {
		@Var BType ParamType = Node.Type.GetParamType(0);
		if(ParamType.IsIntType() || ParamType.IsBooleanType()) {
			this.Source.Append("LibZen_NewIntMap(");
		}
		else if(ParamType.IsFloatType()) {
			this.Source.Append("LibZen_NewFloatMap(");
		}
		else if(ParamType.IsStringType()) {
			this.Source.Append("LibZen_NewStringMap(");
		}
		else {
			this.Source.Append("LibZen_NewMap(");
		}
		this.Source.Append(String.valueOf(Node.GetListSize()));
		if(Node.GetListSize() > 0) {
			this.Source.Append(this.Camma);
		}
		this.GenerateListNode("", Node, 0, ", ", ")");
	}

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		this.Source.Append("_New"+this.NameClass(Node.Type));
		this.GenerateListNode("(", Node, ")");
	}

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		this.Source.Append(this.NameType(Node.getTypeAt(GetIndexNode._Recv)) + "GetIndex");
		this.Source.Append("(");
		this.GenerateExpression(Node.IndexNode());
		this.Source.Append(")");
	}

	//	@Override public void VisitSetIndexNode(SetIndexNode Node) {
	//		this.Source.Append(this.NameType(Node.GetAstType(GetIndexNode._Recv)) + "SetIndex");
	//		this.Source.Append("(");
	//		this.GenerateExpression(Node.IndexNode());
	//		this.Source.Append(this.Camma);
	//		this.GenerateExpression(Node.ExprNode());
	//		this.Source.Append(")");
	//	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append("->");
		this.Source.Append(Node.GetName());
	}

	@Override public void VisitMethodCallNode(MethodCallNode Node) {
		//		this.GenerateSurroundCode(Node.RecvNode());
		//		this.CurrentBuilder.Append(".");
		//		this.CurrentBuilder.Append(Node.MethodName());
		//		this.GenerateListNode("(", Node, ")");
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		@Var BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		if(FuncNameNode != null) {
			this.GenerateFuncName(FuncNameNode);
		}
		else {
			this.GenerateExpression(Node.FunctorNode());
		}
		this.GenerateListNode("(", Node, ")");
	}

	//	@Override public void VisitCastNode(ZCastNode Node) {
	//		this.CurrentBuilder.Append("(");
	//		this.VisitType(Node.Type);
	//		this.CurrentBuilder.Append(")");
	//		this.GenerateSurroundCode(Node.ExprNode());
	//	}

	@Override public void VisitThrowNode(BunThrowNode Node) {
		this.GenerateExpression(Node.ExprNode());
		this.Source.Append("longjump(1)"); // FIXME
		this.Source.AppendWhiteSpace();
	}

	@Override public void VisitTryNode(BunTryNode Node) {
		//		this.CurrentBuilder.Append("try");
		//		this.GenerateCode(Node.TryNode());
		//		if(Node.CatchNode() != null) {
		//			this.GenerateCode(Node.CatchNode());
		//		}
		//		if (Node.FinallyNode() != null) {
		//			this.CurrentBuilder.Append("finally");
		//			this.GenerateCode(Node.FinallyNode());
		//		}
	}

	private String ParamTypeName(BType Type) {
		if(Type.IsArrayType()) {
			return "ArrayOf" + this.ParamTypeName(Type.GetParamType(0));
		}
		if(Type.IsMapType()) {
			return "MapOf" + this.ParamTypeName(Type.GetParamType(0));
		}
		if(Type.IsFuncType()) {
			@Var String s = "FuncOf";
			@Var int i = 0;
			while(i < Type.GetParamSize()) {
				s = s +  this.ParamTypeName(Type.GetParamType(i));
				i = i + 1;
			}
			return s + "_";
		}
		if(Type.IsIntType()) {
			return "Int";
		}
		if(Type.IsFloatType()) {
			return "Float";
		}
		if(Type.IsVoidType()) {
			return "Void";
		}
		if(Type.IsVarType()) {
			return "Var";
		}
		return Type.ShortName;
	}

	private String GetCTypeName(BType Type) {
		@Var String TypeName = null;
		if(Type.IsArrayType() || Type.IsMapType()) {
			TypeName = this.ParamTypeName(Type) + " *";
		}
		if(Type instanceof BClassType) {
			TypeName = "struct " + this.NameClass(Type) + " *";
		}
		if(TypeName == null) {
			TypeName = this.GetNativeTypeName(Type);
		}
		return TypeName;
	}

	protected void GenerateFuncTypeName(BType Type, String FuncName) {
		this.GenerateTypeName(Type.GetParamType(0));
		this.Source.Append(" (*" + FuncName + ")");
		@Var int i = 1;
		this.Source.Append("(");
		while(i < Type.GetParamSize()) {
			if(i > 1) {
				this.Source.Append(",");
			}
			this.GenerateTypeName(Type.GetParamType(i));
			i = i + 1;
		}
		this.Source.Append(")");
	}

	@Override protected void GenerateTypeName(BType Type) {
		if(Type.IsFuncType()) {
			this.GenerateFuncTypeName(Type, "");
		}
		else {
			this.Source.Append(this.GetCTypeName(Type.GetRealType()));
		}
	}

	@Override protected void VisitVarDeclNode(BunLetVarNode Node) {
		this.GenerateTypeName(Node.DeclType());
		this.Source.Append(" ");
		this.Source.Append(this.NameLocalVariable(Node.GetGamma(), Node.GetGivenName() + "0"));
		this.GenerateExpression(" = ", Node.InitValueNode(), this.SemiColon);
	}

	@Override public void VisitLetNode(BunLetVarNode Node) {
		this.Source.Append("static ");
		this.GenerateTypeName(Node.getTypeAt(BunLetVarNode._InitValue));
		this.Source.Append(" ");
		this.Source.Append(Node.GetUniqueName(this));
		this.GenerateExpression(" = ", Node.InitValueNode(), this.SemiColon);
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		if(Node.Type.IsFuncType()) {
			this.GenerateFuncTypeName(Node.DeclType(), Node.GetUniqueName(this));
		}
		else {
			this.GenerateTypeName(Node.DeclType());
			this.Source.Append(" ");
			this.Source.Append(Node.GetUniqueName(this));
		}
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		Node.Accept(new SSAConverter(this));
		if(!Node.IsTopLevelDefineFunction()) {
			@Var String FuncName = Node.GetUniqueName(this);
			this.Source = this.InsertNewSourceBuilder();
			this.Source.AppendNewLine("static ");
			this.GenerateTypeName(Node.ReturnType());
			this.Source.Append(" ", FuncName);
			this.VisitFuncParamNode("(", Node, ")");
			this.GenerateExpression(Node.blockNode());
			this.Source.AppendLineFeed();
			this.Source = this.Source.Pop();
			this.Source.Append(FuncName);
		}
		else {
			@Var int StartIndex = this.Source.GetPosition();
			this.Source.AppendNewLine("static ");
			this.GenerateTypeName(Node.ReturnType());
			this.Source.Append(" ", Node.GetSignature());
			this.VisitFuncParamNode("(", Node, ")");
			@Var String Prototype = this.Source.CopyString(StartIndex, this.Source.GetPosition());
			this.GenerateExpression(Node.blockNode());
			this.Source.AppendLineFeed();

			this.Header.Append(Prototype);
			this.Header.Append(this.SemiColon);
			@Var BFuncType FuncType = Node.GetFuncType();
			if(Node.IsExport()) {
				this.GenerateExportFunction(Node);
			}
			if(this.IsMethod(Node.FuncName(), FuncType)) {
				this.Header.AppendNewLine("#define _" + this.NameMethod(FuncType.GetRecvType(), Node.FuncName()));
			}
		}
	}

	private void GenerateExportFunction(BunFunctionNode Node) {
		this.Source.AppendNewLine();
		if(Node.FuncName().equals("main")) {
			this.Source.Append("int");
		}
		else {
			this.GenerateTypeName(Node.ReturnType());
		}
		this.Source.Append(" ", Node.FuncName());
		this.GenerateListNode("(", Node.ParamNode(), ")");
		this.Source.OpenIndent(" {");
		if(!Node.ReturnType().IsVoidType()) {
			this.Source.AppendNewLine("return ", Node.GetSignature());
		}
		else {
			this.Source.AppendNewLine(Node.GetSignature());
		}
		this.GenerateWrapperCall("(", Node, ")");
		this.Source.Append(this.SemiColon);
		if(Node.FuncName().equals("main")) {
			this.Source.AppendNewLine("return 0;");
		}
		this.Source.CloseIndent("}");
	}

	@Override public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		this.Source.Append("LibZen_Is(");
		this.GenerateExpression(Node.AST[BunInstanceOfNode._Left]);
		this.Source.Append(this.Camma);
		this.Source.AppendInt(Node.TargetType().TypeId);
		this.Source.Append(")");
	}

	private void GenerateCField(String CType, String FieldName) {
		this.Source.AppendNewLine(CType, " ");
		this.Source.Append(FieldName, this.SemiColon);
	}

	private void GenerateField(BType DeclType, String FieldName) {
		this.Source.AppendNewLine();
		this.GenerateTypeName(DeclType);
		this.Source.Append(" ", FieldName, this.SemiColon);
	}

	private void GenerateFields(BClassType ClassType, BType ThisType) {
		@Var BType SuperType = ThisType.GetSuperType();
		if(!SuperType.Equals(BClassType._ObjectType)) {
			this.GenerateFields(ClassType, SuperType);
		}
		@Var int i = 0;
		this.GenerateCField("int", "_classId" + ThisType.TypeId);
		this.GenerateCField("int", "_delta" + ThisType.TypeId);
		while (i < ClassType.GetFieldSize()) {
			@Var BClassField ClassField = ClassType.GetFieldAt(i);
			if(ClassField.ClassType == ThisType) {
				this.GenerateField(ClassField.FieldType, ClassField.FieldName);
			}
			i = i + 1;
		}
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		this.Source.AppendNewLine("struct ", this.NameClass(Node.ClassType));
		this.Source.OpenIndent(" {");
		this.GenerateFields(Node.ClassType, Node.ClassType);
		this.GenerateCField("int", "_nextId");
		this.Source.CloseIndent("}");

		this.Source.AppendNewLine("static void _Init", this.NameClass(Node.ClassType), "(");
		this.GenerateTypeName(Node.ClassType);
		this.Source.OpenIndent(" o) {");
		@Var BType SuperType = Node.ClassType.GetSuperType();
		if(!SuperType.Equals(BClassType._ObjectType)) {
			this.Source.AppendNewLine("_Init", this.NameClass(SuperType), "((");
			this.GenerateTypeName(SuperType);
			this.Source.Append(")o);");
		}
		this.Source.AppendNewLine("o->_classId" + Node.ClassType.TypeId, " = " + Node.ClassType.TypeId, this.SemiColon);
		this.Source.AppendNewLine("o->_delta" + Node.ClassType.TypeId, " = sizeof(struct " + this.NameClass(Node.ClassType)+ ") - ");
		if(SuperType.Equals(BClassType._ObjectType)) {
			this.Source.Append("sizeof(int);");
		}
		else {
			this.Source.Append("sizeof(struct " + this.NameClass(SuperType) + ");");
		}
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.Source.AppendNewLine("o->", FieldNode.GetGivenName(), " = ");
			if(FieldNode.DeclType().IsFuncType()) {
				this.Source.Append("NULL");
			}
			else {
				this.GenerateExpression(FieldNode.InitValueNode());
			}
			this.Source.Append(this.SemiColon);
			i = i + 1;
		}

		i = 0;
		while (i < Node.ClassType.GetFieldSize()) {
			@Var BClassField ClassField = Node.ClassType.GetFieldAt(i);
			if(ClassField.FieldType.IsFuncType()) {
				this.Source.AppendLineFeed();
				this.Source.Append("#ifdef ", this.NameMethod(Node.ClassType, ClassField.FieldName));
				this.Source.AppendNewLine("o->", ClassField.FieldName, " = ");
				this.Source.Append(BFunc._StringfySignature(ClassField.FieldName, ClassField.FieldType.GetParamSize()-1, Node.ClassType));
				this.Source.AppendLineFeed();
				this.Source.Append("#endif");
			}
			i = i + 1;
		}

		this.Source.AppendNewLine("o->_nextId = 0;");
		this.Source.CloseIndent("}");

		this.Source.AppendNewLine("static ");
		this.GenerateTypeName(Node.ClassType);
		this.Source.Append(" _New", this.NameClass(Node.ClassType));
		this.Source.OpenIndent("(void) {");
		this.Source.AppendNewLine();
		this.GenerateTypeName(Node.ClassType);
		this.Source.Append("o = LibZen_Malloc(sizeof(struct ", this.NameClass(Node.ClassType), "));");
		this.Source.AppendNewLine("_Init", this.NameClass(Node.ClassType), "(o);");
		this.Source.AppendNewLine("return o;");
		this.Source.CloseIndent("}");
	}

	@Override public void VisitLocalDefinedNode(LocalDefinedNode Node) {
		if(!(Node instanceof PHINode)) {
			this.VisitUndefinedNode(Node);
			return;
		}
		PHINode phi = (PHINode) Node;
		this.Source.Append(this.NameLocalVariable(Node.GetGamma(), phi.GetName())  + phi.GetVarIndex(), " = ");
		this.Source.Append("phi(");
		@Var int i = 0;
		while(i < phi.Args.size()) {
			AstNode Arg = CommonArray.GetIndex(phi.Args, i);
			// Arg is instanceof ZLetVarNode or ZVarblockNode or ZSetNameNode or PHINode
			if(i != 0) {
				this.Source.Append(", ");
			}
			this.Source.Append(phi.VariableName);
			this.Source.Append("" + NodeLib.GetVarIndex(Arg));
			i = i + 1;
		}
		this.Source.Append(")");
	}

}
