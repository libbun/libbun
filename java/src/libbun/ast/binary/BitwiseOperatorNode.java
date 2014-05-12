package libbun.ast.binary;

import libbun.ast.AstNode;


public abstract class BitwiseOperatorNode extends BinaryOperatorNode {
	public BitwiseOperatorNode(AstNode ParentNode, int Precedence) {
		super(ParentNode, Precedence);
	}
}
