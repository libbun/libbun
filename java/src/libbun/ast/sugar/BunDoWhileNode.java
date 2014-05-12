package libbun.ast.sugar;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.statement.BunWhileNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.Var;

public final class BunDoWhileNode extends SyntaxSugarNode {
	public final static int _Cond  = 0;
	public final static int _Block = 1;
	public final static int _Next  = 2;   // optional iteration statement

	public BunDoWhileNode(AstNode ParentNode) {
		super(ParentNode, 3);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunDoWhileNode(ParentNode));
	}

	public BunDoWhileNode(AstNode CondNode, LegacyBlockNode blockNode) {
		super(null, 3);
		this.SetNode(BunDoWhileNode._Cond, CondNode);
		this.SetNode(BunDoWhileNode._Block, blockNode);
		this.Type = BType.VoidType;
	}

	public final AstNode CondNode() {
		return this.AST[BunDoWhileNode._Cond];
	}

	public final LegacyBlockNode blockNode() {
		@Var AstNode blockNode = this.AST[BunDoWhileNode._Block];
		if(blockNode instanceof LegacyBlockNode) {
			return (LegacyBlockNode)blockNode;
		}
		assert(blockNode == null); // this must not happen
		return null;
	}

	public final boolean HasNextNode() {
		return (this.AST[BunDoWhileNode._Next] != null);
	}

	public final AstNode NextNode() {
		return this.AST[BunDoWhileNode._Next];
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		TypeChecker.CheckTypeAt(this, BunWhileNode._Cond, BType.BooleanType);
		TypeChecker.CheckTypeAt(this, BunWhileNode._Block, BType.VoidType);
		if(this.HasNextNode()) {
			TypeChecker.CheckTypeAt(this, BunWhileNode._Next, BType.VoidType);
			this.blockNode().appendNode(this.NextNode());
		}
		TypeChecker.ReturnTypeNode(this, BType.VoidType);
	}

	/**
	do {
        A;
	}while(EXPR);
	==
	while(true) {
		A;
		if(!EXPR) {
	        break;
	    }
	}
	 **/


	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChekcer) {
		@Var String SugarCode = ""     +
				"while(true) {\n" +
				"  Block::X;\n"          +
				"  if(!Expr::Y) {\n"    +
				"    break;\n"    +
				"  }\n"           +
				"}";
		@Var AstNode ParentNode = this.ParentNode;
		@Var AstNode WhileNode = ParentNode.ParseExpression(SugarCode);
		WhileNode.ReplaceNode("Block::X", this.blockNode());
		WhileNode.ReplaceNode("Expr::Y", this.CondNode());
		System.out.println("WhileNode: " + WhileNode);
		return new DesugarNode(this, WhileNode);
	}

}
