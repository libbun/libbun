package libbun.parser.common;

import libbun.ast.AstNode;
import libbun.ast.decl.DefSymbolNode;
import libbun.ast.error.LegacyErrorNode;
import libbun.ast.error.TypeErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.unary.BunCastNode;
import libbun.type.BFuncType;
import libbun.type.BGreekType;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;

public abstract class TypeChecker extends BunChecker {

	Namespace root = null;
	private BType        StackedContextType = null;
	private AstNode      ReturnedNode = null;

	public TypeChecker() {
	}

	@Override public AstNode startCheck(AstNode bnode) {
		this.root = bnode.getSymbolTable().namespace;
		return this.checkType(bnode, BType.VoidType);
	}

	public final AstNode checkType(AstNode Node, BType ContextType) {
		if(Node != null) {
			if(Node.HasUntypedNode()) {
				Node = this.visitEachNode(Node, ContextType);
				this.inferType(ContextType, Node);
			}
			Node = this.typeCheckImpl(Node, ContextType);
			this.inferType(ContextType, Node);
		}
		this.ReturnedNode = null;
		return Node;
	}

	public final AstNode tryType(AstNode Node, BType ContextType) {
		if(Node != null) {
			if(Node.HasUntypedNode()) {
				Node = this.visitEachNode(Node, ContextType);
				this.inferType(ContextType, Node);
			}
			Node = this.tryTypeImpl(Node, ContextType);
			this.inferType(ContextType, Node);
		}
		this.ReturnedNode = null;
		return Node;
	}

	protected void inferType(BType contextType, AstNode bnode) {

	}

	private AstNode visitEachNode(AstNode Node, BType ContextType) {
		AstNode parentNode = Node.ParentNode;
		this.StackedContextType = ContextType;
		this.ReturnedNode = null;
		Node.Accept(this);
		if(this.ReturnedNode == null) {  /* debug check */
			LibBunSystem._PrintDebug("!! returns no value: " + Node);
		}
		else {
			Node = this.ReturnedNode;
		}
		if(parentNode != Node.ParentNode && parentNode != null) {
			if(Node.ParentNode != null) {
				LibBunSystem._PrintDebug("Preserving parent of typed new node: " + Node);
			}
			parentNode.SetChild(Node, AstNode._PreservedParent);
		}
		return Node;
	}

	private final AstNode typeCheckImpl(AstNode Node, BType ContextType) {
		Node = this.tryTypeImpl(Node, ContextType);
		if(Node.IsUntyped() || ContextType.IsVarType()) {
			return Node;
		}
		if(Node.Type == ContextType || ContextType.Accept(Node.Type)) {
			return Node;
		}
		if(ContextType.IsVoidType() && !Node.Type.IsVoidType()) {
			return new BunCastNode(Node.ParentNode, BType.VoidType, Node);
		}
		if(ContextType.IsFloatType() && Node.Type.IsIntType()) {
			return this.EnforceNodeType(Node, ContextType);
		}
		if(ContextType.IsIntType() && Node.Type.IsFloatType()) {
			return this.EnforceNodeType(Node, ContextType);
		}
		return this.CreateStupidCastNode(ContextType, Node);
	}

	private final AstNode tryTypeImpl(AstNode Node, BType ContextType) {
		if(Node.IsErrorNode()) {
			if(!ContextType.IsVarType()) {
				this.typeNode(Node, ContextType);
			}
			return Node;
		}
		return Node;
	}

