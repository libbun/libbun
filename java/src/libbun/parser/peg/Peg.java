package libbun.parser.peg;

import libbun.common.CommonArray;
import libbun.common.CommonMap;
import libbun.parser.common.BunToken;
import libbun.util.LibBunSystem;

public abstract class Peg {
	public final static boolean _BackTrack = true;
	public BunToken source;
	public int priority = 0;
	public Peg nextExpr = null;
	public String leftLabel = null;
	boolean debug = false;

	Peg(String leftLabel, BunToken source) {
		this.leftLabel = leftLabel;
		this.source = source;
	}

	public void appendNext(Peg e) {
		Peg list = this;
		while(list.nextExpr != null) {
			list = list.nextExpr;
		}
		list.nextExpr = e;
	}

	public void setDebug(boolean debug) {
		Peg e = this;
		while(e != null) {
			e.debug = debug;
			if(e instanceof PegChoice) {
				((PegChoice) e).firstExpr.setDebug(debug);
				((PegChoice) e).secondExpr.setDebug(debug);
			}
			if(e instanceof PegPredicate) {
				((PegPredicate) e).innerExpr.setDebug(debug);
			}
			e = e.nextExpr;
		}
	}

	PegObject lazyMatchAll(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		Peg e = this;
		PegObject node = parentNode;
		while(e.nextExpr != null) {
			node = e.debugMatch(node, sourceContext, hasNextChoice);
			if(node.isErrorNode()) {
				return node;
			}
			e = e.nextExpr;
		}
		return e.debugMatch(node, sourceContext, hasNextChoice);
	}

	protected PegObject debugMatch(PegObject node, PegContext sourceContext, boolean hasNextChoice) {
		if(this.debug) {
			PegObject node2 = this.lazyMatch(node, sourceContext, false);
			String msg = "matched";
			if(node2.isErrorNode()) {
				msg = "failed";
			}
			String line = sourceContext.formatErrorMessage(msg, this.stringfy() + " (" + this.nextExpr + ")");
			System.out.println(line + "\n\tnode #" + node + "# => #" + node2 + "#");
			return node2;
		}
		return this.lazyMatch(node, sourceContext, hasNextChoice);
	}

