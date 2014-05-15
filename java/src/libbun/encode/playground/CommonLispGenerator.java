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

package libbun.encode.playground;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.GroupNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.BunAddNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BunBitwiseAndNode;
import libbun.ast.binary.BunBitwiseOrNode;
import libbun.ast.binary.BunBitwiseXorNode;
import libbun.ast.binary.BunDivNode;
import libbun.ast.binary.BunEqualsNode;
import libbun.ast.binary.BunGreaterThanEqualsNode;
import libbun.ast.binary.BunGreaterThanNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.binary.BunLeftShiftNode;
import libbun.ast.binary.BunLessThanEqualsNode;
import libbun.ast.binary.BunLessThanNode;
import libbun.ast.binary.BunModNode;
import libbun.ast.binary.BunMulNode;
import libbun.ast.binary.BunNotEqualsNode;
import libbun.ast.binary.BunOrNode;
import libbun.ast.binary.BunRightShiftNode;
import libbun.ast.binary.BunSubNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapEntryNode;
import libbun.ast.literal.BunMapNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.sugar.StringInterpolationNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunComplementNode;
import libbun.ast.unary.BunMinusNode;
import libbun.ast.unary.BunNotNode;
import libbun.ast.unary.BunPlusNode;
import libbun.ast.unary.UnaryOperatorNode;
import libbun.encode.LibBunSourceGenerator;
import libbun.parser.classic.LibBunLangInfo;
import libbun.parser.common.BunLogger;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Var;
import libbun.util.ZenMethod;

public class CommonLispGenerator extends LibBunSourceGenerator {
	private boolean hasMain = false;
	public CommonLispGenerator() {
		super(new LibBunLangInfo("CommonLisp", "cl"));
		this.LoadInlineLibrary("inline.cl", ";;");
	}

	@Override
	protected void GenerateImportLibrary(String LibName) {
		//		this.Header.AppendNewLine("require ", LibName, ";");
	}

	@Override public void VisitNullNode(BunNullNode Node) {
		this.Source.Append("nil");
	}

	@Override public void VisitBooleanNode(BunBooleanNode Node) {
		if (Node.BooleanValue) {
			this.Source.Append("t");
		} else {
			this.Source.Append("nil");
		}
	}

	@Override public void VisitIntNode(BunIntNode Node) {
		this.Source.Append(String.valueOf(Node.IntValue));
	}

	@Override public void VisitFloatNode(BunFloatNode Node) {
		this.Source.Append(String.valueOf(Node.FloatValue));
	}

	@Override public void VisitStringNode(BunStringNode Node) {
		this.Source.AppendQuotedText(Node.StringValue);
	}

	@Override public void VisitArrayLiteralNode(BunArrayNode Node) {
		//		this.Source.Append("(let ((a (make-array 8 :adjustable T :fill-pointer 0 :initial-element 'e)))");
		this.Source.Append("(let ((a (make-array ");
		this.Source.AppendInt(Node.size());
		this.Source.Append(" :initial-element ", this.InitArrayValue(Node));
		this.Source.Append(" :adjustable T :fill-pointer 0)))");
		@Var int i = 0;
		while(i < Node.size()) {
			this.GenerateExpression(" (vector-push ", Node.get(i), " a)");
			i = i + 1;
		}
		this.Source.Append(" a)");
	}

	private String InitArrayValue(BunArrayNode Node) {
		@Var BType ParamType = Node.Type.GetParamType(0);
		if(ParamType.IsIntType()) {
			return "0";
		}
		if(ParamType.IsFloatType()) {
			return "0.0";
		}
		return "nil";
	}

