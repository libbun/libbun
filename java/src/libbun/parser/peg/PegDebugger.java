package libbun.parser.peg;

import java.io.IOException;

import libbun.ast.BNode;
import libbun.parser.classic.LibBunConst;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class PegDebugger {
	public final static void test(String FileName) {
		PegParser p = new PegParser(null, null);
		BunSemanticActionSet.LoadGrammar(p);
		p.loadPegFile(FileName);
		PerformShell(p);
	}

	public final static void PerformShell(PegParser p) {
		LibBunSystem._PrintLine(LibBunConst.ProgName + LibBunConst.Version + " (" + LibBunConst.CodeName + ") on " + LibBunSystem._GetPlatform());
		LibBunSystem._PrintLine(LibBunConst.Copyright);
		LibBunSystem._PrintLine("PEG debugger!!");
		@Var int linenum = 1;
		@Var String Line = null;
		while ((Line = ReadLine2(">>> ", "    ")) != null) {
			try {
				BunSource source = new BunSource("(stdin)", linenum, Line, null);
				PegContext context = (PegContext) p.newContext(source, 0, Line.length());
				BunToken token = context.newToken(0, Line.length());
				PegObject po = context.parsePegNode(new PegParsedNode(null, 0, 0), "Stmt", false/*hasNextChoice*/);
				System.out.println("parsed: " + po.toString(token));
				if(context.hasChar()) {
					System.out.println("** uncosumed: '" + context + "' **");
				}
				System.out.println("hit: " + context.memoHit + ", miss: " + context.memoMiss + ", object=" + context.objectCount + ", error=" + context.errorCount);
				System.out.println("backtrackCount: " + context.backtrackCount + ", backtrackLength: " + context.backtrackSize);

				BNode bnode = po.eval(source, null);
				System.out.println("bun: " + bnode);
				System.out.println("bunfy: " + bnode.bunfy());
				linenum = linenum + 1;
			}
			catch (Exception e) {
				PrintStackTrace(e, linenum);
				linenum = linenum + 1;
			}
		}
		LibBunSystem._PrintLine("");
	}

	private static jline.ConsoleReader ConsoleReader = null;

	private final static String ReadLine2(String Prompt, String Prompt2) {
		if(ConsoleReader == null) {
			try {
				ConsoleReader = new jline.ConsoleReader();
				//ConsoleReader.setExpandEvents(false);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		@Var String Line;
		try {
			Line = ConsoleReader.readLine(Prompt);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		if(Line == null) {
			System.exit(0);
		}
		if(Prompt2 != null) {
			@Var int level = 0;
			while((level = CheckBraceLevel(Line)) > 0) {
				String Line2;
				try {
					Line2 = ConsoleReader.readLine(Prompt2);
					//Line2 = ConsoleReader.readLine(Prompt2 + ZenUtils.JoinStrings("  ", level));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				Line += "\n" + Line2;
			}
			if(level < 0) {
				Line = "";
				LibBunSystem._PrintLine(" .. canceled");
			}
		}
		ConsoleReader.getHistory().addToHistory(Line);
		return Line;
	}

	private static void PrintStackTrace(Exception e, int linenum) {
		@Var StackTraceElement[] elements = e.getStackTrace();
		@Var int size = elements.length + 1;
		@Var StackTraceElement[] newElements = new StackTraceElement[size];
		@Var int i = 0;
		for(; i < size; i++) {
			if(i == size - 1) {
				newElements[i] = new StackTraceElement("<TopLevel>", "TopLevelEval", "stdin", linenum);
				break;
			}
			newElements[i] = elements[i];
		}
		e.setStackTrace(newElements);
		e.printStackTrace();
	}

	private final static int CheckBraceLevel(String Text) {
		@Var int level = 0;
		@Var int i = 0;
		while(i < Text.length()) {
			@Var char ch = Text.charAt(i);
			if(ch == '{' || ch == '[') {
				level = level + 1;
			}
			if(ch == '}' || ch == ']') {
				level = level - 1;
			}
			i = i + 1;
		}
		return level;
	}
}

