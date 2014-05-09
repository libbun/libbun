package libbun.parser.common;

import libbun.common.CommonArray;
import libbun.parser.peg.PegParser;

public class Namespace extends SymbolTable {

	BunPipeline pipe;
	public PegParser parser;
	public CommonArray<String> exportSymbolList;

	public Namespace(BunPipeline pipe) {
		super(null);
		this.namespace = this;
		this.pipe = pipe;
		this.parser = pipe.parser;
	}

	public void importFrom(Namespace ns) {
		for(int i = 0; i < ns.exportSymbolList.size(); i++) {
			String symbol = ns.exportSymbolList.ArrayValues[i];
			this.SetSymbol(symbol, ns.GetSymbol(symbol));
		}
	}


}
