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


package libbun.encode.haskell;

import java.util.ArrayList;

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.ComparatorNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.encode.LibBunSourceBuilder;
import libbun.encode.obsolete.OldSourceGenerator;
import libbun.parser.classic.BNodeUtils;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;


public class HaskellSourceGenerator extends OldSourceGenerator {
	@BField public ArrayList <String> Variables;
	private static int IndentLevel = 0;

	public HaskellSourceGenerator() {
		super("hs", "Haskell-7.6.3");
		this.LineFeed = "\n";
		this.Tab = "\t";
		this.LineComment = "#"; // if not, set null
		this.BeginComment = "{-";
		this.EndComment = "-}";
		this.Camma = ",";
		this.SemiColon = "";

		this.TrueLiteral = "True";
		this.FalseLiteral = "False";
		this.NullLiteral = "None";

		this.AndOperator = "&&";
		this.OrOperator = "||";
		this.NotOperator = "not ";

		this.TopType = "object";
		this.SetNativeType(BType.BooleanType, "Bool");
		this.SetNativeType(BType.IntType, "Int");
		this.SetNativeType(BType.FloatType, "Float");
		this.SetNativeType(BType.StringType, "String");

		this.ImportLibrary("Data.IORef");
	}

	@Override protected void GenerateImportLibrary(String LibName) {
		this.Header.AppendNewLine("import ", LibName, this.SemiColon);
	}

	private void Indent(LibBunSourceBuilder builder) {
		IndentLevel = IndentLevel + 1;
		builder.OpenIndent();
	}

	private void UnIndent(LibBunSourceBuilder builder) {
		IndentLevel = IndentLevel - 1;
		builder.CloseIndent();
	}

	@Override
	public void VisitBlockNode(BunBlockNode Node) {
		@Var int count = 0;

		this.Indent(this.Source);
		this.Source.AppendNewLine();
		this.Source.Append("do ");

		@Var int limit = Node.GetListSize();
		for (@Var int i = 0; i < limit; i++) {
			BNode SubNode = Node.GetListAt(i);
			this.Source.AppendLineFeed();
			this.Source.AppendNewLine();

			// Last Statement in function definition
			if (IndentLevel == 1 && i == limit - 1) {
				this.Source.Append("return (");
			}

			this.GenerateExpression(SubNode);
			this.Source.Append(this.SemiColon);

			if (IndentLevel == 1 && i == limit - 1) {
				this.Source.Append(")");
			}

			count = count + 1;
		}
		if (count == 0) {
			this.Source.Append("return ()");
		}

		this.UnIndent(this.Source);
		this.Source.AppendLineFeed();
	}

	@Override
	public void VisitCastNode(BunCastNode Node) {
	}

	@Override
	public void VisitThrowNode(BunThrowNode Node) {
		this.Source.Append("raise ");
		this.GenerateExpression(Node.ExprNode());
	}

	@Override
	public void VisitTryNode(BunTryNode Node) {
		// See: http://d.hatena.ne.jp/kazu-yamamoto/20090819/1250660658
		this.GenerateExpression(Node.TryBlockNode());
		this.Source.Append(" `catch` ");
		if (Node.CatchBlockNode() != null) {
			this.GenerateExpression(Node.CatchBlockNode());
		}
		if (Node.FinallyBlockNode() != null) {
			this.GenerateExpression(Node.FinallyBlockNode());
		}
	}


	@Override
	protected void VisitVarDeclNode(BunLetVarNode Node) {
		this.Source.Append(Node.GetGivenName() + " <- readIORef ");
		this.Source.Append(Node.GetGivenName() + "_ref");
		this.GenerateExpression(Node.InitValueNode());
		this.Source.AppendLineFeed();
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		this.Source.Append(Node.GetGivenName());
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		this.Variables = new ArrayList<String>();
		if(Node.FuncName().equals("main")){
			this.Source.Append("main");
		}else{
			this.Source.Append(Node.GetSignature());
		}
		this.VisitFuncParamNode(" ", Node, " ");

		this.Source.Append(" = do");
		this.Source.AppendLineFeed();

		this.Indent(this.Source);

		for (int i = 0; i < Node.GetListSize(); i++) {
			BunLetVarNode Param = Node.GetParamNode(i);
			this.Variables.add(Param.GetGivenName());

			this.Source.AppendNewLine();
			this.Source.Append(Param.GetGivenName()
					+ "_ref <- newIORef "
					+ Param.GetGivenName());
			this.Source.AppendLineFeed();
		}

		for (int i = 0; i < Node.GetListSize(); i++) {
			BunLetVarNode node1 = Node.GetParamNode(i);

			this.Source.AppendNewLine();
			this.Source.Append(node1.GetGivenName()
					+ " <- readIORef "
					+ node1.GetGivenName() + "_ref");
			this.Source.AppendLineFeed();
		}
		this.UnIndent(this.Source);

		BunReturnNode ReturnNode = BNodeUtils._CheckIfSingleReturnNode(Node);
		if(ReturnNode != null && ReturnNode.HasReturnExpr()) {
			this.Indent(this.Source);

			String Indentation = LibBunSystem._JoinStrings("\t", IndentLevel);
			this.Source.Append(Indentation);
			this.Source.Append("return ");
			this.GenerateExpression(ReturnNode.ExprNode());
			this.UnIndent(this.Source);
		} else {
			this.GenerateExpression(Node.BlockNode());
		}
	}