	public final void TryTypeAt(AstNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.tryType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public final void CheckTypeAt(AstNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.checkType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public void typeNode(AstNode node, BType type) {
		node.Type= type;
	}

	public final BType getContextType() {
		return this.StackedContextType;
	}

	public final void returnNode(AstNode Node) {
		if(this.ReturnedNode != null) {
			LibBunSystem._PrintDebug("previous returned node " + Node);
		}
		this.ReturnedNode = Node;
	}

	public final void returnTypeNode(AstNode Node, BType Type) {
		this.typeNode(Node, Type);
		this.returnNode(Node);
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex, BType t0, BType t1) {
		this.returnNode(this.checkAstWithFuncType(name, bnode, startIndex, t0, t1));
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex, BType t0) {
		this.returnNode(this.checkAstWithFuncType(name, bnode, startIndex, t0, BType.VarType));
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex) {
		this.returnNode(this.checkAstWithFuncType(name, bnode, startIndex, BType.VarType, BType.VarType));
	}

	public final void returnNode(AstNode Node, BunToken ErrorToken, String Message) {
		if(ErrorToken == null) {
			ErrorToken = Node.SourceToken;
		}
		this.returnNode(new LegacyErrorNode(Node.ParentNode, ErrorToken, Message));
	}

	public final void returnErrorNode(AstNode Node, String Message) {
		this.returnNode(new LegacyErrorNode(Node.ParentNode, Node.SourceToken, Message));
	}

	public final void returnTypeErrorNode(String Message, AstNode ErrorNode) {
		this.returnNode(new TypeErrorNode(Message, ErrorNode));
	}

	protected final AstNode CreateStupidCastNode(BType Requested, AstNode Node, BunToken bunToken, String Message) {
		@Var AstNode ErrorNode = new TypeErrorNode(Message + ": " + Node.Type.GetName() + " must be " + Requested.GetName(), Node);
		ErrorNode.SourceToken = bunToken;
		ErrorNode.Type = Requested;
		return ErrorNode;
	}

	public final AstNode CreateStupidCastNode(BType Requested, AstNode Node) {
		return this.CreateStupidCastNode(Requested, Node, Node.SourceToken, "type error");
	}

	public AstNode EnforceNodeType(AstNode Node, BType EnforcedType) {
		//		@Var BFunc Func = this.Generator.LookupConverterFunc(Node.Type, EnforcedType);
		//		if(Func == null && EnforcedType.IsStringType()) {
		//			Func = this.Generator.LookupFunc("toString", Node.Type, 1);
		//		}
		//		if(Func != null) {
		//			@Var AbstractListNode FuncNode = this.CreateDefinedFuncCallNode(Node.ParentNode, null, Func);
		//			FuncNode.Append(Node);
		//			return this.TypeListNodeAsFuncCall(FuncNode, Func.GetFuncType());
		//		}
		return this.CreateStupidCastNode(EnforcedType, Node);
	}

	protected AstNode checkAstWithFuncType(String name, AstNode bnode, int startIndex, BType t0, BType t1) {
		t0 = this.checkAstAt(bnode, startIndex, t0);
		t1 = this.checkAstAt(bnode, startIndex+1, t1);
		SymbolTable table = bnode.getSymbolTable();
		BFuncType funcType = this.lookupFuncType(table, name, t0, t1, bnode.size() - startIndex);
		bnode = this.checkTypeByFuncType(bnode, startIndex, funcType);
		if(funcType != null) {
			String macroText = this.lookupMacroText(table, name, t0, t1, bnode.size() - startIndex);
			if(macroText != null) {
				bnode = new ApplyMacroNode(macroText, bnode, startIndex);
			}
		}
		return bnode;
	}

	private BType checkAstAt(AstNode bnode, int startIndex, BType t0) {
		if(startIndex < bnode.size()) {
			this.CheckTypeAt(bnode, startIndex, t0);
			if(t0.IsVarType()) {
				return bnode.getTypeAt(startIndex);
			}
			return t0;
		}
		return BType.VoidType;
	}

	protected BFuncType lookupFuncType(SymbolTable table, String name, BType t0, BType t1, int paramSize) {
		DefSymbolNode defNode = table.getSymbol(name, t0, t1, paramSize);
		if(defNode != null) {
			return defNode.getDeclTypeAsFuncType();
		}
		return null;
	}

	protected String lookupMacroText(SymbolTable table, String name, BType t0, BType t1, int paramSize) {
		DefSymbolNode defNode = table.getSymbol(name, t0, t1, paramSize);
		if(defNode != null && defNode.isDefFunc()) {
			return null; // defNode.getMacroText();  // fix me
		}
		return null;
	}

	protected AstNode checkTypeByFuncType(AstNode bnode, int startIndex, @Nullable BFuncType funcType) {
		int i = startIndex;
		if(funcType == null) {
			for(;i < bnode.size(); i++) {
				this.CheckTypeAt(bnode, i, BType.VarType);
			}
			this.typeNode(bnode, BType.VarType);
			return bnode;
		}
		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
		for(;i < bnode.size(); i++) {
			@Var BType paramType =  funcType.GetFuncParamType(i-startIndex);
			@Var AstNode SubNode = bnode.AST[i];
			SubNode = this.tryType(SubNode, paramType);
			if(!SubNode.IsUntyped() || !paramType.IsVarType()) {
				if(!paramType.AcceptValueType(SubNode.Type, false, Greek)) {
					SubNode = this.CreateStupidCastNode(paramType.GetGreekRealType(Greek), SubNode);
				}
			}
			bnode.SetNode(i, SubNode);
		}
		this.typeNode(bnode, funcType.GetReturnType().GetGreekRealType(Greek));
		return bnode;
	}


}
