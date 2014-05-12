package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;


public class DefaultValueNode extends BunValueNode {
	public DefaultValueNode(AstNode ParentNode) {
		super(ParentNode);
	}
	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new DefaultValueNode(ParentNode));
	}
	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("unspecified");
	}
	@Override public final void Accept(LibBunVisitor Visitor) {
		if(Visitor instanceof BunVisitor) {
			((BunVisitor)Visitor).VisitDefaultValueNode(this);
		}
	}
	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitDefaultValueNode(this);
	}


}
