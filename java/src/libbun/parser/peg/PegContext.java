package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.common.CommonArray;
import libbun.common.CommonMap;
import libbun.parser.common.BunParserContext;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public final class PegContext extends BunParserContext {
	@BField public final      BunSource source;
	@BField private int       sourcePosition = 0;
	@BField public final int  endPosition;
	@BField public  PegParser    parser;
	@BField private final boolean IsAllowSkipIndent = false;

	public final CommonArray<Log> logStack = new CommonArray<Log>(new Log[64]);
	int stackTop = 0;
	int backtrackCount = 0;
	int backtrackSize = 0;

	public PegContext(PegParser parser, BunSource Source, int StartIndex, int EndIndex) {
		this.parser = parser;
		this.source = Source;
		this.sourcePosition = StartIndex;
		this.endPosition = EndIndex;
	}

	@Override public boolean hasNode() {
		this.skipWhiteSpace(true);
		return this.sourcePosition < this.endPosition;
	}

	@Override public BNode parseNode(BNode parentNode, String key) {
		PegObject po = this.parsePegNode(new PegParsedNode(null, 0, 0), key, false);
		return po.eval(this.source, parentNode);
	}


	//	PegContext(PegParser Parser, String source) {
	//		this(Parser, new BunSource("", 0, source), 0, source.length());
	//	}

	public PegContext subContext(int startIndex, int endIndex) {
		return new PegContext(this.parser, this.source, startIndex, endIndex);
	}

	@Override public String toString() {
		BunToken token = this.newToken(this.sourcePosition, this.endPosition);
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
		if(this.sourcePosition > pos) {
			//			System.out.println("backtracking");
			//			new Exception().printStackTrace();
			this.backtrackCount = this.backtrackCount + 1;
			this.backtrackSize = this.backtrackSize + (this.sourcePosition - pos);
		}
		this.sourcePosition = pos;
	}

	public final boolean hasChar() {
		return this.sourcePosition < this.endPosition;
	}

	public final char charAt(int n) {
		return LibBunSystem._GetChar(this.source.sourceText, n);
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

	public boolean match(char ch) {
		if(this.getChar() == ch) {
			this.consume(1);
			return true;
		}
		return false;
	}

	public boolean match(char ch, char ch2) {
		if(this.getChar(0) == ch && this.getChar(1) == ch2) {
			this.consume(2);
			return true;
		}
		return false;
	}

	public boolean checkSymbolLetter(int plus) {
		char ch = this.getChar(plus);
		if(this.isSymbolLetter(ch)) {
			return true;
		}
		return false;
	}


	public void skipIndent(int indentSize) {
		int pos = this.sourcePosition;
		//		this.showPosition("skip characters until indent="+indentSize + ", pos=" + pos, pos);
		for(;pos < this.endPosition; pos = pos + 1) {
			char ch = this.charAt(pos);
			if(ch == '\n' && pos > this.sourcePosition) {
				int posIndent = this.source.getIndentSize(pos+1);
				if(posIndent <= indentSize) {
					this.sourcePosition = pos + 1;
					//					System.out.println("skip characters until indent="+indentSize + ", pos=" + this.sourcePosition);
					return ;
				}
			}
		}
		//		System.out.println("skip characters until indent="+indentSize + ", pos = endPosition");
		this.sourcePosition = this.endPosition;
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

	public final BunToken newToken() {
		return new BunToken(this.source, this.sourcePosition, this.sourcePosition);
	}

	public final BunToken newToken(int startIndex, int endIndex) {
		return new BunToken(this.source, startIndex, endIndex);
	}

	public final boolean sliceNumber(BunToken token) {
		char ch = this.nextChar();
		if(LibBunSystem._IsDigit(ch)) {
			for(;this.hasChar(); this.consume(1)) {
				ch = this.charAt(this.sourcePosition);
				if(!LibBunSystem._IsDigit(ch)) {
					break;
				}
			}
			token.endIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean isSymbolLetter(char ch) {
		return (LibBunSystem._IsLetter(ch)  || ch == '_');
	}

	public final boolean sliceSymbol(BunToken token, String allowedChars) {
		char ch = this.nextChar();
		if(this.isSymbolLetter(ch) || allowedChars.indexOf(ch) != -1) {
			for(;this.hasChar(); this.consume(1)) {
				ch = this.charAt(this.sourcePosition);
				if(!this.isSymbolLetter(ch) && !LibBunSystem._IsDigit(ch) && allowedChars.indexOf(ch) == -1) {
					break;
				}
			}
			token.endIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean sliceMatchedText(BunToken token, String text) {
		if(this.endPosition - this.sourcePosition >= text.length()) {
			for(int i = 0; i < text.length(); i++) {
				//System.out.println("i="+i+", '"+text.charAt(i) + "', '"+this.charAt(this.currentPosition + i));
				if(text.charAt(i) != this.charAt(this.sourcePosition + i)) {
					return false;
				}
			}
			this.consume(text.length());
			token.endIndex = this.sourcePosition;
			return true;
		}
		return false;
	}

	public final boolean sliceQuotedTextUntil(BunToken token, char endChar, String stopChars) {
		for(; this.hasChar(); this.consume(1)) {
			char ch = this.charAt(this.sourcePosition);
			if(ch == endChar) {
				token.endIndex = this.sourcePosition;
				return true;
			}
			if(stopChars.indexOf(ch) != -1) {
				break;
			}
			if(ch == '\\') {
				this.consume(1);  // skip next char;
			}
		}
		token.endIndex = this.sourcePosition;
		return false;
	}

	public final boolean sliceUntilWhiteSpace(BunToken token, String stopChars) {
		for(; this.hasChar(); this.consume(1)) {
			char ch = this.charAt(this.sourcePosition);
			if(ch == '\\') {
				this.consume(1);  // skip next char;
			}
			else {
				if(ch == ' ' || ch == '\t' || ch == '\n') {
					token.endIndex = this.sourcePosition;
					return true;
				}
				if(stopChars.indexOf(ch) != 0) {
					break;
				}
			}
		}
		token.endIndex = this.sourcePosition;
		return false;
	}

	public String getFirstChar() {
		return String.valueOf(this.getChar());
	}

	public boolean isLeftRecursion(String PatternName) {
		Peg e = this.parser.getRightPattern(PatternName, this.getFirstChar());
		return e != null;
	}



	private final CommonMap<PegObject> memoMap = new CommonMap<PegObject>(null);
	private final CommonMap<Memo> memoMap2 = new CommonMap<Memo>(null);
	private final PegObject trueNode = new PegParsedNode(null, 0, 0);
	int memoHit = 0;
	int memoMiss = 0;
	int memoSize = 0;
	int objectCount = 0;
	int errorCount = 0;

	public final PegObject parsePegNode(PegObject parentNode, String pattern, boolean hasNextChoice) {
		int pos = this.getPosition();
		String key = pattern + ":" + pos;
		Memo m = this.memoMap2.GetValue(key, null);
		if(m != null) {
			this.memoHit = this.memoHit + 1;
			this.sourcePosition = m.nextPosition;
			if(m.result == null) {
				return parentNode;
			}
			return m.result;
		}
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			PegObject node = e.lazyMatchAll(parentNode, this, hasNextChoice);
			m = new Memo();
			m.nextPosition = this.getPosition();
			if(node != parentNode) {
				m.result = node;
			}
			this.memoMiss = this.memoMiss + 1;
			this.memoMap2.put(key, m);
			return node;
		}
		LibBunSystem._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parsePegNode2(PegObject parentNode, String pattern, boolean hasNextChoice) {
		int pos = this.getPosition();
		String key = pattern + ":" + pos;
		PegObject node = this.memoMap.GetValue(key, null);
		if(node != null) {
			this.memoHit = this.memoHit + 1;
			return node;
		}
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			node = e.lazyMatchAll(parentNode, this, hasNextChoice);
			if(node.isErrorNode() && hasNextChoice) {
				this.memoMiss = this.memoMiss + 1;
				this.memoMap.put(key, node);
				return node;
			}
			if(node != parentNode && node.isErrorNode()) {
				this.memoMiss = this.memoMiss + 1;
				this.memoMap.put(key, node);
				return node;
			}
			return node;
		}
		LibBunSystem._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parsePegNodeNon(PegObject parentNode, String pattern, boolean hasNextChoice) {
		Peg e = this.parser.getPattern(pattern, this.getFirstChar());
		if(e != null) {
			return e.lazyMatchAll(parentNode, this, hasNextChoice);
		}
		LibBunSystem._Exit(1, "undefined label " + pattern + " '" + this.getFirstChar() + "'");
		return this.defaultFailureNode;
	}

	public final PegObject parseRightPegNode(PegObject left, String symbol) {
		String key = this.parser.nameRightJoinName(symbol);
		Peg e = this.parser.getPattern(key, this.getFirstChar());
		while(e != null) {
			Peg sec = e;
			if(e instanceof PegChoice) {
				sec = ((PegChoice) e).firstExpr;
				e = ((PegChoice) e).secondExpr;
			}
			else {
				e = null;
			}
			PegObject right = sec.lazyMatchAll(left, this, true);
			if(!right.isErrorNode()) {
				left = right;
			}
		}
		return left;
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
		node.debugSource = this.source;
		this.objectCount = this.objectCount + 1;
		//System.out.println("pos="+this.sourcePosition+", by " + created);
		return node;
	}

	private final PegObject defaultFailureNode = new PegFailureNode(null, 0, "failure");

	public PegObject newErrorNode(Peg created, String msg, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		else {
			PegObject node = new PegFailureNode(created, this.sourcePosition, msg);
			node.debugSource = this.source;
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

	public PegObject newFunctionErrorNode(Peg created, SemanticFunction f, boolean hasNextChoice) {
		if(hasNextChoice) {
			return this.defaultFailureNode;
		}
		return this.newErrorNode(created, "function  " + f + " was failed", false);
	}

	public String formatErrorMessage(String msg1, String msg2) {
		return this.source.formatErrorLineMarker(msg1, this.sourcePosition, msg2);
	}
	public void showPosition(String msg, int pos) {
		System.out.println(this.source.formatErrorLineMarker("debug", pos, msg));
	}
	public void showErrorMessage(String msg) {
		System.out.println(this.source.formatErrorLineMarker("error", this.sourcePosition, msg));
		LibBunSystem._Exit(1, msg);
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

class Memo {
	PegObject result;
	int nextPosition;
}
