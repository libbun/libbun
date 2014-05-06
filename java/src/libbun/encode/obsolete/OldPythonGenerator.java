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


package libbun.encode.obsolete;

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.error.TypeErrorNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.unary.BunCastNode;
import libbun.parser.classic.LibBunLogger;
import libbun.type.BClassField;
import libbun.type.BClassType;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;
import libbun.util.ZenMethod;

//Zen Generator should be written in each language.

public class OldPythonGenerator extends OldSourceGenerator {

	@BField boolean HasMainFunction = false;

	public OldPythonGenerator() {
		super("py", "Python-2.7.1");
		this.LoadInlineLibrary("inline.py", "##");
		this.LineComment = "#"; // if not, set null
		this.BeginComment = null; //"'''";
		this.EndComment = null; //"'''";
		this.Camma = ", ";
		this.SemiColon = "";
		this.StringLiteralPrefix = "";

		this.TrueLiteral = "True";
		this.FalseLiteral = "False";
		this.NullLiteral = "None";

		this.AndOperator = "and";
		this.OrOperator = "or";
		this.NotOperator = "not ";

		this.TopType = "object";
		this.SetNativeType(BType.BooleanType, "bool");
		this.SetNativeType(BType.IntType, "int");
		this.SetNativeType(BType.FloatType, "float");
		this.SetNativeType(BType.StringType, "str");

		this.Header.Append("#! /usr/bin/env python");
		this.Header.AppendNewLine("# -*- coding: utf-8 -*-");
		//this.Header.AppendNewLine("def zstr(s) : return str(s) if s != None else \'null\'");
		this.Source.AppendNewLine("## end of header", this.LineFeed);

	}

	@Override protected void GenerateImportLibrary(String LibName) {
		this.Header.AppendNewLine("import ", LibName);
	}

	@Override @ZenMethod protected void Finish(String FileName) {
		if(this.HasMainFunction) {
			this.Source.AppendNewLine("if __name__ == \"__main__\":\n\tmain()\n");
		}
	}



	@Override
	public void GenerateStmtListNode(BunBlockNode BlockNode) {
		@Var int i = 0;
		while (i < BlockNode.GetListSize()) {
			BNode SubNode = BlockNode.GetListAt(i);
			this.GenerateStatement(SubNode);
			i = i + 1;
		}
		if (i == 0) {
			this.Source.AppendNewLine("pass");
		}
	}

	@Override public void VisitBlockNode(BunBlockNode Node) {
		this.Source.OpenIndent(":");
		this.GenerateStmtListNode(Node);
		this.Source.CloseIndent("");
	}

	@Override protected void VisitVarDeclNode(BunLetVarNode Node) {
		this.Source.AppendNewLine(Node.GetUniqueName(this), " = ");
		this.GenerateExpression(Node.InitValueNode());
	}

