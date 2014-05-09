package libbun.ast.sugar;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.literal.BunStringNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.Var;

public class BunAssertNode extends SyntaxSugarNode {
	public final static int _Expr = 0;

	public BunAssertNode(BNode ParentNode) {
		super(ParentNode, 1);
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunAssertNode(ParentNode));
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		TypeChecker.CheckTypeAt(this, BunAssertNode._Expr, BType.BooleanType);
		TypeChecker.TypeNode(this, BType.VoidType);
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChecker) {
		@Var BFormFunc Func = TypeChecker.Generator.GetFormFunc("assert", BType.BooleanType, 2);
		if(Func != null) {
			@Var AbstractListNode FuncNode = TypeChecker.CreateDefinedFuncCallNode(this.ParentNode, this.SourceToken, Func);
			FuncNode.Append(this.AST[BunAssertNode._Expr]);
			FuncNode.Append(new BunStringNode(FuncNode, null, this.GetSourceLocation()));
			return new DesugarNode(this, FuncNode);
		}
		else {
			@Var FuncCallNode MacroNode = TypeChecker.CreateFuncCallNode(this.ParentNode, this.SourceToken, "assert", BFuncType._FuncType);
			MacroNode.Append(this.AST[BunAssertNode._Expr]);
			return new DesugarNode(this, MacroNode);
		}
	}


}
