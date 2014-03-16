package libbun.lang.bun;

import libbun.parser.ZSourceContext;
import libbun.util.LibZen;
import libbun.util.Var;
import libbun.util.ZTokenFunction;

public class NumberLiteralTokenFunction extends ZTokenFunction {

	public final static char _ParseDigit(ZSourceContext SourceContext) {
		@Var char ch = '\0';
		while(SourceContext.HasChar()) {
			ch = SourceContext.GetCurrentChar();
			if(!LibZen._IsDigit(ch)) {
				break;
			}
			SourceContext.MoveNext();
		}
		return ch;
	}

	@Override public boolean Invoke(ZSourceContext SourceContext) {
		@Var int StartIndex = SourceContext.GetPosition();
		@Var char ch = NumberLiteralTokenFunction._ParseDigit(SourceContext);
		if(ch == '.') {
			SourceContext.MoveNext();
			ch = NumberLiteralTokenFunction._ParseDigit(SourceContext);
			if(ch == 'e' || ch == 'E') {
				SourceContext.MoveNext();
				if(SourceContext.HasChar()) {
					ch = SourceContext.GetCurrentChar();
					if(ch == '+' || ch == '-') {
						SourceContext.MoveNext();
					}
				}
				NumberLiteralTokenFunction._ParseDigit(SourceContext);
			}
			SourceContext.Tokenize("$FloatLiteral$", StartIndex, SourceContext.GetPosition());
		}
		else {
			SourceContext.Tokenize("$IntegerLiteral$", StartIndex, SourceContext.GetPosition());
		}
		return true;
	}

}
