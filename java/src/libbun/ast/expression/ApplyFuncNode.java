package libbun.ast.expression;

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BFuncType;

public class ApplyFuncNode extends BunNode {
	public String    funcName;
	public BFuncType funcType;
	public ApplyFuncNode(AstNode ParentNode, String funcName, BFuncType funcType) {
		super(ParentNode, 0);
		this.funcName = funcName;
		this.funcType = funcType;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new ApplyFuncNode(ParentNode, this.funcName, this.funcType));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(apply " + this.funcName, 0, ")");
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitApplyFuncNode(this);
	}

}