	protected abstract PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice);

	public String firstChars(CommonMap<Peg> m) {
		return "";
	}

	@Override public String toString() {
		if(this.nextExpr != null) {
			String s = "";
			Peg e = this;
			while(e != null) {
				if(e instanceof PegChoice) {
					s = s + "(" + e.stringfy() + ")";
				}
				else {
					s = s + e.stringfy();
				}
				if(e.nextExpr != null) {
					s = s + " ";
				}
				e = e.nextExpr;
			}
			return s + "";
		}
		else {
			return this.stringfy();
		}
	}

	public final String groupfy(Peg e) {
		if(e.nextExpr != null && e instanceof PegString) {
			return e.toString();
		}
		else {
			return "(" + e.toString() + ")";
		}
	}
	protected abstract String stringfy();

	private String joinPrintableString(String s, Peg e) {
		if(e instanceof PegChoice) {
			s = s + ((PegChoice)e).firstExpr;
			s = s + "\n\t/ ";
			s = this.joinPrintableString(s, ((PegChoice)e).secondExpr);
		}
		else {
			s = s + e.toString();
		}
		return s;
	}

	public final String toPrintableString(String name) {
		return (this.joinPrintableString(name + " <- ", this));
	}

	Peg removeLeftRecursion(PegParser p) {
		return this;
	}

	final boolean checkAll(PegParser p, String leftName, int order) {
		Peg e = this;
		boolean checkResult = true;
		while(e != null) {
			if(!e.check(p, leftName, order)) {
				checkResult = false;
			}
			order = order + 1;
			e = e.nextExpr;
		}
		return checkResult;
	}
	boolean check(PegParser p, String leftName, int order) {
		return true; //
	}

	private static boolean sliceGroup(PegContext sourceContext, BunToken token, int openChar, int closeChar) {
		int order = 1;
		while(sourceContext.hasChar()) {
			char ch = sourceContext.nextChar();
			if(ch == closeChar) {
				order = order - 1;
				if(order == 0) {
					token.endIndex = sourceContext.getPosition() - 1;
					return true;
				}
			}
			if(ch == openChar) {
				order = order + 1;
			}
			if(ch == '"' || ch == '\'') {
				if(!sourceContext.sliceQuotedTextUntil(token, ch, "")) {
					return false;
				}
				sourceContext.consume(1);
			}
			if(ch == '[') {
				sourceContext.consume(1);
				if(!sourceContext.sliceQuotedTextUntil(token, ']', "")) {
					return false;
				}
				sourceContext.consume(1);
			}
		}
		return false;
	}

	private static String sliceName(PegContext sourceContext) {
		return null;
	}

	private static Peg _ParsePostfix(String leftName, PegContext sourceContext, Peg left) {
		if(left != null && sourceContext.hasChar()) {
			char ch = sourceContext.getChar();
			if(ch == ' ') {
				return left;
			}
			BunToken source = sourceContext.newToken();
			if(ch == '*') {
				sourceContext.consume(1);
				return new PegOneMoreExpr(leftName, source, left, 0);
			}
			if(ch == '?') {
				sourceContext.consume(1);
				return new PegOptionalExpr(leftName, source, left);
			}
			if(ch == '+') {  // a+  => a
				sourceContext.consume(1);
				return new PegOneMoreExpr(leftName, source, left, 1);
			}
			sourceContext.showErrorMessage("unknown postfix = '" + ch + "'");
		}
		return left;
	}

	private static Peg _ParseSingleExpr(String leftLabel, PegContext sourceContext) {
		Peg right = null;
		sourceContext.skipWhiteSpace(false);
		BunToken source = sourceContext.newToken();
		char ch = sourceContext.getChar();
		//System.out.println(">> " + ch + " next=" + Context.GetPosition());
		if(ch == '\0') {
			return null;
		}
		if(sourceContext.isSymbolLetter(ch)) {
			if(sourceContext.sliceSymbol(source, ".")) {
				right = new PegLabel(leftLabel, source, source.GetText());
				return Peg._ParsePostfix(leftLabel, sourceContext, right);
			}
		}
		if(sourceContext.match(':') || sourceContext.match('#')) {
			if(sourceContext.sliceSymbol(source, ".")) {
				String name = source.GetText().substring(1);
				SemanticFunction f = sourceContext.parser.getSemanticAction(name);
				right = new PegSemanticAction(leftLabel, source, name, f);
				if(f == null) {
					right.warning("undefined semantic function: " + name);
				}
				Peg._ParsePostfix(leftLabel, sourceContext, right);  // ignored
				return right;
			}
			sourceContext.showErrorMessage("expected semantic action name'" + sourceContext.getChar() + "'");
			return null;
		}
		sourceContext.consume(1);
		if(ch == '\'' || ch == '"') {
			if(sourceContext.sliceQuotedTextUntil(source, ch, "")) {
				source.endIndex = sourceContext.consume(1);
				right = new PegString(leftLabel, source, LibBunSystem._UnquoteString(source.GetText()));
				return Peg._ParsePostfix(leftLabel, sourceContext, right);
			}
		}
		if(ch == '.') {
			right = new PegAny(leftLabel, source);
			return Peg._ParsePostfix(leftLabel, sourceContext, right);
		}
		if(ch == '[') {
			source.startIndex = sourceContext.getPosition();
			if(sourceContext.sliceQuotedTextUntil(source, ']', "")) {
				source.endIndex = sourceContext.getPosition();
				sourceContext.consume(1);
				right = new PegCharacter(leftLabel, source, source.GetText());
				return Peg._ParsePostfix(leftLabel, sourceContext, right);
			}
		}
		if(ch == '$') {
			boolean allowError = false;
			if(sourceContext.match('$')) {
				allowError = true;
			}
			right = Peg._ParseSingleExpr(leftLabel, sourceContext);
			if(right != null) {
				right = new PegSetter(leftLabel, source, right, allowError);
			}
			return right;
		}
		if(ch == '&') {
			right = Peg._ParseSingleExpr(leftLabel, sourceContext);
			if(right != null) {
				right = new PegAndPredicate(leftLabel, source, right);
			}
			return right;
		}
		if(ch == '!') {
			right = Peg._ParseSingleExpr(leftLabel, sourceContext);
			if(right != null) {
				right = new PegNotPredicate(leftLabel, source, right);
			}
			return right;
		}
		if(ch == '(') {
			source.startIndex = sourceContext.getPosition();
			if(Peg.sliceGroup(sourceContext, source, ch, ')')) {
				PegContext sub = sourceContext.subContext(source.startIndex, source.endIndex);
				right = Peg._ParsePegExpr(leftLabel, sub);
				if(right != null) {
					right = Peg._ParsePostfix(leftLabel, sourceContext, right);
				}
				return right;
			}
			sourceContext.showErrorMessage("unclosed ')'");
			return null;
		}
		if(ch == '{') {
			boolean leftJoin = false;
			if(sourceContext.match('$', ' ') || sourceContext.match('$', '\n') || sourceContext.match('+')) {
				leftJoin = true;
			}
			source.startIndex = sourceContext.getPosition();
			if(Peg.sliceGroup(sourceContext, source, ch, '}')) {
				String name = Peg.sliceName(sourceContext);
				PegContext sub = sourceContext.subContext(source.startIndex, source.endIndex);
				right = Peg._ParsePegExpr(leftLabel, sub);
				if(right != null) {
					right = new PegNewObject(leftLabel, source, leftJoin, right, name);
					right = Peg._ParsePostfix(leftLabel, sourceContext, right);
				}
				return right;
			}
			sourceContext.showErrorMessage("unclosed ')'");
			return null;
		}
		sourceContext.showErrorMessage("unexpected character '" + ch + "'");
		return right;
	}

	private final static Peg _ParseSequenceExpr(String leftLabel, PegContext sourceContext) {
		Peg left = Peg._ParseSingleExpr(leftLabel, sourceContext);
		if(left == null) {
			return left;
		}
		sourceContext.skipWhiteSpace(false);
		if(sourceContext.hasChar()) {
			sourceContext.skipWhiteSpace(false);
			char ch = sourceContext.getChar();
			if(ch == '/') {
				sourceContext.consume(1);
				sourceContext.skipWhiteSpace(false);
				return left;
			}
			Peg right = Peg._ParseSequenceExpr(leftLabel, sourceContext);
			if(right != null) {
				left.appendNext(right);
			}
		}
		return left;
	}

	public final static Peg _ParsePegExpr(String leftLabel, PegContext sourceContext) {
		Peg left = Peg._ParseSequenceExpr(leftLabel, sourceContext);
		sourceContext.skipWhiteSpace(false);
		if(sourceContext.hasChar()) {
			Peg right = Peg._ParsePegExpr(leftLabel, sourceContext);
			if(right != null) {
				return new PegChoice(leftLabel, sourceContext.newToken(), left, right);
			}
		}
		return left;
	}

	protected void dump(String msg) {
		if(this.source != null) {
			System.out.println(this.source.source.formatErrorLineMarker("*", this.source.startIndex, msg));
		}
		else {
			System.out.println("unknown source: " + msg);
		}
	}

	protected void warning(String msg) {
		LibBunSystem._PrintLine("PEG warning: " + msg);
	}

	protected void debug(String msg) {
		System.out.println("debug: " + msg + "   by " + this.stringfy() + " <" + this + ">");
	}

	protected void debugMatched(String msg) {
		//System.out.println("matched: " + msg + "   @" + this);
	}

	protected void debugUnmatched(String msg) {
		//System.out.println("unmatched: " + msg + "   @" + this);
	}
}

