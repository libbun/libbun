package libbun.parser.common;

import libbun.ast.BNode;

public abstract class BunParserContext {

	public abstract boolean hasNode();
	public abstract BNode parseNode(BNode parentNode, String key);

}
