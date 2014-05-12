package libbun.ast.expression;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BFuncType;

public class ApplyNode extends AbstractListNode {
	public String    funcName;
	public BFuncType funcType;
	public ApplyNode(AstNode ParentNode, String funcName, BFuncType funcType) {
		super(ParentNode, 0);
		this.funcName = funcName;
		this.funcType = funcType;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new ApplyNode(ParentNode, this.funcName, this.funcType));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(apply " + this.funcName, 0, ")");
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		// TODO Auto-generated method stub

	}

}
