package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public class BunAsmNode extends BNode {
	public final static int _Form = 0;
	public static final int _TypeInfo = 1;
	@BField public String RequiredLibrary = null;

	@BField String FormText = null;
	@BField BType  FormType = null;

	public BunAsmNode(BNode ParentNode, String LibName, String FormText, BType FormType) {
		super(ParentNode, 2);
		this.RequiredLibrary = LibName;
		this.FormText = FormText;
		this.FormType = FormType;
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new BunAsmNode(ParentNode, this.RequiredLibrary, this.FormText, this.FormType));
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		builder.Append("(asm ", LibBunSystem._QuoteString("\"", this.GetFormText(), "\""), " ");
		this.FormType().bunfy(builder);
		builder.Append(")");
	}


	public final BType FormType() {
		if(this.FormType == null) {
			this.FormType = this.AST[BunAsmNode._TypeInfo].Type;
		}
		return this.FormType;
	}

	public final String GetFormText() {
		if(this.FormText == null) {
			@Var BNode Node = this.AST[BunAsmNode._Form];
			if(Node instanceof BunStringNode) {
				this.FormText = ((BunStringNode)Node).StringValue;
			}
		}
		return this.FormText;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.VisitAsmNode(this);
	}

}
