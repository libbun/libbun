package libbun.ast.sugar;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.Var;

public final class BunForInNode extends SyntaxSugarNode {
	public final static int _Var   = 0;
	public final static int _List  = 1;
	public final static int _Block = 2;

	public BunForInNode(AstNode ParentNode) {
		super(ParentNode, 3);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunForInNode(ParentNode));
	}

	public final AstNode VarNode() {
		return this.AST[BunForInNode._Var];
	}

	public final AstNode ListNode() {
		return this.AST[BunForInNode._List];
	}

	public final LegacyBlockNode blockNode() {
		@Var AstNode blockNode = this.AST[BunForInNode._Block];
		if(blockNode instanceof LegacyBlockNode) {
			return (LegacyBlockNode)blockNode;
		}
		assert(blockNode == null); // this must not happen
		return null;
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		TypeChecker.CheckTypeAt(this, BunForInNode._List, BType.VarType);
		if(this.ListNode().Type.IsArrayType()) {

		}
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChekcer) {
		// TODO Auto-generated method stub
		return null;
	}
}