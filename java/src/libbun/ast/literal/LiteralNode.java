package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.parser.common.BunToken;

public abstract class LiteralNode extends ConstNode {
	protected LiteralNode(BNode ParentNode, BunToken SourceToken) {
		super(ParentNode, SourceToken);
	}
}
