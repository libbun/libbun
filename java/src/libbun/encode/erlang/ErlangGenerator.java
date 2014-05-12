
package libbun.encode.erlang;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.BlockNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BunOrNode;
import libbun.ast.binary.ComparatorNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunNotNode;
import libbun.encode.LibBunSourceBuilder;
import libbun.encode.obsolete.OldSourceGenerator;
import libbun.parser.common.BunToken;
import libbun.type.BClassType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;
import libbun.util.ZenMethod;

public class ErlangGenerator extends OldSourceGenerator {
	@BField private int LoopNodeNumber;
	@BField private int BreakMark;
	@BField private final VariableManager VarMgr;

	public ErlangGenerator() {
		super("erl", "Erlang-5.10.4");
		this.NotOperator = "not";
		this.AndOperator = "and";
		this.OrOperator = "or";

		this.LoopNodeNumber = 0;
		this.BreakMark = -1;
		this.VarMgr = new VariableManager();

		this.Header.Append("-module(generated).");
		this.Header.AppendLineFeed();
	}

	@Override @ZenMethod protected void Finish(String FileName) {
		this.AppendAssertDecl();
		this.AppendDigitsDecl();
		this.AppendZStrDecl();
	}

	@Override public void GenerateStmtListNode(BlockNode blockNode) {
		this.GenerateStmtListNode(blockNode, ",");
	}

	public void GenerateStmtListNode(BlockNode blockNode, String last) {
		@Var int i = 0;
		@Var int size = blockNode.GetListSize();
		while (i < size) {
			@Var BNode SubNode = blockNode.GetListAt(i);
			this.Source.AppendNewLine();
			this.GenerateExpression(SubNode);
			if (i == size - 1) {
				this.Source.Append(last);
			}
			else {
				this.Source.Append(",");
			}
			i = i + 1;
		}
	}

	@Override public void VisitblockNode(BlockNode Node) {
		this.Source.OpenIndent();
		this.GenerateStmtListNode(Node, ".");
		this.Source.CloseIndent();
	}
	public void VisitblockNode(BlockNode Node, String last) {
		this.VarMgr.PushScope();
		this.Source.OpenIndent();
		this.VisitblockNode(Node);
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("__Arguments__ = " + this.VarMgr.GenVarTuple(VarFlag.Assigned | VarFlag.DefinedByParentScope, false));
		this.Source.Append(last);
		this.Source.CloseIndent();
		this.VarMgr.PopScope();
	}

	// @Override public void VisitNullNode(ZNullNode Node) {
	// 	this.CurrentBuilder.Append(this.NullLiteral);
	// }

	// @Override public void VisitBooleanNode(ZBooleanNode Node) {
	// 	if (Node.BooleanValue) {
	// 		this.CurrentBuilder.Append(this.TrueLiteral);
	// 	} else {
	// 		this.CurrentBuilder.Append(this.FalseLiteral);
	// 	}
	// }

	// @Override public void VisitIntNode(ZIntNode Node) {
	// 	this.CurrentBuilder.Append(String.valueOf(Node.IntValue));
	// }

	// @Override public void VisitFloatNode(ZFloatNode Node) {
	// 	this.CurrentBuilder.Append(String.valueOf(Node.FloatValue));
	// }

	// @Override public void VisitStringNode(ZStringNode Node) {
	// 	this.CurrentBuilder.Append(LibZen._QuoteString(Node.StringValue));
	// }

	// @Override public void VisitArrayLiteralNode(ZArrayLiteralNode Node) {
	// 	this.GenerateListNode("[", Node, "]");
	// 	// TODO Auto-generated method stub
	// }

