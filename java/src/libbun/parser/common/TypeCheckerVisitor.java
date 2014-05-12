package libbun.parser.common;

import libbun.ast.AstNode;
import libbun.ast.BlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.GroupNode;
import libbun.ast.LocalDefinedNode;
import libbun.ast.SyntaxSugarNode;
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
import libbun.ast.decl.DefSymbolNode;
import libbun.ast.decl.TopLevelNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunAsmNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.literal.BunFalseNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.ast.literal.LiteralNode;
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
import libbun.parser.classic.BNodeUtils;
import libbun.parser.classic.LibBunGamma;
import libbun.type.BClassType;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.Var;

public class TypeCheckerVisitor extends TypeChecker {

	@Override
	public void VisitDefaultValueNode(DefaultValueNode node) {
		@Var BType Type = this.GetContextType();
		if(Type.IsIntType()) {
			this.ReturnTypeNode(new BunIntNode(node.ParentNode, 0), Type);
			return;
		}
		if(Type.IsBooleanType()) {
			this.ReturnTypeNode(new BunFalseNode(node.ParentNode), Type);
			return;
		}
		if(Type.IsFloatType()) {
			this.ReturnTypeNode(new BunFloatNode(node.ParentNode, 0.0), Type);
			return;
		}
		if(!Type.IsVarType()) {
			this.ReturnTypeNode(new BunNullNode(node.ParentNode), Type);
			return;
		}
		this.ReturnTypeNode(node, Type);
	}

	@Override
	public void VisitNullNode(BunNullNode node) {
		@Var BType Type = this.GetContextType();
		this.ReturnTypeNode(node, Type);
	}

	@Override
	public void VisitBooleanNode(BunBooleanNode node) {
		this.ReturnTypeNode(node, BType.BooleanType);
	}

	@Override
	public void VisitIntNode(BunIntNode node) {
		this.ReturnTypeNode(node, BType.IntType);
	}

	@Override
	public void VisitFloatNode(BunFloatNode node) {
		this.ReturnTypeNode(node, BType.FloatType);
	}

	@Override
	public void VisitStringNode(BunStringNode node) {
		this.ReturnTypeNode(node, BType.StringType);
	}

