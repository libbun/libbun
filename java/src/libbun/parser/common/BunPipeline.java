package libbun.parser.common;

import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.common.CommonMap;
import libbun.parser.peg.PegParser;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;


public class BunPipeline {
	BunLogger    logger;
	PegParser    parser;
	Namespace    root;
	BunChecker   checker;
	BunDriver    driver;

	public CommonMap<Namespace> spaceMap = new CommonMap<Namespace>(null);

	public final boolean perform(Namespace ns, String ScriptText, String FileName, int LineNumber) {
		boolean AllPassed = true;
		BunBlockNode TopBlockNode = new BunBlockNode(null, ns);
		BunSource source = new BunSource(FileName, LineNumber, ScriptText, this.logger);
		BunParserContext parserContext = this.parser.newContext(source, 0, ScriptText.length());
		while(parserContext.hasNode()) {
			BNode parsedNode = parserContext.parseNode(TopBlockNode, "TopLevel");
			TopBlockNode.Append(parsedNode);
			if(parsedNode.IsErrorNode()) {
				break;
			}
			BNode checkedNode = this.checker.startCheck(parsedNode);
			if(checkedNode != parsedNode) {
				TopBlockNode.ReplaceWith(parsedNode, checkedNode);
			}
			if(checkedNode.IsErrorNode()) {
				break;
			}
		}
		this.logger.OutputErrorsToStdErr();
		@Var int i = 0;
		while(i < TopBlockNode.GetListSize()) {
			@Var BNode bnode = TopBlockNode.GetListAt(i);
			this.driver.perform(bnode);
			i = i + 1;
		}
		this.logger.OutputErrorsToStdErr();
		return AllPassed;
	}


	public final Namespace loadFile(Namespace parent, String fileName, @Nullable BunToken source) {
		Namespace ns = this.spaceMap.GetValue(fileName, null);
		if(ns != null) {
			parent.importFrom(ns);
			return ns;
		}
		ns = new Namespace(this);
		this.spaceMap.put(fileName, ns);
		@Var String ScriptText = LibBunSystem._LoadTextFile(fileName);
		if(ScriptText == null) {
			BunLogger._LogErrorExit(source, "file not found: " + fileName);
			return ns;
		}
		if(!this.perform(ns, ScriptText, fileName, 1)) {
			LibBunSystem._Exit(1, "found top level error: " + fileName);
		}
		parent.importFrom(ns);
		return ns;
	}


}
