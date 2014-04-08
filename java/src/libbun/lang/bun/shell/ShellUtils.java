package libbun.lang.bun.shell;

import java.io.File;

import libbun.ast.BNode;
import libbun.ast.binary.BBinaryNode;
import libbun.ast.literal.BStringNode;
import libbun.parser.BSource;
import libbun.parser.BSyntax;
import libbun.parser.BToken;
import libbun.parser.BTokenContext;
import libbun.util.BArray;

// you must implement this class if you use shell grammar
public class ShellUtils {
	// suffix option symbol
	public final static String _background = "&";
	// prefix option symbol
	public final static String _timeout = "timeout";
	public final static String _trace = "trace";

	public static String _ToCommandSymbol(String Symbol) {
		return "__$" + Symbol;
	}

	public static boolean _MatchStopToken(BTokenContext TokenContext) { // ;,)]}&&||
		BToken Token = TokenContext.GetToken();
		if(!TokenContext.HasNext()) {
			return true;
		}
		if(Token.IsIndent() || Token.EqualsText(";")) {
			return true;
		}
		if(Token.EqualsText(",") || Token.EqualsText(")") || Token.EqualsText("]") ||
				Token.EqualsText("}") || Token.EqualsText("&&") || Token.EqualsText("||") || Token.EqualsText("`")) {
			return true;
		}
		return false;
	}

	public static BNode _ToNode(BNode ParentNode, BTokenContext TokenContext, BArray<BNode> NodeList) {
		BToken Token = TokenContext.GetToken();
		BNode Node = new BStringNode(ParentNode, null, "");
		BSyntax Pattern = TokenContext.NameSpace.GetRightSyntaxPattern("+");
		BToken PlusToken = new BToken(new BSource(Token.GetFileName(), Token.GetLineNumber(), "+", TokenContext), 0, "+".length());
		int size = NodeList.size();
		for(int i = 0; i < size; i++) {
			BNode CurrentNode = BArray.GetIndex(NodeList, i);
			BBinaryNode BinaryNode = new BBinaryNode(ParentNode, PlusToken, Node, Pattern.SyntaxFlag);
			BinaryNode.SetNode(BBinaryNode._Right, CurrentNode);
			Node = BinaryNode;
		}
		return Node;
	}

	public static boolean _IsFileExecutable(String Path) {
		return new File(Path).canExecute();
	}

	public static String _ResolveHome(String Path) {
		if(Path.equals("~")) {
			return System.getenv("HOME");
		}
		else if(Path.startsWith("~/")) {
			return System.getenv("HOME") + Path.substring(1);
		}
		return Path;
	}

	public static String _GetUnixCommand(String cmd) {
		String[] Paths = System.getenv("PATH").split(":");
		for(String Path : Paths) {
			String FullPath = _ResolveHome(Path + "/" + cmd);
			if(_IsFileExecutable(FullPath)) {
				return FullPath;
			}
		}
		return null;
	}
}
