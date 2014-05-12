package libbun.ast.error;

import libbun.ast.AstNode;
import libbun.util.BField;
import libbun.util.Var;

public class TypeErrorNode extends ErrorNode {
	@BField public AstNode ErrorNode;
	public TypeErrorNode(String ErrorMessage, AstNode ErrorNode) {
		super(ErrorNode.ParentNode, ErrorNode.SourceToken, ErrorMessage);
		this.ErrorNode = ErrorNode;
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		@Var AstNode NewErrorNode =  this.ErrorNode.dup(TypedClone, ParentNode);
		if(TypedClone) {
			@Var TypeErrorNode NewNode = new TypeErrorNode(this.ErrorMessage, NewErrorNode);
			return this.dupField(TypedClone, NewNode);
		}
		return NewErrorNode;
	}

}
