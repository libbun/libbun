package libbun.ast.literal;

import libbun.ast.BNode;
import libbun.parser.BVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public class BunAsmNode extends BNode {
	public final static int _Form = 0;
	public static final int _TypeInfo = 1;
	@BField public String RequiredLibrary = null;

	@BField String FormText = null;
	@BField BType  FormType = null;

	public BunAsmNode(BNode ParentNode, String LibName, String FormText, BType FormType) {
		super(ParentNode, null, 2);
		this.RequiredLibrary = LibName;
		this.FormText = FormText;
		this.FormType = FormType;
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		return this.DupField(TypedClone, new BunAsmNode(ParentNode, this.RequiredLibrary, this.FormText, this.FormType));
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

	@Override public void Accept(BVisitor Visitor) {
		Visitor.VisitAsmNode(this);
	}

}