	@Override
	public void VisitUnaryNode(UnaryOperatorNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType);
	}

	@Override
	public void VisitNotNode(BunNotNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.BooleanType);
	}

	@Override
	public void VisitPlusNode(BunPlusNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType);
	}

	@Override
	public void VisitMinusNode(BunMinusNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType);
	}

	@Override
	public void VisitComplementNode(BunComplementNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType);
	}

	@Override
	public void VisitCastNode(BunCastNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBinaryNode(BinaryOperatorNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitInstanceOfNode(BunInstanceOfNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitAndNode(BunAndNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.BooleanType, BType.BooleanType);
	}

	@Override
	public void VisitOrNode(BunOrNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.BooleanType, BType.BooleanType);
	}

	@Override
	public void VisitAddNode(BunAddNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitSubNode(BunSubNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitMulNode(BunMulNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitDivNode(BunDivNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitModNode(BunModNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitLeftShiftNode(BunLeftShiftNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitRightShiftNode(BunRightShiftNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitBitwiseAndNode(BunBitwiseAndNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitBitwiseOrNode(BunBitwiseOrNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitBitwiseXorNode(BunBitwiseXorNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitEqualsNode(BunEqualsNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitNotEqualsNode(BunNotEqualsNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitLessThanNode(BunLessThanNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitLessThanEqualsNode(BunLessThanEqualsNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitGreaterThanNode(BunGreaterThanNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode node) {
		this.returnNodeAsFuncType(node.GetOperator(), node, 0, BType.VarType, BType.VarType);
	}

	@Override
	public void VisitGroupNode(GroupNode node) {
		BType contextType = this.GetContextType();
		this.CheckTypeAt(node, GroupNode._Expr, contextType);
		this.ReturnTypeNode(node, node.getTypeAt(GroupNode._Expr));
	}

	@Override
	public void VisitAsmNode(BunAsmNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLiteralNode(LiteralNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitArrayLiteralNode(BunArrayNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMapLiteralNode(BunMapNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNewObjectNode(NewObjectNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMethodCallNode(MethodCallNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitFuncCallNode(FuncCallNode node) {
		GetNameNode nameNode = node.getNameNode();
		if(nameNode != null) {
			SymbolTable table = nameNode.getLocalSymbolTable();
			String name = nameNode.getSimpleName();
			DefSymbolNode varNode = table.GetSymbol(name);
			if(varNode != null) {
				this.returnNodeAsFuncType(name, node, 1);
				return;
			}
		}
		this.CheckTypeAt(node, FuncCallNode._Functor, BType.VarType);
		@Var BType FuncNodeType = node.getTypeAt(FuncCallNode._Functor);
		if(FuncNodeType instanceof BFuncType) {
			this.checkTypeByFuncType(node, 1, (BFuncType)FuncNodeType);
			this.ReturnNode(node);
		}
		this.ReturnTypeErrorNode("not function: " + FuncNodeType + " of node " + node.FunctorNode(), node);
	}

	@Override
	public void visitApplyMacroNode(ApplyMacroNode FuncNode) {
		// TODO Auto-generated method stub

	}

	@Override public void VisitGetNameNode(GetNameNode node) {
		@Var SymbolTable table = node.getLocalSymbolTable();
		DefSymbolNode varNode = table.GetSymbol(node.GivenName);
		if(varNode instanceof BunLetVarNode) {
			node.ResolvedNode = (BunLetVarNode)varNode;
			varNode.Used();
			if(varNode.InitValueNode() instanceof BunAsmNode) {
				this.ReturnTypeNode(varNode.InitValueNode(), varNode.DeclType());
				return;
			}
			this.ReturnTypeNode(node, varNode.DeclType());
			return;
		}
		this.ReturnTypeNode(node, BType.VarType);
	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.returnNodeAsFuncType("." + Node.GetName(), Node, 1);
	}

	@Override
	public void VisitGetIndexNode(GetIndexNode node) {
		this.returnNodeAsFuncType("[]", node, 0);
	}

	@Override
	public void VisitAssignNode(AssignNode node) {

	}


	@Override
	public void VisitBlockNode(BlockNode node) {
		for(int i = 0; i < node.size(); i++) {
			@Var AstNode subNode = node.AST[i];
			@Var AstNode typedNode = this.CheckType(subNode, BType.VoidType);
			while(subNode != node.AST[i]) {  // detecting replacement
				subNode = node.AST[i];
				typedNode = this.CheckType(subNode, BType.VoidType);
			}
			if(typedNode != subNode) {
				node.SetNode(i, typedNode);
			}
			if(BNodeUtils._IsBlockBreak(typedNode)) {
				node.ClearListToSize(i+1);
				break;
			}
		}
		this.ReturnTypeNode(node, BType.VoidType);
	}

	@Override
	public void VisitVarblockNode(BunVarBlockNode node) {

	}

	@Override
	public void VisitIfNode(BunIfNode node) {
		this.CheckTypeAt(node, BunIfNode._Cond, BType.BooleanType);
		this.CheckTypeAt(node, BunIfNode._Then, BType.VoidType);
		if(node.HasElseNode()) {
			this.CheckTypeAt(node, BunIfNode._Else, BType.VoidType);
		}
		this.ReturnTypeNode(node, BType.VoidType);
	}


	@Override public void VisitWhileNode(BunWhileNode node) {
		this.CheckTypeAt(node, BunWhileNode._Cond, BType.BooleanType);
		this.CheckTypeAt(node, BunWhileNode._Block, BType.VoidType);
		if(node.HasNextNode()) {
			this.CheckTypeAt(node, BunWhileNode._Next, BType.VoidType);
			//Node.blockNode().Append(Node.NextNode());
		}
		this.ReturnTypeNode(node, BType.VoidType);
	}

	@Override public void VisitBreakNode(BunBreakNode node) {
		this.ReturnTypeNode(node, BType.VoidType);
	}

	@Override public void VisitThrowNode(BunThrowNode node) {
		//		BunFunctionNode FuncNode = this.FindParentFuncNode(node);
		//		if(FuncNode != null && FuncNode == node.GetDefiningFunctionNode()) {
		//			this.CurrentFunctionNode.SetReturnType(BType.VoidType);
		//		}
		this.CheckTypeAt(node, BunThrowNode._Expr, BType.VarType);
		this.ReturnTypeNode(node, BType.VoidType);
	}

	//	private BunFunctionNode FindParentFuncNode(BNode Node) {
	//		if(Node == null) {
	//			return null;
	//		}
	//		if(Node instanceof BlockNode && !(Node instanceof BunVarBlockNode)) {
	//			if(Node.ParentNode != null && Node.ParentNode instanceof BunFunctionNode) {
	//				return (BunFunctionNode) Node.ParentNode;
	//			}
	//		}
	//		else {
	//			return this.FindParentFuncNode(Node.ParentNode);
	//		}
	//		return null;
	//	}

	@Override
	public void VisitTryNode(BunTryNode node) {
		this.CheckTypeAt(node, BunTryNode._Try, BType.VoidType);
		if(node.HasCatchblockNode()) {
			@Var LibBunGamma Gamma = node.CatchblockNode().GetBlockGamma();
			@Var BunLetVarNode VarNode = new BunLetVarNode(node, BunLetVarNode._IsReadOnly, null, null);
			VarNode.GivenName = node.ExceptionName();
			VarNode.GivenType = BClassType._ObjectType;
			Gamma.SetSymbol(VarNode.GetGivenName(), VarNode);
			this.CheckTypeAt(node, BunTryNode._Catch, BType.VoidType);
		}
		if(node.HasFinallyblockNode()) {
			this.CheckTypeAt(node, BunTryNode._Finally, BType.VoidType);
		}
		this.ReturnTypeNode(node, BType.VoidType);
	}

	@Override
	public void VisitReturnNode(BunReturnNode node) {
		//		if(node.IsTopLevel()) {
		//			this.ReturnErrorNode(node, node.SourceToken, "only available inside function");
		//			return;
		//		}
		//		@Var BType ReturnType = this.CurrentFunctionNode.ReturnType();
		//		if(node.HasReturnExpr() && ReturnType.IsVoidType()) {
		//			node.AST[BunReturnNode._Expr] = null;
		//		}
		//		else if(!node.HasReturnExpr() && !ReturnType.IsVarType() && !ReturnType.IsVoidType()) {
		//			BunLogger._LogWarning(node.SourceToken, "returning default value of " + ReturnType);
		//			node.SetNode(BunReturnNode._Expr, new DefaultValueNode(node));
		//		}
		//		if(node.HasReturnExpr()) {
		//			this.CheckTypeAt(node, BunReturnNode._Expr, ReturnType);
		//		}
		//		else {
		//			if(ReturnType instanceof BVarType) {
		//				((BVarType)ReturnType).Infer(BType.VoidType, node.SourceToken);
		//			}
		//		}
		this.ReturnTypeNode(node, BType.VoidType);
	}

	@Override
	public void VisitLetNode(BunLetVarNode node) {

		if(node.isTopLevel()) {
			@Var BType DeclType = node.DeclType();
			this.CheckTypeAt(node, BunLetVarNode._InitValue, DeclType);
			@Var BType ConstType = node.InitValueNode().Type;
			node.setTypeAt(BunLetVarNode._NameInfo, ConstType);
			if(DeclType.IsVarType()) {
				node.SetDeclType(ConstType);
			}
			node.GetGamma().SetSymbol(node.GetGivenName(), node);
			this.ReturnTypeNode(node, BType.VoidType);
		}
		else {
			@Var BType ContextType = this.GetContextType();
			@Var AstNode blockNode = new BunVarBlockNode(node.ParentNode, node, node.GetScopeblockNode());
			blockNode = this.CheckType(blockNode, ContextType);
			this.ReturnNode(blockNode);
		}
	}

	@Override public void VisitFunctionNode(BunFunctionNode node) {
		if(!BNodeUtils._HasFunctionBreak(node.blockNode())) {
			node.blockNode().SetNode(AstNode._AppendIndex, new BunReturnNode(node));
		}
		@Var SymbolTable blockTable = node.blockNode().getBlockSymbolTable();
		this.pushFunctionStack(node);
		for(int i = 0; i < node.getParamSize(); i++) {
			@Var BunLetVarNode paramNode = node.GetParamNode(i);
			paramNode.SetDeclType(this.newVarType(paramNode.DeclType(), paramNode));
			blockTable.SetSymbol(paramNode.GetGivenName(), paramNode);
		}
		node.SetReturnType(this.newVarType(node.ReturnType(), node));
		for(int safeCount = 0; safeCount < 10; safeCount++) {
			if(!this.defineFunction(node, false/*Enforced*/)) {
				this.ReturnErrorNode(node, "redefinition of function: " + node.FuncName() + ": " + node.GetFuncType());
			}
			this.CheckTypeAt(node, BunFunctionNode._Block, BType.VoidType);
			if(!node.blockNode().IsUntyped()) {
				break;
			}
		}
		if(!this.defineFunction(node, true/*Enforced*/)) {
			this.ReturnErrorNode(node, "ambigious function: " + node.FuncName() + ": " + node.GetFuncType());
		}
		this.popFunctionStack(node);
		this.ReturnTypeNode(node, node.GetFuncType());
	}

	private BType newVarType(BType type, AstNode node) {
		return type;
	}

	private boolean defineFunction(BunFunctionNode node, boolean Enforced) {
		String funcName =  node.FuncName();
		if(funcName != null && node.GivenType == null) {
			@Var BFuncType FuncType = node.GetFuncType();
			if(!FuncType.IsVarType()) {
				@Var SymbolTable table = node.getSymbolTable();
				DefSymbolNode f = table.getSymbol(funcName, FuncType);
				if(f != null) {
					return false;
				}
				table.setSymbol(funcName, FuncType, node);
				return true;
			}
			if(Enforced) {
				return false;
			}
		}
		return true;
	}

	private void pushFunctionStack(BunFunctionNode node) {
		//		this.CurrentFunctionNode = FunctionNode.Push(this.CurrentFunctionNode);
		//		this.VarScope = new BVarScope(this.VarScope, this.Logger, null);
	}

	private void popFunctionStack(BunFunctionNode node) {
		//		this.CurrentFunctionNode = this.CurrentFunctionNode.Pop();
		//		this.VarScope = this.VarScope.Parent;
	}

	@Override
	public void VisitClassNode(BunClassNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitTopLevelNode(TopLevelNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitSyntaxSugarNode(SyntaxSugarNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitDesugarNode(DesugarNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLocalDefinedNode(LocalDefinedNode node) {
		// TODO Auto-generated method stub

	}

}
