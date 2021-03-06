package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.ast.literal.DefaultValueNode;
import libbun.type.BType;
import libbun.util.BField;

@Deprecated
public abstract class DefineNameNode extends AstNode {
	public static final int _NameInfo = 0;
	public static final int _TypeInfo = 1;
	public final static int _InitValue = 2;

	@BField public boolean IsReadOnly = false;
	@BField public BType   GivenType = null;
	@BField public String  GivenName = null;

	protected DefineNameNode(AstNode ParentNode, int Size) {
		super(ParentNode, Size);
	}

	@Override public AstNode dup(boolean typedClone, AstNode ParentNode) {
		return null; // FIXME
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


	public final String GetName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[BunLetVarNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public final AstNode InitValueNode() {
		if(this.AST[BunLetVarNode._InitValue] == null) {
			this.SetNode(BunLetVarNode._InitValue, new DefaultValueNode(null));
		}
		return this.AST[BunLetVarNode._InitValue];
	}

}
