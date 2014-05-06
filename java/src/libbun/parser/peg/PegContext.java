package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.util.BArray;
import libbun.util.BField;
import libbun.util.BunMap;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public final class PegContext  {
	@BField public final      PegSource source;
	@BField private int       sourcePosition = 0;
	@BField private final int endPosition;
	@BField public  PegParser    parser;
	@BField private final boolean IsAllowSkipIndent = false;

	public final BArray<Log> logStack = new BArray<Log>(new Log[64]);
	int stackTop = 0;
	PegToken debugToken ;

	public PegContext(PegParser parser, PegSource Source, int StartIndex, int EndIndex) {
		this.parser = parser;
		this.source = Source;
		this.sourcePosition = StartIndex;
		this.endPosition = EndIndex;
		this.debugToken = this.newToken(StartIndex, EndIndex);
	}

	PegContext(PegParser Parser, String source) {
		this(Parser, new PegSource("", 0, source), 0, source.length());
	}

	public PegContext subContext(int startIndex, int endIndex) {
		return new PegContext(this.parser, this.source, startIndex, endIndex);
	}

	@Override public String toString() {
		PegToken token = this.newToken(this.sourcePosition, this.endPosition);
		return token.GetText();
	}

	//	private boolean SetParseFlag(boolean AllowSkipIndent) {
	//		@Var boolean OldFlag = this.IsAllowSkipIndent;
	//		this.IsAllowSkipIndent = AllowSkipIndent;
	//		return OldFlag;
	//	}

	public int getPosition() {
		return this.sourcePosition;
	}

	public void rollback(int pos) {
		if(pos == 0 && this.sourcePosition > pos) {
			System.out.println("backtracking");
			new Exception().printStackTrace();
		}
		this.sourcePosition = pos;
	}

	public final boolean hasChar() {
		return this.sourcePosition < this.endPosition;
	}

	public final char charAt(int n) {
		return LibBunSystem._GetChar(this.source.SourceText, n);
	}

	public final char getChar() {
		if(this.hasChar()) {
			return this.charAt(this.sourcePosition);
		}
		return '\0';
	}

	public final char getChar(int n) {
		int pos = this.sourcePosition + n;
		if(pos >= 0 && pos < this.endPosition) {
			return this.charAt(pos);
		}
		return '\0';
	}

	public final int consume(int plus) {
		this.sourcePosition = this.sourcePosition + plus;
		return this.sourcePosition;
	}

	public final char nextChar() {
		if(this.hasChar()) {
			int pos = this.sourcePosition;
			this.consume(1);
			return this.charAt(pos);
		}
		return '\0';
	}

	public final int skipWhiteSpace(boolean IncludeNewLine) {
		if(IncludeNewLine) {
			while(this.hasChar()) {
				@Var char ch = this.charAt(this.sourcePosition);
				if(ch != ' ' && ch != '\t' && ch != '\n') {
					break;
				}
				this.consume(1);
			}
		}
		else {
			while(this.hasChar()) {
				@Var char ch = this.charAt(this.sourcePosition);
				if(ch != ' ' && ch != '\t') {
					break;
				}
				this.consume(1);
			}
		}
		return this.sourcePosition;
	}

	public final PegToken newToken() {
		return new PegToken(this.source, this.sourcePosition, this.sourcePosition);
	}

	public final PegToken newToken(int startIndex, int endIndex) {
		return new PegToken(this.source, startIndex, endIndex);
	}

	public final boolean sliceNumber(PegToken token) {
		char ch = this.nextChar();
		if(LibBunSystem._IsDigit(ch)) {
			for(;this.hasChar(); this.consume(1)) {
				ch = this.charAt(this.sourcePosition);
				if(!LibBunSystem._IsDigit(ch)) {
					break;
				}
			}
			token.EndIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean isSymbolLetter(char ch) {
		return (LibBunSystem._IsLetter(ch)  || ch == '_');
	}

	public final boolean sliceSymbol(PegToken token, String allowedChars) {
		char ch = this.nextChar();
		if(this.isSymbolLetter(ch) || allowedChars.indexOf(ch) != -1) {
			for(;this.hasChar(); this.consume(1)) {
				ch = this.charAt(this.sourcePosition);
				if(!this.isSymbolLetter(ch) && !LibBunSystem._IsDigit(ch) && allowedChars.indexOf(ch) == -1) {
					break;
				}
			}
			token.EndIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean sliceMatchedText(PegToken token, String text) {
		if(this.endPosition - this.sourcePosition >= text.length()) {
			for(int i = 0; i < text.length(); i++) {
				//System.out.println("i="+i+", '"+text.charAt(i) + "', '"+this.charAt(this.currentPosition + i));
				if(text.charAt(i) != this.charAt(this.sourcePosition + i)) {
					return false;
				}
			}
			this.consume(text.length());
			token.EndIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean sliceQuotedTextUntil(PegToken token, char endChar, String stopChars) {
		for(; this.hasChar(); this.consume(1)) {
			char ch = this.charAt(this.sourcePosition);
			if(ch == endChar) {
				token.EndIndex = this.sourcePosition;
				return true;
			}
			if(stopChars.indexOf(ch) != -1) {
				break;
			}
			if(ch == '\\') {
				this.consume(1);  // skip next char;
			}
		}
		token.EndIndex = this.sourcePosition;
		return false;
	}

	public final boolean sliceUntilWhiteSpace(PegToken token, String stopChars) {
		for(; this.hasChar(); this.consume(1)) {
			char ch = this.charAt(this.sourcePosition);
			if(ch == '\\') {
				this.consume(1);  // skip next char;
			}
			else {
				if(ch == ' ' || ch == '\t' || ch == '\n') {
					token.EndIndex = this.sourcePosition;
					return true;
				}
				if(stopChars.indexOf(ch) != 0) {
					break;
				}
			}
		}
		token.EndIndex = this.sourcePosition;
		return false;
	}

	public String getFirstChar() {
		return String.valueOf(this.getChar());
	}

	public boolean isLeftRecursion(String PatternName) {
		Peg e = this.parser.getRightPattern(PatternName, this.getFirstChar());
		return e != null;
	}

	//	public BNode matchRightPattern(BNode ParentNode, String PatternName) {
	//		Peg e = this.parser.getRightPattern(PatternName, this.getFirstChar());
	//		if(e != null) {
	//			int pos = this.currentPosition;
	//			BNode Node = e.matchAll(ParentNode, this);
	//			if(Node == null || Node.IsErrorNode()) {
	//				this.rollback(pos);
	//			}
	//			return Node;
	//		}
	//		return null;
	//	}

	private final BunMap<PegObject> memoMap = new BunMap<PegObject>(null);
	private final PegObject trueNode = new PegParsedNode(null, 0, 0);
	int memoHit = 0;
	int memoMiss = 0;
	int memoSize = 0;
	int objectCount = 0;
	int errorCount = 0;

	public final PegObject parsePegNodeMemo(PegObject parentNode, String pattern, boolean hasNextChoice) {
		int pos = this.getPosition();
		String key = pattern + ":" + pos;
		PegObject node = this.memoMap.GetValue(key, null);
		if(node != null) {
			this.memoHit = this.memoHit + 1;
			if(node == this.trueNode) {
				return parentNode;
			}
			return node;
		}
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		//System.out.println("matching " + parentNode + "   " + pattern + "... " + e);
		if(e != null) {
			node = e.lazyMatchAll(parentNode, this, hasNextChoice);
			this.memoMiss = this.memoMiss + 1;
		}
		else {
			LibBunSystem._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		}
		//System.out.println("matched " + parentNode + "   " + pattern + "... " + node);
		if(node == parentNode) {
			this.memoMap.put(key, this.trueNode);
			return node;
		}
		this.memoMap.put(key, node);
		return node;
	}

	public final PegObject parsePegNode(PegObject parentNode, String pattern, boolean hasNextChoice) {
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			return e.lazyMatchAll(parentNode, this, hasNextChoice);
		}
		LibBunSystem._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return null;
	}


	public PegObject parseRightPegNode(PegObject left, String symbol) {
		return this.parsePegNode(left, this.parser.nameRightJoinName(symbol), true);
	}


	final int getStackPosition(Peg trace) {
		this.pushImpl(trace, null, '\0', null, 0, null);
		return this.stackTop;
	}

	private void pushImpl(Peg trace, String msg, char type, Object parentNode, int index, Object childNode) {
		Log log = null;
		if(this.stackTop < this.logStack.size()) {
			if(this.logStack.ArrayValues[this.stackTop] == null) {
				this.logStack.ArrayValues[this.stackTop] = new Log();
			}
			log = this.logStack.ArrayValues[this.stackTop];
		}
		else {
			log = new Log();
			this.logStack.add(log);
		}
		log.trace = trace;
		log.sourcePosition = this.sourcePosition;
		log.msg = msg;
		log.type = type;
		log.parentNode = parentNode;
		log.index = index;
		log.childNode = childNode;
		this.stackTop = this.stackTop + 1;
	}

	void pushLog(Peg trace, String msg) {
		this.pushImpl(trace, msg, 'm', null, 0, null);
	}

	void push(Peg trace, BNode parentNode, int index, BNode childNode) {
		this.pushImpl(trace, "", 'p', parentNode, index, childNode);
	}

	void popBack(int stackPostion, boolean backtrack) {
		this.stackTop = stackPostion-1;
		Log log = this.logStack.ArrayValues[stackPostion-1];
		if(backtrack) {
			this.rollback(log.sourcePosition);
		}
	}

	public void push(Peg trace, PegObject parentNode, int index, PegObject node) {
		this.pushImpl(trace, "", 'p', parentNode, index, node);
	}

	public PegParsedNode newPegNode(Peg created, int startIndex, int endIndex) {
		PegParsedNode node = new PegParsedNode(created, startIndex, endIndex);
		node.debugSource = this.debugToken;
		this.objectCount = this.objectCount + 1;
		return node;
	}

	private final PegObject defaultFailureNode = new PegFailureNode(null, 0, "failure");

	public PegObject newErrorNode(Peg created, String msg, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		else {
			PegObject node = new PegFailureNode(created, this.sourcePosition, msg);
			node.debugSource = this.debugToken;
			this.errorCount = this.errorCount + 1;
			return node;
		}
	}

	public PegObject newExpectedErrorNode(Peg created, Peg e, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "expected " + e.stringfy(), false);
	}

	public PegObject newUnexpectedErrorNode(Peg created, Peg e, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "unexpected " + e.stringfy(), false);
	}

	public PegObject newFunctionErrorNode(Peg created, PegFunction f, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "function  " + f + " was failed", false);
	}

}

class Log {
	int sourcePosition;
	Peg trace;
	String msg;
	char type;
	Object parentNode;
	int index;
	Object childNode;

	@Override public String toString() {
		return "" + this.sourcePosition + " " + this.msg;
	}
}
