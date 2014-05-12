package libbun.parser.common;

import libbun.ast.AbstractListNode;
import libbun.ast.AstNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.error.TypeErrorNode;
import libbun.ast.expression.ApplyMacroNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.unary.BunCastNode;
import libbun.type.BFormFunc;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BGreekType;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Nullable;
import libbun.util.Var;

public abstract class TypeChecker extends BunChecker {

	Namespace root = null;
	private BType      StackedContextType = null;
	private AstNode      ReturnedNode = null;

	public TypeChecker() {
	}

	@Override public AstNode startCheck(AstNode bnode) {
		this.root = bnode.getSymbolTable().namespace;
		return this.CheckType(bnode, BType.VoidType);
	}

	public final AstNode CheckType(AstNode Node, BType ContextType) {
		if(Node != null) {
			if(Node.HasUntypedNode()) {
				Node = this.VisitNode(Node, ContextType);
				this.InferType(ContextType, Node);
			}
			Node = this.typeCheckImpl(Node, ContextType);
			this.InferType(ContextType, Node);
		}
		this.ReturnedNode = null;
		return Node;
	}

	public final AstNode TryType(AstNode Node, BType ContextType) {
		if(Node != null) {
			if(Node.HasUntypedNode()) {
				Node = this.VisitNode(Node, ContextType);
				this.InferType(ContextType, Node);
			}
			Node = this.tryTypeImpl(Node, ContextType);
			this.InferType(ContextType, Node);
		}
		this.ReturnedNode = null;
		return Node;
	}

	protected void InferType(BType contextType, AstNode bnode) {

	}

