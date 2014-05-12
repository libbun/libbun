package libbun.ast.expression;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunToken;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.util.BField;

public class ApplyMacroNode extends AbstractListNode {

	public String macroText = null;
	public BNode  origNode = null;

	public ApplyMacroNode(String macroText, BNode node, int startIndex) {
		super(node.ParentNode, 0);
		this.macroText = macroText;
		this.origNode = node;
		this.SourceToken = node.SourceToken;
		for(int i = startIndex; i < node.GetAstSize(); i++) {
			this.Append(node.AST[i]);
		}
		this.Type = node.Type;
	}

	@BField public BFormFunc FormFunc;

	public ApplyMacroNode(BNode ParentNode, BunToken sourceToken, BFormFunc FormFunc) {
		super(ParentNode, 0);
		this.SourceToken = sourceToken;
		this.FormFunc = FormFunc;
		assert(FormFunc != null);
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		return this.dupField(TypedClone, new ApplyMacroNode(ParentNode, this.SourceToken, this.FormFunc));
	}

	public final BFuncType GetFuncType() {
		return this.FormFunc.GetFuncType();
	}

	public final String GetFormText() {
		if(this.FormFunc != null) {
			return this.FormFunc.FormText;
		}
		return this.macroText;
	}

	@Override public void Accept(LibBunVisitor Visitor) {
		Visitor.visitApplyMacroNode(this);
	}

}

