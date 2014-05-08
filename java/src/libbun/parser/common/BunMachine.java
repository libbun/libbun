package libbun.parser.common;


public class BunMachine {
	//	BunLogger    logger;
	//	BunParser    parser;
	//	LibBunGamma  rootGamma;
	//	BunChecker   checker;
	//	BunEncoder   encoder;
	//
	//	public final boolean LoadScript(String ScriptText, String FileName, int LineNumber, String pattern) {
	//		@Var boolean AllPassed = true;
	//		BunBlockNode TopBlockNode = new BunBlockNode(null, this.rootGamma);
	//		BunSource source = new BunSource(FileName, LineNumber, ScriptText, this.logger);
	//		@Var BunParserContext parserContext = this.parser.newContext(source, 0, ScriptText.length());
	//		while(parserContext.hasNode()) {
	//			@Var BNode StmtNode = parserContext.parseNode(TopBlockNode, pattern);
	//			TopBlockNode.Append(StmtNode);
	//			if(StmtNode.IsErrorNode()) {
	//				break;
	//			}
	//			BNode checkedNode = this.checker.check(StmtNode);
	//			if(checkedNode != StmtNode) {
	//				TopBlockNode.ReplaceWith(StmtNode, checkedNode);
	//			}
	//			if(checkedNode.IsErrorNode()) {
	//				break;
	//			}
	//		}
	//		this.logger.OutputErrorsToStdErr();
	//		@Var int i = 0;
	//		while(i < TopBlockNode.GetListSize()) {
	//			@Var BNode StmtNode = TopBlockNode.GetListAt(i);
	//			if(!this.encoder.encode(StmtNode)) {
	//				return null;
	//			}
	//			i = i + 1;
	//		}
	//		this.logger.OutputErrorsToStdErr();
	//		return AllPassed;
	//	}

}