abstract class PegAbstractSymbol extends Peg {
	String symbol;
	public PegAbstractSymbol (String leftLabel, BunToken source, String symbol) {
		super(leftLabel, source);
		this.symbol = symbol;
	}
}

class PegString extends PegAbstractSymbol {
	public PegString(String leftLabel, BunToken source, String symbol) {
		super(leftLabel, source, symbol);
	}
	@Override protected String stringfy() {
		return LibBunSystem._QuoteString("'", this.symbol, "'");
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		//sourceContext.skipWhiteSpace(false);
		BunToken token = sourceContext.newToken();
		//System.out.println("? ch = " + sourceContext.getChar() + " in " + this.symbol + " at pos = " + sourceContext.getPosition());
		if(sourceContext.sliceMatchedText(token, this.symbol)) {
			//			if(parentNode.endIndex == 0) {
			//				parentNode.startIndex = token.StartIndex;
			//				parentNode.endIndex = token.EndIndex;
			//				System.out.println("#######" + token.GetText() + "########");
			//			}
			return parentNode;
		}
		return sourceContext.newExpectedErrorNode(this, this, hasNextChoice);
	}

	@Override public String firstChars(CommonMap<Peg> m) {
		return ""+this.symbol.charAt(0);
	}

	@Override boolean check(PegParser p, String leftName, int order) {
		p.keywordCache.put(this.symbol, this.symbol);
		if(p.enableFirstCharChache && order == 0) {
			String ch = this.firstChars(null);
			p.setFirstCharSet(ch, this);
		}
		return true;
	}
}

