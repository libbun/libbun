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

//ifdef  JAVA
package libbun.encode.obsolete;
import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.statement.BunBreakNode;
import libbun.parser.common.BToken;
import libbun.type.BClassField;
import libbun.type.BClassType;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Var;


//Zen Generator should be written in each language.

public class PerlGenerator extends OldSourceGenerator {

	public PerlGenerator() {
		super("pl", "Perl-5.0 or later");
		this.IsDynamicLanguage = true;
		this.TrueLiteral  = "1";
		this.FalseLiteral = "0";
		this.NullLiteral = "undef";
		this.LineComment = "##";

	}

	protected String GetBinaryOperator(BType Type, BToken Token) {
		if(Type.IsStringType()) {
			if(Token.EqualsText('+')) {
				return ".";
			}
		}
		return Token.GetText();
	}


	//	@Override public void VisitArrayLiteralNode(ZArrayLiteralNode Node) {
	//		this.CurrentBuilder.Append("{");
	//		for (int i = 0; i < LibZ.ListSize(Node.NodeList); i++) {
	//			if(i != 0) {
	//				this.CurrentBuilder.Append(", ");
	//			}
	//			Node.NodeList.get(i).Accept(this);
	//		}
	//		this.CurrentBuilder.Append("}");
	//	}

	//	@Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
	//		this.VisitingBuilder.Append("");
	//	}

	//	@Override public void VisitFunctionNode(ZFunctionNode Node) {
	//		this.VisitingBuilder.Append("");
	//	}

	private String VariablePrefix(BType Type) {
		if(Type.IsArrayType()) {
			return "@";
		}
		if(Type.IsMapType() || Type instanceof BClassType) {
			return "%";
		}
		return "$";
	}

	@Override public void VisitGetNameNode(GetNameNode Node) {
		this.Source.Append(this.VariablePrefix(Node.Type), this.NameLocalVariable(Node.GetGamma(), Node.GetUniqueName(this)));
	}

	@Override public void VisitAssignNode(AssignNode Node) {
		//		this.Source.Append(this.VariablePrefix(Node.GetAstType(SetNameNode._Expr)));
		//		this.GenerateExpression(Node.LeftNode());
		//		this.Source.Append(" = ");
		//		this.GenerateExpression(Node.ExprNode());
	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append("->{\'", Node.GetName(), "\'} = ");
	}

	//	@Override public void VisitSetFieldNode(SetFieldNode Node) {
	//		this.GenerateExpression(Node.RecvNode());
	//		this.Source.Append("->{\'", Node.GetName(), "\'}");
	//		this.GenerateExpression(Node.ExprNode());
	//	}

	@Override
	protected void VisitVarDeclNode(BunLetVarNode Node) {
		this.Source.Append("my ", this.VariablePrefix(Node.DeclType().GetRealType()));
		this.Source.Append(Node.GetUniqueName(this), " = ");
		this.GenerateExpression(Node.InitValueNode());
		this.Source.Append(this.SemiColon);
	}

	@Override public void VisitLetNode(BunLetVarNode Node) {
		this.Source.Append(this.VariablePrefix(Node.DeclType().GetRealType()), Node.GetUniqueName(this), " = ");
		this.GenerateExpression(Node.InitValueNode());
	}

	//	@Override public void VisitIfNode(ZIfNode Node) {
	//		this.CurrentBuilder.Append("if(");
	//		Node.AST[ZIfNode.Cond].Accept(this);
	//		this.CurrentBuilder.Append(")");
	//		this.VisitIndentBlock("{", Node.AST[ZIfNode.Then], "}");
	//		if(Node.AST[ZIfNode.Else] != null) {
	//			this.CurrentBuilder.Append("else");
	//			this.VisitIndentBlock("{", Node.AST[ZIfNode.Else], "}");
	//		}
	//	}
	//
	//	@Override public void VisitWhileNode(ZWhileNode Node) {
	//		this.CurrentBuilder.Append("while(");
	//		Node.AST[ZIfNode.Cond].Accept(this);
	//		this.CurrentBuilder.Append(")");
	//		this.VisitIndentBlock("{", Node.BodyNode, "}");
	//	}


