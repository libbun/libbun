package libbun.parser.ssa;

import libbun.ast.AstNode;
import libbun.ast.expression.GetNameNode;
import libbun.encode.LibBunGenerator;

public class ValueReplacer extends ZASTTransformer {
	//private BNode OldNode;
	private AstNode NewNode;
	private final LibBunGenerator Generator;
	public ValueReplacer(LibBunGenerator Generator) {
		this.SetTarget(null, null);
		this.Generator = Generator;
	}

	public void SetTarget(AstNode OldNode, AstNode NewNode) {
		//this.OldNode = OldNode;
		this.NewNode = NewNode;
	}

	@Override
	protected void VisitAfter(AstNode Node, int Index) {
		if(Node.AST[Index] instanceof GetNameNode) {
			GetNameNode GNode = (GetNameNode) Node.AST[Index];
			PHINode phi = (PHINode) this.NewNode;
			if(phi.EqualsName(GNode, this.Generator)) {
				GNode.VarIndex = phi.GetVarIndex();
			}
		}
	}
}