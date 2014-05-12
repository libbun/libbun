package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.ast.BunNode;
import libbun.encode.LibBunGenerator;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public abstract class DefSymbolNode extends BunNode {

	public static final int _NameInfo = 0;    // SymbolNode
	public static final int _TypeInfo = 1;    // TypeNode
	public final static int _InitValue = 2;   // InitValue

	public final static int _IsExport   = 1;
	public final static int _IsReadOnly = 1 << 1;
	public final static int _IsDefined  = 1 << 2;
	public final static int _IsUsed     = 1 << 3;
	public final static int _DefMacro   = 1 << 5;
	public final static int _DefFunc    = 1 << 6;
	public final static int _Parameter  = 1 << 7;


	@BField public int     symbolFlag = 0;
	@BField public String  GivenName = null;
	@BField public BType   GivenType = null;
	@BField public int     NameIndex = 0;

	public DefSymbolNode(AstNode parentNode, int size, int symbolFlag) {
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

	public final boolean isDefMacro() {
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._DefMacro);
	}

	public final boolean isDefFunc() {
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._DefFunc);
	}

	public final boolean isParam() {
		return LibBunSystem._IsFlag(this.symbolFlag, BunLetVarNode._Parameter);
	}

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

	public final BType DeclType() {
		if(this.GivenType == null) {
			if(this.AST[BunLetVarNode._TypeInfo] != null) {
				this.GivenType = this.AST[BunLetVarNode._TypeInfo].Type;
			}
			else {
				this.GivenType = BType.VarType;
			}
		}
		return this.GivenType;
	}

	public final void SetDeclType(BType Type) {
		this.GivenType = Type;
	}

	public final BFuncType getDeclTypeAsFuncType() {
		BType funcType = this.DeclType();
		if(funcType instanceof BFuncType) {
			return (BFuncType)funcType;
		}
		return null;
	}


}
