package libbun.parser.ssa;

import java.util.HashMap;

import libbun.ast.AstNode;
import libbun.ast.LegacyBlockNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunWhileNode;
import libbun.common.CommonArray;
import libbun.common.CommonMap;
import libbun.encode.LibBunGenerator;
import libbun.type.BType;
import libbun.util.Var;

/**
 * @see
 * Brandis, M. M. and Moessenboeck, H.: Single-pass Generation of Static
 * Single-assignment Form for Structured Languages, ACM Trans.
 * Program. Lang. Syst., Vol. 16, No. 6, pp. 1684-1698
 */

public class SSAConverter extends ZASTTransformer {
	private static final int IfThenBranchIndex = 0;
	private static final int IfElseBranchIndex = 1;
	private static final int WhileBodyBranchIndex = IfElseBranchIndex;

	public SSAConverterState State;
	public CommonArray<Variable> LocalVariables;
	public ValueReplacer Replacer;
	public CommonMap<Integer> ValueNumber;
	private final HashMap<AstNode, CommonArray<Variable>> CurVariableTableBefore;
	private final HashMap<AstNode, CommonArray<Variable>> CurVariableTableAfter;
	private final LibBunGenerator Generator;

	public SSAConverter(LibBunGenerator Generator) {
		this.Generator = Generator;
		this.LocalVariables = null;
		this.Replacer = new ValueReplacer(Generator);
		this.State = new SSAConverterState(null, -1);
		this.ValueNumber = new CommonMap<Integer>(BType.IntType);
		this.CurVariableTableBefore = new HashMap<AstNode, CommonArray<Variable>>();
		this.CurVariableTableAfter = new HashMap<AstNode, CommonArray<Variable>>();
	}

	private void RecordListOfVariablesBeforeVisit(AstNode Node) {
		this.CurVariableTableBefore.put(Node, this.CloneCurrentValues());
	}

	private void RecordListOfVariablesAfterVisit(AstNode Node) {
		this.CurVariableTableAfter.put(Node, this.CloneCurrentValues());
	}

	/**
	 * Returns the variable information prior to processing the Node.
	 * @param Node
	 * @return	List of information of local variables.
	 * 			The information contains variable name and variable index
	 * example:
	 *   int x = 0;
	 *   int y = 0;
	 *   x = y;
	 *  GetCurrentVariablesBefore(int x = 0) returns []
	 *  GetCurrentVariablesBefore(int y = 0) returns [(x, 0)]
	 *  GetCurrentVariablesBefore(x = y    ) returns [(x, 0), (y,0)]
	 */
	public CommonArray<Variable> GetCurrentVariablesBefore(AstNode Node) {
		return this.CurVariableTableBefore.get(Node);
	}

	/**
	 * Returns the variable information after processing the Node.
	 * @param Node
	 * @return	List of information of local variables.
	 * 			The information contains variable name and variable index
	 * example:
	 *   int x = 0;
	 *   int y = 0;
	 *   x = y;
	 *  GetCurrentVariablesAfter(int x = 0) returns []
	 *  GetCurrentVariablesAfter(int y = 0) returns [(x, 0)]
	 *  GetCurrentVariablesAfter(x = y    ) returns [(x, 0), (y,0)]
	 */
	public CommonArray<Variable> GetCurrentVariablesAfter(AstNode Node) {
		return this.CurVariableTableAfter.get(Node);
	}

	private void PushState(SSAConverterState State) {
		State.SetPrev(this.State);
		this.State = State;
	}

	private void PopState() {
		this.State = this.State.Prev;
	}

	private JoinNode GetCurrentJoinNode() {
		return this.State.Node;
	}

	private int GetCurrentBranchIndex() {
		return this.State.BranchIndex;
	}

	private void SetCurrentBranchIndex(int BranchIndex) {
		this.State.BranchIndex = BranchIndex;
	}

	private JoinNode GetParentJoinNode() {
		return this.State.Prev.Node;
	}

	private int GetParentBranchIndex() {
		return this.State.Prev.BranchIndex;
	}

	private int GetVariableIndex(String Name) {
		@Var int i = this.LocalVariables.size() - 1;
		while(i >= 0) {
			@Var Variable V = CommonArray.GetIndex(this.LocalVariables, i);
			if(V != null && V.Name.equals(Name)) {
				return i;
			}
			i = i - 1;
		}
		assert(false); // unreachable
		return -1;
	}

	private Variable FindVariable(String Name) {
		return CommonArray.GetIndex(this.LocalVariables, this.GetVariableIndex(Name));
	}

	private void RemoveVariable(String Name) {
		CommonArray.SetIndex(this.LocalVariables, this.GetVariableIndex(Name), null);
	}