	private AstNode VisitNode(AstNode Node, BType ContextType) {
		@Var AstNode ParentNode = Node.ParentNode;
		this.StackedContextType = ContextType;
		this.ReturnedNode = null;
		Node.Accept(this);
		if(this.ReturnedNode == null) {  /* debug check */
			LibBunSystem._PrintDebug("!! returns no value: " + Node);
		}
		else {
			Node = this.ReturnedNode;
		}
		if(ParentNode != Node.ParentNode && ParentNode != null) {
			if(Node.ParentNode != null) {
				LibBunSystem._PrintDebug("Preserving parent of typed new node: " + Node);
			}
			ParentNode.SetChild(Node, AstNode._PreservedParent);
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
				this.TypeNode(Node, ContextType);
			}
			return Node;
		}
		return Node;
	}



	public final void TryTypeAt(AstNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.TryType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public final void CheckTypeAt(AstNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.CheckType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public void TypeNode(AstNode node, BType type) {
		node.Type= type;
	}

	public final BType GetContextType() {
		return this.StackedContextType;
	}

	public final void ReturnNode(AstNode Node) {
		if(this.ReturnedNode != null) {
			LibBunSystem._PrintDebug("previous returned node " + Node);
		}
		this.ReturnedNode = Node;
	}

	public final void ReturnTypeNode(AstNode Node, BType Type) {
		this.TypeNode(Node, Type);
		this.ReturnNode(Node);
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex, BType t0, BType t1) {
		this.ReturnNode(this.checkTypeAsFuncType(name, bnode, startIndex, t0, t1));
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex, BType t0) {
		this.ReturnNode(this.checkTypeAsFuncType(name, bnode, startIndex, t0, BType.VarType));
	}

	public final void returnNodeAsFuncType(String name, AstNode bnode, int startIndex) {
		this.ReturnNode(this.checkTypeAsFuncType(name, bnode, startIndex, BType.VarType, BType.VarType));
	}

	public final void ReturnErrorNode(AstNode Node, BunToken ErrorToken, String Message) {
		if(ErrorToken == null) {
			ErrorToken = Node.SourceToken;
		}
		this.ReturnNode(new ErrorNode(Node.ParentNode, ErrorToken, Message));
	}

	public final void ReturnErrorNode(AstNode Node, String Message) {
		this.ReturnNode(new ErrorNode(Node.ParentNode, Node.SourceToken, Message));
	}

	public final void ReturnTypeErrorNode(String Message, AstNode ErrorNode) {
		this.ReturnNode(new TypeErrorNode(Message, ErrorNode));
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

	protected AstNode checkTypeAsFuncType(String name, AstNode bnode, int startIndex, BType t0, BType t1) {
		t0 = this.typecheckat(bnode, startIndex, t0);
		t1 = this.typecheckat(bnode, startIndex+1, t1);
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

	private BType typecheckat(AstNode bnode, int startIndex, BType t0) {
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
		return null;
	}

	protected String lookupMacroText(SymbolTable table, String name, BType t0, BType t1, int paramSize) {
		return null;
	}

	protected AstNode checkTypeByFuncType(AstNode bnode, int startIndex, @Nullable BFuncType funcType) {
		int i = startIndex;
		if(funcType == null) {
			for(;i < bnode.size(); i++) {
				this.CheckTypeAt(bnode, i, BType.VarType);
			}
			this.TypeNode(bnode, BType.VarType);
			return bnode;
		}
		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
		for(;i < bnode.size(); i++) {
			@Var BType paramType =  funcType.GetFuncParamType(i-startIndex);
			@Var AstNode SubNode = bnode.AST[i];
			SubNode = this.TryType(SubNode, paramType);
			if(!SubNode.IsUntyped() || !paramType.IsVarType()) {
				if(!paramType.AcceptValueType(SubNode.Type, false, Greek)) {
					SubNode = this.CreateStupidCastNode(paramType.GetGreekRealType(Greek), SubNode);
				}
			}
			bnode.SetNode(i, SubNode);
		}
		this.TypeNode(bnode, funcType.GetReturnType().GetGreekRealType(Greek));
		return bnode;
	}


	public final void TypeCheckNodeList(AbstractListNode List) {
		@Var int i = 0;
		while(i < List.GetListSize()) {
			@Var AstNode SubNode = List.GetListAt(i);
			SubNode = this.CheckType(SubNode, BType.VarType);
			List.SetListAt(i, SubNode);
			i = i + 1;
		}
	}

	public final AstNode TypeListNodeAsFuncCall(AbstractListNode FuncNode, BFuncType FuncType) {
		@Var int i = 0;
		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
		//		if(FuncNode.GetListSize() != FuncType.GetFuncParamSize()) {
		//			System.err.println(ZLogger._LogError(FuncNode.SourceToken, "mismatch " + FuncType + ", " + FuncNode.GetListSize()+": " + FuncNode));
		//		}
		while(i < FuncNode.GetListSize()) {
			@Var AstNode SubNode = FuncNode.GetListAt(i);
			@Var BType ParamType =  FuncType.GetFuncParamType(i);
			SubNode = this.TryType(SubNode, ParamType);
			if(!SubNode.IsUntyped() || !ParamType.IsVarType()) {
				if(!ParamType.AcceptValueType(SubNode.Type, false, Greek)) {
					SubNode = this.CreateStupidCastNode(ParamType.GetGreekRealType(Greek), SubNode);
				}
			}
			FuncNode.SetListAt(i, SubNode);
			i = i + 1;
		}
		this.TypeNode(FuncNode, FuncType.GetReturnType().GetGreekRealType(Greek));
		return FuncNode;
	}

	public FuncCallNode CreateFuncCallNode(AstNode ParentNode, BunToken sourceToken, String FuncName, BFuncType FuncType) {
		@Var FuncCallNode FuncNode = new FuncCallNode(ParentNode, new BunFuncNameNode(null, sourceToken, FuncName, FuncType));
		FuncNode.Type = FuncType.GetReturnType();
		return FuncNode;
	}

	public final AbstractListNode CreateDefinedFuncCallNode(AstNode ParentNode, BunToken sourceToken, BFunc Func) {
		@Var AbstractListNode FuncNode = null;
		if(Func instanceof BFormFunc) {
			FuncNode = new ApplyMacroNode(ParentNode, sourceToken, (BFormFunc)Func);
		}
		else {
			FuncNode = this.CreateFuncCallNode(ParentNode, sourceToken, Func.FuncName, Func.GetFuncType());
		}
		//		FuncNode.Type = Func.GetFuncType().GetRealType();
		return FuncNode;
	}

}
