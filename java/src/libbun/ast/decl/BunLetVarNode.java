package libbun.ast.decl;

import libbun.ast.BNode;
import libbun.ast.literal.ConstNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.encode.AbstractGenerator;
import libbun.parser.BVisitor;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.BLib;
import libbun.util.Nullable;
import libbun.util.Var;

public class BunLetVarNode extends BNode {
	public static final int _NameInfo = 0;
	public static final int _TypeInfo = 1;
	public final static int _InitValue = 2;

	public final static int _IsExport   = 1;
	public final static int _IsReadOnly = 1 << 1;
	public final static int _IsDefined  = 1 << 2;
	public final static int _IsUsed     = 1 << 3;

	@BField public int     NameFlag = 0;
	@BField public BType   GivenType = null;
	@BField public String  GivenName = null;
	@BField public int     NameIndex = 0;

	public BunLetVarNode(BNode ParentNode, int NameFlag, @Nullable BType GivenType, @Nullable String GivenName) {
		super(ParentNode, null, 3);
		this.NameFlag = NameFlag;
		this.GivenType = GivenType;
		this.GivenName = GivenName;
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		@Var BunLetVarNode NewNode = new BunLetVarNode(ParentNode, this.NameFlag, this.GivenType, this.GivenName);
		NewNode.NameIndex = this.NameIndex;
		return this.DupField(TypedClone, NewNode);
	}

	public final boolean IsExport() {  // export let at top level
		return BLib._IsFlag(this.NameFlag, BunLetVarNode._IsExport);
	}

	public final boolean IsReadOnly() {   // let readonly var writable
		return BLib._IsFlag(this.NameFlag, BunLetVarNode._IsReadOnly);
	}

	public final boolean IsDefined() {    // if assigned
		return BLib._IsFlag(this.NameFlag, BunLetVarNode._IsDefined);
	}

	public final boolean IsUsed() {
		return BLib._IsFlag(this.NameFlag, BunLetVarNode._IsUsed);
	}

	public final void Defined() {
		this.NameFlag = this.NameFlag | BunLetVarNode._IsDefined;
	}

	public final void Used() {
		this.NameFlag = this.NameFlag | BunLetVarNode._IsUsed;
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

	public final String GetGivenName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[BunLetVarNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public final String GetUniqueName(AbstractGenerator Generator) {
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

	@Override public final void Accept(BVisitor Visitor) {
		Visitor.VisitLetNode(this);
	}

	public final boolean IsParamNode() {
		return this.ParentNode instanceof BunFunctionNode;
	}

	public final boolean IsConstValue() {
		return this.InitValueNode() instanceof ConstNode;
	}

}
