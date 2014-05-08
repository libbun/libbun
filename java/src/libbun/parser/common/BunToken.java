package libbun.parser.common;



public class BunToken {
	public final BunSource source;
	public int  startIndex;
	public int  endIndex;

	public BunToken(BunSource Source, int StartIndex, int EndIndex) {
		this.source = Source;
		this.startIndex = StartIndex;
		this.endIndex = EndIndex;
	}

	public final int size() {
		return this.endIndex - this.startIndex;
	}

	public final boolean IsNull() {
		return (this.startIndex == this.endIndex);
	}

	public final int indexOf(String s) {
		int loc = this.source.sourceText.indexOf(s, this.startIndex);
		if(loc != -1 && loc < this.endIndex) {
			return loc - this.startIndex;
		}
		return -1;
	}

	public final String substring(int startIndex) {
		return this.source.sourceText.substring(startIndex + this.startIndex, this.endIndex);
	}

	public final String substring(int startIndex, int endIndex) {
		startIndex = startIndex + this.startIndex;
		endIndex = endIndex + this.startIndex;
		if(endIndex <= this.endIndex) {
			return this.source.sourceText.substring(startIndex, endIndex);
		}
		return null;
	}

	public final String GetText() {
		if(this.source != null) {
			return this.source.substring(this.startIndex, this.endIndex);
		}
		return "";
	}

	public final String GetTextAsName() {
		return this.GetText();
	}

	public final String GetFileName() {
		return this.source.fileName;
	}

	public final int GetLineNumber() {
		return this.source.getLineNumber(this.startIndex);
	}

	//
	//	public final char GetChar() {
	//		if(this.source != null) {
	//			return LibBunSystem._GetChar(this.source.SourceText, this.startIndex);
	//		}
	//		return '\0';
	//	}
	//
	//
	//
	//	@Override public final String toString() {
	//		@Var char ch = this.source.GetCharAt(this.startIndex-1);
	//		if(ch == '\"') {
	//			return "\"" + this.GetText() + "\"";
	//		}
	//		return this.GetText();
	//	}
	//
	//	@BIgnored public final boolean EqualsText(char ch) {
	//		if(this.endIndex - this.startIndex == 1) {
	//			if(LibBunSystem._GetChar(this.source.SourceText, this.startIndex) == ch) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//
	//	public final boolean EqualsText(String Text) {
	//		if(Text.length() == this.endIndex - this.startIndex) {
	//			@Var String s = this.source.SourceText;
	//			@Var int i = 0;
	//			while(i < Text.length()) {
	//				if(LibBunSystem._GetChar(s, this.startIndex+i) != LibBunSystem._GetChar(Text, i)) {
	//					return false;
	//				}
	//				i = i + 1;
	//			}
	//			return true;
	//		}
	//		return false;
	//	}
	//
	//	public final boolean StartsWith(String Text) {
	//		if(Text.length() <= this.endIndex - this.startIndex) {
	//			@Var String s = this.source.SourceText;
	//			@Var int i = 0;
	//			while(i < Text.length()) {
	//				if(LibBunSystem._GetChar(s, this.startIndex+i) != LibBunSystem._GetChar(Text, i)) {
	//					return false;
	//				}
	//				i = i + 1;
	//			}
	//			return true;
	//		}
	//		return false;
	//	}
	//
	//	public final boolean EndsWith(String Text) {
	//		@Var int i = this.endIndex - 1;
	//		@Var int j = Text.length() - 1;
	//		@Var String s = this.source.SourceText;
	//		while(i >= this.startIndex && j >= 0) {
	//			if(LibBunSystem._GetChar(s, i) != LibBunSystem._GetChar(Text, j)) {
	//				return false;
	//			}
	//			i = i - 1;
	//			j = j - 1;
	//		}
	//		return true;
	//	}
	//
	//	public final boolean IsNextWhiteSpace() {
	//		@Var char ch = this.source.GetCharAt(this.endIndex);
	//		if(ch == ' ' || ch == '\t' || ch == '\n') {
	//			return true;
	//		}
	//		return false;
	//	}
	//
	//	public final boolean IsNameSymbol() {
	//		@Var char ch = this.source.GetCharAt(this.startIndex);
	//		return LibBunSystem._IsSymbol(ch);
	//	}
	//
	//	public final int GetIndentSize() {
	//		if(this.source != null) {
	//			return this.source.getIndentSize(this.startIndex);
	//		}
	//		return 0;
	//	}

}
