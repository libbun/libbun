package libbun.parser.peg;

import libbun.ast.AstNode;
import libbun.ast.literal.BunNullNode;
import libbun.common.CommonMap;

public class NodeFactory {
	public final static CommonMap<AstNode> nodeMap = new CommonMap<AstNode>(null);

	public final static void _SetNode(String name, AstNode node) {
		NodeFactory.nodeMap.put(name, node);
	}

	public final static AstNode _NewNode(String name) {
		AstNode node = NodeFactory.nodeMap.GetValue(name, null);
		if(node != null) {
			return node.dup(false, null);
		}
		return null;
	}

	static {
		NodeFactory._SetNode("Null", new BunNullNode(null));
	}

}
