package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.ast.BlockNode;
import libbun.ast.literal.ConstNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.common.CommonStringBuilder;
import libbun.encode.LibBunGenerator;
import libbun.parser.classic.LibBunVisitor;
import libbun.parser.common.BunModelVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public class BunLetVarNode extends DefSymbolNode {
	public static final int _NameInfo = 0;    // SymbolNode
	public static final int _TypeInfo = 1;    // TypeNode
	public final static int _InitValue = 2;   // InitValue
	public final static int _Block     = 3;   // Extentional Block


	@BField public int     NameIndex = 0;

	public BunLetVarNode(AstNode ParentNode, int symbolFlag, @Nullable BType GivenType, @Nullable String GivenName) {
		super(ParentNode, 3, symbolFlag);
		this.GivenType = GivenType;
		this.GivenName = GivenName;
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		@Var BunLetVarNode NewNode = new BunLetVarNode(ParentNode, this.symbolFlag, this.GivenType, this.GivenName);
		NewNode.NameIndex = this.NameIndex;
		return this.dupField(typedClone, NewNode);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		String predicate = "(var";
		if(this.IsReadOnly()) {
			predicate = "(let";
		}
		this.bunfyAST(builder, predicate, 0, ")");
	}

	@Override
	public final String GetUniqueName(LibBunGenerator Generator) {
		@Var String Name = Generator.GetNonKeyword(this.GetGivenName());
		if(this.NameIndex == 0 || this.IsExport()) {
			return Name;
		}
		return Generator.NameUniqueSymbol(Name, this.NameIndex);
	}

	@Override public final void Accept(LibBunVisitor Visitor) {
		Visitor.VisitLetNode(this);
	}

	public final AstNode InitValueNode() {
		if(this.AST[BunLetVarNode._InitValue] == null) {
			this.SetNode(BunLetVarNode._InitValue, new DefaultValueNode(this));
		}
		return this.AST[BunLetVarNode._InitValue];
	}

	public final boolean IsParamNode() {
		return (this.ParentNode.ParentNode instanceof BunFunctionNode);
	}

	public final boolean IsConstValue() {
		return this.InitValueNode() instanceof ConstNode;
	}

	public final BlockNode getBlockNode() {
		if(BunLetVarNode._Block < this.size()) {
			AstNode node = this.get(BunLetVarNode._Block);
			if(node instanceof BlockNode) {
				return (BlockNode)node;
			}
		}
		return null;
	}

	@Override
	public void acceptBunModel(BunModelVisitor visitor) {
		visitor.visitLetNode(this);

	}

}
