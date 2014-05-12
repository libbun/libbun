package libbun.parser.peg;

import libbun.ast.AstNode;
import libbun.parser.common.BunSource;
import libbun.util.BField;

public abstract class SemanticFunction {
	@BField final String FUNCTION;
	public SemanticFunction() {
		this.FUNCTION = this.getClass().getSimpleName();
	}
	@Override public String toString() {
		return this.FUNCTION;
	}
	public abstract AstNode Invoke(BunSource source, AstNode parentNode, PegObject pegObject);
}

