package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.util.BField;

public abstract class SemanticFunction {
	@BField final String FUNCTION;
	public SemanticFunction() {
		this.FUNCTION = this.getClass().getSimpleName();
	}
	@Override public String toString() {
		return this.FUNCTION;
	}
	public BNode Invoke(BNode parentNode, PegObject pegObject) {
		return null;
	}
}

