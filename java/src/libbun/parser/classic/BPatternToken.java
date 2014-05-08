package libbun.parser.classic;

import libbun.parser.common.BToken;
import libbun.parser.common.BunSource;
import libbun.util.BField;

public class BPatternToken extends BToken {
	@BField public LibBunSyntax	PresetPattern;
	public BPatternToken(BunSource Source, int StartIndex, int EndIndex, LibBunSyntax	PresetPattern) {
		super(Source, StartIndex, EndIndex);
		this.PresetPattern = PresetPattern;
	}

}
