package libbun.ast.decl;

import libbun.ast.BNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.encode.LibBunGenerator;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public abstract class DefSymbolNode extends BNode {

	public static final int _NameInfo = 0;    // SymbolNode
	public static final int _TypeInfo = 1;    // TypeNode
	public final static int _InitValue = 2;   // InitValue

	public final static int _IsExport   = 1;
	public final static int _IsReadOnly = 1 << 1;
	public final static int _IsDefined  = 1 << 2;
	public final static int _IsUsed     = 1 << 3;

	@BField public int     symbolFlag = 0;
	@BField public String  GivenName = null;
	@BField public BType   GivenType = null;
	@BField public int     NameIndex = 0;

	public DefSymbolNode(BNode parentNode, int size, int symbolFlag) {
		super(parentNode, size);
		this.symbolFlag = symbolFlag;
	}

	public final boolean IsExport() {  // export let at top level
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._IsExport);
	}

	public final boolean IsReadOnly() {   // let readonly var writable
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._IsReadOnly);
	}

	public final boolean IsDefined() {    // if assigned
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._IsDefined);
	}

	public final boolean IsUsed() {
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._IsUsed);
	}

	public final void Defined() {
		this.symbolFlag = this.symbolFlag | BunLetVarNode._IsDefined;
	}

	public final void Used() {
		this.symbolFlag = this.symbolFlag | BunLetVarNode._IsUsed;
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

	public final String GetGivenName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[BunLetVarNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public String GetUniqueName(LibBunGenerator Generator) {
		@Var String Name = Generator.GetNonKeyword(this.GetGivenName());
		if(this.NameIndex == 0 || this.IsExport()) {
			return Name;
		}
		return Generator.NameUniqueSymbol(Name, this.NameIndex);
	}

	public final BNode InitValueNode() {
		if(this.AST[BunLetVarNode._InitValue] == null) {
			this.SetNode(BunLetVarNode._InitValue, new DefaultValueNode(this));
		}
		return this.AST[BunLetVarNode._InitValue];
	}


}
