package libbun.ast.sugar;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunWhileNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BunContinueNode extends SyntaxSugarNode {

	public BunContinueNode(AstNode ParentNode) {
		super(ParentNode, 0);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunContinueNode(ParentNode));
	}

	/**
	while(EXPR) {
        A;
        continue;
	}
	==
	var continue = true;
	while(continue) {
	        continue = false;
	        while($EXPR) {
	                A;
	                continue = true;
	                break;
	        }
	}
	 **/

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		TypeChecker.typeNode(this, BType.VoidType);
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker Typer) {
		@Var BunWhileNode WhileNode = this.LookupWhileNode();
		if(WhileNode == null) {
			return new DesugarNode(this, new LegacyErrorNode(this.ParentNode, this.SourceToken, "continue must be inside the while statement"));
		}
		@Var LegacyBlockNode ParentblockNode = WhileNode.GetScopeLegacyBlockNode();
		//@Var String VarName = Typer.Generator.NameUniqueSymbol("continue");
		@Var String SugarCode = ""     +
				"var _continue_ = true in {\n" +
				"  while(_continue_) {\n" +
				"    _continue_ = false;\n"          +
				"    Sugar::While;"   +
				"  }\n"           +
				"}";
		@Var AstNode NewNode = this.ParentNode.ParseExpression(SugarCode);
		NewNode.ReplaceNode("Sugar::While", WhileNode);

		@Var AstNode[] Nodes = null;
		if(WhileNode.HasNextNode()) {
			Nodes = LibBunSystem._NewNodeArray(3);
			Nodes[0] = new AssignNode("_continue_", new BunBooleanNode(true));
			Nodes[1] = WhileNode.NextNode();
			Nodes[2] = new BunBreakNode(null);
		}
		else {
			Nodes = LibBunSystem._NewNodeArray(2);
			Nodes[0] = new AssignNode("_continue_", new BunBooleanNode(true));
			Nodes[1] = new BunBreakNode(null);
		}
		ParentblockNode.ReplaceWith(WhileNode, NewNode);
		return this.ReplaceContinue(WhileNode, this, Nodes, null);
	}


	private BunWhileNode LookupWhileNode() {
		@Var AstNode Node = this;
		while(Node != null) {
			if(Node instanceof BunWhileNode) {
				return (BunWhileNode)Node;
			}
			Node = Node.ParentNode;
		}
		return null;
	}

	private DesugarNode ReplaceContinue(AstNode Node, BunContinueNode FirstNode, AstNode[] NodeList, DesugarNode FirstDesugarNode) {
		@Var int i = 0;
		while(i < Node.size()) {
			@Var AstNode SubNode = Node.AST[i];
			if(SubNode instanceof BunContinueNode) {
				@Var DesugarNode DesugarNode = new DesugarNode(SubNode, NodeList);
				if(SubNode == FirstNode) {
					FirstDesugarNode = DesugarNode;
				}
				else {
					Node.SetNode(i, DesugarNode);
				}
				break;
			}
			if(SubNode != null) {
				FirstDesugarNode = this.ReplaceContinue(SubNode, FirstNode, NodeList, FirstDesugarNode);
			}
			i = i + 1;
		}
		return FirstDesugarNode;
	}

	//	public DesugarNode PerformDesugar2(LibBunTypeChecker Typer) {
	//		@Var BunWhileNode WhileNode = this.LookupWhileNode();
	//		if(WhileNode == null) {
	//			return new DesugarNode(this, new ErrorNode(this.ParentNode, this.SourceToken, "continue must be inside the while statement"));
	//		}
	//		@Var BlockNode ParentblockNode = WhileNode.GetScopeblockNode();
	//		@Var String VarName = Typer.Generator.NameUniqueSymbol("continue");
	//		@Var BunVarBlockNode VarNode = Typer.CreateVarNode(null, VarName, BType.BooleanType, new BunBooleanNode(true));
	//		@Var BunWhileNode ContinueWhile = VarNode.SetNewWhileNode(BNode._AppendIndex, Typer);
	//		ContinueWhile.SetNewGetNameNode(BunWhileNode._Cond, Typer, VarName, BType.BooleanType);
	//		@Var BlockNode WhileblockNode = ContinueWhile.SetNewblockNode(BunWhileNode._Block, Typer);
	//		WhileblockNode.Append(new AssignNode(VarName, new BunBooleanNode(false)));
	//		WhileblockNode.Append(WhileNode);
	//
	//		@Var BNode[] Nodes = null;
	//		if(WhileNode.HasNextNode()) {
	//			Nodes = LibBunSystem._NewNodeArray(3);
	//			Nodes[0] = new AssignNode(VarName, new BunBooleanNode(true));
	//			Nodes[1] = WhileNode.NextNode();
	//			Nodes[2] = new BunBreakNode(null);
	//		}
	//		else {
	//			Nodes = LibBunSystem._NewNodeArray(2);
	//			Nodes[0] = new AssignNode(VarName, new BunBooleanNode(true));
	//			Nodes[1] = new BunBreakNode(null);
	//		}
	//		ParentblockNode.ReplaceWith(WhileNode, VarNode);
	//		return this.ReplaceContinue(WhileNode, this, Nodes, null);
	//	}

}