class PegAny extends PegAbstractSymbol {
	public PegAny(String leftLabel, BunToken source) {
		super(leftLabel, source, ".");
	}
	@Override protected String stringfy() {
		return ".";
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		if(sourceContext.hasChar()) {
			sourceContext.consume(1);
			return parentNode;
		}
		return sourceContext.newExpectedErrorNode(this, this, hasNextChoice);
	}

	@Override boolean check(PegParser p, String leftName, int order) {
		if(p.enableFirstCharChache && order == 0) {
			p.setFirstCharSet("", this);
		}
		return true;
	}

}

class TokenReader {
	int pos;
	String token;
	public TokenReader(String token) {
		this.token = token;
		this.pos = 0;
	}
	char get() {
		return this.token.charAt(this.pos);
	}
	void consume() {
		this.pos += 1;
	}
	boolean hasNext() {
		return (this.pos < this.token.length());
	}
}

class PegCharacter extends PegAbstractSymbol {
	String charSet;
	boolean[] cclass;
	public PegCharacter(String leftLabel, BunToken source, String token) {
		super(leftLabel, source, token);
		//token = token.replaceAll("A-Z", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		//token = token.replaceAll("a-z", "abcdefghijklmnopqrstuvwxyz");
		//token = token.replaceAll("A-z", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
		//token = token.replaceAll("0-9", "0123456789");
		//token = token.replaceAll("\\-", "-");
		//this.charSet = token;
		this.makeCharClass(token);
	}

	private boolean ishexnum(int c) {
		return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
	}

