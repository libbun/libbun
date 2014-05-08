package libbun.parser.ssa;

import java.util.HashMap;

import libbun.common.CommonArray;

public class ConditionalConstantPropagationOptimizer {
	static final private Variable OverDefined = new Variable("", -1, null);
	static final private Variable Undefined = new Variable("", -1, null);
	HashMap<Variable, Variable> Variables;
	CommonArray<Boolean> Epsilon;
}
