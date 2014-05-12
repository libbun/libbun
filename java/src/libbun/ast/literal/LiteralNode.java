package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.parser.common.BunToken;

public abstract class LiteralNode extends ConstNode {
	protected LiteralNode(AstNode ParentNode, BunToken SourceToken) {
		super(ParentNode, SourceToken);
	}
}