	private int cnext(TokenReader reader) {
		int c = reader.get();
		reader.consume();
		if (c > 0) {
			if (c == '\\' && reader.hasNext()) {
				c = reader.get();
				reader.consume();
				switch (c) {
				case 'a':  c= '\007'; break; /* bel */
				case 'b':  c= '\b';   break; /* bs */
				case 'e':  c= '\033'; break; /* esc */
				case 'f':  c= '\f';   break; /* ff */
				case 'n':  c= '\n';   break; /* nl */
				case 'r':  c= '\r';   break; /* cr */
				case 't':  c= '\t';   break; /* ht */
				case 'v':  c= '\013'; break; /* vt */
				case 'u': /* unicode character */
				case 'U': /* \U0010 */
					if (this.ishexnum(reader.get())) {
						c = reader.get() - '0';
						reader.consume();
						if (reader.hasNext() && this.ishexnum(reader.get())) {
							c = (c * 16) + reader.get() - '0';
							reader.consume();
						}
						if (reader.hasNext() && this.ishexnum(reader.get())) {
							c = (c * 16) + reader.get() - '0';
							reader.consume();
						}
						if (reader.hasNext() && this.ishexnum(reader.get())) {
							c = (c * 16) + reader.get() - '0';
							reader.consume();
						}
					}
					else {} /* case "\]", "\\" */
					break;
				}
			}
		}
		return c;
	}

	private boolean[] makeCharClass(String token) {
		boolean bits[] = new boolean[256];
		TokenReader reader = new TokenReader(token);
		int c;
		int prev = -1;
		boolean flip = false;
		this.charSet = "";
		this.cclass = bits;
		if(reader.hasNext() && reader.get() == '^') { // convert [^0] to "!'0' ."
			reader.consume();
			flip = true;
		}
		while (reader.hasNext()) {
			if ('-' == reader.get() && reader.pos + 1 < token.length() && prev >= 0) {
				reader.consume();
				c = this.cnext(reader);
				while(true) {
					if(prev > c) {
						break;
					}
					bits[prev] = true;
					prev += 1;
				}
				prev = -1;
			}
			else {
				c = this.cnext(reader);
				bits[c] = true;
				prev= c;
			}
		}
		if(flip) {
			for (int i = 0; i < bits.length; i++) {
				bits[i] = !bits[i];
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < bits.length; i++) {
			if(bits[i]) {
				if(33 <= i && i <= 126) { // "(char) i" is printable characters
					builder.append((char)i);
				}
				else {
					builder.append("\\");
					builder.append(i);
				}
			}
		}
		this.charSet = builder.toString();
		return bits;
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		char ch = sourceContext.getChar();
		//System.out.println("? ch = " + ch + " in " + this.charSet + " at pos = " + sourceContext.getPosition());
		if(!this.cclass[ch]) {
			return sourceContext.newExpectedErrorNode(this, this, hasNextChoice);
		}
		sourceContext.consume(1);
		return parentNode;
	}

	@Override protected String stringfy() {
		return LibBunSystem._QuoteString("[", this.symbol, "]");
	}
	@Override public String firstChars(CommonMap<Peg> m) {
		return this.charSet;
	}
	@Override boolean check(PegParser p, String leftName, int order) {
		if(p.enableFirstCharChache && order == 0) {
			p.setFirstCharSet(this.charSet, this);
		}
		return true;
	}
}

class PegLabel extends PegAbstractSymbol {
	public PegLabel(String leftLabel, BunToken source, String token) {
		super(leftLabel, source, token);
	}
	@Override protected String stringfy() {
		return this.symbol;
	}

	@Override public String firstChars(CommonMap<Peg> m) {
		//		if(m != null) {
		//			Peg e = m.GetValue(this.symbol, null);
		//			if(e != null) {
		//				return e.firstChars(m);
		//			}
		//		}
		return "";
	}

	@Override boolean check(PegParser p, String leftName, int order) {
		if(!p.hasPattern(this.symbol)) {
			LibBunSystem._Exit(1, "undefined label: " + this.symbol);
		}
		return true;
	}

	@Override Peg removeLeftRecursion(PegParser p) {
		if(this.nextExpr != null) {
			CommonArray<String> list = p.pegMap.keys();
			for(int i = 0; i < list.size(); i++) {
				String key = list.ArrayValues[i];
				Peg e = p.pegMap.GetValue(key, null);
				if(this.lookupFirstLabel(e, this.symbol)) {
					LibBunSystem._Exit(1, "find indirect left recursion " + this.leftLabel + " <- " + this.symbol + "...");
					return null;
				}
			}
		}
		return this;
	}