	private void UpdateVariable(Variable NewVal) {
		@Var int Index = this.GetVariableIndex(NewVal.Name);
		CommonArray.SetIndex(this.LocalVariables, Index, NewVal);
	}

	private void AddVariable(Variable V) {
		this.UpdateValueNumber(V, false);
		this.LocalVariables.add(V);
	}

	private int UpdateValueNumber(Variable V, boolean UpdateValue) {
		@Var Integer Num = this.ValueNumber.GetOrNull(V.Name);
		if(Num == null) {
			Num = 0;
		}
		if(Num < V.Index) {
			Num = V.Index;
		}
		if(UpdateValue) {
			Num = Num + 1;
		}
		this.ValueNumber.put(V.Name, Num);
		return Num;
	}

	private int GetRefreshNumber(Variable Val) {
		return this.UpdateValueNumber(Val, true);
	}

	private CommonArray<Variable> CloneCurrentValues() {
		return new CommonArray<Variable>(this.LocalVariables.CompactArray());
	}

	private void InsertPHI(JoinNode JNode, int BranchIndex, Variable OldVal, Variable NewVal) {
		if(JNode == null) { // top-level statament.
			return;
		}
		// 1. Find PHINode from JoinNode
		@Var PHINode phi = JNode.FindPHINode(OldVal);

		// 2. If PHINode for OldVal.Name is not defined, create new one.
		if(phi == null) {
			phi = new PHINode(OldVal, NewVal.Name);
			phi.VarRef = new Variable(OldVal.Name, this.GetRefreshNumber(OldVal), phi);
			JNode.Append(phi);
			if(this.GetCurrentJoinNode().ParentNode instanceof BunWhileNode) {
				BunWhileNode WNode = (BunWhileNode) this.GetCurrentJoinNode().ParentNode;
				this.ReplaceNodeWith(WNode, OldVal, phi);
			}
		}
		// 3. Added Incomming Variable infomation
		phi.AddIncoming(BranchIndex, null/*FIXME*/, NewVal.Node);
	}

	private void ReplaceNodeWith(AstNode Node, Variable OldVal, PHINode PHI) {
		this.Replacer.SetTarget(OldVal.Node, PHI);
		Node.Accept(this.Replacer);
	}

	private void MakeCurrentVariableTo(Variable NewVal) {
		this.UpdateVariable(NewVal);
	}

	private void CommitPHINode(JoinNode JNode) {
		if(this.GetParentJoinNode() == null) {
			return;
		}
		@Var int i = JNode.getPhiSize() - 1;
		while(i >= 0) {
			PHINode phi = JNode.ListAt(i);
			AstNode node;
			if (JNode.isJoinNodeOfRepeatNode()) {
				//this.State.Prev != null && this.GetParentJoinNode().ParentNode instanceof ZWhileNode
				node = phi.GetArgument(phi.Args.size() - 1);
			} else {
				node = phi.GetArgument(this.GetCurrentBranchIndex());
			}
			Variable val = this.FindVariable(NodeLib.GetVarName(node, this.Generator));
			this.MakeCurrentVariableTo(val);
			this.InsertPHI(this.GetParentJoinNode(), this.GetParentBranchIndex(), phi.BackupValue, val);
			i = i - 1;
		}
	}

	/**
	 * Merge JoinNode into a parent node of TargetNode
	 * @param TargetNode
	 * @param JNode
	 * Example.
	 * TargetNode := if(y) {...} else { ... }
	 * JNode      := [x2 = phi(x0, x1)]
	 *    Before      |   After
	 * function f() { | function f() {
	 *   if(y) {      |  if(y) {
	 *     x0 = ...   |    x0 = ...
	 *   } else {     |  } else {
	 *     x1 = ...   |    x1 = ...
	 *   }            |  }
	 *                |  x2 = phi(x0, x1)
	 * }              | }
	 */
	private void RemoveJoinNode(AstNode TargetNode, JoinNode JNode) {
		@Var LegacyBlockNode Parent = TargetNode.GetScopeLegacyBlockNode();
		@Var int Index = 0;
		assert(Parent != null);
		while(Index < Parent.GetListSize()) {
			AstNode Node = Parent.GetListAt(Index);
			Index = Index + 1;
			if(Node == TargetNode) {
				break;
			}
		}
		assert(Index < Parent.GetListSize());

		if(TargetNode instanceof BunIfNode) {
			// JoinNode for ZIfNode is placed after if-statement.
			@Var int i = JNode.getPhiSize() - 1;
			while(i >= 0) {
				PHINode phi = JNode.ListAt(i);
				Parent.InsertListAt(Index, phi);
				this.UpdateVariable(phi.VarRef);
				i = i - 1;
			}
		} else if (TargetNode instanceof BunWhileNode) {
			// JoinNode for WhileNode is placed at a header of loop.
			// ... while((x1 = phi() && i1 = phi()) && x1 == true) {...}
			@Var BunWhileNode WNode = (BunWhileNode) TargetNode;
			@Var AstNode CondNode = WNode.CondNode();
			@Var int i = JNode.getPhiSize() - 1;
			while(i >= 0) {
				@Var PHINode phi = JNode.ListAt(i);
				@Var BunAndNode And = new BunAndNode(Parent);
				And.SetLeftNode(phi);
				And.SetRightNode(CondNode);
				And.Type = BType.BooleanType;
				CondNode = And;
				this.ReplaceNodeWith(TargetNode, phi.VarRef, phi);
				this.UpdateVariable(phi.VarRef);
				i = i - 1;
			}
			WNode.SetNode(BunWhileNode._Cond, CondNode);
		}
	}