	@Override public void VisitVarBlockNode(BunVarBlockNode Node) {
		this.VisitVarDeclNode(Node.VarDeclNode());
		this.GenerateStmtListNode(Node);
	}

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		this.Source.Append(this.NameClass(Node.Type));
		this.GenerateListNode("(", Node, ")");
	}

	@Override public void VisitCastNode(BunCastNode Node) {
		this.GenerateExpression(Node.ExprNode());
	}

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		@Var BType RecvType = Node.GetAstType(GetIndexNode._Recv);
		if(RecvType.IsMapType()) {
			this.ImportLibrary("@mapget");
			this.GenerateExpression("libbun_mapget(", Node.RecvNode(), ", ");
			this.GenerateExpression("", Node.IndexNode(), ")");
		}
		else {
			this.GenerateExpression(Node.RecvNode());
			this.GenerateExpression("[", Node.IndexNode(), "]");
		}
	}

	@Override public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		this.Source.Append("isinstance(");
		this.GenerateExpression(Node.LeftNode());
		if(Node.TargetType() instanceof BClassType) {
			this.Source.Append(this.Camma, this.NameClass(Node.TargetType()), ")");
		}
		else {
			this.Source.Append(this.Camma);
			this.GenerateTypeName(Node.TargetType());
			this.Source.Append(")");
		}
	}

	@Override public void VisitIfNode(BunIfNode Node) {
		this.Source.Append("if ");
		this.GenerateExpression(Node.CondNode());
		this.GenerateExpression(Node.ThenNode());
		if (Node.HasElseNode()) {
			BNode ElseNode = Node.ElseNode();
			if(ElseNode instanceof BunIfNode) {
				this.Source.AppendNewLine("el");
			}
			else {
				this.Source.AppendNewLine("else");
			}
			this.GenerateExpression(Node.ElseNode());
		}
	}

	@Override public void VisitLetNode(BunLetVarNode Node) {
		if(this.ReadableCode || !Node.IsConstValue()) {
			this.Source.Append(Node.GetUniqueName(this));
			this.Source.Append(" = ");
			this.GenerateExpression(Node.InitValueNode());
		}
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		this.Source.Append(Node.GetUniqueName(this));
	}

	/**
	>>> def f(x):
		...   def g(y):
		...     return x + y
		...   return g
		...
		>>> f(1)(3)
		4
	 **/

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		if(!Node.IsTopLevelDefineFunction()) {
			@Var String FuncName = Node.GetUniqueName(this);
			this.Source = this.InsertNewSourceBuilder();
			this.Source.Append("def ");
			this.Source.Append(FuncName);
			this.VisitFuncParamNode("(", Node, ")");
			this.GenerateExpression(Node.BlockNode());
			this.Source.AppendLineFeed();
			this.Source.AppendLineFeed();
			this.Source = this.Source.Pop();
			this.Source.Append(FuncName);
		}
		else {
			@Var BFuncType FuncType = Node.GetFuncType();
			this.Source.Append("def ");
			this.Source.Append(Node.GetSignature());
			this.VisitFuncParamNode("(", Node, ")");
			this.GenerateExpression(Node.BlockNode());
			this.Source.AppendLineFeed();
			if(Node.IsExport) {
				this.Source.Append(Node.FuncName(), " = ", FuncType.StringfySignature(Node.FuncName()));
				this.Source.AppendLineFeed();
				if(Node.FuncName().equals("main")) {
					this.HasMainFunction = true;
				}
			}
			if(this.IsMethod(Node.FuncName(), FuncType)) {
				this.Source.Append(this.NameMethod(FuncType.GetRecvType(), Node.FuncName()));
				this.Source.Append(" = ", FuncType.StringfySignature(Node.FuncName()));
				this.Source.AppendLineFeed();
			}
		}
	}

	private void GenerateMethodVariables(BunClassNode Node) {
		@Var int i = 0;
		while (i < Node.ClassType.GetFieldSize()) {
			@Var BClassField ClassField = Node.ClassType.GetFieldAt(i);
			if(ClassField.FieldType.IsFuncType()) {
				this.Source.AppendNewLine();
				this.Source.Append(this.NameMethod(Node.ClassType, ClassField.FieldName));
				this.Source.Append(" = ", this.NullLiteral);
			}
			i = i + 1;
		}
		this.Source.AppendNewLine();
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		@Var BType SuperType = Node.ClassType.GetSuperType();
		this.GenerateMethodVariables(Node);
		this.Source.Append("class ");
		this.Source.Append(this.NameClass(Node.ClassType));
		if(!SuperType.Equals(BClassType._ObjectType)) {
			this.Source.Append("(");
			this.Source.Append(this.NameClass(SuperType));
			this.Source.Append(")");
		}
		this.Source.OpenIndent(":");
		this.Source.AppendNewLine("def __init__(self)");
		this.Source.OpenIndent(":");
		if(!Node.SuperType().Equals(BClassType._ObjectType)) {
			this.Source.AppendNewLine();
			this.Source.Append(this.NameClass(SuperType));
			this.Source.Append(".__init__(self)");
		}
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			if(!FieldNode.DeclType().IsFuncType()) {
				this.Source.AppendNewLine();
				this.Source.Append("self." + FieldNode.GetGivenName() + " = ");
				this.GenerateExpression(FieldNode.InitValueNode());
			}
			this.Source.Append(this.SemiColon);
			i = i + 1;
		}

		i = 0;
		while (i < Node.ClassType.GetFieldSize()) {
			@Var BClassField ClassField = Node.ClassType.GetFieldAt(i);
			if(ClassField.FieldType.IsFuncType()) {
				this.Source.AppendNewLine();
				this.Source.Append("self." + ClassField.FieldName);
				this.Source.Append(" = _" + this.NameClass(Node.ClassType) + "_" + ClassField.FieldName);
			}
			i = i + 1;
		}
		this.Source.CloseIndent(null);
		this.Source.CloseIndent(null);
		this.Source.AppendLineFeed();
	}

	@Override public void VisitErrorNode(ErrorNode Node) {
		if(Node instanceof TypeErrorNode) {
			@Var TypeErrorNode ErrorNode = (TypeErrorNode)Node;
			this.GenerateExpression(ErrorNode.ErrorNode);
		}
		else {
			@Var String Message = LibBunLogger._LogError(Node.SourceToken, Node.ErrorMessage);
			this.Source.AppendWhiteSpace();
			this.Source.Append("LibZen.ThrowError(");
			this.Source.Append(LibBunSystem._QuoteString(Message));
			this.Source.Append(")");
		}
	}

	@Override public void VisitThrowNode(BunThrowNode Node) {
		this.Source.Append("raise ");
		this.GenerateExpression(Node.ExprNode());
	}

	@Override public void VisitTryNode(BunTryNode Node) {
		this.Source.Append("try");
		this.GenerateExpression(Node.TryBlockNode());
		if(Node.HasCatchBlockNode()) {
			@Var String VarName = this.NameUniqueSymbol("e");
			this.Source.AppendNewLine("except Exception as ", VarName);
			this.Source.OpenIndent(":");
			this.Source.AppendNewLine(Node.ExceptionName(), " = ", VarName);
			this.GenerateStmtListNode(Node.CatchBlockNode());
			this.Source.CloseIndent("");
		}
		if(Node.HasFinallyBlockNode()) {
			this.Source.AppendNewLine("finally");
			this.GenerateExpression(Node.FinallyBlockNode());
		}
	}

}