	private boolean lookupFirstLabel(Peg e, String symbol) {
		if(e instanceof PegChoice) {
			if(!this.lookupFirstLabel(((PegChoice) e).firstExpr, symbol)) {
				return this.lookupFirstLabel(((PegChoice) e).secondExpr, symbol);
			}
		}
		if(e instanceof PegLabel) {
			if(symbol.equals(((PegLabel) e).symbol)) {
				return true;
			}
		}
		return false;
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		PegObject left = sourceContext.parsePegNode(parentNode, this.symbol, hasNextChoice);
		if(left.isErrorNode()) {
			return left;
		}
		return sourceContext.parseRightPegNode(left, this.symbol);
	}

}

abstract class PegPredicate extends Peg {
	Peg innerExpr;
	public PegPredicate(String leftLabel, BunToken source, Peg e) {
		super(leftLabel, source);
		this.innerExpr = e;
	}
	@Override boolean check(PegParser p, String leftName, int order) {
		return this.innerExpr.checkAll(p, leftName, order);
	}

}

class PegOptionalExpr extends PegPredicate {
	public PegOptionalExpr(String leftLabel, BunToken source, Peg e) {
		super(leftLabel, source, e);
	}
	@Override protected String stringfy() {
		return this.groupfy(this.innerExpr) + "?";
	}
	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = sourceContext.getStackPosition(this);
		node = this.innerExpr.lazyMatchAll(node, sourceContext, true);
		if(node.isErrorNode()) {
			sourceContext.popBack(stackPosition, Peg._BackTrack);
			node = parentNode;
		}
		return node;
	}

	@Override public String firstChars(CommonMap<Peg> m) {
		if(this.nextExpr == null) {
			return this.innerExpr.firstChars(m);
		}
		else {
			return this.innerExpr.firstChars(m) + this.nextExpr.firstChars(m);
		}
	}
}

class PegOneMoreExpr extends PegPredicate {
	int min = 0;
	public PegOneMoreExpr(String leftLabel, BunToken source, Peg e, int min) {
		super(leftLabel, source, e);
		this.min = min;
	}
	@Override protected String stringfy() {
		if(this.min == 0) {
			return this.groupfy(this.innerExpr) + "*";
		}
		else {
			return this.groupfy(this.innerExpr) + "+";
		}
	}
	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		PegObject prevNode = parentNode;
		int count = 0;
		while(true) {
			boolean aChoice = true;
			if(count < this.min) {
				aChoice = hasNextChoice;
			}
			PegObject node = this.innerExpr.lazyMatchAll(prevNode, sourceContext, aChoice);
			if(node.isErrorNode()) {
				break;
			}
			if(node != prevNode) {
				this.warning("ignored result of " + this.innerExpr);
			}
			prevNode = node;
			count = count + 1;
		}
		if(count < this.min) {
			return sourceContext.newExpectedErrorNode(this, this.innerExpr, hasNextChoice);
		}
		//System.out.println("prevNode: " + prevNode + "s,e=" + prevNode.startIndex + ", " + prevNode.endIndex);
		return prevNode;
	}

	@Override public String firstChars(CommonMap<Peg> m) {
		if(this.nextExpr == null) {
			return this.innerExpr.firstChars(m);
		}
		else {
			return this.innerExpr.firstChars(m) + this.nextExpr.firstChars(m);
		}
	}
}

class PegAndPredicate extends PegPredicate {
	PegAndPredicate(String leftLabel, BunToken source, Peg e) {
		super(leftLabel, source, e);
	}
	@Override protected String stringfy() {
		return "&" + this.groupfy(this.innerExpr);
	}
	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = sourceContext.getStackPosition(this);
		node = this.innerExpr.lazyMatchAll(node, sourceContext, true);
		sourceContext.popBack(stackPosition, Peg._BackTrack);
		return node;
	}
	@Override public String firstChars(CommonMap<Peg> m) {
		return this.innerExpr.firstChars(m);
	}

}

