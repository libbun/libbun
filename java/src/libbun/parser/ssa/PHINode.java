package libbun.parser.ssa;

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.LocalDefinedNode;
import libbun.ast.expression.GetNameNode;
import libbun.common.CommonArray;
import libbun.encode.LibBunGenerator;
import libbun.util.Var;

public class PHINode extends LocalDefinedNode {
	public CommonArray<BNode>      Args;
	public CommonArray<BunBlockNode> Blocks;
	public Variable VarRef;
	public Variable BackupValue;
	public String VariableName;

	public PHINode(Variable BackupValue, String VariableName) {
		super(null, 0);
		this.BackupValue = BackupValue;
		this.VariableName = VariableName;
		this.Args = new CommonArray<BNode>(new BNode[0]);
		this.Blocks = new CommonArray<BunBlockNode>(new BunBlockNode[0]);
		this.Type = NodeLib.GetType(BackupValue.Node);
	}

	public void AddIncoming(int Index, BunBlockNode block, BNode node) {
		while(Index + 1 > this.Args.size()) {
			this.Args.add(this.BackupValue.Node);
			this.Blocks.add(null);
		}
		CommonArray.SetIndex(this.Args, Index, node);
		CommonArray.SetIndex(this.Blocks, Index, block);
	}

	public boolean IsSameVariable(Variable Var) {
		return this.VariableName.equals(Var.Name);
	}

	@Override
	public String toString() {
		@Var String Txt = "PHI[";
		@Var int i = 0;
		while(i < this.Args.size()) {
			BNode Node = CommonArray.GetIndex(this.Args, i);
			if (i != 0) {
				Txt += ", ";
			}
			Txt += Node.getClass().toString();
			i = i + 1;
		}
		Txt += "]";
		return Txt;
	}

	public int GetVarIndex() {
		return this.VarRef.Index;
	}

	public String GetName() {
		return this.VariableName;
	}

	public BNode GetArgument(int Index) {
		return CommonArray.GetIndex(this.Args, Index);
	}

	public boolean EqualsName(GetNameNode Node, LibBunGenerator Generator) {
		return Node.GetUniqueName(Generator).equals(this.VariableName);
	}
}