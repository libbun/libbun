package libbun.ast.decl;

import libbun.ast.BNode;
import libbun.ast.literal.ConstNode;
import libbun.common.CommonStringBuilder;
import libbun.encode.LibBunGenerator;
import libbun.parser.classic.LibBunVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Nullable;
import libbun.util.Var;

public class BunLetVarNode extends DefSymbolNode {
	public static final int _NameInfo = 0;    // SymbolNode
	public static final int _TypeInfo = 1;    // TypeNode
	public final static int _InitValue = 2;   // InitValue

	@BField public int     NameIndex = 0;

	public BunLetVarNode(BNode ParentNode, int symbolFlag, @Nullable BType GivenType, @Nullable String GivenName) {
		super(ParentNode, 3, symbolFlag);
		this.GivenType = GivenType;
		this.GivenName = GivenName;
	}

	@Override public BNode dup(boolean TypedClone, BNode ParentNode) {
		@Var BunLetVarNode NewNode = new BunLetVarNode(ParentNode, this.symbolFlag, this.GivenType, this.GivenName);
		NewNode.NameIndex = this.NameIndex;
		return this.dupField(TypedClone, NewNode);
	}

	@Override public void bunfy(CommonStringBuilder builder) {
		String predicate = "(var";
		if(this.IsReadOnly()) {
			predicate = "(let";
		}
		this.bunfyAST(builder, predicate, 0, ")");
	}


	//	public final BType DeclType() {
	//		if(this.GivenType == null) {
	//			if(this.AST[BunLetVarNode._TypeInfo] != null) {
	//				this.GivenType = this.AST[BunLetVarNode._TypeInfo].Type;
	//			}
	//			else {
	//				this.GivenType = BType.VarType;
	//			}
	//		}
	//		return this.GivenType;
	//	}
	//
	//	public final void SetDeclType(BType Type) {
	//		this.GivenType = Type;
	//	}

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

	public final boolean IsParamNode() {
		return (this.ParentNode.ParentNode instanceof BunFunctionNode);
	}

	public final boolean IsConstValue() {
		return this.InitValueNode() instanceof ConstNode;
	}

}
