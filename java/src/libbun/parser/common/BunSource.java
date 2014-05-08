package libbun.parser.common;

import libbun.parser.classic.BToken;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BunSource {
	public final BunLogger logger;
	String fileName;
	int    lineNumber;
	public String  sourceText;

	public BunSource(String fileName, int lineNumber, String sourceText, BunLogger logger) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.sourceText = sourceText;
		this.logger = logger;
	}

	public final char charAt(int n) {
		if(0 <= n && n < this.sourceText.length()) {
			return LibBunSystem._GetChar(this.sourceText, n);
		}
		return '\0';
	}

	public final String substring(int startIndex, int endIndex) {
		return this.sourceText.substring(startIndex, endIndex);
	}

	public final BToken newToken(int startIndex, int endIndex) {
		return new BToken(this, startIndex, endIndex);
	}



	public final int getLineNumber(int Position) {
		@Var int LineNumber = this.lineNumber;
		@Var int i = 0;
		while(i < Position) {
			@Var char ch = LibBunSystem._GetChar(this.sourceText, i);
			if(ch == '\n') {
				LineNumber = LineNumber + 1;
			}
			i = i + 1;
		}
		return LineNumber;
	}

	public final int getIndentSize(int fromPosition) {
		int startPosition = this.getLineStartPosition(fromPosition);
		@Var int length = 0;
		@Var String s = this.sourceText;
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
		@Var String s = this.sourceText;
		@Var int startIndex = fromPostion;
		if(!(startIndex < s.length())) {
			startIndex = s.length() - 1;
		}
		if(startIndex < 0) {
			startIndex = 0;
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
		@Var String s = this.sourceText;
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
		@Var String s = this.sourceText;
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

	public final String formatErrorLineMarker(String Error, int pos, String Message) {
		@Var String Line = this.getLineTextAt(pos);
		@Var String Delim = "\n\t";
		if(Line.startsWith("\t") || Line.startsWith(" ")) {
			Delim = "\n";
		}
		@Var String Header = this.formatErrorHeader(Error, pos, Message);
		@Var String Marker = this.getMakerLine(pos);
		Message = Header + Delim + Line + Delim + Marker;
		return Message;
	}

	public final String formatErrorHeader(String Error, int Position, String Message) {
		return "(" + this.fileName + ":" + this.getLineNumber(Position) + ") [" + Error +"] " + Message;
	}




	public final int GetLineHeadPosition(int Position) {
		@Var String s = this.sourceText;
		@Var int StartIndex = 0;
		@Var int i = Position;
		if(!(i < s.length())) {
			i = s.length() - 1;
		}
		while(i >= 0) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				StartIndex = i + 1;
				break;
			}
			i = i - 1;
		}
		return StartIndex;
	}

	public final int CountIndentSize(int Position) {
		@Var String s = this.sourceText;
		@Var int length = 0;
		@Var int i = Position;
		while(i < s.length()) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\t') {
				length = length + 8;
			}
			else if(ch == ' ') {
				length = length + 1;
			}
			else {
				break;
			}
			i = i + 1;
		}
		return length;
	}

	public final String GetLineText(int Position) {
		@Var String s = this.sourceText;
		@Var int StartIndex = 0;
		@Var int EndIndex = s.length();
		@Var int i = Position;
		if(!(i < s.length())) {
			i = s.length() - 1;
		}
		while(i >= 0) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				StartIndex = i + 1;
				break;
			}
			i = i - 1;
		}
		i = Position;
		while(i < s.length()) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				EndIndex = i;
				break;
			}
			i = i + 1;
		}
		return s.substring(StartIndex, EndIndex);
	}

	public final String GetLineMarker(int Position) {
		@Var String s = this.sourceText;
		@Var int StartIndex = 0;
		@Var int i = Position;
		if(!(i < s.length())) {
			i = s.length() - 1;
		}
		while(i >= 0) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				StartIndex = i + 1;
				break;
			}
			i = i - 1;
		}
		@Var String Line = "";
		i = StartIndex;
		while(i < Position) {
			@Var char ch = LibBunSystem._GetChar(s, i);
			if(ch == '\n') {
				break;
			}
			if(ch == '\t') {
				Line = Line + "\t";
			}
			else {
				Line = Line + " ";
			}
			i = i + 1;
		}
		return Line + "^";
	}

	public final String FormatErrorHeader(String Error, int Position, String Message) {
		return "(" + this.fileName + ":" + this.getLineNumber(Position) + ") [" + Error +"] " + Message;
	}

	public final String FormatErrorMarker(String Error, int Position, String Message) {
		@Var String Line = this.GetLineText(Position);
		@Var String Delim = "\n\t";
		if(Line.startsWith("\t") || Line.startsWith(" ")) {
			Delim = "\n";
		}
		@Var String Header = this.FormatErrorHeader(Error, Position, Message);
		@Var String Marker = this.GetLineMarker(Position);
		Message = Header + Delim + Line + Delim + Marker;
		return Message;
	}

}
