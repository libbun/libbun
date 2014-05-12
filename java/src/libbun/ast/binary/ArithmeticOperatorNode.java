package libbun.ast.binary;

import libbun.ast.AstNode;

public abstract class ArithmeticOperatorNode extends BinaryOperatorNode {

	public ArithmeticOperatorNode(AstNode ParentNode, int Precedence) {
		super(ParentNode, Precedence);
	}

}
