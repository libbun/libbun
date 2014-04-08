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

import libbun.ast.BunBlockNode;
import libbun.ast.BNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.ComparatorNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.SetNameNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.encode.ZSourceBuilder;
import libbun.encode.ZSourceGenerator;
import libbun.parser.BNodeUtils;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.BLib;
import libbun.util.Var;


public class HaskellSourceGenerator extends ZSourceGenerator {
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
		this.HeaderBuilder.AppendNewLine("import ", LibName, this.SemiColon);
	}

	private void Indent(ZSourceBuilder builder) {
		IndentLevel = IndentLevel + 1;
		builder.Indent();
	}

	private void UnIndent(ZSourceBuilder builder) {
		IndentLevel = IndentLevel - 1;
		builder.UnIndent();
	}

	@Override
	public void VisitBlockNode(BunBlockNode Node) {
		@Var int count = 0;

		this.Indent(this.CurrentBuilder);
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("do ");

		@Var int limit = Node.GetListSize();
		for (@Var int i = 0; i < limit; i++) {
			BNode SubNode = Node.GetListAt(i);
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.AppendIndent();

			// Last Statement in function definition
			if (IndentLevel == 1 && i == limit - 1) {
				this.CurrentBuilder.Append("return (");
			}

			this.GenerateCode(null, SubNode);
			this.CurrentBuilder.Append(this.SemiColon);

			if (IndentLevel == 1 && i == limit - 1) {
				this.CurrentBuilder.Append(")");
			}

			count = count + 1;
		}
		if (count == 0) {
			this.CurrentBuilder.Append("return ()");
		}

		this.UnIndent(this.CurrentBuilder);
		this.CurrentBuilder.AppendLineFeed();
	}

	@Override
	public void VisitCastNode(BunCastNode Node) {
	}

	@Override
	public void VisitThrowNode(BunThrowNode Node) {
		this.CurrentBuilder.Append("raise ");
		this.GenerateCode(null, Node.ExprNode());
	}

	@Override
	public void VisitTryNode(BunTryNode Node) {
		// See: http://d.hatena.ne.jp/kazu-yamamoto/20090819/1250660658
		this.GenerateCode(null, Node.TryBlockNode());
		this.CurrentBuilder.Append(" `catch` ");
		if (Node.CatchBlockNode() != null) {
			this.GenerateCode(null, Node.CatchBlockNode());
		}
		if (Node.FinallyBlockNode() != null) {
			this.GenerateCode(null, Node.FinallyBlockNode());
		}
	}


	@Override
	protected void VisitVarDeclNode(BunLetVarNode Node) {
		this.CurrentBuilder.Append(Node.GetGivenName() + " <- readIORef ");
		this.CurrentBuilder.Append(Node.GetGivenName() + "_ref");
		this.GenerateCode(null, Node.InitValueNode());
		this.CurrentBuilder.AppendLineFeed();
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		this.CurrentBuilder.Append(Node.GetGivenName());
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		this.Variables = new ArrayList<String>();
		if(Node.FuncName().equals("main")){
			this.CurrentBuilder.Append("main");
		}else{
			this.CurrentBuilder.Append(Node.GetSignature());
		}
		this.VisitFuncParamNode(" ", Node, " ");

		this.CurrentBuilder.Append(" = do");
		this.CurrentBuilder.AppendLineFeed();

		this.Indent(this.CurrentBuilder);

		for (int i = 0; i < Node.GetListSize(); i++) {
			BunLetVarNode Param = Node.GetParamNode(i);
			this.Variables.add(Param.GetGivenName());

			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append(Param.GetGivenName()
					+ "_ref <- newIORef "
					+ Param.GetGivenName());
			this.CurrentBuilder.AppendLineFeed();
		}

		for (int i = 0; i < Node.GetListSize(); i++) {
			BunLetVarNode node1 = Node.GetParamNode(i);

			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append(node1.GetGivenName()
					+ " <- readIORef "
					+ node1.GetGivenName() + "_ref");
			this.CurrentBuilder.AppendLineFeed();
		}
		this.UnIndent(this.CurrentBuilder);

		BunReturnNode ReturnNode = BNodeUtils._CheckIfSingleReturnNode(Node);
		if(ReturnNode != null && ReturnNode.HasReturnExpr()) {
			this.Indent(this.CurrentBuilder);

			String Indentation = BLib._JoinStrings("\t", IndentLevel);
			this.CurrentBuilder.Append(Indentation);
			this.CurrentBuilder.Append("return ");
			this.GenerateCode(null, ReturnNode.ExprNode());
			this.UnIndent(this.CurrentBuilder);
		} else {
			this.GenerateCode(null, Node.BlockNode());
		}
	}

	@Override
	public void VisitGetNameNode(GetNameNode Node) {
		this.CurrentBuilder.Append(Node.GetUniqueName(this));
	}

	@Override
	public void VisitSetNameNode(SetNameNode Node) {
		this.CurrentBuilder.Append("writeIORef ");
		this.CurrentBuilder.Append(Node.NameNode().GetUniqueName(this) + "_ref ");
		this.GenerateCode(null, Node.ExprNode());
		this.CurrentBuilder.AppendLineFeed();

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append(Node.NameNode().GetUniqueName(this));
		this.CurrentBuilder.Append(" <- readIORef ");
		this.CurrentBuilder.Append(Node.NameNode().GetUniqueName(this) + "_ref");
		this.CurrentBuilder.AppendLineFeed();
	}

	@Override
	public void VisitReturnNode(BunReturnNode Node) {
		if (Node.HasReturnExpr()) {
			this.GenerateCode(null, Node.ExprNode());
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

		this.CurrentBuilder.Append("(");
		Node.LeftNode().Accept(this);
		this.CurrentBuilder.Append(" " + Op + " ");
		Node.RightNode().Accept(this);
		this.CurrentBuilder.Append(")");
	}

	@Override public void VisitComparatorNode(ComparatorNode Node) {
		String Op = this.ZenOpToHaskellOp(Node.SourceToken.GetText());

		this.CurrentBuilder.Append("(");
		Node.LeftNode().Accept(this);
		this.CurrentBuilder.Append(" " + Op + " ");
		Node.RightNode().Accept(this);
		this.CurrentBuilder.Append(")");
	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		this.CurrentBuilder.Append("let __loop = do");
		this.CurrentBuilder.AppendLineFeed();

		this.Indent(this.CurrentBuilder);

		for (String var : this.Variables) {
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append(var + " <- ");
			this.CurrentBuilder.Append("readIORef " + var + "_ref");
			this.CurrentBuilder.AppendLineFeed();
		}

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("if ");
		Node.CondNode().Accept(this);
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("then");
		this.CurrentBuilder.AppendLineFeed();

		// XXX Is this correct node type ?
		BNode LoopNode = new GetNameNode(Node, null, "__loop");
		Node.BlockNode().SetNode(BNode._AppendIndex, LoopNode);
		Node.BlockNode().Accept(this);

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("else");

		this.Indent(this.CurrentBuilder);
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("return ()");
		this.CurrentBuilder.AppendLineFeed();
		this.UnIndent(this.CurrentBuilder);

		this.UnIndent(this.CurrentBuilder);

		this.CurrentBuilder.AppendIndent();
		this.CurrentBuilder.Append("__loop");
		this.CurrentBuilder.AppendLineFeed();

		for (String var : this.Variables) {
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append(var + " <- ");
			this.CurrentBuilder.Append("readIORef " + var + "_ref");
			this.CurrentBuilder.AppendLineFeed();
		}
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		if(Node.ParentNode instanceof BunBlockNode){
			this.GenerateCode(null, Node.FunctorNode());
			this.VisitListNode(" ", Node, " ", " ");
		}else{
			this.ImportLibrary("System.IO.Unsafe");
			this.CurrentBuilder.Append("(unsafePerformIO (");
			this.GenerateCode(null, Node.FunctorNode());
			this.VisitListNode(" ", Node, " ", " ");
			this.CurrentBuilder.Append("))");
		}
	}
}