	// @Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		this.Source.Append("#");
		this.Source.Append(this.ToErlangTypeName(Node.Type.ShortName));
		this.GenerateListNode("{", Node, "}");
	}

	// @Override public void VisitGroupNode(ZGroupNode Node) {
	// 	this.CurrentBuilder.Append("(");
	// 	this.GenerateCode(null, Node.ExprNode());
	// 	this.CurrentBuilder.Append(")");
	// }

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		if (Node.Type.Equals(BType.StringType)) {
			this.Source.Append("string:substr(");
			this.GenerateExpression(Node.RecvNode());
			this.Source.Append(", ");
			this.GenerateExpression(Node.IndexNode());
			this.Source.Append(" + 1, 1)");
		} else {
			throw new RuntimeException("GetIndex of this Type is not supported yet");
		}
	}

	// @Override public void VisitSetIndexNode(ZSetIndexNode Node) {
	// 	this.GenerateCode(null, Node.RecvNode());
	// 	this.CurrentBuilder.Append("[");
	// 	this.GenerateCode(null, Node.IndexNode());
	// 	this.CurrentBuilder.Append("]");
	// 	this.CurrentBuilder.AppendToken("=");
	// 	this.GenerateCode(null, Node.ExprNode());
	// }

	// @Override public void VisitGlobalNameNode(ZGlobalNameNode Node) {
	// 	if(Node.IsUntyped()) {
	// 		ZLogger._LogError(Node.SourceToken, "undefined symbol: " + Node.GlobalName);
	// 	}
	// 	if(Node.IsFuncNameNode()) {
	// 		this.CurrentBuilder.Append(Node.Type.StringfySignature(Node.GlobalName));
	// 	}
	// 	else {
	// 		this.CurrentBuilder.Append(Node.GlobalName);
	// 	}
	// }

	@Override public void VisitGetNameNode(GetNameNode Node) {
		String VarName = this.ToErlangVarName(Node.GetUniqueName(this));
		VarName = this.VarMgr.GenVariableName(VarName);
		this.Source.Append(VarName);
	}

	@Override public void VisitAssignNode(AssignNode Node) {
		// FIXME:
		//		int mark = this.GetLazyMark();
		//
		//		this.GenerateExpression(Node.ExprNode());
		//
		//		String VarName = this.ToErlangVarName(Node.NameNode().GetUniqueName(this));
		//		this.VarMgr.AssignVariable(VarName);
		//		this.AppendLazy(mark, this.VarMgr.GenVariableName(VarName) + " = ");
	}


	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append("#");
		this.Source.Append(this.ToErlangTypeName(Node.RecvNode().Type.ShortName));
		this.Source.Append(".");
		this.Source.Append(this.ToErlangTypeName(Node.GetName()));
	}

	//  FIXME:
	//	@Override public void VisitSetFieldNode(SetFieldNode Node) {
	//		int mark = this.GetLazyMark();
	//
	//		GetNameNode GetNameNode = (GetNameNode)Node.RecvNode();
	//		this.GenerateExpression(GetNameNode);
	//		this.Source.Append("#");
	//		this.Source.Append(this.ToErlangTypeName(Node.RecvNode().Type.ShortName));
	//		this.Source.Append("{");
	//		this.Source.Append(Node.GetName(), " = ");
	//		this.GenerateExpression(Node.ExprNode());
	//		this.Source.Append("}");
	//		this.VarMgr.AssignVariable(GetNameNode.GetUniqueName(this));
	//		LibBunSourceBuilder LazyBuilder = new LibBunSourceBuilder(this, this.Source);
	//		LibBunSourceBuilder BodyBuilder = this.Source;
	//		this.Source = LazyBuilder;
	//		this.GenerateExpression(Node.RecvNode());
	//		this.Source.Append(" = ");
	//		this.Source = BodyBuilder;
	//		this.AppendLazy(mark, LazyBuilder.toString());
	//	}

	// @Override public void VisitMethodCallNode(ZMethodCallNode Node) {
	// 	this.GenerateSurroundCode(Node.RecvNode());
	// 	this.CurrentBuilder.Append(".");
	// 	this.CurrentBuilder.Append(Node.MethodName());
	// 	this.GenerateListNode("(", Node, ")");
	// }

	// @Override public void VisitMacroNode(ZMacroNode Node) {
	// 	@Var String Macro = Node.GetFormText();
	// 	@Var ZFuncType FuncType = Node.GetFuncType();
	// 	@Var int fromIndex = 0;
	// 	@Var int BeginNum = Macro.indexOf("$[", fromIndex);
	// 	while(BeginNum != -1) {
	// 		@Var int EndNum = Macro.indexOf("]", BeginNum + 2);
	// 		if(EndNum == -1) {
	// 			break;
	// 		}
	// 		this.CurrentBuilder.Append(Macro.substring(fromIndex, BeginNum));
	// 		@Var int Index = (int)LibZen._ParseInt(Macro.substring(BeginNum+2, EndNum));
	// 		if(Node.HasAst(Index)) {
	// 			this.GenerateCode(FuncType.GetFuncParamType(Index), Node.AST[Index]);
	// 		}
	// 		fromIndex = EndNum + 1;
	// 		BeginNum = Macro.indexOf("$[", fromIndex);
	// 	}
	// 	this.CurrentBuilder.Append(Macro.substring(fromIndex));
	// }

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		if (FuncNameNode != null) {
			this.Source.Append(this.ToErlangFuncName(Node.FuncNameNode().GetSignature()));
		}
		else {
			this.GenerateExpression(Node.FunctorNode());
		}
		this.GenerateListNode("(", Node, ")");
	}

	// @Override public void VisitUnaryNode(ZUnaryNode Node) {
	// 	this.CurrentBuilder.Append(Node.SourceToken.GetText());
	// 	this.GenerateCode(null, Node.RecvNode());
	// }

	@Override public void VisitNotNode(BunNotNode Node) {
		this.Source.Append(this.NotOperator);
		this.GenerateExpression(Node.RecvNode());
	}

	@Override public void VisitCastNode(BunCastNode Node) {
		// this.CurrentBuilder.Append("(");
		// this.GenerateTypeName(Node.Type);
		// this.CurrentBuilder.Append(")");
		this.GenerateExpression(Node.ExprNode());
	}

	// @Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
	// 	this.GenerateCode(null, Node.LeftNode());
	// 	this.CurrentBuilder.AppendToken("instanceof");
	// 	this.GenerateTypeName(Node.RightNode().Type);
	// }

	protected String GetBinaryOperator(BType Type, BunToken sourceToken) {
		//FIXME:
		//sourceToken is deprecated
		//		if(sourceToken.EqualsText("<=")) {
		//			return "=<";
		//		}
		//		if(sourceToken.EqualsText("==")) {
		//			return "=:=";
		//		}
		//		if(sourceToken.EqualsText("!=")) {
		//			return "=/=";
		//		}
		//		if(sourceToken.EqualsText("<<")) {
		//			return "bsl";
		//		}
		//		if(sourceToken.EqualsText(">>")) {
		//			return "bsr";
		//		}
		//		if(sourceToken.EqualsText('%')) {
		//			return "rem";
		//		}
		//		if(sourceToken.EqualsText('/') && Type.Equals(BType.IntType)) {
		//			return "div";
		//		}
		return sourceToken.GetText();
	}


	@Override public void VisitBinaryNode(BinaryOperatorNode Node) {
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.Source.Append("(");
		}
		this.GenerateExpression(Node.LeftNode());
		//		this.CurrentBuilder.AppendToken(Node.SourceToken.GetText());
		@Var String Operator = this.GetBinaryOperator(Node.Type, Node.SourceToken);
		this.Source.Append(Operator);
		this.GenerateExpression(Node.RightNode());
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.Source.Append(")");
		}
	}

	@Override public void VisitComparatorNode(ComparatorNode Node) {
		this.GenerateExpression(Node.LeftNode());
		@Var String Operator = this.GetBinaryOperator(Node.Type, Node.SourceToken);
		this.Source.Append(" ", Operator, " ");
		this.GenerateExpression(Node.RightNode());
	}

	@Override public void VisitAndNode(BunAndNode Node) {
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" ", this.AndOperator, " ");
		this.GenerateExpression(Node.RightNode());
	}

	@Override public void VisitOrNode(BunOrNode Node) {
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" ", this.OrOperator, " ");
		this.GenerateExpression(Node.RightNode());
	}

	public void AppendGuardAndBlock(BNode Node) {
		if (Node instanceof BunIfNode) {
			BunIfNode IfNode = (BunIfNode)Node;
			this.Source.AppendNewLine();
			this.GenerateExpression(IfNode.CondNode());
			this.Source.Append(" ->");
			this.VisitblockNode((BlockNode)IfNode.ThenNode(), ";");
			this.Source.AppendLineFeed();
			if (IfNode.HasElseNode()) {
				this.AppendGuardAndBlock(IfNode.ElseNode());
			} else {
				this.AppendGuardAndBlock(null);
			}
		} else {
			this.Source.AppendNewLine("true ->");
			if (Node != null) {
				this.VisitblockNode((BlockNode)Node, "");
			} else {
				this.Source.OpenIndent(null);
				this.Source.AppendNewLine(this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, false));
				this.Source.CloseIndent(null);
			}
		}
	}

	@Override public void VisitIfNode(BunIfNode Node) {
		int mark = this.GetLazyMark();

		this.Source.Append("if");
		this.Source.AppendLineFeed();
		this.AppendGuardAndBlock(Node);
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("end");

		this.AppendLazy(mark, this.VarMgr.GenVarTuple(VarFlag.Assigned, true) + " = ");
	}

	@Override public void VisitReturnNode(BunReturnNode Node) {
		this.Source.Append("throw({return, ");
		if (Node.HasReturnExpr()) {
			this.GenerateExpression(Node.ExprNode());
		} else {
			this.Source.Append("void");
		}
		this.Source.Append("})");
	}

	@Override public void VisitWhileNode(BunWhileNode Node) {
		this.LoopNodeNumber += 1;
		String WhileNodeName = "Loop" + Integer.toString(this.LoopNodeNumber);

		int mark1 = this.GetLazyMark();

		//Generate WhileBlock
		this.VarMgr.FilterStart();
		this.VarMgr.ChangeFilterFlag(VarFlag.None);
		this.VisitblockNode(Node.blockNode(), ",");
		this.Source.AppendLineFeed();
		this.Source.OpenIndent();
		this.Source.AppendNewLine(WhileNodeName + "(" + WhileNodeName + ", __Arguments__);");
		this.Source.CloseIndent();

		//Generate Else Guard and Block
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("(_, Args) ->");
		this.Source.OpenIndent();
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("Args");
		this.Source.CloseIndent();

		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("end,");

		//Generate While Guard
		this.VarMgr.ChangeFilterFlag(VarFlag.Assigned);
		LibBunSourceBuilder LazyBuilder = new LibBunSourceBuilder(this, this.Source);
		LibBunSourceBuilder BodyBuilder = this.Source;
		this.Source = LazyBuilder;
		this.GenerateExpression(Node.CondNode());
		this.Source = BodyBuilder;
		this.AppendLazy(mark1, ""
				+ WhileNodeName
				+ " = fun(" + WhileNodeName + ", "
				+ this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, false)
				+ ") when "
				+ LazyBuilder.toString()
				+ " -> ");

		//Generate Loop Function Call
		this.VarMgr.ChangeFilterFlag(VarFlag.None);
		this.VarMgr.FilterFinish();
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine();
		int mark2 = this.GetLazyMark();
		this.Source.Append(" = " + WhileNodeName + "(" + WhileNodeName + ", ");
		this.Source.Append(this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, false) + ")");
		this.AppendLazy(mark2, this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, true));
	}

	@Override public void VisitBreakNode(BunBreakNode Node) {
		this.Source.Append("throw({break, ");
		//this.VarMgr.GenVarTupleOnlyUsed(false);
		this.BreakMark = this.GetLazyMark();
		this.Source.Append("})");
	}

	// @Override public void VisitThrowNode(ZThrowNode Node) {
	// 	this.CurrentBuilder.Append("throw");
	// 	this.CurrentBuilder.AppendWhiteSpace();
	// 	this.GenerateCode(null, Node.ExprNode());
	// }

	// @Override public void VisitTryNode(ZTryNode Node) {
	// 	this.CurrentBuilder.Append("try");
	// 	this.GenerateCode(null, Node.TryNode());
	// 	if(Node.CatchNode() != null) {
	// 		this.GenerateCode(null, Node.CatchNode());
	// 	}
	// 	if (Node.FinallyNode() != null) {
	// 		this.CurrentBuilder.Append("finally");
	// 		this.GenerateCode(null, Node.FinallyNode());
	// 	}
	// }

	// public void VisitCatchNode(ZCatchNode Node) {
	// 	this.CurrentBuilder.Append("catch (");
	// 	this.CurrentBuilder.Append(Node.ExceptionName);
	// 	this.VisitTypeAnnotation(Node.ExceptionType);
	// 	this.CurrentBuilder.Append(")");
	// 	this.GenerateCode(null, Node.AST[ZCatchNode._Block]);
	// }

	@Override
	protected void VisitVarDeclNode(BunLetVarNode Node) {
		@Var int mark = this.GetLazyMark();

		this.GenerateExpression(Node.InitValueNode());

		@Var String VarName = this.ToErlangVarName(Node.GetGivenName());
		this.VarMgr.DefineVariable(VarName);
		this.AppendLazy(mark, this.VarMgr.GenVariableName(VarName) + " = ");

		this.Source.Append(",");
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("pad");
	}

	// protected void VisitTypeAnnotation(ZType Type) {
	// 	this.CurrentBuilder.Append(": ");
	// 	this.GenerateTypeName(Type);
	// }

	@Override public void VisitLetNode(BunLetVarNode Node) {
		this.Source.Append("put(");
		this.Source.Append(Node.GetUniqueName(this));
		this.Source.Append(", ");
		this.GenerateExpression(Node.InitValueNode());
		this.Source.Append(")");
	}

	@Override
	protected void VisitParamNode(BunLetVarNode Node) {
		String VarName = this.ToErlangVarName(Node.GetGivenName());
		VarName = this.VarMgr.GenVariableName(VarName);
		this.Source.Append(VarName);
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		this.VarMgr.Init();
		this.DefineVariables(Node.ParamNode());

		String FuncName = this.ToErlangFuncName(Node.FuncName());
		if (FuncName.equals("main")) {
			this.Header.Append("-export([main/1]).");
		} else {
			this.Header.Append("-export([" + FuncName + "/" + Node.getParamSize() + "]).");
		}
		this.Header.AppendLineFeed();

		this.Source.Append(FuncName + "_inner");
		this.VisitFuncParamNode("(", Node, ")");
		this.Source.Append("->");
		if (Node.blockNode() == null) {
			this.Source.AppendNewLine();
			this.Source.Append("pass.");
		} else {
			this.GenerateExpression(Node.blockNode());
		}

		this.Source.AppendLineFeed();
		this.AppendWrapperFuncDecl(Node);
		this.Source.AppendLineFeed();
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		LibBunSourceBuilder BodyBuilder = this.Source;
		this.Source = this.Header;

		this.Source.Append("-record(");
		this.Source.Append(this.ToErlangTypeName(Node.ClassName()));
		if(!Node.SuperType().Equals(BClassType._ObjectType)) {
			throw new RuntimeException("\"extends\" is not supported yet");
		}
		this.Source.Append(", {");
		@Var int i = 0;
		@Var int size = Node.GetListSize();
		while (i < size) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.Source.Append(this.ToErlangTypeName(FieldNode.GetGivenName()));
			this.Source.Append(" = ");
			this.GenerateExpression(FieldNode.InitValueNode());
			if (i < size - 1) {
				this.Source.AppendWhiteSpace();
				this.Source.Append(",");
			}
			i = i + 1;
		}
		this.Source.Append("}).");
		this.Source.AppendLineFeed();

		this.Source = BodyBuilder;
	}

	// @Override public void VisitErrorNode(ZErrorNode Node) {
	// 	ZLogger._LogError(Node.SourceToken, Node.ErrorMessage);
	// 	this.CurrentBuilder.Append("ThrowError(");
	// 	this.CurrentBuilder.Append(LibZen._QuoteString(Node.ErrorMessage));
	// 	this.CurrentBuilder.Append(")");
	// }

	// @Override public void VisitExtendedNode(ZNode Node) {
	// 	if(Node instanceof ZLetVarNode) {
	// 		this.VisitParamNode((ZLetVarNode)Node);
	// 	}
	// 	else {
	// 		@Var ZSugarNode SugarNode = Node.DeSugar(this);
	// 		this.VisitSugarNode(SugarNode);
	// 	}
	// }

	// @Override public void VisitSugarNode(ZSugarNode Node) {
	// 	this.GenerateCode(null, Node.AST[ZSugarNode._DeSugar]);
	// }

	// // Utils
	// protected void GenerateTypeName(ZType Type) {
	// 	this.CurrentBuilder.Append(this.GetNativeTypeName(Type.GetRealType()));
	// }

	@Override
	protected void GenerateListNode(String OpenToken, AbstractListNode VargNode, String DelimToken, String CloseToken) {
		this.Source.Append(OpenToken);
		@Var int i = 0;
		while(i < VargNode.GetListSize()) {
			@Var BNode ParamNode = VargNode.GetListAt(i);
			if (i > 0) {
				this.Source.Append(DelimToken);
			}
			this.GenerateExpression(ParamNode);
			i = i + 1;
		}
		this.Source.Append(CloseToken);
	}

	@Override protected void GenerateListNode(String OpenToken, AbstractListNode VargNode, String CloseToken) {
		this.GenerateListNode(OpenToken, VargNode, ", ", CloseToken);
	}

	private void AppendAssertDecl() {
		// this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= false ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.AppendNewLine("exit(\"Assertion Failed\");");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= true ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.AppendNewLine("do_nothing;");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Append("assert(_Expr) ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.AppendNewLine("exit(\"Assertion Failed (Expr is not true or false)\").");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();

		this.Header.Append("assert(_Expr) when _Expr =:= false -> exit(\"Assertion Failed\");");
		this.Header.AppendLineFeed();
		this.Header.Append("assert(_Expr) when _Expr =:= true -> do_nothing;");
		this.Header.AppendLineFeed();
		this.Header.Append("assert(_Expr) -> exit(\"Assertion Failed (Expr is not true or false)\").");
		this.Header.AppendLineFeed();
	}

	private void AppendDigitsDecl() {
		this.Header.Append("digits(N) when is_integer(N) -> integer_to_list(N);");
		this.Header.AppendLineFeed();
		this.Header.Append("digits(0.0) -> \"0.0\";");
		this.Header.AppendLineFeed();
		this.Header.Append("digits(Float) -> float_to_list(Float).");
		this.Header.AppendLineFeed();
	}

	private void AppendZStrDecl() {
		this.Header.Append("zstr(Str) when Str =:= null -> \"null\";");
		this.Header.AppendLineFeed();
		this.Header.Append("zstr(Str) -> Str.");
		this.Header.AppendLineFeed();
	}

	private void AppendWrapperFuncDecl(BunFunctionNode Node) {
		String FuncName = this.ToErlangFuncName(Node.FuncName());
		this.Source.Append(FuncName);
		if (FuncName.equals("main")) { //FIX ME!!
			this.Source.Append("(_)");
		} else {
			this.GenerateListNode("(", Node.ParamNode(), ")");
		}

		this.Source.Append(" ->");
		this.Source.AppendLineFeed();
		this.Source.OpenIndent();
		this.Source.AppendNewLine("try "+ FuncName + "_inner");
		this.GenerateListNode("(", Node.ParamNode(), ")");
		this.Source.Append(" of");
		this.Source.AppendLineFeed();
		this.Source.OpenIndent();
		this.Source.AppendNewLine("_ -> void");
		this.Source.AppendLineFeed();
		this.Source.CloseIndent();
		this.Source.AppendNewLine("catch");
		this.Source.AppendLineFeed();
		this.Source.OpenIndent();
		this.Source.AppendNewLine("throw:{return, Ret} -> Ret;");
		this.Source.AppendLineFeed();
		this.Source.AppendNewLine("throw:UnKnown -> throw(UnKnown)");
		this.Source.AppendLineFeed();
		this.Source.CloseIndent();
		this.Source.AppendNewLine("end.");

		this.Source.CloseIndent();
	}


	private int GetLazyMark() {
		this.Source.Append(null);
		return this.Source.SourceList.size() - 1;
	}
	private void AppendLazy(int mark, String Code) {
		this.Source.SourceList.ArrayValues[mark] = Code;
	}
	private void DefineVariables(AbstractListNode VargNode) {
		@Var int i = 0;
		while(i < VargNode.GetListSize()) {
			@Var BunLetVarNode ParamNode = (BunLetVarNode)VargNode.GetListAt(i);
			this.VarMgr.DefineVariable(this.ToErlangVarName(ParamNode.GetGivenName()));
			i += 1;
		}
	}
	private String ToErlangFuncName(String FuncName) {
		return FuncName != null ? FuncName.toLowerCase() : "";
	}
	private String ToErlangTypeName(String TypeName) {
		return TypeName != null ? TypeName.toLowerCase() : "";
	}
	private String ToErlangVarName(String VarName) {
		return VarName != null ? VarName.toUpperCase() : "";
	}
}
