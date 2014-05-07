package libbun.parser.peg;

import libbun.util.LibBunSystem;
import libbun.util.Var;

public class PegSource {
	String FileName;
	int    LineNumber;
	public String  SourceText;

	PegSource() {
		this.FileName = null;
		this.LineNumber = 0;
		this.SourceText ="";
	}

	public PegSource(String FileName, int LineNumber, String Source) {
		this.FileName = FileName;
		this.LineNumber = LineNumber;
		this.SourceText = Source;
	}

	public final int GetLineNumber(int Position) {
		@Var int LineNumber = this.LineNumber;
		@Var int i = 0;
		while(i < Position) {
			@Var char ch = LibBunSystem._GetChar(this.SourceText, i);
			if(ch == '\n') {
				LineNumber = LineNumber + 1;
			}
			i = i + 1;
		}
		return LineNumber;
	}

	//	public final int GetLineHeadPosition(int Position) {
	//		@Var String s = this.SourceText;
	//		@Var int StartIndex = 0;
	//		@Var int i = Position;
	//		if(!(i < s.length())) {
	//			i = s.length() - 1;
	//		}
	//		while(i >= 0) {
	//			@Var char ch = LibBunSystem._GetChar(s, i);
	//			if(ch == '\n') {
	//				StartIndex = i + 1;
	//				break;
	//			}
	//			i = i - 1;
	//		}
	//		return StartIndex;
	//	}
	//
	//	public final int CountIndentSize(int Position) {
	//		@Var String s = this.SourceText;
	//		@Var int length = 0;
	//		@Var int i = Position;
	//		while(i < s.length()) {
	//			@Var char ch = LibBunSystem._GetChar(s, i);
	//			if(ch == '\t') {
	//				length = length + 8;
	//			}
	//			else if(ch == ' ') {
	//				length = length + 1;
	//			}
	//			else {
	//				break;
	//			}
	//			i = i + 1;
	//		}
	//		return length;
	//	}

	public final int getIndentSize(int fromPosition) {
		int startPosition = this.getLineStartPosition(fromPosition);
		@Var int length = 0;
		@Var String s = this.SourceText;
		for(;startPosition < s.length();startPosition=startPosition+1) {
			@Var char ch = LibBunSystem._GetChar(s, startPosition);
			if(ch == '\t') {
				length = length + 8;
			}
			else if(ch == ' ') {
				length = length + 1;
			}
			else {
				break;
			}
		}
		return length;

	}

	public final int getLineStartPosition(int fromPostion) {
		@Var String s = this.SourceText;
		@Var int startIndex = fromPostion;
		if(!(startIndex < s.length())) {
			startIndex = s.length() - 1;
		}
		while(startIndex > 0) {
			@Var char ch = LibBunSystem._GetChar(s, startIndex);
			if(ch == '\n') {
				startIndex = startIndex + 1;
				break;
			}
			startIndex = startIndex - 1;
		}
		return startIndex;
	}

	public final String getLineTextAt(int pos) {
		@Var String s = this.SourceText;
		@Var int startIndex = this.getLineStartPosition(pos);
		int endIndex = startIndex;
		while(endIndex < s.length()) {
			@Var char ch = LibBunSystem._GetChar(s, endIndex);
			if(ch == '\n') {
				break;
			}
			endIndex = endIndex + 1;
		}
		return s.substring(startIndex, endIndex);
	}

	public final String getMakerLine(int pos) {
		@Var String s = this.SourceText;
		@Var int startIndex = this.getLineStartPosition(pos);
		@Var String markerLine = "";
		int i = startIndex;
		while(i < pos) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				break;
			}
			if(ch == '\t') {
				markerLine = markerLine + "\t";
			}
			else {
				markerLine = markerLine + " ";
			}
			i = i + 1;
		}
		return markerLine + "^";
	}

	public final String FormatErrorHeader(String Error, int Position, String Message) {
		return "(" + this.FileName + ":" + this.GetLineNumber(Position) + ") [" + Error +"] " + Message;
	}

	public final String formatErrorLineMarker(String Error, int pos, String Message) {
		@Var String Line = this.getLineTextAt(pos);
		@Var String Delim = "\n\t";
		if(Line.startsWith("\t") || Line.startsWith(" ")) {
			Delim = "\n";
		}
		@Var String Header = this.FormatErrorHeader(Error, pos, Message);
		@Var String Marker = this.getMakerLine(pos);
		Message = Header + Delim + Line + Delim + Marker;
		return Message;
	}

	public final char GetCharAt(int n) {
		if(0 <= n && n < this.SourceText.length()) {
			return LibBunSystem._GetChar(this.SourceText, n);
		}
		return '\0';
	}
}
