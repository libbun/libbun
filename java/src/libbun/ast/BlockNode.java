package libbun.ast;

import libbun.ast.decl.BunLetVarNode;
import libbun.common.CommonStringBuilder;
import libbun.parser.common.BunModelVisitor;
import libbun.parser.common.SymbolTable;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public class BlockNode extends BunNode {

	@BField public SymbolTable NullableGamma;

	public BlockNode(AstNode parentNode, @Nullable SymbolTable gamma) {
		super(parentNode, 0);
		this.NullableGamma = gamma;
	}


	@Override
	public AstNode dup(boolean typedClone, AstNode parentNode) {
		return this.dupField(typedClone, new BlockNode(parentNode, this.NullableGamma));
	}

	public void normalize() {
		for(int i = 0; i < this.size(); i++) {
			AstNode node = this.get(i);
			if(node instanceof BunLetVarNode) {
				if(((BunLetVarNode) node).getBlockNode() == null) {
					BlockNode newblock = new BlockNode(node, null);
					int newsize = this.size() - (i + 1);
					if(newsize > 0) {
						newblock.expandAstToSize(newsize);
						this.copyAstTo(i+1, newblock, 0, newsize);
						node.set(BunLetVarNode._Block, newblock);
						this.resizeAst(i+1);
						newblock.normalize();
					}
				}
			}
		}
	}

	@Override
	public void bunfy(CommonStringBuilder builder) {
		this.bunfyAST(builder, "(block", 0, ")");
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitBlockNode(this);
	}

	public final SymbolTable getBlockSymbolTable() {
		if(this.NullableGamma == null) {
			@Var SymbolTable parentTable = this.getSymbolTable();
			this.NullableGamma = new SymbolTable(parentTable.namespace);
		}
		return this.NullableGamma;
	}



	public final void ReplaceWith(AstNode OldNode, AstNode NewNode) {
		@Var int i = 0;
		while(i < this.size()) {
			if(this.AST[i] == OldNode) {
				this.AST[i] = NewNode;
				this.SetChild(NewNode, AstNode._EnforcedParent);
				if(NewNode.HasUntypedNode()) {
					this.HasUntyped = true;
				}
				return;
			}
			i = i + 1;
		}
		//		System.out.println("no replacement");
		assert(OldNode == NewNode);  // this must not happen!!
	}



}
