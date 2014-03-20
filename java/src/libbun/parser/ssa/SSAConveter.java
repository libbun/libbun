package libbun.parser.ssa;

import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZVarBlockNode;
import libbun.util.ZArray;


public class SSAConveter extends ZASTTransformer {
	static final boolean	DEBUG_DOMTREE	= !false;
	public ZArray<ZVarBlockNode> LocalVars;

	public boolean Apply(ZBlockNode Block) {
		this.LocalVars = new ZArray<ZVarBlockNode>(new ZVarBlockNode[0]);

		ControlFlowGraph cfg = new ControlFlowGraph(Block);
		cfg.ComputeDominanceFrontier();
		cfg.EntryBlock.InsertPHI(this);
		cfg.EntryBlock.InsertUndefinedVariables(this);
		cfg.RenamingVariables(this);
		return true;
	}
}