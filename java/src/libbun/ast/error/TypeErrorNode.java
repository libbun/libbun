package libbun.ast.error;

import libbun.ast.BNode;
import libbun.util.BField;
import libbun.util.Var;

public class TypeErrorNode extends ErrorNode {
	@BField public BNode ErrorNode;
	public TypeErrorNode(String ErrorMessage, BNode ErrorNode) {
		super(ErrorNode.ParentNode, ErrorNode.SourceToken, ErrorMessage);
		this.ErrorNode = ErrorNode;
	}
	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		@Var BNode NewErrorNode =  this.ErrorNode.dup(TypedClone, ParentNode);
		if(TypedClone) {
			@Var TypeErrorNode NewNode = new TypeErrorNode(this.ErrorMessage, NewErrorNode);
			return this.dupField(TypedClone, NewNode);
		}
		return NewErrorNode;
	}

}