	@Override public void VisitMapLiteralNode(BunMapNode Node) {
		this.Source.Append("(let ((m (make-hash-table :test #'equal)))");
		this.Source.Append("(setf");
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			@Var BunMapEntryNode Entry = Node.getMapEntryNode(i);
			this.GenerateExpression(" (gethash ", Entry.KeyNode(), " m)", Entry.ValueNode(), "");
			i = i + 1;
		}
		this.Source.Append(")");/*setf*/
		this.Source.Append(" m)");/*let*/
	}

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		// FIXME
		this.Source.Append("new ");
		this.GenerateTypeName(Node.Type);
		this.GenerateListNode("(", Node, 1, ",", ")");
	}

	@Override public void VisitGroupNode(GroupNode Node) {
		this.GenerateExpression(Node.ExprNode());
	}

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		@Var BType RecvType = Node.RecvNode().Type;
		if (RecvType.IsStringType()) {
			this.GenerateExpression("(string (aref ", Node.RecvNode(), " ", Node.IndexNode(), "))");
		}
		else if(RecvType.IsMapType()) {
			this.GenerateExpression("(gethash ", Node.IndexNode(), " ", Node.RecvNode(), ")");
		}
		else {
			this.GenerateExpression("(nth ", Node.IndexNode(), " ", Node.RecvNode(), ")");
		}
	}

	//	@Override public void VisitSetIndexNode(SetIndexNode Node) {
	//		@Var BType RecvType = Node.RecvNode().Type;
	//		if(RecvType.IsMapType()) {
	//			this.GenerateExpression("(setf (gethash ", Node.IndexNode(), " ", Node.RecvNode(), ") ", Node.ExprNode(), ")");
	//		}
	//		else {
	//			this.GenerateExpression("(setf (nth ", Node.IndexNode(), " ", Node.RecvNode(), ") ", Node.ExprNode(), ")");
	//		}
	//	}

	@Override public void VisitGetNameNode(GetNameNode Node) {
		this.Source.Append(Node.GetUniqueName(this));
	}

	@Override public void VisitAssignNode(AssignNode Node) {
		this.Source.Append("(setf ");
		this.GenerateExpression(Node.LeftNode());
		this.GenerateExpression(" ", Node.RightNode(), ")");
	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(".", Node.GetName());
	}

	//	@Override public void VisitSetFieldNode(SetFieldNode Node) {
	//		this.GenerateExpression(Node.RecvNode());
	//		this.Source.Append(".", Node.GetName(), " = ");
	//		this.GenerateExpression(Node.ExprNode());
	//	}

	@Override public void VisitMethodCallNode(MethodCallNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(".", Node.MethodName());
		this.GenerateListNode("(", Node, 2, ",", ")");
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		this.Source.Append("(");
		@Var BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		if(FuncNameNode != null) {
			this.Source.Append(FuncNameNode.GetSignature());
		}
		else {
			this.Source.Append("funcall ");
			this.GenerateExpression(Node.FunctorNode());
		}
		this.GenerateListNode(" ", Node, 1, " ", ")");
	}

	@Override public void VisitUnaryNode(UnaryOperatorNode Node) {
		this.Source.Append("(", Node.GetOperator(), " ");
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(")");
	}

	@Override public void VisitPlusNode(BunPlusNode Node) {
		this.VisitUnaryNode(Node);
	}

	@Override public void VisitMinusNode(BunMinusNode Node) {
		this.VisitUnaryNode(Node);
	}

	@Override public void VisitComplementNode(BunComplementNode Node) {
		this.GenerateExpression("(lognot ", Node.RecvNode(), ")");
	}

	@Override public void VisitNotNode(BunNotNode Node) {
		this.GenerateExpression("(not ", Node.RecvNode(), ")");
	}

	@Override public void VisitCastNode(BunCastNode Node) {
		//		if(Node.Type.IsVoidType()) {
		this.GenerateExpression(Node.ExprNode());
		//		}
		//		else {
		//			this.Source.Append("(");
		//			this.GenerateTypeName(Node.Type);
		//			this.Source.Append(")");
		//			this.GenerateExpression(Node.ExprNode());
		//		}
	}

	public void GenerateBinaryNode(String Op, BinaryOperatorNode Node, String Extra) {
		this.Source.Append("(", Op, " ");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" ");
		this.GenerateExpression(Node.RightNode());
		if(Extra != null) {
			this.Source.Append(Extra);
		}
		this.Source.Append(")");
	}

	@Override public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		this.Source.Append("(eq ");
		this.Source.Append("(type-of ");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" ) '");
		this.GenerateTypeName(Node.TargetType());
		this.Source.Append(")");
	}

	@Override public void VisitAddNode(BunAddNode Node) {
		if(Node.Type.IsStringType()) {
			this.VisitStringInterpolationNode(StringInterpolationNode._ToStringInterpolationNode(Node));
		}
		else {
			this.GenerateBinaryNode(Node.GetOperator(), Node, null);
		}
	}

	@Override public void VisitSubNode(BunSubNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitMulNode(BunMulNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitDivNode(BunDivNode Node) {
		if(Node.Type.IsIntType()) {
			this.Source.Append("(floor (/ ");
			this.GenerateExpression(Node.LeftNode());
			this.Source.Append(" ");
			this.GenerateExpression(Node.RightNode());
			this.Source.Append("))");
		}
		else {
			this.Source.Append("(/");
			this.GenerateExpression(Node.LeftNode());
			this.Source.Append(" ");
			this.GenerateExpression(Node.RightNode());
			this.Source.Append(")");
		}
	}

	@Override public void VisitModNode(BunModNode Node) {
		this.GenerateBinaryNode("mod", Node, null);
	}

	@Override public void VisitLeftShiftNode(BunLeftShiftNode Node) {
		this.GenerateBinaryNode("ash", Node, null);
	}

	@Override public void VisitRightShiftNode(BunRightShiftNode Node) {
		this.GenerateBinaryNode("ash (-", Node, ")");
	}

	@Override public void VisitBitwiseAndNode(BunBitwiseAndNode Node) {
		this.GenerateBinaryNode("logand", Node, null);
	}

	@Override public void VisitBitwiseOrNode(BunBitwiseOrNode Node) {
		this.GenerateBinaryNode("logor", Node, null);
	}

	@Override public void VisitBitwiseXorNode(BunBitwiseXorNode Node) {
		this.GenerateBinaryNode("logxor", Node, null);
	}

	@Override public void VisitEqualsNode(BunEqualsNode Node) {
		this.GenerateBinaryNode("equal", Node, null);
	}

	@Override public void VisitNotEqualsNode(BunNotEqualsNode Node) {
		this.GenerateBinaryNode("not (equal", Node, ")");
	}

	@Override public void VisitLessThanNode(BunLessThanNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitLessThanEqualsNode(BunLessThanEqualsNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitGreaterThanNode(BunGreaterThanNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode Node) {
		this.GenerateBinaryNode(Node.GetOperator(), Node, null);
	}

	@Override public void VisitAndNode(BunAndNode Node) {
		this.GenerateBinaryNode("and", Node, null);
	}

	@Override public void VisitOrNode(BunOrNode Node) {
		this.GenerateBinaryNode("or", Node, null);
	}


	@Override protected void GenerateStatementEnd(AstNode Node) {
	}

	protected void GenerateStmtListNode(LegacyBlockNode blockNode) {
		@Var int Size = blockNode.GetListSize();
		if(Size == 0) {
			this.Source.Append("()");
		}
		else if(Size == 1) {
			this.GenerateStatement(blockNode.GetListAt(0));
		}
		else {
			this.Source.OpenIndent("(progn");
			@Var int i = 0;
			while (i < blockNode.GetListSize()) {
				@Var AstNode SubNode = blockNode.GetListAt(i);
				this.GenerateStatement(SubNode);
				i = i + 1;
			}
			this.Source.CloseIndent(")");
		}
	}

	@Override public void VisitBlockNode(LegacyBlockNode Node) {
		this.GenerateStmtListNode(Node);
	}

	@Override public void VisitVarblockNode(BunVarBlockNode Node) {
		this.Source.Append("(let (");
		this.Source.Append("(", Node.VarDeclNode().GetUniqueName(this), " ");
		this.GenerateExpression(Node.VarDeclNode().InitValueNode());
		this.Source.Append("))");
		this.GenerateStmtListNode(Node);
		this.Source.Append(")");
	}

	@Override public void VisitIfNode(BunIfNode Node) {
		this.Source.Append("(if  ");
		this.GenerateExpression(Node.CondNode());
		this.Source.Append(" ");
		this.GenerateExpression(Node.ThenNode());
		this.Source.Append(" ");
		if(Node.HasElseNode()) {
			this.GenerateExpression(Node.ElseNode());
		}
		else {
			this.Source.Append("nil");
		}
		this.Source.Append(")");
	}

	@Override public void VisitWhileNode(BunWhileNode Node) {
		this.Source.Append("(loop while ");
		this.GenerateExpression(Node.CondNode());
		this.Source.AppendNewLine("do");
		if(Node.HasNextNode()) {
			Node.blockNode().appendNode(Node.NextNode());
		}
		this.GenerateExpression(Node.blockNode());
		this.Source.Append(")");
	}

	@Override public void VisitReturnNode(BunReturnNode Node) {
		@Var BunFunctionNode FuncNode = this.LookupFunctionNode(Node);
		if(FuncNode != null) {
			this.Source.Append("(return-from ", this.ClFunctionName(FuncNode), " ");
		}
		else {
			this.Source.Append("(return ");
		}
		if(Node.HasReturnExpr()) {
			this.GenerateExpression(Node.ExprNode());
		}
		else {
			this.Source.Append("nil");
		}
		this.Source.Append(")");
	}

	private BunFunctionNode LookupFunctionNode(AstNode Node) {
		while(Node != null) {
			if(Node instanceof BunFunctionNode) {
				return (BunFunctionNode)Node;
			}
			Node = Node.ParentNode;
		}
		return null;
	}

	private String ClFunctionName(BunFunctionNode FuncNode) {
		String FuncName = FuncNode.FuncName();
		if (FuncName != null) {
			if (FuncName.equals("main")) {
				return "main";
			}
			else {
				return FuncNode.GetSignature();
			}
		}
		else {
			return "nil";
		}
	}

	@Override public void VisitBreakNode(BunBreakNode Node) {
		this.Source.Append("(return)");
	}

	@Override public void VisitThrowNode(BunThrowNode Node) {
		this.Source.Append("(throw nil ");
		this.GenerateExpression(Node.ExprNode());
		this.Source.Append(")");
	}

	@Override public void VisitTryNode(BunTryNode Node) {
		this.ImportLibrary("@SoftwareFault");
		this.Source.Append("(unwind-protect ");
		this.Source.Append("(handler-case ");
		this.GenerateExpression(Node.TryblockNode());
		if(Node.HasCatchblockNode()) {
			this.ImportLibrary("@catch");
			@Var String VarName = this.NameUniqueSymbol("e");
			this.Source.AppendNewLine("(error (", VarName, ")");
			this.Source.AppendNewLine("(setf " + Node.ExceptionName() + " (libbun-catch " + VarName + "))");
			this.GenerateStmtListNode(Node.CatchblockNode());
			this.Source.AppendNewLine(")");
		}
		this.Source.Append(")");
		if(Node.HasFinallyblockNode()) {
			this.GenerateExpression(Node.FinallyblockNode());
		}
		this.Source.Append(")");
	}


	protected void GenerateTypeAnnotation(BType Type) {
		if(!Type.IsVarType()) {
			this.Source.Append(": ");
			this.GenerateTypeName(Type);
		}
	}


	@Override public void VisitLetNode(BunLetVarNode Node) {
		if(Node.IsParamNode()) {
			this.Source.Append(Node.GetUniqueName(this));
		}
		else {
			this.Source.AppendNewLine("(setf ", Node.GetUniqueName(this), "");
			this.Source.Append(" ");
			this.GenerateExpression(Node.InitValueNode());
			this.Source.Append(")");

		}
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		if(!Node.IsTopLevelDefineFunction()) {
			this.Source.Append("#'(lambda ");
			this.GenerateListNode("(", Node.ParamNode(), " ", ")");
			this.Source.Append("(block nil ");
			this.GenerateExpression(Node.blockNode());
			this.Source.Append("))");
		}
		else {
			@Var BFuncType FuncType = Node.GetFuncType();
			this.Source.Append("(defun ");
			this.Source.Append(this.ClFunctionName(Node));
			this.GenerateListNode("(", Node.ParamNode(), " ", ")");
			this.GenerateExpression(Node.blockNode());
			this.Source.Append(")");
			if(Node.IsExport()) {
				if(Node.FuncName().equals("main")) {
					this.hasMain = true;
				}
			}
			if(this.IsMethod(Node.FuncName(), FuncType)) {
				//				this.CurrentBuilder.Append(this.NameMethod(FuncType.GetRecvType(), Node.FuncName));
				//				this.CurrentBuilder.Append(" = ", FuncType.StringfySignature(Node.FuncName));
				//				this.CurrentBuilder.AppendLineFeed();
			}
		}
	}


	@Override public void VisitClassNode(BunClassNode Node) {
		this.Source.AppendNewLine("class ", Node.ClassName());
		if(Node.SuperType() != null) {
			this.Source.Append(" extends ");
			this.GenerateTypeName(Node.SuperType());
		}
		this.Source.OpenIndent(" {");
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.Source.AppendNewLine("var ", FieldNode.GetGivenName());
			this.GenerateTypeAnnotation(FieldNode.DeclType());
			this.Source.Append(" = ");
			this.GenerateExpression(FieldNode.InitValueNode());
			this.GenerateStatementEnd(FieldNode);
			i = i + 1;
		}
		this.Source.CloseIndent("}");
	}

	@Override public void VisitErrorNode(LegacyErrorNode Node) {
		@Var String Message = BunLogger._LogError(Node.SourceToken, Node.ErrorMessage);
		this.Source.Append("/*", Message, "*/");
	}

	@Override @ZenMethod protected void Finish(String FileName) {
		if(this.hasMain) {
			this.Source.AppendNewLine("(main)");
			this.Source.AppendLineFeed();
		}
	}

	@Override
	public void VisitBinaryNode(BinaryOperatorNode Node) {
		// TODO Auto-generated method stub

	}

	protected boolean VisitStringInterpolationNode(StringInterpolationNode Node) {
		@Var String Format = "";
		@Var int i = 0;
		while(i < Node.size()) {
			if(i % 2 == 0) {
				if(Node.AST[i] instanceof BunStringNode) {
					Format = Format + Node.GetStringLiteralAt(i);
				}
			}
			else {
				@Var BType Type = Node.getTypeAt(i);
				@Var String Formatter = "~a";
				if(Type.IsIntType()) {
					Formatter = "~D";
				}
				else if(Type.IsFloatType()) {
					Formatter = "~F";
				}
				Format = Format + Formatter;
			}
			i = i + 1;
		}
		this.Source.Append("(format nil \"" + Format + "\"");
		if(Node.size() > 1) {
			this.Source.Append(" ");
			i = 1;
			while(i < Node.size()) {
				if(i > 2) {
					this.Source.Append(" ");
				}
				this.GenerateExpression(Node.AST[i]);
				i = i + 2;
			}
		}
		this.Source.Append(")");
		return true;
	}
}