	@Override public void VisitBreakNode(BunBreakNode Node) {
		this.Source.Append("last");
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		this.Source.Append("my ", this.VariablePrefix(Node.Type));
		this.Source.Append(Node.GetUniqueName(this), " = shift");
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		this.Source.Append("sub");
		if(Node.FuncName() != null) {
			this.Source.AppendWhiteSpace();
			this.Source.Append(Node.FuncName());
		}
		this.Source.OpenIndent(" {");
		//		if(Node.HasNextVarNode()) { this.VisitVarDeclNode(Node.NextVarNode()); }
		this.Source.Append(this.SemiColon);
		@Var BNode BlockNode = Node.BlockNode();
		if(BlockNode instanceof BunBlockNode) {
			this.GenerateStmtListNode((BunBlockNode)BlockNode);
		}
		this.Source.Append(this.SemiColon);
		this.Source.CloseIndent("}");
		//this.CurrentBuilder.AppendNewLine();
	}

	//	private void GenerateCField(String CType, String FieldName) {
	//		this.CurrentBuilder.AppendLineFeed();
	//		this.CurrentBuilder.AppendNewLine();
	//		this.CurrentBuilder.Append(CType);
	//		this.CurrentBuilder.AppendWhiteSpace();
	//		this.CurrentBuilder.Append(FieldName);
	//		this.CurrentBuilder.Append(this.SemiColon);
	//	}
	//
	//	private void GenerateField(ZType DeclType, String FieldName) {
	//		this.CurrentBuilder.AppendNewLine();
	//		this.GenerateTypeName(DeclType);
	//		this.CurrentBuilder.AppendWhiteSpace();
	//		this.CurrentBuilder.Append(FieldName);
	//		this.CurrentBuilder.Append(this.SemiColon);
	//	}


	private String ClassKey(BType ClassType) {
		return LibBunSystem._QuoteString(this.NameClass(ClassType));
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		this.Source.Append("sub _Init", this.NameClass(Node.ClassType));
		this.Source.OpenIndent(" {");
		this.Source.AppendNewLine();
		this.Source.Append("%o = shift", this.SemiColon);

		@Var BType SuperType = Node.ClassType.GetSuperType();
		if(!SuperType.Equals(BClassType._ObjectType)) {
			this.Source.AppendNewLine();
			this.Source.Append("_Init" + this.NameClass(SuperType) + "(%o);");
		}
		this.Source.AppendNewLine();
		this.Source.Append("$o{", this.ClassKey(Node.ClassType), "} = ");
		this.Source.AppendInt(Node.ClassType.TypeId);
		this.Source.Append(this.SemiColon);
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.Source.AppendNewLine();
			this.Source.Append("$o{", LibBunSystem._QuoteString(FieldNode.GetGivenName()), "} = ");
			this.GenerateExpression(FieldNode.InitValueNode());
			this.Source.Append(this.SemiColon);
			i = i + 1;
		}

		i = 0;
		while (i < Node.ClassType.GetFieldSize()) {
			@Var BClassField ClassField = Node.ClassType.GetFieldAt(i);
			if(ClassField.FieldType.IsFuncType()) {
				this.Source.AppendNewLine();
				this.Source.Append("if (defined $", this.NameMethod(Node.ClassType, ClassField.FieldName), ")");
				this.Source.OpenIndent(" {");
				this.Source.AppendNewLine();
				this.Source.Append("$o{", LibBunSystem._QuoteString(ClassField.FieldName), "} = $");
				this.Source.Append(this.NameMethod(Node.ClassType, ClassField.FieldName), this.SemiColon);
				this.Source.CloseIndent("}");
			}
			i = i + 1;
		}
		this.Source.AppendNewLine();
		this.Source.CloseIndent("}");
		this.Source.AppendLineFeed();

		this.Source.Append("sub _New", this.NameClass(Node.ClassType));
		this.Source.OpenIndent(" {");
		this.Source.AppendNewLine("%o = {}", this.SemiColon);
		this.Source.AppendNewLine("_Init" + this.NameClass(Node.ClassType) + "(%o);");
		this.Source.AppendNewLine("return %o;");
		this.Source.CloseIndent("}");
	}
}