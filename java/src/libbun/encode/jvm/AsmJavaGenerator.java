// ***************************************************************************
// Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

package libbun.encode.jvm;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Stack;

import libbun.ast.BBlockNode;
import libbun.ast.BGroupNode;
import libbun.ast.BListNode;
import libbun.ast.BNode;
import libbun.ast.ZLocalDefinedNode;
import libbun.ast.binary.BAndNode;
import libbun.ast.binary.BBinaryNode;
import libbun.ast.binary.BInstanceOfNode;
import libbun.ast.binary.BOrNode;
import libbun.ast.binary.BunAddNode;
import libbun.ast.binary.BunBitwiseAndNode;
import libbun.ast.binary.BunBitwiseOrNode;
import libbun.ast.binary.BunBitwiseXorNode;
import libbun.ast.binary.BunDivNode;
import libbun.ast.binary.BunEqualsNode;
import libbun.ast.binary.BunGreaterThanEqualsNode;
import libbun.ast.binary.BunGreaterThanNode;
import libbun.ast.binary.BunLeftShiftNode;
import libbun.ast.binary.BunLessThanEqualsNode;
import libbun.ast.binary.BunLessThanNode;
import libbun.ast.binary.BunModNode;
import libbun.ast.binary.BunMulNode;
import libbun.ast.binary.BunNotEqualsNode;
import libbun.ast.binary.BunRightShiftNode;
import libbun.ast.binary.BunSubNode;
import libbun.ast.decl.BClassNode;
import libbun.ast.decl.BFunctionNode;
import libbun.ast.decl.BLetVarNode;
import libbun.ast.decl.ZTopLevelNode;
import libbun.ast.decl.ZVarBlockNode;
import libbun.ast.error.BErrorNode;
import libbun.ast.expression.BFuncCallNode;
import libbun.ast.expression.BFuncNameNode;
import libbun.ast.expression.BGetIndexNode;
import libbun.ast.expression.BGetNameNode;
import libbun.ast.expression.BGetterNode;
import libbun.ast.expression.BMacroNode;
import libbun.ast.expression.BMethodCallNode;
import libbun.ast.expression.BNewObjectNode;
import libbun.ast.expression.BSetIndexNode;
import libbun.ast.expression.BSetNameNode;
import libbun.ast.expression.BSetterNode;
import libbun.ast.literal.BArrayLiteralNode;
import libbun.ast.literal.BAsmNode;
import libbun.ast.literal.BBooleanNode;
import libbun.ast.literal.BFloatNode;
import libbun.ast.literal.BIntNode;
import libbun.ast.literal.BNullNode;
import libbun.ast.literal.BStringNode;
import libbun.ast.literal.BTypeNode;
import libbun.ast.literal.LiteralNode;
import libbun.ast.literal.ZMapEntryNode;
import libbun.ast.literal.ZMapLiteralNode;
import libbun.ast.statement.BBreakNode;
import libbun.ast.statement.BIfNode;
import libbun.ast.statement.BReturnNode;
import libbun.ast.statement.BThrowNode;
import libbun.ast.statement.BTryNode;
import libbun.ast.statement.BWhileNode;
import libbun.ast.unary.BCastNode;
import libbun.ast.unary.BNotNode;
import libbun.ast.unary.BUnaryNode;
import libbun.ast.unary.BunComplementNode;
import libbun.ast.unary.BunMinusNode;
import libbun.ast.unary.BunPlusNode;
import libbun.parser.BGenerator;
import libbun.parser.BLangInfo;
import libbun.parser.BLogger;
import libbun.parser.BNameSpace;
import libbun.parser.BSourceContext;
import libbun.parser.BTokenContext;
import libbun.type.BClassField;
import libbun.type.BClassType;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.type.BTypePool;
import libbun.util.BArray;
import libbun.util.BFunction;
import libbun.util.BLib;
import libbun.util.BMap;
import libbun.util.BMatchFunction;
import libbun.util.BObject;
import libbun.util.BTokenFunction;
import libbun.util.Var;
import libbun.util.ZObjectMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class AsmJavaGenerator extends BGenerator {
	private final BMap<Class<?>> GeneratedClassMap = new BMap<Class<?>>(null);
	public JavaStaticFieldNode MainFuncNode = null;
	AsmClassLoader AsmLoader = null;
	Stack<AsmTryCatchLabel> TryCatchLabel;
	AsmMethodBuilder AsmBuilder;

	public AsmJavaGenerator() {
		super(new BLangInfo("Java1.6", "jvm"));
		this.InitFuncClass();
		this.ImportLocalGrammar(this.RootNameSpace);
		this.TryCatchLabel = new Stack<AsmTryCatchLabel>();
		this.AsmLoader = new AsmClassLoader(this);
	}

	private void ImportLocalGrammar(BNameSpace NameSpace) {
		NameSpace.DefineStatement("import", new JavaImportPattern());
		NameSpace.DefineExpression("$JavaClassPath$", new JavaClassPathPattern());
	}

	private void InitFuncClass() {
		BFuncType FuncType = JavaTypeTable.FuncType(boolean.class, BSourceContext.class);
		this.SetGeneratedClass(this.NameType(FuncType), BTokenFunction.class);
		FuncType = JavaTypeTable.FuncType(BNode.class, BNode.class, BTokenContext.class, BNode.class);
		this.SetGeneratedClass(this.NameType(FuncType), BMatchFunction.class);
	}

	private final void SetGeneratedClass(String Key, Class<?> C) {
		this.GeneratedClassMap.put(Key, C);
	}

	private final Class<?> GetGeneratedClass(String Key, Class<?> DefaultClass) {
		Class<?> C = this.GeneratedClassMap.GetOrNull(Key);
		if(C != null) {
			return C;
		}
		return DefaultClass;
	}

	public Class<?> GetDefinedFunctionClass(String FuncName, BFuncType FuncType) {
		return this.GeneratedClassMap.GetOrNull(this.NameFunctionClass(FuncName, FuncType));
	}

	public Class<?> GetDefinedFunctionClass(String FuncName, BType RecvType, int FuncParamSize) {
		return this.GeneratedClassMap.GetOrNull(this.NameFunctionClass(FuncName, RecvType, FuncParamSize));
	}

	private final BMap<BNode> LazyNodeMap = new BMap<BNode>(null);
	protected void LazyBuild(BFunctionNode Node) {
		this.LazyNodeMap.put(Node.GetSignature(), Node);
	}

	protected void LazyBuild(String Signature) {
		BNode Node = this.LazyNodeMap.GetOrNull(Signature);
		if(Node != null) {
			BLib._PrintDebug("LazyBuilding: " + Signature);
			this.LazyNodeMap.remove(Signature);
			Node.Accept(this);
		}
	}



	public final Class<?> GetJavaClass(BType zType, Class<?> C) {
		if(zType instanceof BFuncType) {
			return this.LoadFuncClass((BFuncType)zType);
		}
		else {
			return JavaTypeTable.GetJavaClass(zType, C);
		}
	}

	public final Class<?> GetJavaClass(BType zType) {
		return this.GetJavaClass(zType, Object.class);
	}

	final Type AsmType(BType zType) {
		Class<?> jClass = this.GetJavaClass(zType, Object.class);
		return Type.getType(jClass);
	}

	final String GetDescripter(BType zType) {
		Class<?> jClass = this.GetJavaClass(zType, null);
		if(jClass != null) {
			return Type.getType(jClass).toString();
		}
		else {
			return "L" + zType + ";";
		}
	}

	final String GetTypeDesc(BType zType) {
		Class<?> JClass = this.GetJavaClass(zType);
		return Type.getDescriptor(JClass);
	}

	final String GetMethodDescriptor(BFuncType FuncType) {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for(int i = 0; i < FuncType.GetFuncParamSize(); i++) {
			BType ParamType = FuncType.GetFuncParamType(i);
			sb.append(this.GetDescripter(ParamType));
		}
		sb.append(")");
		sb.append(this.GetDescripter(FuncType.GetReturnType()));
		String Desc = sb.toString();
		//		String Desc2 = this.GetMethodDescriptor2(FuncType);
		//		System.out.println(" ** Desc: " + Desc + ", " + Desc2 + ", FuncType: " + FuncType);
		return Desc;
	}

	@Override public BType GetFieldType(BType RecvType, String FieldName) {
		Class<?> NativeClass = this.GetJavaClass(RecvType);
		if(NativeClass != null) {
			try {
				java.lang.reflect.Field NativeField = NativeClass.getField(FieldName);
				if(Modifier.isPublic(NativeField.getModifiers())) {
					return JavaTypeTable.GetZenType(NativeField.getType());
				}
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			}
			return BType.VoidType;     // undefined
		}
		return BType.VarType;     // undefined
	}

	@Override public BType GetSetterType(BType RecvType, String FieldName) {
		Class<?> NativeClass = this.GetJavaClass(RecvType);
		if(NativeClass != null) {
			try {
				java.lang.reflect.Field NativeField = NativeClass.getField(FieldName);
				if(Modifier.isPublic(NativeField.getModifiers()) && !Modifier.isFinal(NativeField.getModifiers())) {
					return JavaTypeTable.GetZenType(NativeField.getType());
				}
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			}
			return BType.VoidType;     // undefined
		}
		return BType.VarType;     // undefined
	}

	private boolean MatchParam(Class<?>[] jParams, BListNode ParamList) {
		if(jParams.length != ParamList.GetListSize()) {
			return false;
		}
		for(int j = 0; j < jParams.length; j++) {
			if(jParams[j] == Object.class) {
				continue; // accepting all types
			}
			@Var BType jParamType = JavaTypeTable.GetZenType(jParams[j]);
			@Var BType ParamType = ParamList.GetListAt(j).Type;
			if(jParamType == ParamType || jParamType.Accept(ParamList.GetListAt(j).Type)) {
				continue;
			}
			if(jParamType.IsFloatType() && ParamType.IsIntType()) {
				continue;
			}
			if(jParamType.IsIntType() && ParamType.IsFloatType()) {
				continue;
			}
			return false;
		}
		return true;
	}

	protected Constructor<?> GetConstructor(BType RecvType, BListNode ParamList) {
		Class<?> NativeClass = this.GetJavaClass(RecvType);
		if(NativeClass != null) {
			try {
				Constructor<?>[] Methods = NativeClass.getConstructors();
				for(int i = 0; i < Methods.length; i++) {
					@Var Constructor<?> jMethod = Methods[i];
					if(!Modifier.isPublic(jMethod.getModifiers())) {
						continue;
					}
					if(this.MatchParam(jMethod.getParameterTypes(), ParamList)) {
						return jMethod;
					}
				}
			} catch (SecurityException e) {
			}
		}
		return null;
	}

	protected Method GetMethod(BType RecvType, String MethodName, BListNode ParamList) {
		Class<?> NativeClass = this.GetJavaClass(RecvType);
		if(NativeClass != null) {
			try {
				Method[] Methods = NativeClass.getMethods();
				for(int i = 0; i < Methods.length; i++) {
					@Var Method jMethod = Methods[i];
					if(!MethodName.equals(jMethod.getName())) {
						continue;
					}
					if(!Modifier.isPublic(jMethod.getModifiers())) {
						continue;
					}
					if(this.MatchParam(jMethod.getParameterTypes(), ParamList)) {
						return jMethod;
					}
				}
			} catch (SecurityException e) {
			}
		}
		return null;
	}

	@Override public BFuncType GetMethodFuncType(BType RecvType, String MethodName, BListNode ParamList) {
		if(MethodName == null) {
			Constructor<?> jMethod = this.GetConstructor(RecvType, ParamList);
			if(jMethod != null) {
				@Var Class<?>[] ParamTypes = jMethod.getParameterTypes();
				@Var BArray<BType> TypeList = new BArray<BType>(new BType[ParamTypes.length + 2]);
				if (ParamTypes != null) {
					@Var int j = 0;
					while(j < BLib._Size(ParamTypes)) {
						TypeList.add(JavaTypeTable.GetZenType(ParamTypes[j]));
						j = j + 1;
					}
				}
				TypeList.add(RecvType);
				return BTypePool._LookupFuncType2(TypeList);
			}
		}
		else {
			Method jMethod = this.GetMethod(RecvType, MethodName, ParamList);
			if(jMethod != null) {
				return JavaTypeTable.ConvertToFuncType(jMethod);
			}
		}
		return null;
	}



	@Override public void VisitNullNode(BNullNode Node) {
		this.AsmBuilder.visitInsn(Opcodes.ACONST_NULL);
	}

	@Override public void VisitBooleanNode(BBooleanNode Node) {
		this.AsmBuilder.PushBoolean(Node.BooleanValue);
	}

	@Override public void VisitIntNode(BIntNode Node) {
		this.AsmBuilder.PushLong(Node.IntValue);
	}

	@Override public void VisitFloatNode(BFloatNode Node) {
		this.AsmBuilder.PushDouble(Node.FloatValue);
	}

	@Override public void VisitStringNode(BStringNode Node) {
		this.AsmBuilder.visitLdcInsn(Node.StringValue);
	}

	@Override public void VisitArrayLiteralNode(BArrayLiteralNode Node) {
		if(Node.IsUntyped()) {
			this.VisitErrorNode(new BErrorNode(Node, "ambigious array"));
		}
		else {
			Class<?> ArrayClass = LibAsm.AsArrayClass(Node.Type);
			String Owner = Type.getInternalName(ArrayClass);
			this.AsmBuilder.visitTypeInsn(NEW, Owner);
			this.AsmBuilder.visitInsn(DUP);
			this.AsmBuilder.PushInt(Node.Type.TypeId);
			this.AsmBuilder.PushNodeListAsArray(LibAsm.AsElementClass(Node.Type), 0, Node);
			this.AsmBuilder.SetLineNumber(Node);
			this.AsmBuilder.visitMethodInsn(INVOKESPECIAL, Owner, "<init>", LibAsm.NewArrayDescriptor(Node.Type));
		}
	}

	@Override public void VisitMapLiteralNode(ZMapLiteralNode Node) {
		if(Node.IsUntyped()) {
			this.VisitErrorNode(new BErrorNode(Node, "ambigious map"));
		}
		else {
			String Owner = Type.getInternalName(ZObjectMap.class);
			this.AsmBuilder.visitTypeInsn(NEW, Owner);
			this.AsmBuilder.visitInsn(DUP);
			this.AsmBuilder.PushInt(Node.Type.TypeId);
			this.AsmBuilder.PushInt(Node.GetListSize() * 2);
			this.AsmBuilder.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
			for(int i = 0; i < Node.GetListSize() ; i++) {
				ZMapEntryNode EntryNode = Node.GetMapEntryNode(i);
				this.AsmBuilder.visitInsn(DUP);
				this.AsmBuilder.PushInt(i * 2);
				this.AsmBuilder.PushNode(String.class, EntryNode.KeyNode());
				this.AsmBuilder.visitInsn(Opcodes.AASTORE);
				this.AsmBuilder.visitInsn(DUP);
				this.AsmBuilder.PushInt(i * 2 + 1);
				this.AsmBuilder.PushNode(Object.class, EntryNode.ValueNode());
				this.AsmBuilder.visitInsn(Opcodes.AASTORE);
			}
			this.AsmBuilder.SetLineNumber(Node);
			String Desc = Type.getMethodDescriptor(Type.getType(void.class), new Type[] { Type.getType(int.class),  Type.getType(Object[].class)});
			this.AsmBuilder.visitMethodInsn(INVOKESPECIAL, Owner, "<init>", Desc);
		}
	}

	//	@Override public void VisitNewArrayNode(ZNewArrayNode Node) {
	//		this.Debug("TODO");
	//		this.AsmBuilder.visitInsn(Opcodes.ACONST_NULL);
	//		//		this.CurrentBuilder.LoadConst(Node.Type);
	//		//		this.CurrentBuilder.LoadNewArray(this, 0, Node.NodeList);
	//		//		this.CurrentBuilder.InvokeMethodCall(Node.Type, JLib.NewArray);
	//	}

	@Override public void VisitNewObjectNode(BNewObjectNode Node) {
		if(Node.IsUntyped()) {
			this.VisitErrorNode(new BErrorNode(Node, "no class for new operator"));
			return;
		}
		// check class existence
		if(!Node.Type.Equals(JavaTypeTable.GetZenType(this.GetJavaClass(Node.Type)))) {
			this.VisitErrorNode(new BErrorNode(Node, "undefined class: " + Node.Type));
			return;
		}
		String ClassName = Type.getInternalName(this.GetJavaClass(Node.Type));
		this.AsmBuilder.visitTypeInsn(NEW, ClassName);
		this.AsmBuilder.visitInsn(DUP);
		Constructor<?> jMethod = this.GetConstructor(Node.Type, Node);
		if(jMethod != null) {
			Class<?>[] P = jMethod.getParameterTypes();
			for(int i = 0; i < P.length; i++) {
				this.AsmBuilder.PushNode(P[i], Node.GetListAt(i));
			}
			this.AsmBuilder.SetLineNumber(Node);
			this.AsmBuilder.visitMethodInsn(INVOKESPECIAL, ClassName, "<init>", Type.getConstructorDescriptor(jMethod));
		}
		else {
			this.VisitErrorNode(new BErrorNode(Node, "no constructor: " + Node.Type));
		}
	}

	protected void VisitVarDeclNode(BLetVarNode Node) {
		Class<?> DeclClass = this.GetJavaClass(Node.DeclType());
		this.AsmBuilder.AddLocal(DeclClass, Node.GetGivenName());
		this.AsmBuilder.PushNode(DeclClass, Node.InitValueNode());
		this.AsmBuilder.StoreLocal(Node.GetGivenName());
		if(Node.HasNextVarNode()) {
			this.VisitVarDeclNode(Node.NextVarNode());
		}
		//		this.VisitBlockNode(Node);
		//		this.AsmBuilder.RemoveLocal(DeclClass, Node.GetName());
	}

	protected void VisitVarDeclNode2(BLetVarNode Node) {
		if(Node.HasNextVarNode()) {
			this.VisitVarDeclNode(Node.NextVarNode());
		}
		Class<?> DeclClass = this.GetJavaClass(Node.DeclType());
		this.AsmBuilder.RemoveLocal(DeclClass, Node.GetGivenName());
	}

	@Override public void VisitVarBlockNode(ZVarBlockNode Node) {
		this.VisitVarDeclNode(Node.VarDeclNode());
		this.VisitBlockNode(Node);
		this.VisitVarDeclNode2(Node.VarDeclNode());
	}

	public void VisitStaticFieldNode(JavaStaticFieldNode Node) {
		this.AsmBuilder.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Node.StaticClass), Node.FieldName, this.GetJavaClass(Node.Type));
	}

	//	@Override public void VisitGlobalNameNode(ZFuncNameNode Node) {
	//		if(Node.IsFuncNameNode()) {
	//			this.AsmBuilder.visitFieldInsn(Opcodes.GETSTATIC, this.NameFunctionClass(Node.GlobalName, Node.FuncType), "f", this.GetJavaClass(Node.Type));
	//		}
	//		else if(!Node.IsUntyped()) {
	//			this.AsmBuilder.visitFieldInsn(Opcodes.GETSTATIC, this.NameGlobalNameClass(Node.GlobalName), "_", this.GetJavaClass(Node.Type));
	//		}
	//		else {
	//			ZLogger._LogError(Node.SourceToken, "undefined symbol: " + Node.GlobalName);
	//			this.AsmBuilder.visitInsn(Opcodes.ACONST_NULL);
	//		}
	//	}

	protected void VisitGlobalNameNode(BGetNameNode Node) {
		if(Node.ResolvedNode instanceof BLetVarNode) {
			BLetVarNode LetNode = Node.ResolvedNode;
			Class<?> JavaClass = this.GetJavaClass(LetNode.GetAstType(BLetVarNode._NameInfo));
			this.AsmBuilder.visitFieldInsn(GETSTATIC, this.NameGlobalNameClass(LetNode.GetUniqueName(this)), "_", JavaClass);
		}
		else {
			this.VisitErrorNode(new BErrorNode(Node, "unimplemented ResolvedNode: " + Node.ResolvedNode.getClass().getName()));
		}
	}

	@Override public void VisitGetNameNode(BGetNameNode Node) {
		if(Node.ResolvedNode == null) {
			this.VisitErrorNode(new BErrorNode(Node, "undefined symbol: " + Node.GivenName));
			return;
		}
		if(Node.ResolvedNode.GetDefiningFunctionNode() == null) {
			this.VisitGlobalNameNode(Node);
			return;
		}
		this.AsmBuilder.LoadLocal(Node.GetUniqueName(this));
		this.AsmBuilder.CheckReturnCast(Node, this.AsmBuilder.GetLocalType(Node.GetUniqueName(this)));
	}

	@Override public void VisitSetNameNode(BSetNameNode Node) {
		@Var String Name = Node.NameNode().GetUniqueName(this);
		this.AsmBuilder.PushNode(this.AsmBuilder.GetLocalType(Name), Node.ExprNode());
		this.AsmBuilder.StoreLocal(Name);
	}

	@Override public void VisitGroupNode(BGroupNode Node) {
		Node.ExprNode().Accept(this);
	}

	private Field GetField(Class<?> RecvClass, String Name) {
		try {
			return RecvClass.getField(Name);
		} catch (Exception e) {
			BLib._FixMe(e);
		}
		return null;  // type checker guarantees field exists
	}

	@Override public void VisitGetterNode(BGetterNode Node) {
		if(Node.IsUntyped()) {
			Method sMethod = JavaMethodTable.GetStaticMethod("GetField");
			BNode NameNode = new BStringNode(Node, null, Node.GetName());
			this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.RecvNode(), NameNode});
		}
		else {
			Class<?> RecvClass = this.GetJavaClass(Node.RecvNode().Type);
			Field jField = this.GetField(RecvClass, Node.GetName());
			String Owner = Type.getType(RecvClass).getInternalName();
			String Desc = Type.getType(jField.getType()).getDescriptor();
			if(Modifier.isStatic(jField.getModifiers())) {
				this.AsmBuilder.visitFieldInsn(Opcodes.GETSTATIC, Owner, Node.GetName(), Desc);
			}
			else {
				this.AsmBuilder.PushNode(null, Node.RecvNode());
				this.AsmBuilder.visitFieldInsn(GETFIELD, Owner, Node.GetName(), Desc);
			}
			this.AsmBuilder.CheckReturnCast(Node, jField.getType());
		}
	}

	@Override public void VisitSetterNode(BSetterNode Node) {
		if(Node.IsUntyped()) {
			Method sMethod = JavaMethodTable.GetStaticMethod("SetField");
			BNode NameNode = new BStringNode(Node, null, Node.GetName());
			this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.RecvNode(), NameNode, Node.ExprNode()});
		}
		else {
			Class<?> RecvClass = this.GetJavaClass(Node.RecvNode().Type);
			Field jField = this.GetField(RecvClass, Node.GetName());
			String Owner = Type.getType(RecvClass).getInternalName();
			String Desc = Type.getType(jField.getType()).getDescriptor();
			if(Modifier.isStatic(jField.getModifiers())) {
				this.AsmBuilder.PushNode(jField.getType(), Node.ExprNode());
				this.AsmBuilder.visitFieldInsn(PUTSTATIC, Owner, Node.GetName(), Desc);
			}
			else {
				this.AsmBuilder.PushNode(null, Node.RecvNode());
				this.AsmBuilder.PushNode(jField.getType(), Node.ExprNode());
				this.AsmBuilder.visitFieldInsn(PUTFIELD, Owner, Node.GetName(), Desc);
			}
		}
	}

	@Override public void VisitGetIndexNode(BGetIndexNode Node) {
		Method sMethod = JavaMethodTable.GetBinaryStaticMethod(Node.RecvNode().Type, "[]", Node.IndexNode().Type);
		this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.RecvNode(), Node.IndexNode()});
	}

	@Override public void VisitSetIndexNode(BSetIndexNode Node) {
		Method sMethod = JavaMethodTable.GetBinaryStaticMethod(Node.RecvNode().Type, "[]=", Node.IndexNode().Type);
		this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.RecvNode(), Node.IndexNode(), Node.ExprNode()});
	}

	private int GetInvokeType(Method jMethod) {
		if(Modifier.isStatic(jMethod.getModifiers())) {
			return INVOKESTATIC;
		}
		if(Modifier.isInterface(jMethod.getModifiers())) {
			return INVOKEINTERFACE;
		}
		return INVOKEVIRTUAL;
	}

	@Override public void VisitMethodCallNode(BMethodCallNode Node) {
		this.AsmBuilder.SetLineNumber(Node);
		Method jMethod = this.GetMethod(Node.RecvNode().Type, Node.MethodName(), Node);
		if(jMethod != null) {
			if(!Modifier.isStatic(jMethod.getModifiers())) {
				this.AsmBuilder.PushNode(null, Node.RecvNode());
			}
			Class<?>[] P = jMethod.getParameterTypes();
			for(int i = 0; i < P.length; i++) {
				this.AsmBuilder.PushNode(P[i], Node.GetListAt(i));
			}
			int inst = this.GetInvokeType(jMethod);
			String owner = Type.getInternalName(jMethod.getDeclaringClass());
			this.AsmBuilder.visitMethodInsn(inst, owner, jMethod.getName(), Type.getMethodDescriptor(jMethod));
			this.AsmBuilder.CheckReturnCast(Node, jMethod.getReturnType());
		}
		else {
			jMethod = JavaMethodTable.GetStaticMethod("InvokeUnresolvedMethod");
			this.AsmBuilder.PushNode(Object.class, Node.RecvNode());
			this.AsmBuilder.PushConst(Node.MethodName());
			this.AsmBuilder.PushNodeListAsArray(Object.class, 0, Node);
			this.AsmBuilder.ApplyStaticMethod(Node, jMethod);
		}
	}

	@Override public void VisitMacroNode(BMacroNode Node) {
		for(int i = 0; i < Node.GetListSize(); i++) {
			this.AsmBuilder.PushNode(null, Node.GetListAt(i));
		}
		@Var String MacroText = Node.MacroFunc.MacroText;
		@Var int ClassEnd = MacroText.indexOf(".");
		@Var int MethodEnd = MacroText.indexOf("(");
		//System.out.println("MacroText: " + MacroText + " " + ClassEnd + ", " + MethodEnd);
		@Var String ClassName = MacroText.substring(0, ClassEnd);
		@Var String MethodName = MacroText.substring(ClassEnd+1, MethodEnd);
		this.AsmBuilder.SetLineNumber(Node);
		//System.out.println("debug: " + ClassName + ", " + MethodName);
		this.AsmBuilder.visitMethodInsn(INVOKESTATIC, ClassName, MethodName, Node.MacroFunc.FuncType);
	}

	@Override public void VisitFuncCallNode(BFuncCallNode Node) {
		BType FuncType = Node.FunctorNode().Type;
		if(FuncType instanceof BFuncType) {
			@Var BFuncNameNode FuncNameNode = Node.FuncNameNode();
			if(FuncNameNode != null) {
				this.AsmBuilder.ApplyFuncName(FuncNameNode, FuncNameNode.FuncName, (BFuncType)FuncType, Node);
			}
			else {
				Class<?> FuncClass = this.LoadFuncClass((BFuncType)FuncType);
				this.AsmBuilder.ApplyFuncObject(Node, FuncClass, Node.FunctorNode(), (BFuncType)FuncType, Node);
			}
		}
		else {
			this.VisitErrorNode(new BErrorNode(Node, "not function"));
		}
	}

	@Override public void VisitUnaryNode(BUnaryNode Node) {
		Method sMethod = JavaMethodTable.GetUnaryStaticMethod(Node.SourceToken.GetText(), Node.RecvNode().Type);
		this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.RecvNode()});
	}

	@Override public void VisitNotNode(BNotNode Node) {
		this.VisitUnaryNode(Node);
	}

	@Override public void VisitPlusNode(BunPlusNode Node) {
		this.VisitUnaryNode(Node);
	}

	@Override public void VisitMinusNode(BunMinusNode Node) {
		this.VisitUnaryNode(Node);
	}

	@Override public void VisitComplementNode(BunComplementNode Node) {
		this.VisitUnaryNode(Node);

	}


	@Override public void VisitCastNode(BCastNode Node) {
		if(Node.Type.IsVoidType()) {
			Node.ExprNode().Accept(this);
			this.AsmBuilder.Pop(Node.ExprNode().Type);
		}
		else {
			Class<?> TargetClass = this.GetJavaClass(Node.Type);
			Class<?> SourceClass = this.GetJavaClass(Node.ExprNode().Type);
			Method sMethod = JavaMethodTable.GetCastMethod(TargetClass, SourceClass);
			if(sMethod != null) {
				this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.ExprNode()});
			}
			else if(!TargetClass.isAssignableFrom(SourceClass)) {
				this.AsmBuilder.visitTypeInsn(CHECKCAST, TargetClass);
			}
		}
	}

	@Override public void VisitInstanceOfNode(BInstanceOfNode Node) {
		// TODO Auto-generated method stub

	}

	@Override public void VisitBinaryNode(BBinaryNode Node) {
		Method sMethod = JavaMethodTable.GetBinaryStaticMethod(Node.LeftNode().Type, Node.SourceToken.GetText(), Node.RightNode().Type);
		this.AsmBuilder.ApplyStaticMethod(Node, sMethod, new BNode[] {Node.LeftNode(), Node.RightNode()});
	}

	@Override public void VisitAddNode(BunAddNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitSubNode(BunSubNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitMulNode(BunMulNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitDivNode(BunDivNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitModNode(BunModNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitLeftShiftNode(BunLeftShiftNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitRightShiftNode(BunRightShiftNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitBitwiseAndNode(BunBitwiseAndNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitBitwiseOrNode(BunBitwiseOrNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitBitwiseXorNode(BunBitwiseXorNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitEqualsNode(BunEqualsNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitNotEqualsNode(BunNotEqualsNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitLessThanNode(BunLessThanNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitLessThanEqualsNode(BunLessThanEqualsNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitGreaterThanNode(BunGreaterThanNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode Node) {
		this.VisitBinaryNode(Node);

	}

	@Override public void VisitAndNode(BAndNode Node) {
		Label elseLabel = new Label();
		Label mergeLabel = new Label();
		this.AsmBuilder.PushNode(boolean.class, Node.LeftNode());
		this.AsmBuilder.visitJumpInsn(IFEQ, elseLabel);

		this.AsmBuilder.PushNode(boolean.class, Node.RightNode());
		this.AsmBuilder.visitJumpInsn(IFEQ, elseLabel);

		this.AsmBuilder.visitLdcInsn(true);
		this.AsmBuilder.visitJumpInsn(GOTO, mergeLabel);

		this.AsmBuilder.visitLabel(elseLabel);
		this.AsmBuilder.visitLdcInsn(false);
		this.AsmBuilder.visitJumpInsn(GOTO, mergeLabel);

		this.AsmBuilder.visitLabel(mergeLabel);
	}

	@Override public void VisitOrNode(BOrNode Node) {
		Label thenLabel = new Label();
		Label mergeLabel = new Label();
		this.AsmBuilder.PushNode(boolean.class, Node.LeftNode());
		this.AsmBuilder.visitJumpInsn(IFNE, thenLabel);

		this.AsmBuilder.PushNode(boolean.class, Node.RightNode());
		this.AsmBuilder.visitJumpInsn(IFNE, thenLabel);

		this.AsmBuilder.visitLdcInsn(false);
		this.AsmBuilder.visitJumpInsn(GOTO, mergeLabel);

		this.AsmBuilder.visitLabel(thenLabel);
		this.AsmBuilder.visitLdcInsn(true);
		this.AsmBuilder.visitJumpInsn(GOTO, mergeLabel);

		this.AsmBuilder.visitLabel(mergeLabel);
	}

	@Override public void VisitBlockNode(BBlockNode Node) {
		for (int i = 0; i < Node.GetListSize(); i++) {
			Node.GetListAt(i).Accept(this);
		}
	}

	@Override public void VisitIfNode(BIfNode Node) {
		Label ElseLabel = new Label();
		Label EndLabel = new Label();
		this.AsmBuilder.PushNode(boolean.class, Node.CondNode());
		this.AsmBuilder.visitJumpInsn(IFEQ, ElseLabel);
		// Then
		Node.ThenNode().Accept(this);
		this.AsmBuilder.visitJumpInsn(GOTO, EndLabel);
		// Else
		this.AsmBuilder.visitLabel(ElseLabel);
		if(Node.ElseNode() != null) {
			Node.ElseNode().Accept(this);
			this.AsmBuilder.visitJumpInsn(GOTO, EndLabel);
		}
		// End
		this.AsmBuilder.visitLabel(EndLabel);
	}

	@Override public void VisitReturnNode(BReturnNode Node) {
		if(Node.HasReturnExpr()) {
			Node.ExprNode().Accept(this);
			Type type = this.AsmType(Node.ExprNode().Type);
			this.AsmBuilder.visitInsn(type.getOpcode(IRETURN));
		}
		else {
			this.AsmBuilder.visitInsn(RETURN);
		}
	}

	@Override public void VisitWhileNode(BWhileNode Node) {
		Label continueLabel = new Label();
		Label breakLabel = new Label();
		this.AsmBuilder.BreakLabelStack.push(breakLabel);
		this.AsmBuilder.ContinueLabelStack.push(continueLabel);

		this.AsmBuilder.visitLabel(continueLabel);
		this.AsmBuilder.PushNode(boolean.class, Node.CondNode());
		this.AsmBuilder.visitJumpInsn(IFEQ, breakLabel); // condition
		Node.BlockNode().Accept(this);
		this.AsmBuilder.visitJumpInsn(GOTO, continueLabel);
		this.AsmBuilder.visitLabel(breakLabel);

		this.AsmBuilder.BreakLabelStack.pop();
		this.AsmBuilder.ContinueLabelStack.pop();
	}

	@Override public void VisitBreakNode(BBreakNode Node) {
		Label l = this.AsmBuilder.BreakLabelStack.peek();
		this.AsmBuilder.visitJumpInsn(GOTO, l);
	}

	@Override public void VisitThrowNode(BThrowNode Node) {
		// use wrapper
		//String name = Type.getInternalName(ZenThrowableWrapper.class);
		//this.CurrentVisitor.MethodVisitor.visitTypeInsn(NEW, name);
		//this.CurrentVisitor.MethodVisitor.visitInsn(DUP);
		//Node.Expr.Accept(this);
		//this.box();
		//this.CurrentVisitor.typeStack.pop();
		//this.CurrentVisitor.MethodVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
		//this.CurrentVisitor.MethodVisitor.visitInsn(ATHROW);
	}

	@Override public void VisitTryNode(BTryNode Node) {
		MethodVisitor mv = this.AsmBuilder;
		AsmTryCatchLabel TryCatchLabel = new AsmTryCatchLabel();
		this.TryCatchLabel.push(TryCatchLabel); // push

		// try block
		this.AsmBuilder.visitLabel(TryCatchLabel.BeginTryLabel);
		Node.TryBlockNode().Accept(this);
		this.AsmBuilder.visitLabel(TryCatchLabel.EndTryLabel);
		mv.visitJumpInsn(GOTO, TryCatchLabel.FinallyLabel);
		if(Node.HasCatchBlockNode()) {

		}

		// finally block
		this.AsmBuilder.visitLabel(TryCatchLabel.FinallyLabel);
		if(Node.HasFinallyBlockNode()) {
			Node.FinallyBlockNode().Accept(this);
		}
		this.TryCatchLabel.pop();
	}

	//	public void VisitCatchNode(ZCatchNode Node) {
	//		MethodVisitor mv = this.AsmBuilder;
	//		Label catchLabel = new Label();
	//		TryCatchLabel Label = this.TryCatchLabel.peek();
	//
	//		// prepare
	//		//TODO: add exception class name
	//		String throwType = this.AsmType(Node.GivenType).getInternalName();
	//		mv.visitTryCatchBlock(Label.beginTryLabel, Label.endTryLabel, catchLabel, throwType);
	//
	//		// catch block
	//		this.AsmBuilder.AddLocal(this.GetJavaClass(Node.GivenType), Node.GivenName);
	//		mv.visitLabel(catchLabel);
	//		this.AsmBuilder.StoreLocal(Node.GivenName);
	//		Node.AST[ZCatchNode._Block].Accept(this);
	//		mv.visitJumpInsn(GOTO, Label.finallyLabel);
	//
	//		this.AsmBuilder.RemoveLocal(this.GetJavaClass(Node.GivenType), Node.GivenName);
	//	}

	@Override public void VisitLetNode(BLetVarNode Node) {
		//		if(Node.InitValueNode().HasUntypedNode()) {
		//			ZLogger._LogWarning(Node.InitValueNode().SourceToken, "type is ambigiou: " + Node.InitValueNode());
		//			return;
		//		}
		String ClassName = this.NameGlobalNameClass(Node.GetUniqueName(this));
		@Var AsmClassBuilder ClassBuilder = this.AsmLoader.NewClass(ACC_PUBLIC|ACC_FINAL, Node, ClassName, "java/lang/Object");
		Class<?> ValueClass = this.GetJavaClass(Node.GetAstType(BLetVarNode._InitValue));
		ClassBuilder.AddField(ACC_PUBLIC|ACC_STATIC, "_", ValueClass, null);

		AsmMethodBuilder StaticInitMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V");
		StaticInitMethod.PushNode(ValueClass, Node.InitValueNode());
		StaticInitMethod.visitFieldInsn(Opcodes.PUTSTATIC, ClassName, "_",  Type.getDescriptor(ValueClass));
		StaticInitMethod.visitInsn(RETURN);
		StaticInitMethod.Finish();
		this.AsmLoader.LoadGeneratedClass(ClassName);
		//Class<?> StaticClass = this.AsmLoader.LoadGeneratedClass(ClassName);
		//		Node.GetNameSpace().SetLocalSymbol(Node.GetName(), new JavaStaticFieldNode(Node, StaticClass, Node.InitValueNode().Type, "_"));
	}

	Class<?> LoadFuncClass(BFuncType FuncType) {
		String ClassName = this.NameType(FuncType);
		Class<?> FuncClass = this.GetGeneratedClass(ClassName, null);
		if(FuncClass == null) {
			@Var String SuperClassName = Type.getInternalName(BFunction.class);
			@Var AsmClassBuilder ClassBuilder = this.AsmLoader.NewClass(ACC_PUBLIC| ACC_ABSTRACT, null, ClassName, BFunction.class);
			AsmMethodBuilder InvokeMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_ABSTRACT, "Invoke", FuncType);
			InvokeMethod.Finish();

			AsmMethodBuilder InitMethod = ClassBuilder.NewMethod(ACC_PUBLIC, "<init>", "(ILjava/lang/String;)V");
			InitMethod.visitVarInsn(ALOAD, 0);
			InitMethod.visitVarInsn(ILOAD, 1);
			InitMethod.visitVarInsn(ALOAD, 2);
			InitMethod.visitMethodInsn(INVOKESPECIAL, SuperClassName, "<init>", "(ILjava/lang/String;)V");
			InitMethod.visitInsn(RETURN);
			InitMethod.Finish();

			FuncClass = this.AsmLoader.LoadGeneratedClass(ClassName);
			this.SetGeneratedClass(ClassName, FuncClass);
		}
		return FuncClass;
	}

	@Override public void VisitFunctionNode(BFunctionNode Node) {
		if(Node.IsTopLevelDefineFunction()) {
			assert(Node.FuncName() != null);
			assert(Node.IsTopLevel());  // otherwise, transformed to var f = function ()..
			JavaStaticFieldNode FuncNode = this.GenerateFunctionAsSymbolField(Node.FuncName(), Node);
			if(Node.IsExport) {
				if(Node.FuncName().equals("main")) {
					this.MainFuncNode = FuncNode;
				}
			}
			this.SetMethod(Node.FuncName(), (BFuncType)FuncNode.Type, FuncNode.StaticClass);
		}
		else {
			JavaStaticFieldNode FuncNode = this.GenerateFunctionAsSymbolField(Node.GetUniqueName(this), Node);
			if(this.AsmBuilder != null) {
				this.VisitStaticFieldNode(FuncNode);
			}
		}
	}

	private JavaStaticFieldNode GenerateFunctionAsSymbolField(String FuncName, BFunctionNode Node) {
		@Var BFuncType FuncType = Node.GetFuncType();
		String ClassName = this.NameFunctionClass(FuncName, FuncType);
		Class<?> FuncClass = this.LoadFuncClass(FuncType);
		@Var AsmClassBuilder ClassBuilder = this.AsmLoader.NewClass(ACC_PUBLIC|ACC_FINAL, Node, ClassName, FuncClass);

		AsmMethodBuilder InvokeMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_FINAL, "Invoke", FuncType);
		int index = 1;
		for(int i = 0; i < FuncType.GetFuncParamSize(); i++) {
			Type AsmType = this.AsmType(FuncType.GetFuncParamType(i));
			InvokeMethod.visitVarInsn(AsmType.getOpcode(ILOAD), index);
			index += AsmType.getSize();
		}
		InvokeMethod.visitMethodInsn(INVOKESTATIC, ClassName, "f", FuncType);
		InvokeMethod.visitReturn(FuncType.GetReturnType());
		InvokeMethod.Finish();

		ClassBuilder.AddField(ACC_PUBLIC | ACC_STATIC | ACC_FINAL, "function", FuncClass, null);

		// static init
		AsmMethodBuilder StaticInitMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_STATIC , "<clinit>", "()V");
		StaticInitMethod.visitTypeInsn(NEW, ClassName);
		StaticInitMethod.visitInsn(DUP);
		StaticInitMethod.visitMethodInsn(INVOKESPECIAL, ClassName, "<init>", "()V");
		StaticInitMethod.visitFieldInsn(PUTSTATIC, ClassName, "function",  FuncClass);
		StaticInitMethod.visitInsn(RETURN);
		StaticInitMethod.Finish();

		AsmMethodBuilder InitMethod = ClassBuilder.NewMethod(ACC_PRIVATE, "<init>", "()V");
		InitMethod.visitVarInsn(ALOAD, 0);
		InitMethod.visitLdcInsn(FuncType.TypeId);
		InitMethod.visitLdcInsn(FuncName);
		InitMethod.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(FuncClass), "<init>", "(ILjava/lang/String;)V");
		InitMethod.visitInsn(RETURN);
		InitMethod.Finish();

		AsmMethodBuilder StaticFuncMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_STATIC, "f", FuncType);
		for(int i = 0; i < Node.GetListSize(); i++) {
			BLetVarNode ParamNode = Node.GetParamNode(i);
			Class<?> DeclClass = this.GetJavaClass(ParamNode.DeclType());
			StaticFuncMethod.AddLocal(DeclClass, ParamNode.GetGivenName());
		}
		Node.BlockNode().Accept(this);
		StaticFuncMethod.Finish();

		FuncClass = this.AsmLoader.LoadGeneratedClass(ClassName);
		this.SetGeneratedClass(ClassName, FuncClass);
		return new JavaStaticFieldNode(null, FuncClass, FuncType, "function");
	}

	private BFunction LoadFunction(Class<?> WrapperClass, Class<?> StaticMethodClass) {
		try {
			Field f = StaticMethodClass.getField("function");
			Object func = f.get(null);
			if(WrapperClass != null) {
				Constructor<?> c = WrapperClass.getConstructor(func.getClass().getSuperclass());
				func = c.newInstance(func);
			}
			return (BFunction)func;
		}
		catch(Exception e) {
			e.printStackTrace();
			BLib._Exit(1, "failed: " + e);
		}
		return null;
	}

	private void SetMethod(String FuncName, BFuncType FuncType, Class<?> FuncClass) {
		BType RecvType = FuncType.GetRecvType();
		if(RecvType instanceof BClassType && FuncName != null) {
			BClassType ClassType = (BClassType)RecvType;
			BType FieldType = ClassType.GetFieldType(FuncName, null);
			if(FieldType == null || !FieldType.IsFuncType()) {
				FuncName = BLib._AnotherName(FuncName);
				FieldType = ClassType.GetFieldType(FuncName, null);
				if(FieldType == null || !FieldType.IsFuncType()) {
					return;
				}
			}
			if(FieldType.Equals(FuncType)) {
				this.SetMethod(ClassType, FuncName, this.LoadFunction(null, FuncClass));
			}
			else if(this.IsMethodFuncType((BFuncType)FieldType, FuncType)) {
				Class<?> WrapperClass = this.MethodWrapperClass((BFuncType)FieldType, FuncType);
				this.SetMethod(ClassType, FuncName, this.LoadFunction(WrapperClass, FuncClass));
			}
		}
	}

	private boolean IsMethodFuncType(BFuncType FieldType, BFuncType FuncType) {
		if(FuncType.GetFuncParamSize() == FieldType.GetFuncParamSize() && FuncType.GetReturnType().Equals(FieldType.GetReturnType())) {
			for(int i = 1; i < FuncType.GetFuncParamSize(); i++) {
				if(!FuncType.GetFuncParamType(i).Equals(FieldType.GetFuncParamType(i))) {
					return false;
				}
			}
		}
		return true;
	}

	private Class<?> MethodWrapperClass(BFuncType FuncType, BFuncType SourceFuncType) {
		String ClassName = "W" + this.NameType(FuncType) + "W" + this.NameType(SourceFuncType);
		Class<?> WrapperClass = this.GetGeneratedClass(ClassName, null);
		if(WrapperClass == null) {
			Class<?> FuncClass = this.LoadFuncClass(FuncType);
			Class<?> SourceClass = this.LoadFuncClass(SourceFuncType);
			@Var AsmClassBuilder ClassBuilder = this.AsmLoader.NewClass(ACC_PUBLIC|ACC_FINAL, null, ClassName, FuncClass);

			ClassBuilder.AddField(ACC_PUBLIC, "f", SourceClass, null);

			AsmMethodBuilder InitMethod = ClassBuilder.NewMethod(ACC_PUBLIC, "<init>", "(L"+Type.getInternalName(SourceClass)+";)V");
			InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
			InitMethod.PushInt(FuncType.TypeId);
			InitMethod.visitLdcInsn(SourceFuncType.ShortName);
			InitMethod.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(FuncClass), "<init>", "(ILjava/lang/String;)V");
			InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
			InitMethod.visitVarInsn(Opcodes.ALOAD, 1);
			InitMethod.visitFieldInsn(PUTFIELD, ClassName, "f", Type.getDescriptor(SourceClass));
			InitMethod.visitInsn(RETURN);
			InitMethod.Finish();

			AsmMethodBuilder InvokeMethod = ClassBuilder.NewMethod(ACC_PUBLIC | ACC_FINAL, "Invoke", FuncType);
			InvokeMethod.visitVarInsn(ALOAD, 0);
			InvokeMethod.visitFieldInsn(GETFIELD, ClassName, "f", Type.getDescriptor(SourceClass));
			InvokeMethod.visitVarInsn(ALOAD, 1);
			//			System.out.println("CAST: " + Type.getInternalName(this.GetJavaClass(SourceFuncType.GetFuncParamType(0))));
			InvokeMethod.visitTypeInsn(CHECKCAST, this.GetJavaClass(SourceFuncType.GetFuncParamType(0)));
			int index = 2;
			for(int i = 1; i < FuncType.GetFuncParamSize(); i++) {
				Type AsmType = this.AsmType(FuncType.GetFuncParamType(i));
				InvokeMethod.visitVarInsn(AsmType.getOpcode(ILOAD), index);
				index += AsmType.getSize();
			}
			//String owner = "C" + FuncType.StringfySignature(FuncName);
			InvokeMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(SourceClass), "Invoke", this.GetMethodDescriptor(SourceFuncType));
			InvokeMethod.visitReturn(FuncType.GetReturnType());
			InvokeMethod.Finish();

			WrapperClass = this.AsmLoader.LoadGeneratedClass(ClassName);
			this.SetGeneratedClass(ClassName, WrapperClass);
		}
		return WrapperClass;
	}


	// -----------------------------------------------------------------------

	private Class<?> GetSuperClass(BType SuperType) {
		@Var Class<?> SuperClass = null;
		if(SuperType != null) {
			SuperClass = this.GetJavaClass(SuperType);
		}
		else {
			SuperClass = BObject.class;
		}
		return SuperClass;
	}

	private final static String NameClassMethod(BType ClassType, String FieldName) {
		return FieldName + ClassType.TypeId;
	}

	private void SetMethod(BClassType ClassType, String FuncName, BFunction FuncObject) {
		try {
			Class<?> StaticClass = this.GetJavaClass(ClassType);
			Field f = StaticClass.getField(NameClassMethod(ClassType, FuncName));
			f.set(null, FuncObject);
		}
		catch (Exception e) {
			e.printStackTrace();
			BLib._Exit(1, "failed " + e);
		}
	}

	private Object GetConstValue(BNode Node) {
		if(Node instanceof BNullNode) {
			return null;
		}
		if(Node instanceof BBooleanNode) {
			return ((BBooleanNode)Node).BooleanValue;
		}
		if(Node instanceof BIntNode) {
			return ((BIntNode)Node).IntValue;
		}
		if(Node instanceof BFloatNode) {
			return ((BFloatNode)Node).FloatValue;
		}
		if(Node instanceof BStringNode) {
			return ((BStringNode)Node).StringValue;
		}
		if(Node instanceof BTypeNode) {
			return Node.Type;
		}
		return null;
	}

	@Override public void VisitClassNode(BClassNode Node) {
		@Var Class<?> SuperClass = this.GetSuperClass(Node.SuperType());
		@Var AsmClassBuilder ClassBuilder = this.AsmLoader.NewClass(ACC_PUBLIC, Node, Node.ClassName(), SuperClass);
		// add class field (not function)
		for(int i = 0; i < Node.GetListSize(); i++) {
			@Var BLetVarNode FieldNode = Node.GetFieldNode(i);
			ClassBuilder.AddField(ACC_PUBLIC, FieldNode.GetGivenName(), FieldNode.DeclType(), this.GetConstValue(FieldNode.InitValueNode()));
		}
		// add class field (function)
		for(int i = 0; i < Node.ClassType.GetFieldSize(); i++) {
			@Var BClassField Field = Node.ClassType.GetFieldAt(i);
			if(Field.FieldType.IsFuncType()) {
				ClassBuilder.AddField(ACC_PUBLIC|ACC_STATIC, NameClassMethod(Node.ClassType, Field.FieldName), Field.FieldType, null);
			}
		}
		// public <init>()
		AsmMethodBuilder InitMethod = ClassBuilder.NewMethod(ACC_PUBLIC, "<init>", "()V");
		InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
		InitMethod.PushInt(Node.ClassType.TypeId);
		InitMethod.visitMethodInsn(INVOKESPECIAL, Node.ClassName(), "<init>", "(I)V");
		InitMethod.visitInsn(RETURN);
		InitMethod.Finish();
		// protected <init>(int typeid)
		InitMethod = ClassBuilder.NewMethod(ACC_PROTECTED, "<init>", "(I)V");
		InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
		//		InitMethod.visitVarInsn(Opcodes.ILOAD, 1);
		//		InitMethod.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(SuperClass), "<init>", "(I)V");
		InitMethod.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(SuperClass), "<init>", "()V");	// FIXME: ZObject?
		for(int i = 0; i < Node.GetListSize(); i++) {
			@Var BLetVarNode FieldNode = Node.GetFieldNode(i);
			if(!FieldNode.DeclType().IsFuncType()) {
				InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
				InitMethod.PushNode(this.GetJavaClass(FieldNode.DeclType()), FieldNode.InitValueNode());
				InitMethod.visitFieldInsn(PUTFIELD, Node.ClassName(), FieldNode.GetGivenName(), Type.getDescriptor(this.GetJavaClass(FieldNode.DeclType())));
			}
		}
		// set function
		for(int i = 0; i < Node.ClassType.GetFieldSize(); i++) {
			@Var BClassField Field = Node.ClassType.GetFieldAt(i);
			if(Field.FieldType.IsFuncType()) {
				String FieldDesc = Type.getDescriptor(this.GetJavaClass(Field.FieldType));
				Label JumpLabel = new Label();
				InitMethod.visitFieldInsn(Opcodes.GETSTATIC, Node.ClassName(), NameClassMethod(Node.ClassType, Field.FieldName), FieldDesc);
				InitMethod.visitJumpInsn(Opcodes.IFNULL, JumpLabel);
				InitMethod.visitVarInsn(Opcodes.ALOAD, 0);
				InitMethod.visitFieldInsn(Opcodes.GETSTATIC, Node.ClassName(), NameClassMethod(Node.ClassType, Field.FieldName), FieldDesc);
				InitMethod.visitFieldInsn(Opcodes.PUTFIELD, Node.ClassName(), Field.FieldName, FieldDesc);
				InitMethod.visitLabel(JumpLabel);
			}
		}

		InitMethod.visitInsn(RETURN);
		InitMethod.Finish();

		JavaTypeTable.SetTypeTable(Node.ClassType, this.AsmLoader.LoadGeneratedClass(Node.ClassName()));
	}

	@Override public void VisitErrorNode(BErrorNode Node) {
		@Var String Message = BLogger._LogError(Node.SourceToken, Node.ErrorMessage);
		this.AsmBuilder.PushConst(Message);
		@Var Method sMethod = JavaMethodTable.GetStaticMethod("ThrowError");
		this.AsmBuilder.ApplyStaticMethod(Node, sMethod);
	}

	@Override public void VisitAsmNode(BAsmNode Node) {
		// TODO Auto-generated method stub
	}

	@Override public void VisitTopLevelNode(ZTopLevelNode Node) {
		this.VisitUndefinedNode(Node);
	}

	@Override public void VisitLocalDefinedNode(ZLocalDefinedNode Node) {
		if(Node instanceof JavaStaticFieldNode) {
			this.VisitStaticFieldNode(((JavaStaticFieldNode)Node));
		}
		else {
			this.VisitUndefinedNode(Node);
		}
	}

	@Override public void Perform() {
		if(this.TopLevelSymbol != null) {
			Class<?> FuncClass = this.GetDefinedFunctionClass(this.TopLevelSymbol, BType.VoidType, 0);
			try {
				Method Method = FuncClass.getMethod("f");
				Object Value = Method.invoke(null);
				if(Method.getReturnType() != void.class) {
					System.out.println(" ("+Method.getReturnType().getSimpleName()+") " + Value);
				}
			}
			catch(NoSuchMethodException e) {
				System.out.println(e);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override public final void ExecMain() {
		this.Logger.OutputErrorsToStdErr();
		if(this.MainFuncNode != null) {
			@Var JavaStaticFieldNode MainFunc = this.MainFuncNode;
			try {
				Method Method = MainFunc.StaticClass.getMethod("f");
				Method.invoke(null);
			}
			catch(NoSuchMethodException e) {
				System.out.println(e);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final void Debug(String Message) {
		BLib._PrintDebug(Message);
	}

	@Override public void VisitLiteralNode(LiteralNode Node) {

	}

}


