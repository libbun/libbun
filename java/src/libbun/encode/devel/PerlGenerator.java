package libbun.encode.devel;

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
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayLiteralNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapLiteralNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
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
import libbun.encode.LibBunSourceGenerator;
import libbun.parser.LibBunLangInfo;

public class PerlGenerator extends LibBunSourceGenerator {

	public PerlGenerator() {
		super(new LibBunLangInfo("Perl-5.16","pl"));
	}

	@Override protected void GenerateStatementEnd(BNode Node) {
	}

	@Override
	protected void GenerateImportLibrary(String LibName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNullNode(BunNullNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBooleanNode(BunBooleanNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitIntNode(BunIntNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitFloatNode(BunFloatNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitStringNode(BunStringNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNotNode(BunNotNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitPlusNode(BunPlusNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMinusNode(BunMinusNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitComplementNode(BunComplementNode Node) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitSubNode(BunSubNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMulNode(BunMulNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitDivNode(BunDivNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitModNode(BunModNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLeftShiftNode(BunLeftShiftNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitRightShiftNode(BunRightShiftNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBitwiseAndNode(BunBitwiseAndNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBitwiseOrNode(BunBitwiseOrNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBitwiseXorNode(BunBitwiseXorNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitEqualsNode(BunEqualsNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNotEqualsNode(BunNotEqualsNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLessThanNode(BunLessThanNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitLessThanEqualsNode(BunLessThanEqualsNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitGreaterThanNode(BunGreaterThanNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitGroupNode(GroupNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitArrayLiteralNode(BunArrayLiteralNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitMapLiteralNode(BunMapLiteralNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitNewObjectNode(NewObjectNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitFuncCallNode(FuncCallNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitGetNameNode(GetNameNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitAssignNode(AssignNode Node) {
		// TODO Auto-generated method stub

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

	@Override
	public void VisitBlockNode(BunBlockNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitVarBlockNode(BunVarBlockNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitIfNode(BunIfNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitReturnNode(BunReturnNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitBreakNode(BunBreakNode Node) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

	@Override
	public void VisitFunctionNode(BunFunctionNode Node) {
		// TODO Auto-generated method stub

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