class PegNotPredicate extends PegPredicate {
	PegNotPredicate(String leftLabel, BunToken source, Peg e) {
		super(leftLabel, source, e);
	}
	@Override protected String stringfy() {
		return "!" + this.groupfy(this.innerExpr);
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		PegObject node = parentNode;
		int stackPosition = sourceContext.getStackPosition(this);
		node = this.innerExpr.lazyMatchAll(node, sourceContext, hasNextChoice);
		sourceContext.popBack(stackPosition, Peg._BackTrack);
		if(node.isErrorNode()) {
			return parentNode;
		}
		return sourceContext.newUnexpectedErrorNode(this, this.innerExpr, hasNextChoice);
	}
}

class PegChoice extends Peg {
	Peg firstExpr;
	Peg secondExpr;
	PegChoice(String leftLabel, BunToken source, Peg e, Peg e2) {
		super(leftLabel, source);
		this.firstExpr = e;
		this.secondExpr = e2;
	}
	@Override protected String stringfy() {
		return this.firstExpr + " / " + this.secondExpr;
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		Peg e = this;
		int stackPosition = sourceContext.getStackPosition(this);
		while(e instanceof PegChoice) {
			PegObject node = parentNode;
			node = ((PegChoice) e).firstExpr.lazyMatchAll(node, sourceContext, true);
			if(!node.isErrorNode()) {
				return node;
			}
			sourceContext.popBack(stackPosition, Peg._BackTrack);
			e = ((PegChoice) e).secondExpr;
		}
		return this.secondExpr.lazyMatchAll(parentNode, sourceContext, hasNextChoice);
	}

	@Override public String firstChars(CommonMap<Peg> m) {
		return this.firstExpr.firstChars(m) + this.secondExpr.firstChars(m);
	}

	@Override Peg removeLeftRecursion(PegParser p) {
		this.firstExpr = this.firstExpr.removeLeftRecursion(p);
		this.secondExpr = this.secondExpr.removeLeftRecursion(p);
		if(this.firstExpr == null) {
			if(this.secondExpr == null) {
				return null;
			}
			return this.secondExpr;
		}
		if(this.secondExpr == null) {
			return this.firstExpr;
		}
		return this;
	}

	@Override boolean check(PegParser p, String leftName, int order) {
		boolean checkResult = true;
		if(!this.firstExpr.checkAll(p, leftName, order)) {
			checkResult = false;
		}
		if(!this.secondExpr.checkAll(p, leftName, order)) {
			checkResult = false;
		}
		return checkResult;
	}

}

class PegSetter extends PegPredicate {
	int nodeAppendIndex = -1;
	boolean allowError = false;
	public PegSetter(String leftLabel, BunToken source, Peg e, boolean allowError) {
		super(leftLabel, source, e);
		this.innerExpr = e;
		this.allowError = allowError;
	}
	@Override protected String stringfy() {
		String predicate = "$";
		if(this.allowError) {
			predicate = "$$";
		}
		if(this.nodeAppendIndex == -1) {
			return predicate+this.groupfy(this.innerExpr);
		}
		return predicate + this.nodeAppendIndex + "=" + this.groupfy(this.innerExpr);
	}
	@Override public String firstChars(CommonMap<Peg> m) {
		return this.innerExpr.firstChars(m);
	}
	@Override boolean check(PegParser p, String leftName, int order) {
		return this.innerExpr.checkAll(p, leftName, order);
	}
	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		if(this.allowError) {
			int pos = sourceContext.getPosition();
			PegObject node = this.innerExpr.lazyMatchAll(parentNode, sourceContext, false);
			sourceContext.push(this, parentNode, this.nodeAppendIndex, node);
			if(node.isErrorNode()) {
				sourceContext.showPosition("syntax error: " + node, pos);
				int indent = sourceContext.source.getIndentSize(pos);
				sourceContext.skipIndent(indent);
			}
			return parentNode;
		}
		PegObject node = this.innerExpr.lazyMatchAll(parentNode, sourceContext, hasNextChoice);
		if(node.isErrorNode()) {
			return node;
		}
		if(parentNode == node) {
			this.warning("node was not created: nothing to set " + node);
			return parentNode;
		}
		sourceContext.push(this, parentNode, this.nodeAppendIndex, node);
		return parentNode;
	}
}

