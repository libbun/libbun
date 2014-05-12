package libbun.ast;

import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public class DesugarNode extends SyntaxSugarNode {
	//	public final static int _NewNode = 0;
	@BField public AstNode OriginalNode;

	public DesugarNode(AstNode OriginalNode, AstNode DesugardNode) {
		super(OriginalNode.ParentNode, 1);
		this.OriginalNode = OriginalNode;
		this.SetChild(OriginalNode, AstNode._EnforcedParent);
		this.SetNode(0, DesugardNode);
		this.Type = OriginalNode.Type;
	}

	public DesugarNode(AstNode OriginalNode, AstNode[] DesugarNodes) {
		super(OriginalNode.ParentNode, DesugarNodes.length);
		this.OriginalNode = OriginalNode;
		@Var int i = 0;
		while(i < DesugarNodes.length) {
			this.SetNode(i, DesugarNodes[i]);
			i = i + 1;
		}
		this.Type = OriginalNode.Type;
	}

	private DesugarNode(AstNode ParentNode, AstNode OriginalNode, int Size) {
		super(ParentNode, Size);
		this.OriginalNode = OriginalNode;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		if(typedClone) {
			return this.dupField(typedClone, new DesugarNode(ParentNode, this.OriginalNode.dup(typedClone, ParentNode), this.AST.length));
		}
		else {
			return this.OriginalNode.dup(typedClone, ParentNode);
		}
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitDesugarNode(this);
	}

	public final boolean IsExpression() {
		return this.size() == 1;
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		if(this.size() != 1) {
			@Var int i = 0;
			while(i < this.size()) {
				TypeChecker.CheckTypeAt(this, i, BType.VoidType);
				i = i + 1;
			}
			this.Type = BType.VoidType;
		}
		else {
			TypeChecker.CheckTypeAt(this, 0, BType.VarType);
			this.Type = this.AST[0].Type;
		}
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChekcer) {
		return this;
	}

}
