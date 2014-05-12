package libbun.ast.sugar;

import libbun.ast.BNode;
import libbun.ast.BlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.Var;

public final class BunForInNode extends SyntaxSugarNode {
	public final static int _Var   = 0;
	public final static int _List  = 1;
	public final static int _Block = 2;

	public BunForInNode(BNode ParentNode) {
		super(ParentNode, 3);
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunForInNode(ParentNode));
	}

	public final BNode VarNode() {
		return this.AST[BunForInNode._Var];
	}

	public final BNode ListNode() {
		return this.AST[BunForInNode._List];
	}

	public final BlockNode blockNode() {
		@Var BNode blockNode = this.AST[BunForInNode._Block];
		if(blockNode instanceof BlockNode) {
			return (BlockNode)blockNode;
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