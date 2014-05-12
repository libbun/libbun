package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.BunVisitor;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunVisitor2;


public class DefaultValueNode extends BunValueNode {
	public DefaultValueNode(BNode ParentNode) {
		super(ParentNode);
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new DefaultValueNode(ParentNode));
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
	public void accept2(BunVisitor2 visitor) {
		visitor.visitDefaultValueNode(this);
	}


}