class PegNewObject extends PegPredicate {
	boolean leftJoin = false;
	String nodeName = null;

	public PegNewObject(String leftLabel, BunToken source, boolean leftJoin, Peg e, String nodeName) {
		super(leftLabel, source, e);
		this.leftJoin = leftJoin;
		this.nodeName = nodeName;
	}

	@Override protected String stringfy() {
		String s = "{";
		if(this.leftJoin) {
			s = s + "+ ";
		}
		return s + this.innerExpr + "}"; // + this.nodeName;
	}

	private boolean containSetter(Peg e, boolean checkSequence) {
		if(e instanceof PegSetter) {
			return true;
		}
		if(e instanceof PegPredicate) {
			return this.containSetter(((PegPredicate) e).innerExpr, true);
		}
		if(e instanceof PegChoice) {
			if(this.containSetter(((PegChoice) e).firstExpr, true)) {
				return true;
			}
			if(this.containSetter(((PegChoice) e).secondExpr, true)) {
				return true;
			}
		}
		if(checkSequence && e.nextExpr != null) {
			return this.containSetter(e.nextExpr, true);
		}
		return false;
	}

	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		// prefetch first node..
		int pos = sourceContext.getPosition();
		int stack = sourceContext.getStackPosition(this);
		Peg sequence = this.innerExpr;
		while(sequence != null && !this.containSetter(sequence, false)) {
			PegObject node = sequence.lazyMatch(parentNode, sourceContext, hasNextChoice);
			if(node.isErrorNode()) {
				return node;
			}
			sequence = sequence.nextExpr;
		}
		PegParsedNode newnode = sourceContext.newPegNode(this, pos, 0);
		if(this.leftJoin) {
			sourceContext.push(this, newnode, 0, parentNode);
		}
		if(sequence != null) {
			System.out.println("appending object by " + this);
			PegObject node = sequence.lazyMatchAll(newnode, sourceContext, false);
			if(node.isErrorNode()) {
				System.out.println("disposing... object pos=" + pos + ", by " + this + "error="+node);
				//sourceContext.popBack(stack, Peg._BackTrack);
				return node;
			}
		}
		int top = sourceContext.getStackPosition(this);
		for(int i = stack; i < top; i++) {
			Log log = sourceContext.logStack.ArrayValues[i];
			if(log.type == 'p' && log.parentNode == newnode) {
				newnode.set(log.index, (PegObject)log.childNode);
			}
		}
		if(newnode.endIndex == 0 || newnode.elementList == null) {
			newnode.endIndex = sourceContext.getPosition();
		}
		return newnode;
	}
	@Override public String firstChars(CommonMap<Peg> m) {
		return this.innerExpr.firstChars(m);
	}
}

class PegSemanticAction extends Peg {
	String name;
	SemanticFunction f;
	PegSemanticAction(String leftLabel, BunToken source, String name, SemanticFunction f) {
		super(leftLabel, source);
		this.name = name;
		this.f = f;
	}
	@Override protected String stringfy() {
		return "    :" + this.name;
	}
	@Override public PegObject lazyMatch(PegObject parentNode, PegContext sourceContext, boolean hasNextChoice) {
		parentNode.setSemanticAction(this.f);
		return parentNode;
	}
	@Override boolean check(PegParser p, String leftName, int order) {
		return true;
	}
}
