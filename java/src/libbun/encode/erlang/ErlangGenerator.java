
package libbun.encode.erlang;

import libbun.ast.BunBlockNode;
import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.ComparatorNode;
import libbun.ast.binary.BunOrNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.expression.SetNameNode;
import libbun.ast.expression.SetFieldNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunNotNode;
import libbun.encode.ZSourceBuilder;
import libbun.encode.ZSourceGenerator;
import libbun.parser.BToken;
import libbun.type.BClassType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;
import libbun.util.ZenMethod;



public class ErlangGenerator extends ZSourceGenerator {
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

		this.HeaderBuilder.Append("-module(generated).");
		this.HeaderBuilder.AppendLineFeed();
	}

	@Override @ZenMethod protected void Finish(String FileName) {
		this.AppendAssertDecl();
		this.AppendDigitsDecl();
		this.AppendZStrDecl();
	}

	@Override public void VisitStmtList(BunBlockNode BlockNode) {
		this.VisitStmtList(BlockNode, ",");
	}

	public void VisitStmtList(BunBlockNode BlockNode, String last) {
		@Var int i = 0;
		@Var int size = BlockNode.GetListSize();
		while (i < size) {
			@Var BNode SubNode = BlockNode.GetListAt(i);
			this.CurrentBuilder.AppendLineFeed();
			this.CurrentBuilder.AppendIndent();
			this.GenerateCode(null, SubNode);
			if (i == size - 1) {
				this.CurrentBuilder.Append(last);
			}
			else {
				this.CurrentBuilder.Append(",");
			}
			i = i + 1;
		}
	}

	@Override public void VisitBlockNode(BunBlockNode Node) {
		this.CurrentBuilder.Indent();
		this.VisitStmtList(Node, ".");
		this.CurrentBuilder.UnIndent();
	}
	public void VisitBlockNode(BunBlockNode Node, String last) {
		this.VarMgr.PushScope();
		this.CurrentBuilder.Indent();
		this.VisitBlockNode(Node);
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("__Arguments__ = " + this.VarMgr.GenVarTuple(VarFlag.Assigned | VarFlag.DefinedByParentScope, false));
		this.CurrentBuilder.Append(last);
		this.CurrentBuilder.UnIndent();
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
	// 	this.VisitListNode("[", Node, "]");
	// 	// TODO Auto-generated method stub
	// }

	// @Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
	// 	// TODO Auto-generated method stub
	// }

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		this.CurrentBuilder.Append("#");
		this.CurrentBuilder.Append(this.ToErlangTypeName(Node.Type.ShortName));
		this.VisitListNode("{", Node, "}");
	}

	// @Override public void VisitGroupNode(ZGroupNode Node) {
	// 	this.CurrentBuilder.Append("(");
	// 	this.GenerateCode(null, Node.ExprNode());
	// 	this.CurrentBuilder.Append(")");
	// }

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		if (Node.Type.Equals(BType.StringType)) {
			this.CurrentBuilder.Append("string:substr(");
			this.GenerateCode(null, Node.RecvNode());
			this.CurrentBuilder.Append(", ");
			this.GenerateCode(null, Node.IndexNode());
			this.CurrentBuilder.Append(" + 1, 1)");
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
		this.CurrentBuilder.Append(VarName);
	}

	@Override public void VisitSetNameNode(SetNameNode Node) {
		int mark = this.GetLazyMark();

		this.GenerateCode(null, Node.ExprNode());

		String VarName = this.ToErlangVarName(Node.NameNode().GetUniqueName(this));
		this.VarMgr.AssignVariable(VarName);
		this.AppendLazy(mark, this.VarMgr.GenVariableName(VarName) + " = ");
	}


	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateSurroundCode(Node.RecvNode());
		this.CurrentBuilder.Append("#");
		this.CurrentBuilder.Append(this.ToErlangTypeName(Node.RecvNode().Type.ShortName));
		this.CurrentBuilder.Append(".");
		this.CurrentBuilder.Append(this.ToErlangTypeName(Node.GetName()));
	}

	@Override public void VisitSetFieldNode(SetFieldNode Node) {
		int mark = this.GetLazyMark();

		GetNameNode GetNameNode = (GetNameNode)Node.RecvNode();
		this.GenerateSurroundCode(GetNameNode);
		this.CurrentBuilder.Append("#");
		this.CurrentBuilder.Append(this.ToErlangTypeName(Node.RecvNode().Type.ShortName));
		this.CurrentBuilder.Append("{");
		this.CurrentBuilder.Append(Node.GetName());
		this.CurrentBuilder.AppendToken("=");
		this.GenerateCode(null, Node.ExprNode());
		this.CurrentBuilder.Append("}");
		this.VarMgr.AssignVariable(GetNameNode.GetUniqueName(this));
		ZSourceBuilder LazyBuilder = new ZSourceBuilder(this, this.CurrentBuilder);
		ZSourceBuilder BodyBuilder = this.CurrentBuilder;
		this.CurrentBuilder = LazyBuilder;
		this.GenerateCode(null, Node.RecvNode());
		this.CurrentBuilder.AppendToken("=");
		this.CurrentBuilder = BodyBuilder;
		this.AppendLazy(mark, LazyBuilder.toString());
	}

	// @Override public void VisitMethodCallNode(ZMethodCallNode Node) {
	// 	this.GenerateSurroundCode(Node.RecvNode());
	// 	this.CurrentBuilder.Append(".");
	// 	this.CurrentBuilder.Append(Node.MethodName());
	// 	this.VisitListNode("(", Node, ")");
	// }

	// @Override public void VisitMacroNode(ZMacroNode Node) {
	// 	@Var String Macro = Node.GetMacroText();
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
			this.CurrentBuilder.Append(this.ToErlangFuncName(Node.FuncNameNode().GetSignature()));
		}
		else {
			this.GenerateCode(null, Node.FunctorNode());
		}
		this.VisitListNode("(", Node, ")");
	}

	// @Override public void VisitUnaryNode(ZUnaryNode Node) {
	// 	this.CurrentBuilder.Append(Node.SourceToken.GetText());
	// 	this.GenerateCode(null, Node.RecvNode());
	// }

	@Override public void VisitNotNode(BunNotNode Node) {
		this.CurrentBuilder.AppendToken(this.NotOperator);
		this.GenerateSurroundCode(Node.RecvNode());
	}

	@Override public void VisitCastNode(BunCastNode Node) {
		// this.CurrentBuilder.Append("(");
		// this.GenerateTypeName(Node.Type);
		// this.CurrentBuilder.Append(")");
		this.GenerateSurroundCode(Node.ExprNode());
	}

	// @Override public void VisitInstanceOfNode(ZInstanceOfNode Node) {
	// 	this.GenerateCode(null, Node.LeftNode());
	// 	this.CurrentBuilder.AppendToken("instanceof");
	// 	this.GenerateTypeName(Node.RightNode().Type);
	// }

	protected String GetBinaryOperator(BType Type, BToken Token) {
		if(Token.EqualsText("<=")) {
			return "=<";
		}
		if(Token.EqualsText("==")) {
			return "=:=";
		}
		if(Token.EqualsText("!=")) {
			return "=/=";
		}
		if(Token.EqualsText("<<")) {
			return "bsl";
		}
		if(Token.EqualsText(">>")) {
			return "bsr";
		}
		if(Token.EqualsText('%')) {
			return "rem";
		}
		if(Token.EqualsText('/') && Type.Equals(BType.IntType)) {
			return "div";
		}
		return Token.GetText();
	}


	@Override public void VisitBinaryNode(BinaryOperatorNode Node) {
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.CurrentBuilder.Append("(");
		}
		this.GenerateCode(null, Node.LeftNode());
		//		this.CurrentBuilder.AppendToken(Node.SourceToken.GetText());
		@Var String Operator = this.GetBinaryOperator(Node.Type, Node.SourceToken);
		this.CurrentBuilder.AppendToken(Operator);
		this.GenerateCode(null, Node.RightNode());
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.CurrentBuilder.Append(")");
		}
	}

	@Override public void VisitComparatorNode(ComparatorNode Node) {
		this.GenerateCode(null, Node.LeftNode());
		@Var String Operator = this.GetBinaryOperator(Node.Type, Node.SourceToken);
		this.CurrentBuilder.AppendToken(Operator);
		this.GenerateCode(null, Node.RightNode());
	}

	@Override public void VisitAndNode(BunAndNode Node) {
		this.GenerateSurroundCode(Node.LeftNode());
		this.CurrentBuilder.AppendToken(this.AndOperator);
		this.GenerateSurroundCode(Node.RightNode());
	}

	@Override public void VisitOrNode(BunOrNode Node) {
		this.GenerateSurroundCode(Node.LeftNode());
		this.CurrentBuilder.AppendToken(this.OrOperator);
		this.GenerateSurroundCode(Node.RightNode());
	}

	public void AppendGuardAndBlock(BNode Node) {
		if (Node instanceof BunIfNode) {
			BunIfNode IfNode = (BunIfNode)Node;
			this.CurrentBuilder.AppendIndent();
			this.GenerateSurroundCode(IfNode.CondNode());
			this.CurrentBuilder.Append(" ->");
			this.VisitBlockNode((BunBlockNode)IfNode.ThenNode(), ";");
			this.CurrentBuilder.AppendLineFeed();
			if (IfNode.HasElseNode()) {
				this.AppendGuardAndBlock(IfNode.ElseNode());
			} else {
				this.AppendGuardAndBlock(null);
			}
		} else {
			this.CurrentBuilder.IndentAndAppend("true ->");
			if (Node != null) {
				this.VisitBlockNode((BunBlockNode)Node, "");
			} else {
				this.CurrentBuilder.Indent();
				this.CurrentBuilder.AppendLineFeed();
				this.CurrentBuilder.IndentAndAppend(this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, false));
				this.CurrentBuilder.UnIndent();
			}
		}
	}

	@Override public void VisitIfNode(BunIfNode Node) {
		int mark = this.GetLazyMark();

		this.CurrentBuilder.Append("if");
		this.CurrentBuilder.AppendLineFeed();
		this.AppendGuardAndBlock(Node);
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("end");

		this.AppendLazy(mark, this.VarMgr.GenVarTuple(VarFlag.Assigned, true) + " = ");
	}

	@Override public void VisitReturnNode(BunReturnNode Node) {
		this.CurrentBuilder.Append("throw({return, ");
		if (Node.HasReturnExpr()) {
			this.GenerateCode(null, Node.ExprNode());
		} else {
			this.CurrentBuilder.Append("void");
		}
		this.CurrentBuilder.Append("})");
	}

	@Override public void VisitWhileNode(BunWhileNode Node) {
		this.LoopNodeNumber += 1;
		String WhileNodeName = "Loop" + Integer.toString(this.LoopNodeNumber);

		int mark1 = this.GetLazyMark();

		//Generate WhileBlock
		this.VarMgr.FilterStart();
		this.VarMgr.ChangeFilterFlag(VarFlag.None);
		this.VisitBlockNode(Node.BlockNode(), ",");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend(WhileNodeName + "(" + WhileNodeName + ", __Arguments__);");
		this.CurrentBuilder.UnIndent();

		//Generate Else Guard and Block
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("(_, Args) ->");
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("Args");
		this.CurrentBuilder.UnIndent();

		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("end,");

		//Generate While Guard
		this.VarMgr.ChangeFilterFlag(VarFlag.Assigned);
		ZSourceBuilder LazyBuilder = new ZSourceBuilder(this, this.CurrentBuilder);
		ZSourceBuilder BodyBuilder = this.CurrentBuilder;
		this.CurrentBuilder = LazyBuilder;
		this.GenerateCode(null, Node.CondNode());
		this.CurrentBuilder = BodyBuilder;
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
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.AppendIndent();
		int mark2 = this.GetLazyMark();
		this.CurrentBuilder.Append(" = " + WhileNodeName + "(" + WhileNodeName + ", ");
		this.CurrentBuilder.Append(this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, false) + ")");
		this.AppendLazy(mark2, this.VarMgr.GenVarTuple(VarFlag.AssignedByChildScope, true));
	}

	@Override public void VisitBreakNode(BunBreakNode Node) {
		this.CurrentBuilder.Append("throw({break, ");
		//this.VarMgr.GenVarTupleOnlyUsed(false);
		this.BreakMark = this.GetLazyMark();
		this.CurrentBuilder.Append("})");
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

		this.GenerateCode(null, Node.InitValueNode());

		@Var String VarName = this.ToErlangVarName(Node.GetGivenName());
		this.VarMgr.DefineVariable(VarName);
		this.AppendLazy(mark, this.VarMgr.GenVariableName(VarName) + " = ");

		this.CurrentBuilder.Append(",");
		if (Node.GetListSize() > 0) {
			if(Node.HasNextVarNode()) { this.VisitVarDeclNode(Node.NextVarNode()); }
		}
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("pad");
	}

	// protected void VisitTypeAnnotation(ZType Type) {
	// 	this.CurrentBuilder.Append(": ");
	// 	this.GenerateTypeName(Type);
	// }

	@Override public void VisitLetNode(BunLetVarNode Node) {
		this.CurrentBuilder.Append("put(");
		this.CurrentBuilder.Append(Node.GetUniqueName(this));
		this.CurrentBuilder.Append(", ");
		this.GenerateCode(null, Node.InitValueNode());
		this.CurrentBuilder.Append(")");
	}

	@Override
	protected void VisitParamNode(BunLetVarNode Node) {
		String VarName = this.ToErlangVarName(Node.GetGivenName());
		VarName = this.VarMgr.GenVariableName(VarName);
		this.CurrentBuilder.Append(VarName);
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		this.VarMgr.Init();
		this.DefineVariables(Node);

		String FuncName = this.ToErlangFuncName(Node.FuncName());
		if (FuncName.equals("main")) {
			this.HeaderBuilder.Append("-export([main/1]).");
		} else {
			this.HeaderBuilder.Append("-export([" + FuncName + "/" + Node.GetListSize() + "]).");
		}
		this.HeaderBuilder.AppendLineFeed();

		this.CurrentBuilder.Append(FuncName + "_inner");
		this.VisitFuncParamNode("(", Node, ")");
		this.CurrentBuilder.Append("->");
		if (Node.BlockNode() == null) {
			this.CurrentBuilder.AppendIndent();
			this.CurrentBuilder.Append("pass.");
		} else {
			this.GenerateCode(null, Node.BlockNode());
		}

		this.CurrentBuilder.AppendLineFeed();
		this.AppendWrapperFuncDecl(Node);
		this.CurrentBuilder.AppendLineFeed();
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		ZSourceBuilder BodyBuilder = this.CurrentBuilder;
		this.CurrentBuilder = this.HeaderBuilder;

		this.CurrentBuilder.Append("-record(");
		this.CurrentBuilder.Append(this.ToErlangTypeName(Node.ClassName()));
		if(!Node.SuperType().Equals(BClassType._ObjectType)) {
			throw new RuntimeException("\"extends\" is not supported yet");
		}
		this.CurrentBuilder.Append(", {");
		@Var int i = 0;
		@Var int size = Node.GetListSize();
		while (i < size) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.CurrentBuilder.Append(this.ToErlangTypeName(FieldNode.GetGivenName()));
			this.CurrentBuilder.AppendToken("=");
			this.GenerateCode(null, FieldNode.InitValueNode());
			if (i < size - 1) {
				this.CurrentBuilder.AppendWhiteSpace();
				this.CurrentBuilder.Append(",");
			}
			i = i + 1;
		}
		this.CurrentBuilder.Append("}).");
		this.CurrentBuilder.AppendLineFeed();

		this.CurrentBuilder = BodyBuilder;
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
	protected void VisitListNode(String OpenToken, AbstractListNode VargNode, String DelimToken, String CloseToken) {
		this.CurrentBuilder.Append(OpenToken);
		@Var int i = 0;
		while(i < VargNode.GetListSize()) {
			@Var BNode ParamNode = VargNode.GetListAt(i);
			if (i > 0) {
				this.CurrentBuilder.Append(DelimToken);
			}
			this.GenerateCode(null, ParamNode);
			i = i + 1;
		}
		this.CurrentBuilder.Append(CloseToken);
	}
	@Override
	protected void VisitListNode(String OpenToken, AbstractListNode VargNode, String CloseToken) {
		this.VisitListNode(OpenToken, VargNode, ", ", CloseToken);
	}

	private void AppendAssertDecl() {
		// this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= false ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.IndentAndAppend("exit(\"Assertion Failed\");");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= true ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.IndentAndAppend("do_nothing;");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Append("assert(_Expr) ->");
		// this.HeaderBuilder.AppendLineFeed();
		// this.HeaderBuilder.Indent();
		// this.HeaderBuilder.IndentAndAppend("exit(\"Assertion Failed (Expr is not true or false)\").");
		// this.HeaderBuilder.UnIndent();
		// this.HeaderBuilder.AppendLineFeed();

		this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= false -> exit(\"Assertion Failed\");");
		this.HeaderBuilder.AppendLineFeed();
		this.HeaderBuilder.Append("assert(_Expr) when _Expr =:= true -> do_nothing;");
		this.HeaderBuilder.AppendLineFeed();
		this.HeaderBuilder.Append("assert(_Expr) -> exit(\"Assertion Failed (Expr is not true or false)\").");
		this.HeaderBuilder.AppendLineFeed();
	}

	private void AppendDigitsDecl() {
		this.HeaderBuilder.Append("digits(N) when is_integer(N) -> integer_to_list(N);");
		this.HeaderBuilder.AppendLineFeed();
		this.HeaderBuilder.Append("digits(0.0) -> \"0.0\";");
		this.HeaderBuilder.AppendLineFeed();
		this.HeaderBuilder.Append("digits(Float) -> float_to_list(Float).");
		this.HeaderBuilder.AppendLineFeed();
	}

	private void AppendZStrDecl() {
		this.HeaderBuilder.Append("zstr(Str) when Str =:= null -> \"null\";");
		this.HeaderBuilder.AppendLineFeed();
		this.HeaderBuilder.Append("zstr(Str) -> Str.");
		this.HeaderBuilder.AppendLineFeed();
	}

	private void AppendWrapperFuncDecl(BunFunctionNode Node) {
		String FuncName = this.ToErlangFuncName(Node.FuncName());
		this.CurrentBuilder.Append(FuncName);
		if (FuncName.equals("main")) { //FIX ME!!
			this.CurrentBuilder.Append("(_)");
		} else {
			this.VisitListNode("(", Node, ")");
		}
		this.CurrentBuilder.Append(" ->");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("try "+ FuncName + "_inner");
		this.VisitListNode("(", Node, ")");
		this.CurrentBuilder.Append(" of");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("_ -> void");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.IndentAndAppend("catch");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.Indent();
		this.CurrentBuilder.IndentAndAppend("throw:{return, Ret} -> Ret;");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.IndentAndAppend("throw:UnKnown -> throw(UnKnown)");
		this.CurrentBuilder.AppendLineFeed();
		this.CurrentBuilder.UnIndent();
		this.CurrentBuilder.IndentAndAppend("end.");

		this.CurrentBuilder.UnIndent();
	}


	private int GetLazyMark() {
		this.CurrentBuilder.Append(null);
		return this.CurrentBuilder.SourceList.size() - 1;
	}
	private void AppendLazy(int mark, String Code) {
		this.CurrentBuilder.SourceList.ArrayValues[mark] = Code;
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
