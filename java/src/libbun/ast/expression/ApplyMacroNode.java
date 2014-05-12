package libbun.ast.expression;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunToken;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.util.BField;

public class ApplyMacroNode extends AbstractListNode {

	public String macroText = null;
	public AstNode  origNode = null;

	public ApplyMacroNode(String macroText, AstNode node, int startIndex) {
		super(node.ParentNode, 0);
		this.macroText = macroText;
		this.origNode = node;
		this.SourceToken = node.SourceToken;
		for(int i = startIndex; i < node.size(); i++) {
			this.appendNode(node.AST[i]);
		}
		this.Type = node.Type;
	}

	@BField public BFormFunc FormFunc;

	public ApplyMacroNode(AstNode ParentNode, BunToken sourceToken, BFormFunc FormFunc) {
		super(ParentNode, 0);
		this.SourceToken = sourceToken;
		this.FormFunc = FormFunc;
		assert(FormFunc != null);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return this.dupField(typedClone, new ApplyMacroNode(ParentNode, this.SourceToken, this.FormFunc));
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

