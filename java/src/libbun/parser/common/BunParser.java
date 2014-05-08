package libbun.parser.common;

public abstract class BunParser {

	public abstract BunParserContext newContext(BunSource source, int startIndex, int endIndex);

}


