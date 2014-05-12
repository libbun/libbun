package libbun.lang.bun.shell;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.literal.BunArrayNode;
import libbun.common.CommonArray;
import libbun.parser.classic.BToken;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public class CommandNode extends SyntaxSugarNode {
	@BField private final CommonArray<AstNode> ArgList;
	@BField private BType RetType = BType.VarType;
	@BField public CommandNode PipedNextNode;

	public CommandNode(AstNode ParentNode, BToken Token, String Command) {
		super(ParentNode, 0);
		this.SourceToken = Token;
		this.PipedNextNode = null;
		this.ArgList = new CommonArray<AstNode>(new AstNode[]{});
		this.AppendArgNode(new ArgumentNode(ParentNode, Command));
	}

	public void AppendArgNode(AstNode Node) {
		this.ArgList.add(this.SetChild(Node, true));
	}

	public AstNode AppendPipedNextNode(CommandNode Node) {
		@Var CommandNode CurrentNode = this;
		while(CurrentNode.PipedNextNode != null) {
			CurrentNode = CurrentNode.PipedNextNode;
		}
		CurrentNode.PipedNextNode = (CommandNode) CurrentNode.SetChild(Node, false);
		return this;
	}

	public int GetArgSize() {
		return this.ArgList.size();
	}

	public void SetArgAt(int Index, AstNode ArgNode) {
		CommonArray.SetIndex(this.ArgList, Index, ArgNode);
	}

	public AstNode GetArgAt(int Index) {
		return CommonArray.GetIndex(this.ArgList, Index);
	}

	public void SetType(BType Type) {
		this.RetType = Type;
	}

	public BType RetType() {
		return this.RetType;
	}

	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		if(this.RetType().IsVarType()) {
			if(ContextType.IsBooleanType() || ContextType.IsIntType() || ContextType.IsStringType()) {
				this.SetType(ContextType);
			}
			else if(ContextType.IsVarType() && !(this.ParentNode instanceof LegacyBlockNode)) {
				this.SetType(BType.StringType);
			}
			else {
				this.SetType(BType.IntType);
			}
		}
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChecker) {
		@Var String FuncName = "ExecCommandInt";
		if(this.RetType().IsBooleanType()) {
			FuncName = "ExecCommandBoolean";
		}
		else if(this.RetType().IsStringType()) {
			FuncName = "ExecCommandString";
		}
		@Var BunArrayNode ArrayNode = new BunArrayNode(this.ParentNode);
		@Var CommandNode CurrentNode = this;
		while(CurrentNode != null) {
			@Var BunArrayNode SubArrayNode = new BunArrayNode(ArrayNode);
			@Var int size = CurrentNode.GetArgSize();
			@Var int i = 0;
			while(i < size) {
				SubArrayNode.appendNode(CurrentNode.GetArgAt(i));
				i = i + 1;
			}
			ArrayNode.appendNode(SubArrayNode);
			CurrentNode = CurrentNode.PipedNextNode;
		}
		@Var FuncCallNode Node = new FuncCallNode(this.ParentNode, new GetNameNode(this.ParentNode, FuncName));
		Node.appendNode(ArrayNode);
		return new DesugarNode(this, Node);
	}
}