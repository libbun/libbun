package libbun.parser.classic;

import libbun.util.BField;
import libbun.util.ZenMethod;

public class LibBunLangInfo {
	@BField public String GrammarInfo = "";

	@BField public String LangVersion = null;
	@BField public String Extension = null;

	public boolean AllowTopLevelScript = false;
	public boolean AllowFunctionOverloading = false;
	public boolean AllowUndefinedSymbol = false;

	public LibBunLangInfo(String LangVersion, String Extension) {
		this.LangVersion = LangVersion;
		this.Extension   = Extension;
	}

	public final void AppendGrammarInfo(String GrammarInfo) {
		this.GrammarInfo = this.GrammarInfo + GrammarInfo + " ";
	}

	public final String GetGrammarInfo() {
		return this.GrammarInfo;
	}

	@ZenMethod public String NameOutputFile(String FileName) {
		if(FileName != null) {
			return FileName + "." + this.Extension;
		}
		return FileName;
	}

	public final String GetLibPath(String LibName) {
		return "lib/" + this.Extension + "/" + LibName + ".bun";
	}

	public final String GetLibPath2(String FileName) {
		return "lib/" + this.Extension + "/" + FileName;
	}

}
