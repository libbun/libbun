package libbun.ast.binary;

import libbun.ast.BNode;
import libbun.ast.expression.GetNameNode;
import libbun.lang.bun.BunPrecedence;
import libbun.parser.classic.LibBunVisitor;

public class AssignNode extends BinaryOperatorNode {
	public AssignNode(BNode ParentNode) {
		super(ParentNode, BunPrecedence._CStyleAssign);
	}
	public AssignNode(String Name, BNode RightNode) {
		super(null, BunPrecedence._CStyleAssign);
		this.SetLeftNode(new GetNameNode(null, null, Name));
		this.SetRightNode(RightNode);
	}
	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new AssignNode(ParentNode));
	}
	@Override public String GetOperator() {
		return "=";
	}
	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitAssignNode(this);
	}
}
