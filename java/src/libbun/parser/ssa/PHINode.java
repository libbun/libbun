package libbun.parser.ssa;

import libbun.parser.ast.ZBlockNode;
import libbun.parser.ast.ZLocalDefinedNode;
import libbun.parser.ast.ZNode;
import libbun.util.ZArray;


public class PHINode extends ZLocalDefinedNode {
	public ZArray<ZNode> Args;
	public ZArray<ZBlockNode> Blocks;
	public ZNode Value;
	public PHINode(ZNode Value) {
		super(Value.ParentNode, Value.SourceToken, 0);
		this.Value = Value;
		this.Args = new ZArray<ZNode>(new ZNode[0]);
		this.Blocks = new ZArray<ZBlockNode>(new ZBlockNode[0]);
	}
	public void AddIncoming(ZBlockNode block, ZNode node) {
		this.Blocks.add(block);
		this.Args.add(node);
	}
}