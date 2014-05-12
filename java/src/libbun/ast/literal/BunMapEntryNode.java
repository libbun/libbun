package libbun.ast.literal;

import libbun.ast.AstNode;
import libbun.ast.LocalDefinedNode;
import libbun.common.CommonStringBuilder;
import libbun.util.BField;

public class BunMapEntryNode extends LocalDefinedNode {
	public final static int _Key = 0;
	public final static int _Value = 1;
	@BField public String  Name = null;

	public BunMapEntryNode(AstNode ParentNode, String KeySymbol) {
		super(ParentNode, 2);
		this.Name = KeySymbol;
	}
	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new BunMapEntryNode(ParentNode, this.Name));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(kv ");
		this.KeyNode().bunfy(builder);
		builder.Append(" ");
		this.ValueNode().bunfy(builder);
		builder.Append(")");
	}

	public final AstNode KeyNode() {
		return this.AST[BunMapEntryNode._Key];
	}

	public final AstNode ValueNode() {
		return this.AST[BunMapEntryNode._Value];
	}
}
