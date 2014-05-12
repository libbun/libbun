package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.ast.literal.BunNullNode;
import libbun.common.CommonMap;

public class NodeFactory {
	public final static CommonMap<BNode> nodeMap = new CommonMap<BNode>(null);

	public final static void _SetNode(String name, BNode node) {
		NodeFactory.nodeMap.put(name, node);
	}

	public final static BNode _NewNode(String name) {
		BNode node = NodeFactory.nodeMap.GetValue(name, null);
		if(node != null) {
			return node.dup(false, null);
		}
		return null;
	}

	static {
		NodeFactory._SetNode("Null", new BunNullNode(null));
	}

}
