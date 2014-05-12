package libbun.parser.common;

import libbun.ast.BlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.GroupNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunDefineNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.expression.ApplyNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunFalseNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.literal.BunTrueNode;
import libbun.ast.literal.BunTupleNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.UnaryOperatorNode;

public abstract class BunModelVisitor {

	public abstract void visitDefaultValueNode(DefaultValueNode node);
	public abstract void visitNullNode(BunNullNode node);
	public abstract void visitTrueNode(BunTrueNode node);
	public abstract void visitFalseNode(BunFalseNode node);
	public abstract void visitIntNode(BunIntNode node);
	public abstract void visitFloatNode(BunFloatNode node);
	public abstract void visitStringNode(BunStringNode node);

	public abstract void visitUnaryNode(UnaryOperatorNode node);
	public abstract void visitBinaryNode(BinaryOperatorNode node);

	public abstract void visitGetNameNode(GetNameNode node);
	public abstract void visitGetFieldNode(GetFieldNode node);
	public abstract void visitGetIndexNode(GetIndexNode node);
	public abstract void visitAssignNode(AssignNode node);

	public abstract void visitNewObjectNode(NewObjectNode node);
	public abstract void visitMethodCallNode(MethodCallNode node);
	public abstract void visitFuncCallNode(FuncCallNode node);
	public abstract void visitApplyNode(ApplyNode node);
	public abstract void visitApplyMacroNode(ApplyMacroNode FuncNode);

	//public abstract void visitAsmNode(BunAsmNode node);

	public abstract void visitArrayNode(BunArrayNode node);
	public abstract void visitMapNode(BunMapNode node);
	public abstract void visitTupleNode(BunTupleNode node);

	public abstract void visitCastNode(BunCastNode node);
	public abstract void visitInstanceOfNode(BunInstanceOfNode node);

	public abstract void visitGroupNode(GroupNode node);

	//	public abstract void visitNewArrayNode(ZNewArrayNode node);

	//	public abstract void visitSetIndexNode(SetIndexNode node);
	//	public abstract void visitAssignNode(AssignNode node);
	//	public abstract void visitSetFieldNode(SetFieldNode node);

	public abstract void visitBlockNode(BlockNode node);
	//	public abstract void visitVarblockNode(BunVarBlockNode node);
	public abstract void visitIfNode(BunIfNode node);
	public abstract void visitWhileNode(BunWhileNode node);
	public abstract void visitBreakNode(BunBreakNode node);
	public abstract void visitTryNode(BunTryNode node);
	public abstract void visitThrowNode(BunThrowNode node);
	public abstract void visitReturnNode(BunReturnNode node);

	public abstract void visitDefineNode(BunDefineNode node);
	public abstract void visitLetNode(BunLetVarNode node);
	public abstract void visitFunctionNode(BunFunctionNode node);
	public abstract void visitClassNode(BunClassNode node);

	public abstract void visitSyntaxSugarNode(SyntaxSugarNode node);
	public abstract void visitDesugarNode(DesugarNode node);

	public abstract void visitErrorNode(ErrorNode node);

	//	public abstract void visitTopLevelNode(TopLevelNode node);
	//	public abstract void visitLocalDefinedNode(LocalDefinedNode node);

	private boolean StoppedVisitor;
	public final void EnableVisitor() {
		this.StoppedVisitor = false;
	}

	public final void StopVisitor() {
		this.StoppedVisitor = true;
	}

	public final boolean IsVisitable() {
		return !this.StoppedVisitor;
	}

}