	@Override
	public void VisitGetNameNode(GetNameNode Node) {
		this.Source.Append(Node.GetUniqueName(this));
	}



	@Override
	public void VisitAssignNode(AssignNode Node) {
		// FIXME:
		//		this.Source.Append("writeIORef ");
		//		this.Source.Append(Node.NameNode().GetUniqueName(this) + "_ref ");
		//		this.GenerateExpression(Node.ExprNode());
		//		this.Source.AppendLineFeed();
		//
		//		this.Source.AppendNewLine();
		//		this.Source.Append(Node.NameNode().GetUniqueName(this));
		//		this.Source.Append(" <- readIORef ");
		//		this.Source.Append(Node.NameNode().GetUniqueName(this) + "_ref");
		//		this.Source.AppendLineFeed();
	}

	@Override
	public void VisitReturnNode(BunReturnNode Node) {
		if (Node.HasReturnExpr()) {
			this.GenerateExpression(Node.ExprNode());
		}
	}

	private String ZenOpToHaskellOp(String OpCode) {
		if(OpCode.equals("/")) {
			return "`div`";
		}
		if(OpCode.equals("%")) {
			return "`mod`";
		}
		if(OpCode.equals("!=")) {
			return "/=";
		}
		return OpCode;
	}

	@Override
	public void VisitBinaryNode(BinaryOperatorNode Node) {
		String Op = this.ZenOpToHaskellOp(Node.SourceToken.GetText());

		this.Source.Append("(");
		Node.LeftNode().Accept(this);
		this.Source.Append(" " + Op + " ");
		Node.RightNode().Accept(this);
		this.Source.Append(")");
	}

	@Override public void VisitComparatorNode(ComparatorNode Node) {
		String Op = this.ZenOpToHaskellOp(Node.SourceToken.GetText());

		this.Source.Append("(");
		Node.LeftNode().Accept(this);
		this.Source.Append(" " + Op + " ");
		Node.RightNode().Accept(this);
		this.Source.Append(")");
	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		this.Source.Append("let __loop = do");
		this.Source.AppendLineFeed();

		this.Indent(this.Source);

		for (String var : this.Variables) {
			this.Source.AppendNewLine();
			this.Source.Append(var + " <- ");
			this.Source.Append("readIORef " + var + "_ref");
			this.Source.AppendLineFeed();
		}

		this.Source.AppendNewLine();
		this.Source.Append("if ");
		Node.CondNode().Accept(this);
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine();
		this.Source.Append("then");
		this.Source.AppendLineFeed();

		// XXX Is this correct node type ?
		BNode LoopNode = new GetNameNode(Node, null, "__loop");
		Node.BlockNode().SetNode(BNode._AppendIndex, LoopNode);
		Node.BlockNode().Accept(this);

		this.Source.AppendNewLine();
		this.Source.Append("else");

		this.Indent(this.Source);
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine();
		this.Source.Append("return ()");
		this.Source.AppendLineFeed();
		this.UnIndent(this.Source);

		this.UnIndent(this.Source);

		this.Source.AppendNewLine();
		this.Source.Append("__loop");
		this.Source.AppendLineFeed();

		for (String var : this.Variables) {
			this.Source.AppendNewLine();
			this.Source.Append(var + " <- ");
			this.Source.Append("readIORef " + var + "_ref");
			this.Source.AppendLineFeed();
		}
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		if(Node.ParentNode instanceof BunBlockNode){
			this.GenerateExpression(Node.FunctorNode());
			this.GenerateListNode(" ", Node, " ", " ");
		}else{
			this.ImportLibrary("System.IO.Unsafe");
			this.Source.Append("(unsafePerformIO (");
			this.GenerateExpression(Node.FunctorNode());
			this.GenerateListNode(" ", Node, " ", " ");
			this.Source.Append("))");
		}
	}
}
