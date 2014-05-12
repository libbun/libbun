package libbun.ast.binary;

import libbun.ast.AstNode;
import libbun.ast.expression.GetNameNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.LibBunVisitor;

public class AssignNode extends BinaryOperatorNode {
	public AssignNode(AstNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleAssign);
	}
	public AssignNode(String Name, AstNode RightNode) {
		super(null, BunPrecedence._CStyleAssign);
		this.SetLeftNode(new GetNameNode(null, null, Name));
		this.SetRightNode(RightNode);
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new AssignNode(ParentNode));
	}

	@Override public String GetOperator() {
		return "=";
	}
	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitAssignNode(this);
	}
}
