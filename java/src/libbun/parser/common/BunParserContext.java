package libbun.parser.common;

import libbun.ast.AstNode;

public abstract class BunParserContext {

	public abstract boolean hasNode();
	public abstract AstNode parseNode(AstNode parentNode, String key);

}
