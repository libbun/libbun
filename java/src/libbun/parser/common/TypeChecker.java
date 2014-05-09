package libbun.parser.common;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.error.TypeErrorNode;
import libbun.ast.expression.BunFormNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.unary.BunCastNode;
import libbun.type.BFormFunc;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BGreekType;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public abstract class TypeChecker extends BunChecker {

	Namespace root = null;
	private BType      StackedContextType = null;
	private BNode      ReturnedNode = null;

	public TypeChecker() {

	}

	@Override public BNode startCheck(BNode bnode) {
		this.root = bnode.getSymbolTable().namespace;
		return this.CheckType(bnode, BType.VoidType);
	}

	public final BNode CheckType(BNode Node, BType ContextType) {
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

	public final BNode TryType(BNode Node, BType ContextType) {
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

	protected void InferType(BType contextType, BNode bnode) {

	}

	private BNode VisitNode(BNode Node, BType ContextType) {
		@Var BNode ParentNode = Node.ParentNode;
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
			ParentNode.SetChild(Node, BNode._PreservedParent);
		}
		return Node;
	}

	private final BNode typeCheckImpl(BNode Node, BType ContextType) {
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

	private final BNode tryTypeImpl(BNode Node, BType ContextType) {
		if(Node.IsErrorNode()) {
			if(!ContextType.IsVarType()) {
				this.TypeNode(Node, ContextType);
			}
			return Node;
		}
		return Node;
	}



	public final void TryTypeAt(BNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.TryType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public final void CheckTypeAt(BNode Node, int Index, BType ContextType) {
		//		@Var ZNode N = Node.AST[Index];
		Node.SetNode(Index, this.CheckType(Node.AST[Index], ContextType));
		//		if(N != Node.AST[Index]) {
		//			System.out.println("Node="+Node+"\n\tFrom="+N+"\n\tTo="+Node.AST[Index]);
		//		}
	}


	public void TypeNode(BNode node, BType type) {
		node.Type= type;
	}

	public final BType GetContextType() {
		return this.StackedContextType;
	}

	public final void ReturnNode(BNode Node) {
		if(this.ReturnedNode != null) {
			LibBunSystem._PrintDebug("previous returned node " + Node);
		}
		this.ReturnedNode = Node;
	}

	public final void ReturnTypeNode(BNode Node, BType Type) {
		this.TypeNode(Node, Type);
		this.ReturnNode(Node);
	}

	public final void ReturnErrorNode(BNode Node, BunToken ErrorToken, String Message) {
		if(ErrorToken == null) {
			ErrorToken = Node.SourceToken;
		}
		this.ReturnNode(new ErrorNode(Node.ParentNode, ErrorToken, Message));
	}

	public final void ReturnTypeErrorNode(String Message, BNode ErrorNode) {
		this.ReturnNode(new TypeErrorNode(Message, ErrorNode));
	}

	protected final BNode CreateStupidCastNode(BType Requested, BNode Node, BunToken bunToken, String Message) {
		@Var BNode ErrorNode = new TypeErrorNode(Message + ": " + Node.Type.GetName() + " must be " + Requested.GetName(), Node);
		ErrorNode.SourceToken = bunToken;
		ErrorNode.Type = Requested;
		return ErrorNode;
	}

	public final BNode CreateStupidCastNode(BType Requested, BNode Node) {
		return this.CreateStupidCastNode(Requested, Node, Node.SourceToken, "type error");
	}

	public BNode EnforceNodeType(BNode Node, BType EnforcedType) {
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

	public final void TypeCheckNodeList(AbstractListNode List) {
		@Var int i = 0;
		while(i < List.GetListSize()) {
			@Var BNode SubNode = List.GetListAt(i);
			SubNode = this.CheckType(SubNode, BType.VarType);
			List.SetListAt(i, SubNode);
			i = i + 1;
		}
	}

	public final BNode TypeListNodeAsFuncCall(AbstractListNode FuncNode, BFuncType FuncType) {
		@Var int i = 0;
		@Var BType[] Greek = BGreekType._NewGreekTypes(null);
		//		if(FuncNode.GetListSize() != FuncType.GetFuncParamSize()) {
		//			System.err.println(ZLogger._LogError(FuncNode.SourceToken, "mismatch " + FuncType + ", " + FuncNode.GetListSize()+": " + FuncNode));
		//		}
		while(i < FuncNode.GetListSize()) {
			@Var BNode SubNode = FuncNode.GetListAt(i);
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

	public FuncCallNode CreateFuncCallNode(BNode ParentNode, BunToken sourceToken, String FuncName, BFuncType FuncType) {
		@Var FuncCallNode FuncNode = new FuncCallNode(ParentNode, new BunFuncNameNode(null, sourceToken, FuncName, FuncType));
		FuncNode.Type = FuncType.GetReturnType();
		return FuncNode;
	}

	public final AbstractListNode CreateDefinedFuncCallNode(BNode ParentNode, BunToken sourceToken, BFunc Func) {
		@Var AbstractListNode FuncNode = null;
		if(Func instanceof BFormFunc) {
			FuncNode = new BunFormNode(ParentNode, sourceToken, (BFormFunc)Func);
		}
		else {
			FuncNode = this.CreateFuncCallNode(ParentNode, sourceToken, Func.FuncName, Func.GetFuncType());
		}
		//		FuncNode.Type = Func.GetFuncType().GetRealType();
		return FuncNode;
	}

}
