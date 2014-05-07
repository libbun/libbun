package libbun.parser.peg;

import libbun.parser.classic.LibBunLogger;
import libbun.util.BArray;
import libbun.util.BunMap;
import libbun.util.LibBunSystem;

public final class PegParser {
	public LibBunLogger logger;
	PegParser stackedParser;
	private int priorityCount = 1;
	BunMap<Peg>  pegMap;
	//	BunMap<BNode>    nodeMap;
	BunMap<Peg>      pegCache = null;
	BunMap<String>   keywordCache = null;
	BunMap<String>   firstCharCache = null;

	boolean enableFirstCharChache = false;
	boolean enableMemo = false;

	public PegParser(LibBunLogger logger, PegParser StackedParser) {
		this.logger = logger;
		this.stackedParser = StackedParser;
		this.Init();
	}

	public void Init() {
		this.pegMap = new BunMap<Peg>(null);
		//		this.nodeMap = new BunMap<BNode>(null);
	}

	public PegParser Pop() {
		return this.stackedParser;
	}

	public final boolean loadPegFile(String file) {
		String text = LibBunSystem._LoadTextFile(file);
		if(text == null) {
			LibBunSystem._Exit(1, "file not found: " + file);
		}
		PegContext sourceContext = new PegContext(this, new PegSource(file, 1, text), 0, text.length());
		PegToken token = sourceContext.newToken();
		for(;sourceContext.sliceQuotedTextUntil(token, '\n', "");) {
			int loc = token.indexOf("<-");
			if(loc > 0) {
				String name = token.substring(0,loc).trim();
				PegContext sub = token.newParserContext(this, loc+2);
				Peg e = Peg._ParsePegExpr(name, sub);
				if(e != null) {
					this.setPegRule(name, e);
				}
			}
			token.StartIndex = sourceContext.consume(1);
			token.EndIndex = token.StartIndex;
		}
		this.resetCache();
		return true;
	}

	//	private final void define(String peg) {
	//		int loc = peg.indexOf("<-");
	//		if(loc > 0) {
	//			String name = peg.substring(0, loc).trim();
	//			PegExpr e = builder.parse(peg.substring(loc+2).trim());
	//			this.setPegRule(name, e);
	//			this.pegCache = null;
	//		}
	//	}
	//
	//	public final void define(String peg) {
	//		this.define(new PegBuilder(), peg);
	//	}

	public void setPegRule(String name, Peg e) {
		if(e instanceof PegChoice) {
			this.setPegRule(name, ((PegChoice) e).secondExpr);
			this.setPegRule(name, ((PegChoice) e).firstExpr);
		}
		else {
			this.setPegSequence(name, e);
		}
	}

	private void setPegSequence(String name, Peg e) {
		String key = name;
		if(e instanceof PegLabel) {
			String label = ((PegLabel) e).symbol;
			//System.out.println("first name: " + name + ", " + label);
			if(label.equals(name) && e.nextExpr != null) {
				key = this.nameRightJoinName(key);  // left recursion
				e = e.nextExpr;
			}
		}
		//System.out.println("'"+ key + "' <- " + e + " ## first_chars=" + e.firstChars());
		e.priority = this.priorityCount;
		this.priorityCount = this.priorityCount + 1;
		this.insertOrderedPeg(this.pegMap, key, e);
	}

	String nameRightJoinName(String key) {
		return "+" + key;
	}

	private void insertOrderedPeg(BunMap<Peg> map, String key, Peg e) {
		Peg defined = this.pegMap.GetValue(key, null);
		if(defined != null) {
			e = this.insert(defined, e);
		}
		map.put(key, e);
	}

	private Peg insert(Peg top, Peg added) {
		if(top instanceof PegChoice) {
			Peg e1 = ((PegChoice) top).firstExpr;
			Peg e2 = ((PegChoice) top).secondExpr;
			if(e1.priority == added.priority || e2.priority == added.priority) {
				return top;// drop same priority;
			}
			if(added.priority > e1.priority) {
				return new PegChoice(null, null, added, top);
			}
			((PegChoice) top).secondExpr = this.insert(e2, added);
			return top;
		}
		if(added.priority > top.priority) {
			return new PegChoice(null, null, added, top);
		}
		if(added.priority < top.priority) {
			return new PegChoice(null, null, top, added);
		}
		return top; // drop same priority;
	}

	private void initCache() {
		this.pegCache = new BunMap<Peg>(null);
		this.keywordCache = new BunMap<String>(null);
		this.firstCharCache = new BunMap<String>(null);
		this.firstCharCache.put("0", "");
	}

	public final void resetCache() {
		this.initCache();
		BArray<String> list = this.pegMap.keys();
		for(int i = 0; i < list.size(); i++) {
			String key = list.ArrayValues[i];
			Peg e = this.pegMap.GetValue(key, null);
			//e.removeLeftRecursion(this);
			this.pegMap.put(key, e);
			e.checkAll(this, key, 0);
			System.out.println(e.toPrintableString(key));
		}
		for(int i = 0; i < list.size(); i++) {
			String key = list.ArrayValues[i];
			Peg e = this.pegMap.GetValue(key, null);
			this.setFirstCharCache(key, e);
		}
		list = this.keywordCache.keys();
		for(int i = 0; i < list.size(); i++) {
			String key = list.ArrayValues[i];
			//System.out.println("keyword: " + key);
		}
		list = this.firstCharCache.keys();
		for(int i = 0; i < list.size(); i++) {
			String key = list.ArrayValues[i];
			//System.out.println("cache: '" + key + "'");
		}
		list = this.pegCache.keys();
		for(int i = 0; i < list.size(); i++) {
			String key = list.ArrayValues[i];
			Peg e = this.pegCache.GetValue(key, null);
			//System.out.println("" + key + " <- " + e);
		}
	}

	public void setFirstCharSet(String ch, Peg e) {
		if(ch.length() == 1) {
			this.firstCharCache.put(ch, ch);
		}
	}

	private void setFirstCharCache(String key, Peg e) {
		if(e instanceof PegChoice) {
			this.setFirstCharCache(key, ((PegChoice) e).firstExpr);
			this.setFirstCharCache(key, ((PegChoice) e).secondExpr);
			return;
		}
		String chars = e.firstChars(this.pegMap);
		//		System.out.println("key='"+key+"' " + e + "  chars='"+chars+"'");
		if(chars == null || chars.length() == 0) {
			BArray<String> list = this.firstCharCache.keys();
			for(int i = 0; i < list.size(); i++) {
				String ch = list.ArrayValues[i];
				this.insertOrderedPeg(this.pegCache, key + "$" + ch, e);
			}
		}
		for(int i = 0; i < chars.length(); i++) {
			String ch = "" + chars.charAt(i);
			if(this.firstCharCache.HasKey(ch)) {
				//				System.out.println("key='"+key+"$" + ch + "' " + e + "  chars='"+chars+"'");
				this.insertOrderedPeg(this.pegCache, key + "$" + ch, e);
			}
		}
	}

	public final boolean hasPattern(String name) {
		return this.pegMap.GetValue(name, null) != null;
	}

	public final Peg getPattern(String name, String firstChar) {
		if(this.enableFirstCharChache) {
			if(this.firstCharCache.HasKey(firstChar)) {
				return this.pegCache.GetValue(name+"$"+firstChar, null);
			}
		}
		return this.pegMap.GetValue(name, null);
	}

	public final Peg getRightPattern(String name, String firstChar) {
		return this.getPattern(this.nameRightJoinName(name), firstChar);
	}

}
