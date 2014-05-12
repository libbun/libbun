package libbun.lang.bun.shell;

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
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
	@BField private final CommonArray<BNode> ArgList;
	@BField private BType RetType = BType.VarType;
	@BField public CommandNode PipedNextNode;

	public CommandNode(BNode ParentNode, BToken Token, String Command) {
		super(ParentNode, 0);
		this.SourceToken = Token;
		this.PipedNextNode = null;
		this.ArgList = new CommonArray<BNode>(new BNode[]{});
		this.AppendArgNode(new ArgumentNode(ParentNode, Command));
	}

	public void AppendArgNode(BNode Node) {
		this.ArgList.add(this.SetChild(Node, true));
	}

	public BNode AppendPipedNextNode(CommandNode Node) {
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

	public void SetArgAt(int Index, BNode ArgNode) {
		CommonArray.SetIndex(this.ArgList, Index, ArgNode);
	}

	public BNode GetArgAt(int Index) {
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
			else if(ContextType.IsVarType() && !(this.ParentNode instanceof BunBlockNode)) {
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
				SubArrayNode.Append(CurrentNode.GetArgAt(i));
				i = i + 1;
			}
			ArrayNode.Append(SubArrayNode);
			CurrentNode = CurrentNode.PipedNextNode;
		}
		@Var FuncCallNode Node = new FuncCallNode(this.ParentNode, new GetNameNode(this.ParentNode, null, FuncName));
		Node.Append(ArrayNode);
		return new DesugarNode(this, Node);
	}
}