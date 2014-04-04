package libbun.lang.bun.shell;

import java.util.ArrayList;

import libbun.ast.BNode;
import libbun.ast.ZEmptyNode;
import libbun.ast.decl.BLetVarNode;
import libbun.ast.literal.BStringNode;
import libbun.parser.BNameSpace;
import libbun.parser.BSyntax;
import libbun.parser.BToken;
import libbun.parser.BTokenContext;
import libbun.type.BType;
import libbun.util.BLib;
import libbun.util.Var;
import libbun.util.BMatchFunction;

public class ImportCommandPatternFunction extends BMatchFunction {
	public final static String _PatternName = "$ImportCommand$";

	public String ResolveHome(String Path) {
		return ShellUtils._ResolveHome(Path);
	}

	public boolean IsFileExecutable(String Path) {
		return ShellUtils._IsFileExecutable(Path);
	}

	public String GetUnixCommand(String cmd) {
		return ShellUtils._GetUnixCommand(cmd);
	}

	private BToken ToCommandToken(ArrayList<BToken> TokenList) {
		if(TokenList.isEmpty()) {
			return null;
		}
		@Var int StartIndex = TokenList.get(0).StartIndex;
		@Var int EndIndex = TokenList.get(TokenList.size() - 1).EndIndex;
		@Var BToken CommandToken = new BToken(TokenList.get(0).Source, StartIndex, EndIndex);
		TokenList.clear();
		return CommandToken;
	}

	private void SetCommandSymbol(BNode ParentNode, ArrayList<BToken> TokenList) {
		@Var BToken CommandToken = this.ToCommandToken(TokenList);
		if(CommandToken == null) {
			return;
		}
		@Var String CommandPath = this.ResolveHome(CommandToken.GetText());
		@Var BNameSpace NameSpace = ParentNode.GetNameSpace();
		@Var int loc = CommandPath.lastIndexOf('/');
		@Var String Command = CommandPath;
		if(loc != -1) {
			if(!this.IsFileExecutable(CommandPath)) {
				System.err.println("[warning] unknown command: " + CommandPath);
				return;
			}
			Command = CommandPath.substring(loc + 1);
		}
		else {
			@Var String FullPath = this.GetUnixCommand(CommandPath);
			if(FullPath == null) {
				System.err.println("[warning] unknown command: " + CommandPath);
				return;
			}
			CommandPath = FullPath;
		}
		@Var BSyntax Syntax = NameSpace.GetSyntaxPattern(Command);
		if(Syntax != null && !(Syntax.MatchFunc instanceof CommandSymbolPatternFunction)) {
			if(BLib.DebugMode) {
				System.err.println("found duplicated syntax pattern: " + Syntax);
			}
			return;
		}
		BLetVarNode Node = new BLetVarNode(ParentNode, BLetVarNode._IsReadOnly, BType.StringType, Command);
		Node.SetNode(BLetVarNode._InitValue, new BStringNode(ParentNode, null, CommandPath));
		NameSpace.SetSymbol(ShellUtils._ToCommandSymbol(Command), Node);
	}

	@Override
	public BNode Invoke(BNode ParentNode, BTokenContext TokenContext, BNode LeftNode) {
		@Var ArrayList<BToken> TokenList = new ArrayList<BToken>();
		TokenContext.MoveNext();
		while(TokenContext.HasNext()) {
			@Var BToken Token = TokenContext.GetToken();
			if(Token.EqualsText(";") || Token.IsIndent()) {
				break;
			}
			if(!Token.EqualsText(",")) {
				TokenList.add(Token);
			}
			if(Token.IsNextWhiteSpace()) {
				this.SetCommandSymbol(ParentNode, TokenList);
			}
			TokenContext.MoveNext();
		}
		this.SetCommandSymbol(ParentNode, TokenList);
		return new ZEmptyNode(ParentNode, null);
	}
}
