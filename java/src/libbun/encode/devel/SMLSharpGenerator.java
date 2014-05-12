package libbun.encode.devel;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
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
import libbun.ast.error.ErrorNode;
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
import libbun.ast.literal.BunMapNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.literal.ConstNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunComplementNode;
import libbun.ast.unary.BunMinusNode;
import libbun.ast.unary.BunNotNode;
import libbun.ast.unary.BunPlusNode;
import libbun.ast.unary.UnaryOperatorNode;
import libbun.encode.LibBunSourceBuilder;
import libbun.encode.LibBunSourceGenerator;
import libbun.parser.classic.LibBunLangInfo;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public class SMLSharpGenerator extends LibBunSourceGenerator {
	@BField private final LibBunSourceBuilder Constant;
	@BField private final LibBunSourceBuilder Export;
	@BField private boolean IsDefinedFirstFunction = false;

	public SMLSharpGenerator() {
		super(new LibBunLangInfo("SML#-2.0", "sml"));
		this.Constant = this.InsertNewSourceBuilder();
		this.Export = this.AppendNewSourceBuilder();
		this.SetNativeType(BType.BooleanType, "bool");
		this.SetNativeType(BType.IntType, "intInf");
		this.SetNativeType(BType.FloatType, "real");
		this.SetNativeType(BType.StringType, "string option");
	}

	@Override protected void GenerateStatement(BNode Node) {
		if(Node instanceof BunCastNode && Node.Type == BType.VoidType) {
			this.GenerateStatement(Node.AST[BunCastNode._Expr]);
		}
		else if(Node instanceof BunLetVarNode && Node.IsTopLevel()){
			@Var LibBunSourceBuilder PushedBuilder = this.Source;
			this.Source = this.Constant;
			super.GenerateStatement(Node);
			this.Source = PushedBuilder;
		}
		else {
			super.GenerateStatement(Node);
		}
	}

	@Override
	protected void GenerateStatementEnd(BNode Node) {
	}

	@Override
	protected void GenerateImportLibrary(String LibName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNullNode(BunNullNode Node) {
		if(Node.Type.IsStringType()) {
			this.Source.Append("NONE");
		}
		else {
			// TODO Auto-generated method stub
		}
	}

	@Override
	public void VisitBooleanNode(BunBooleanNode Node) {
		if (Node.BooleanValue) {
			this.Source.Append("true");
		} else {
			this.Source.Append("false");
		}
	}

	@Override
	public void VisitIntNode(BunIntNode Node) {
		this.Source.Append(String.valueOf(Node.IntValue));
	}

	@Override
	public void VisitFloatNode(BunFloatNode Node) {
		this.Source.Append(String.valueOf(Node.FloatValue));
	}

	@Override
	public void VisitStringNode(BunStringNode Node) {
		this.Source.Append("(SOME ");
		this.Source.AppendQuotedText(Node.StringValue);
		this.Source.Append(")");
	}

	@Override
	public void VisitNotNode(BunNotNode Node) {
		this.Source.Append("(Bool.not ");
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitPlusNode(BunPlusNode Node) {
		this.GenerateExpression(Node.RecvNode());
	}

	@Override
	public void VisitMinusNode(BunMinusNode Node) {
		if(Node.RecvNode() instanceof ConstNode) {
			this.Source.Append("~");
			this.GenerateExpression(Node.RecvNode());
		}
		else {
			this.Source.Append("(~ ");
			this.GenerateExpression(Node.RecvNode());
			this.Source.Append(")");
		}
	}

	@Override
	public void VisitComplementNode(BunComplementNode Node) {
		this.Source.Append("(IntInf.notb ");
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitAndNode(BunAndNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitOrNode(BunOrNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitAddNode(BunAddNode Node) {
		if(Node.Type.IsStringType()) {
			this.Source.Append("(SOME ((valOf ");
			@Var BNode LeftNode = Node.LeftNode();
			if(!LeftNode.Type.IsStringType()) {
				LeftNode = this.TypeChecker.EnforceNodeType(Node.LeftNode(), BType.StringType);
			}
			this.GenerateExpression(LeftNode);
			this.Source.Append(") ^ (valOf ");
			@Var BNode RightNode = Node.RightNode();
			if(!RightNode.Type.IsStringType()) {
				RightNode = this.TypeChecker.EnforceNodeType(Node.RightNode(), BType.StringType);
			}
			this.GenerateExpression(RightNode);
			this.Source.Append(")))");
		}
		else {
			this.Source.Append("(");
			this.GenerateExpression(Node.LeftNode());
			this.Source.Append(" + ");
			this.GenerateExpression(Node.RightNode());
			this.Source.Append(")");
		}
	}

	@Override
	public void VisitSubNode(BunSubNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" - ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitMulNode(BunMulNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" * ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitDivNode(BunDivNode Node) {
		if(Node.Type.IsIntType()) {
			this.Source.Append("(");
			this.GenerateExpression(Node.LeftNode());
			this.Source.Append(" div ");
			this.GenerateExpression(Node.RightNode());
			this.Source.Append(")");
		}
		else {
			this.Source.Append("(");
			this.GenerateExpression(Node.LeftNode());
			this.Source.Append(" / ");
			this.GenerateExpression(Node.RightNode());
			this.Source.Append(")");
		}
	}

	@Override
	public void VisitModNode(BunModNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" mod ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitLeftShiftNode(BunLeftShiftNode Node) {
		this.Source.Append("(IntInf.<< (");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(", (Word.fromLargeInt ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")))");
	}

	@Override
	public void VisitRightShiftNode(BunRightShiftNode Node) {
		this.Source.Append("(IntInf.~>> (");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(", (Word.fromLargeInt ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")))");
	}

	@Override
	public void VisitBitwiseAndNode(BunBitwiseAndNode Node) {
		this.Source.Append("(IntInf.andb (");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(", ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append("))");
	}

	@Override
	public void VisitBitwiseOrNode(BunBitwiseOrNode Node) {
		this.Source.Append("(IntInf.orb (");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(", ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append("))");
	}

	@Override
	public void VisitBitwiseXorNode(BunBitwiseXorNode Node) {
		this.Source.Append("(IntInf.xorb (");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(", ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append("))");
	}

	@Override
	public void VisitEqualsNode(BunEqualsNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" = ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitNotEqualsNode(BunNotEqualsNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" <> ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitLessThanNode(BunLessThanNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" < ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitLessThanEqualsNode(BunLessThanEqualsNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" <= ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitGreaterThanNode(BunGreaterThanNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" > ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" >= ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitGroupNode(GroupNode Node) {
		this.GenerateExpression(Node.ExprNode());
	}

	@Override
	public void VisitArrayLiteralNode(BunArrayNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMapLiteralNode(BunMapNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNewObjectNode(NewObjectNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitFuncCallNode(FuncCallNode Node) {
		@Var BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		this.Source.Append("(");
		if(FuncNameNode != null) {
			this.Source.Append(FuncNameNode.GetSignature());
		}
		else {
			this.GenerateExpression(Node.FunctorNode());
		}
		this.GenerateArgumentListNode(Node);
		this.Source.Append(")");
	}

	private boolean DoesNeedReference(BNode Node) {
		/* FIXME */
		return !(Node.ParentNode instanceof AssignNode);
	}

	@Override
	public void VisitGetNameNode(GetNameNode Node) {
		if(!Node.ResolvedNode.IsReadOnly() && this.DoesNeedReference(Node)) {
			this.Source.Append("(!", Node.GetUniqueName(this), ")");
		}
		else {
			this.Source.Append(Node.GetUniqueName(this));
		}
	}

	@Override
	public void VisitAssignNode(AssignNode Node) {
		this.Source.Append("(");
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" := ");
		this.GenerateExpression(Node.RightNode());
		this.Source.Append(")");
	}

	@Override
	public void VisitGetFieldNode(GetFieldNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitGetIndexNode(GetIndexNode Node) {
		// TODO Auto-generated method stub

	}


	@Override
	public void VisitMethodCallNode(MethodCallNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitUnaryNode(UnaryOperatorNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitCastNode(BunCastNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBinaryNode(BinaryOperatorNode Node) {
		// TODO Auto-generated method stub

	}

	private void GenerateStmtListNode(BunBlockNode Node) {
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			if(i > 0) {
				this.Source.Append(";");
			}
			@Var BNode SubNode = Node.GetListAt(i);
			this.GenerateStatement(SubNode);
			i = i + 1;
		}
	}

	@Override
	public void VisitBlockNode(BunBlockNode Node) {
		this.Source.AppendWhiteSpace();
		if(Node.GetListSize() == 0) {
			this.Source.Append("()");
		}
		else {
			this.Source.OpenIndent("(");
			this.GenerateStmtListNode(Node);
			this.Source.CloseIndent(")");
		}
	}

	@Override
	public void VisitVarBlockNode(BunVarBlockNode Node) {
		this.Source.Append("let ");
		this.GenerateExpression(Node.VarDeclNode());
		this.Source.Append(" in");
		this.VisitBlockNode(Node);
		this.Source.Append(" end");
	}

	@Override
	public void VisitIfNode(BunIfNode Node) {
		this.Source.Append("if ");
		this.GenerateExpression(Node.CondNode());
		this.Source.Append(" then ");
		this.GenerateExpression(Node.ThenNode());
		this.Source.AppendNewLine("else ");
		if (Node.HasElseNode()) {
			this.GenerateExpression(Node.ElseNode());
		}
		else {
			this.Source.Append("()");
		}
	}

	@Override
	public void VisitReturnNode(BunReturnNode Node) {
		this.Source.Append("raise Return");
		if (Node.HasReturnExpr() && !Node.ExprNode().Type.IsVoidType()) {
			this.Source.Append(" ");
			this.GenerateExpression(Node.ExprNode());
		}
	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		/* definition of break */
		this.Source.OpenIndent("let exception Break;");

		/* definition of loop */
		this.Source.AppendNewLine("fun WhileLoop () = (if ");
		this.GenerateExpression(Node.CondNode());
		this.Source.Append(" then ");

		/* whatever */
		if(Node.HasNextNode()) {
			Node.BlockNode().Append(Node.NextNode());
		}

		/* loop body */
		if(Node.BlockNode().GetListSize() == 0) {
			this.Source.Append("WhileLoop ()) else ())");
		}
		else {
			this.Source.OpenIndent("(");
			this.GenerateStmtListNode(Node.BlockNode());
			this.Source.Append(";");
			this.Source.AppendNewLine("WhileLoop ()");
			this.Source.CloseIndent(") else ())");
		}

		/* start loop */
		this.Source.CloseIndent("in");
		this.Source.OpenIndent(" (");
		this.Source.AppendNewLine("WhileLoop ()");
		this.Source.AppendNewLine("handle Break => ()");
		this.Source.CloseIndent(") end");

	}

	@Override
	public void VisitBreakNode(BunBreakNode Node) {
		this.Source.Append("raise Break");
	}

	@Override
	public void VisitThrowNode(BunThrowNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitTryNode(BunTryNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLetNode(BunLetVarNode Node) {
		if(Node.IsParamNode()) {
			this.Source.Append(Node.GetUniqueName(this));
		}
		else {
			this.Source.Append("val ", Node.GetUniqueName(this), " = ");
			if(!Node.IsReadOnly()) {
				this.Source.Append("ref ");
			}
			this.GenerateExpression(Node.InitValueNode());
		}
	}

	private void GenerateArgumentListNode(AbstractListNode VargNode) {
		if(VargNode.GetListSize() == 0) {
			this.Source.Append(" ()");
		}
		else {
			this.GenerateListNode(" ", VargNode, " ", "");
		}
	}

	@Override
	public void VisitFunctionNode(BunFunctionNode Node) {
		@Var boolean IsAnonymousFunction = Node.FuncName() == null;
		@Var boolean DoesReturnValue = !Node.ReturnType().IsVoidType();
		if(IsAnonymousFunction) {
			this.Source.Append("(fn");
		}
		else {
			if(Node.IsTopLevelDefineFunction()) {
				if(this.IsDefinedFirstFunction) {
					this.Source.Append("and ");
				}
				else {
					this.Source.Append("fun ");
					this.IsDefinedFirstFunction = true;
				}
			}
			else {
				this.Source.Append("fun ");
			}
			this.Source.Append(Node.GetSignature());
		}
		this.GenerateArgumentListNode(Node.ParamNode());
		if(IsAnonymousFunction) {
			this.Source.Append(" => ");
		}
		else {
			this.Source.Append(" = ");
		}
		this.Source.Append("let exception Return");
		if(DoesReturnValue) {
			this.Source.Append(" of ");
			this.GenerateTypeName(Node.ReturnType());
		}
		this.Source.Append(" in");
		this.GenerateExpression(Node.BlockNode());
		this.Source.Append(" handle Return");
		if(DoesReturnValue) {
			this.Source.Append(" v => v");
		}
		else {
			this.Source.Append(" => ()");

		}
		this.Source.Append(" end");
		if(IsAnonymousFunction) {
			this.Source.Append(")");
		}

		if(!IsAnonymousFunction && Node.IsExport()) {
			this.Export.AppendNewLine("val ", Node.FuncName(), " = ");
			this.Export.Append(Node.GetSignature());
			if(Node.FuncName().equals("main")) {
				this.AppendNewSourceBuilder().Append(";main ()");
			}
		}
	}

	@Override
	public void VisitClassNode(BunClassNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitErrorNode(ErrorNode Node) {
		// TODO Auto-generated method stub

	}

}
