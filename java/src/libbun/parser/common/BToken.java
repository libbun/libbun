package libbun.parser.common;

import libbun.parser.classic.BIndentToken;
import libbun.util.BIgnored;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BToken extends BunToken {
	//	public final static BToken _NullToken = new BToken();
	//
	//	private BToken() {
	//		this.Source = new BunSource();
	//		this.StartIndex = 0;
	//		this.EndIndex = 0;
	//	}

	public BToken(BunSource Source, int StartIndex, int EndIndex) {
		super(Source, StartIndex, EndIndex);
	}

	public final char GetChar() {
		if(this.source != null) {
			return LibBunSystem._GetChar(this.source.sourceText, this.startIndex);
		}
		return '\0';
	}

	@Override public final String toString() {
		@Var char ch = this.source.charAt(this.startIndex-1);
		if(ch == '\"') {
			return "\"" + this.GetText() + "\"";
		}
		return this.GetText();
	}

	@BIgnored public final boolean EqualsText(char ch) {
		if(this.endIndex - this.startIndex == 1) {
			if(LibBunSystem._GetChar(this.source.sourceText, this.startIndex) == ch) {
				return true;
			}
		}
		return false;
	}

	public final boolean EqualsText(String Text) {
		if(Text.length() == this.endIndex - this.startIndex) {
			@Var String s = this.source.sourceText;
			@Var int i = 0;
			while(i < Text.length()) {
				if(LibBunSystem._GetChar(s, this.startIndex+i) != LibBunSystem._GetChar(Text, i)) {
					return false;
				}
				i = i + 1;
			}
			return true;
		}
		return false;
	}

	public final boolean StartsWith(String Text) {
		if(Text.length() <= this.endIndex - this.startIndex) {
			@Var String s = this.source.sourceText;
			@Var int i = 0;
			while(i < Text.length()) {
				if(LibBunSystem._GetChar(s, this.startIndex+i) != LibBunSystem._GetChar(Text, i)) {
					return false;
				}
				i = i + 1;
			}
			return true;
		}
		return false;
	}

	public final boolean EndsWith(String Text) {
		@Var int i = this.endIndex - 1;
		@Var int j = Text.length() - 1;
		@Var String s = this.source.sourceText;
		while(i >= this.startIndex && j >= 0) {
			if(LibBunSystem._GetChar(s, i) != LibBunSystem._GetChar(Text, j)) {
				return false;
			}
			i = i - 1;
			j = j - 1;
		}
		return true;
	}


	public final boolean IsIndent() {
		return this instanceof BIndentToken;
	}

	public final boolean IsNextWhiteSpace() {
		@Var char ch = this.source.charAt(this.endIndex);
		if(ch == ' ' || ch == '\t' || ch == '\n') {
			return true;
		}
		return false;
	}

	public final boolean IsNameSymbol() {
		@Var char ch = this.source.charAt(this.startIndex);
		return LibBunSystem._IsSymbol(ch);
	}

	public final int GetIndentSize() {
		if(this.source != null) {
			return this.source.CountIndentSize(this.source.GetLineHeadPosition(this.startIndex));
		}
		return 0;
	}

}
