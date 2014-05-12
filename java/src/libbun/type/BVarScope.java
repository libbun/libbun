package libbun.type;

import libbun.ast.AstNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.common.CommonArray;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.parser.common.BunLogger;
import libbun.parser.common.BunToken;
import libbun.util.BField;
import libbun.util.Var;

public final class BVarScope {
	@BField public BVarScope Parent;
	@BField public BunLogger Logger;
	@BField CommonArray<BVarType> VarList;
	@BField int TypedNodeCount = 0;
	@BField int VarNodeCount = 0;
	@BField int UnresolvedSymbolCount = 0;

	public BVarScope(BVarScope Parent, BunLogger Logger, CommonArray<BVarType> VarList) {
		this.Parent = Parent;
		this.Logger = Logger;
		this.VarList = VarList;
		if(this.VarList == null) {
			this.VarList = new CommonArray<BVarType>(new BVarType[8]);
		}
	}

	public void TypeNode(AstNode Node, BType Type) {
		if(Type instanceof BVarType) {
			if(!Type.IsVarType()) {
				Type = Type.GetRealType();
			}
		}
		if(Node.Type != Type) {
			Node.Type = Type;
			this.TypedNodeCount = this.TypedNodeCount + 1;
		}
	}

	public final BType NewVarType(BType VarType, String Name, BunToken sourceToken) {
		if(!(VarType instanceof BVarType) && VarType.IsVarType()) {
			//System.out.println("@@ new var = " + Name);
			VarType = new BVarType(this.VarList, Name, sourceToken);
		}
		return VarType;
	}

	public final void FoundUnresolvedSymbol(String FuncName) {
		//System.out.println("unresolved name: " + FuncName);
		this.UnresolvedSymbolCount = this.UnresolvedSymbolCount + 1;
	}

	public final void InferType(BType ContextType, AstNode Node) {
		//System.out.println("@@ infering .. ContextType=" + ContextType + " Node.Type = " + Node.Type + ", at " + Node);
		if(Node.IsUntyped()) {
			this.VarNodeCount = this.VarNodeCount + 1;
		}
		if(ContextType.IsInferrableType() && Node.Type instanceof BVarType) {
			((BVarType)Node.Type).Infer(ContextType, Node.SourceToken);
			Node.Type = ContextType;
		}
		if(ContextType instanceof BVarType && !Node.IsUntyped()) {
			((BVarType)ContextType).Infer(Node.Type, Node.SourceToken);
		}
	}

	//	public final boolean TypeCheckStmtList(ZTypeChecker TypeSafer, ZArray<ZNode> StmtList) {
	//		@Var int PrevCount = -1;
	//		while(true) {
	//			@Var int i = 0;
	//			this.VarNodeCount = 0;
	//			this.UnresolvedSymbolCount = 0;
	//			while(i < StmtList.size()) {
	//				StmtList.ArrayValues[i] = TypeSafer.CheckType(StmtList.ArrayValues[i], ZType.VoidType);
	//				i = i + 1;
	//			}
	//			if(this.VarNodeCount == 0 || PrevCount == this.VarNodeCount) {
	//				break;
	//			}
	//			PrevCount = this.VarNodeCount;
	//		}
	//		if(this.VarNodeCount == 0) {
	//			return true;
	//		}
	//		return false;
	//	}

	public final void TypeCheckFuncBlock(LibBunTypeChecker TypeSafer, BunFunctionNode FunctionNode) {
		@Var int PrevCount = -1;
		while(true) {
			this.VarNodeCount = 0;
			this.UnresolvedSymbolCount = 0;
			this.TypedNodeCount = 0;
			TypeSafer.DefineFunction(FunctionNode, false/*Enforced*/);
			TypeSafer.CheckTypeAt(FunctionNode, BunFunctionNode._Block, BType.VoidType);
			if(!FunctionNode.blockNode().IsUntyped() || this.TypedNodeCount == 0) {
				break;
			}
			//System.out.println("@@ VarNodeCount="+ this.VarNodeCount + " Block.IsUntyped()=" + FunctionNode.blockNode().IsUntyped());
			//System.out.println("@@ TypedNodeCount=" + this.TypedNodeCount + " Block.IsUntyped()=" + FunctionNode.blockNode().IsUntyped());
			if(this.VarNodeCount == 0 || PrevCount == this.VarNodeCount) {
				break;
			}
			PrevCount = this.VarNodeCount;
		}
		if(this.Parent != null) {
			this.Parent.TypedNodeCount = this.Parent.TypedNodeCount = this.TypedNodeCount;
		}
		if(this.UnresolvedSymbolCount == 0) {
			TypeSafer.DefineFunction(FunctionNode, true);
		}
		else {
			TypeSafer.DefineFunction(FunctionNode, false/*Enforced*/);
			if(this.Parent != null) {
				this.Parent.UnresolvedSymbolCount = this.UnresolvedSymbolCount + this.Parent.UnresolvedSymbolCount;
			}
		}
	}

}