	@Override
	public void VisitGetNameNode(GetNameNode Node) {
		@Var Variable V = this.FindVariable(Node.GetUniqueName(this.Generator));
		Node.VarIndex = V.Index;
	}

	@Override
	public void VisitAssignNode(AssignNode Node) {
		if(Node.LeftNode() instanceof GetNameNode) {
			@Var GetNameNode NameNode = (GetNameNode)Node.LeftNode();
			@Var Variable OldVal = this.FindVariable(NameNode.GetUniqueName(this.Generator));
			@Var Variable NewVal = new Variable(OldVal.Name, this.GetRefreshNumber(OldVal), Node);
			this.UpdateVariable(NewVal);
			NameNode.VarIndex = NewVal.Index;
			this.InsertPHI(this.GetCurrentJoinNode(), this.GetCurrentBranchIndex(), OldVal, NewVal);
		}
	}

	@Override
	public void VisitVarblockNode(BunVarBlockNode Node) {
		@Var Variable V = new Variable(Node.VarDeclNode().GetUniqueName(this.Generator), 0, Node);
		this.AddVariable(V);
		@Var int i = 0;
		while(i < Node.GetListSize()) {
			Node.GetListAt(i).Accept(this);
			i = i + 1;
		}
		this.RemoveVariable(V.Name);
	}

	@Override
	public void VisitIfNode(BunIfNode Node) {
		this.PushState(new SSAConverterState(new JoinNode(Node), 0));
		this.RecordListOfVariablesBeforeVisit(Node);
		Node.CondNode().Accept(this);
		this.SetCurrentBranchIndex(IfThenBranchIndex);
		this.RecordListOfVariablesBeforeVisit(Node.ThenNode());
		Node.ThenNode().Accept(this);
		this.RecordListOfVariablesAfterVisit(Node.ThenNode());
		this.SetCurrentBranchIndex(IfElseBranchIndex);
		if(Node.HasElseNode()) {
			this.RecordListOfVariablesBeforeVisit(Node.ElseNode());
			Node.ElseNode().Accept(this);
			this.RecordListOfVariablesAfterVisit(Node.ElseNode());
		}
		else {
			@Var JoinNode JNode = this.GetCurrentJoinNode();
			@Var int i = JNode.getPhiSize() - 1;
			while(i >= 0) {
				PHINode phi = JNode.ListAt(i);
				this.InsertPHI(JNode, IfElseBranchIndex, phi.BackupValue, phi.BackupValue);
				i = i - 1;
			}
		}
		this.RecordListOfVariablesAfterVisit(Node);
		this.RemoveJoinNode(Node, this.GetCurrentJoinNode());
		this.CommitPHINode(this.GetCurrentJoinNode());
		this.PopState();
	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		this.PushState(new SSAConverterState(new JoinNode(Node), 0));
		this.RecordListOfVariablesBeforeVisit(Node);
		Node.CondNode().Accept(this);
		this.RecordListOfVariablesBeforeVisit(Node.blockNode());
		this.SetCurrentBranchIndex(WhileBodyBranchIndex);
		Node.blockNode().Accept(this);
		this.RecordListOfVariablesAfterVisit(Node.blockNode());
		this.RecordListOfVariablesAfterVisit(Node);
		this.RemoveJoinNode(Node, this.GetCurrentJoinNode());
		this.CommitPHINode(this.GetCurrentJoinNode());
		this.PopState();
	}

	@Override
	public void VisitFunctionNode(BunFunctionNode Node) {
		this.LocalVariables = new CommonArray<Variable>(new Variable[0]);
		@Var int i = 0;
		while(i < Node.getParamSize()) {
			BunLetVarNode ParamNode = Node.GetParamNode(i);
			this.AddVariable(new Variable(ParamNode.GetUniqueName(this.Generator), 0, ParamNode));
			i = i + 1;
		}
		Node.blockNode().Accept(this);
	}
}
