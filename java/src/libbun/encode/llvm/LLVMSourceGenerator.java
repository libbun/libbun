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


package libbun.encode.llvm;

import java.util.ArrayList;
import java.util.HashMap;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.GroupNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.binary.BunOrNode;
import libbun.ast.binary.ComparatorNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.expression.BunFormNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayLiteralNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapLiteralNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.literal.ConstNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunNotNode;
import libbun.ast.unary.UnaryOperatorNode;
import libbun.encode.LibBunSourceBuilder;
import libbun.encode.obsolete.OldSourceGenerator;
import libbun.parser.common.BunLogger;
import libbun.type.BClassType;
import libbun.type.BFuncType;
import libbun.type.BGenericType;
import libbun.type.BType;
import libbun.type.BVarType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;


class LLVMScope {
	@BField private int TempLocalSymbolNumber;
	@BField private boolean IsBlockTerminated;
	@BField private String CurrentLabel;
	@BField private final ArrayList<String> ValueStack = new ArrayList<String>();
	@BField private final ArrayList<String> BreakLabelStack = new ArrayList<String>();
	@BField private final ArrayList<String> LocalSymbolList = new ArrayList<String>();
	@BField private final ArrayList<String> LocalVarList = new ArrayList<String>();


	public LLVMScope() {
		this.TempLocalSymbolNumber = 0;
		this.IsBlockTerminated = false;
		this.CurrentLabel = null;
		this.ValueStack.clear();
		this.BreakLabelStack.clear();
		this.LocalSymbolList.clear();
		this.LocalVarList.clear();
	}

	public String CreateTempLocalSymbol() {
		@Var int ReturnNumber = this.TempLocalSymbolNumber;
		this.TempLocalSymbolNumber = this.TempLocalSymbolNumber + 1;
		return "%Temp__" + ReturnNumber;
	}
	public int GetTempLabelNumber() {
		@Var int ReturnNumber = this.TempLocalSymbolNumber;
		this.TempLocalSymbolNumber = this.TempLocalSymbolNumber + 1;
		return ReturnNumber;
	}

	public void SetLabel(String Label) {
		this.IsBlockTerminated = false;
		this.CurrentLabel = Label;
	}
	public void TerminateBlock() {
		this.IsBlockTerminated = true;
	}
	public boolean IsBlockTerminated() {
		return this.IsBlockTerminated;
	}
	public String GetCurrentLabel() {
		return this.CurrentLabel;
	}

	private void PushStrStack(ArrayList<String> Stack, String Label) {
		Stack.add(Label);
	}
	private String PopStrStack(ArrayList<String> Stack) {
		@Var int Size = Stack.size();
		return Stack.remove(Size-1);
	}
	private String PeekStrStack(ArrayList<String> Stack) {
		@Var int Size = Stack.size();
		return Stack.get(Size-1);
	}

	public void PushBreakLabel(String Label) {
		this.PushStrStack(this.BreakLabelStack, Label);
	}
	public String PopBreakLabel() {
		return this.PopStrStack(this.BreakLabelStack);
	}
	public String PeekBreakLabel() {
		return this.PeekStrStack(this.BreakLabelStack);
	}

	public void PushValue(String Value) {
		this.PushStrStack(this.ValueStack, Value);
	}
	public String PopValue() {
		return this.PopStrStack(this.ValueStack);
	}

	public void DefineLocalSymbol(String Symbol) {
		this.LocalSymbolList.add(Symbol);
	}
	public void DefineLocalVar(String VarName) {
		this.LocalVarList.add(VarName);
		this.DefineLocalSymbol(VarName);
	}
	public boolean IsUserDefinedSymbol(String Symbol) {
		return this.LocalSymbolList.contains(Symbol);
	}
	public boolean IsUserDefinedVar(String Symbol) {
		return this.LocalVarList.contains(Symbol);
	}
}

public class LLVMSourceGenerator extends OldSourceGenerator {
	@BField private int TempGlobalSymbolNumber;
	@BField private final ArrayList<String> GlobalSymbolList;
	@BField private final ArrayList<String> ExternalStructList;
	@BField private final HashMap<String, String> ExternalFunctionMap;
	@BField private final HashMap<String, BunClassNode> ClassFieldMap;
	@BField private LLVMScope CurrentScope;

	private static final boolean WithInitValue = true;

	public LLVMSourceGenerator() {
		super("ll", "LLVM3.1");
		this.LineFeed = "\n";
		this.Tab = "\t";
		this.LineComment = ";"; // if not, set null
		this.BeginComment = null; //"'''";
		this.EndComment = null; //"'''";
		this.Camma = ", ";
		this.SemiColon = "";

		this.TrueLiteral = "true";
		this.FalseLiteral = "false";
		this.NullLiteral = "null";
		this.TopType = "opaque";

		this.SetNativeType(BType.VoidType, "void");
		this.SetNativeType(BType.BooleanType, "i1");
		this.SetNativeType(BType.IntType, "i64");
		this.SetNativeType(BType.FloatType, "double");

		this.TempGlobalSymbolNumber = 0;
		this.GlobalSymbolList = new ArrayList<String>();
		this.ExternalStructList = new ArrayList<String>();
		this.ExternalFunctionMap = new HashMap<String, String>();
		this.ClassFieldMap = new HashMap<String, BunClassNode>();
		this.CurrentScope = null;
	}

	private String CreateTempFuncName(BFuncType FuncType) {
		@Var int ReturnNumber = this.TempGlobalSymbolNumber;
		this.TempGlobalSymbolNumber = this.TempGlobalSymbolNumber + 1;
		return "@" + FuncType.StringfySignature("f" + ReturnNumber);
	}
	private String CreateTempGlobalSymbol() {
		@Var int ReturnNumber = this.TempGlobalSymbolNumber;
		this.TempGlobalSymbolNumber = this.TempGlobalSymbolNumber + 1;
		return "@Temp__" + ReturnNumber;
	}

	private void DefineGlobalSymbol(String Symbol) {
		if(!this.IsUserDefinedGlobalSymbol(Symbol)) {
			this.GlobalSymbolList.add(Symbol);
		}
	}
	private void DefineClass(String ClassName, BunClassNode Node) {
		this.ClassFieldMap.put(ClassName, Node);
	}
	private String ToLocalSymbol(String Symbol) {
		if(this.CurrentScope.IsUserDefinedSymbol(Symbol)) {
			return "%" + Symbol;
		}
		else {
			return null;
		}
	}
	private String ToGlobalSymbol(String Symbol) {
		if(this.IsUserDefinedGlobalSymbol(Symbol)) {
			return "@" + Symbol;
		}
		else {
			return null;
		}
	}
	private String ToClassSymbol(String Symbol) {
		if(this.IsUserDefinedClass(Symbol)) {
			return "%Class." + Symbol;
		}
		else {
			return null;
		}
	}
	public boolean IsUserDefinedGlobalSymbol(String Symbol) {
		return this.GlobalSymbolList.contains(Symbol);
	}
	private boolean IsUserDefinedClass(String ClassName) {
		return this.ClassFieldMap.containsKey(ClassName);
	}

	private boolean IsPrimitiveType(BType Type) {
		if(Type.IsBooleanType()) {
			return true;
		}
		else if(Type.IsFloatType()) {
			return true;
		}
		else if(Type.IsIntType()) {
			return true;
		}
		return false;
	}
	private String GetTypeExpr(BType Type) {
		if(Type instanceof BVarType) {
			return this.GetTypeExpr(Type.RefType);
		}
		else if(Type.IsVarType()) {
			return "opaque";
		}
		else if(Type instanceof BFuncType) {
			@Var BFuncType FuncType = (BFuncType)Type;
			return this.GetTypeExpr(FuncType.GetReturnType())+ " " + this.GetFuncParamTypeExpr(FuncType) + "*";
		}
		else if(Type.IsArrayType()) {
			//return this.GetNativeTypeName(((BGenericType)Type).ParamType.GetRealType()) + "*";
			this.DefineExternalStruct("BArray");
			return "%BArray*";
		}
		else if(Type.IsStringType()) {
			this.DefineExternalStruct("BString");
			return "%BString*";
		}
		else if(Type instanceof BClassType) {
			return this.ToClassSymbol(Type.ShortName) + "*";
		}
		else {
			return this.GetNativeTypeName(Type.GetRealType());
		}
	}
	private String GetFuncParamTypeExpr(BFuncType FuncType) {
		@Var int Size = FuncType.GetFuncParamSize();
		LibBunSystem._Assert(Size >= 0);

		@Var StringBuilder sb = new StringBuilder();
		sb.append("(");
		@Var int i = 0;
		while(i < Size) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(this.GetTypeExpr(FuncType.GetFuncParamType(i)));
			i = i + 1;
		}
		sb.append(")");
		return sb.toString();
	}

	private String GetBinaryOpcode(BinaryOperatorNode Node) {
		if(Node.IsUntyped()) {
			BunLogger._LogError(Node.SourceToken, "Binary is untyped");
			return null;
		}
		@Var String Binary = Node.SourceToken.GetText();
		if(Binary.equals("+")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "add";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fadd";
			}
		}
		else if(Binary.equals("-")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "sub";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fsub";
			}
		}
		else if(Binary.equals("*")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "mul";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fmul";
			}
		}
		else if(Binary.equals("/")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "sdiv";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fdiv";
			}
		}
		else if(Binary.equals("%")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "srem";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "frem";
			}
		}
		else if(Binary.equals("|")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "or";
			}
		}
		else if(Binary.equals("&")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "and";
			}
		}
		else if(Binary.equals("<<")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "shl";
			}
		}
		else if(Binary.equals(">>")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "ashr"; //arithmetic
				//return "lshr"; //logical
			}
		}
		else if(Binary.equals("^")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "xor";
			}
		}
		BunLogger._LogError(Node.SourceToken, "Unknown binary \"" + Binary + "\" for this type");
		return null;
	}
	private String GetCompareOpCodeAndCondition(ComparatorNode Node) {
		if(Node.IsUntyped()) {
			BunLogger._LogError(Node.SourceToken, "Comparator is untyped");
			return null;
		}
		@Var String Comparator = Node.SourceToken.GetText();
		if(Comparator.equals("==")) {
			if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp oeq";
			}
			else {
				return "icmp eq";
			}
		}
		else if(Comparator.equals("!=")) {
			if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp one";
			}
			else {
				return "icmp ne";
			}
		}
		else if(Comparator.equals("<")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "icmp slt";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp olt";
			}
		}
		else if(Comparator.equals("<=")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "icmp sle";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp ole";
			}
		}
		else if(Comparator.equals(">")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "icmp sgt";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp ogt";
			}
		}
		else if(Comparator.equals(">=")) {
			if(Node.LeftNode().Type.IsIntType() && Node.RightNode().Type.IsIntType()) {
				return "icmp sge";
			}
			else if(Node.LeftNode().Type.IsFloatType() && Node.RightNode().Type.IsFloatType()) {
				return "fcmp oge";
			}
		}
		BunLogger._LogError(Node.SourceToken, "Unknown comparator \"" + Comparator + "\" for this type");
		return null;
	}
	private String GetCastOpCode(BType BeforeType, BType AfterType) {
		if(BeforeType.IsIntType()) {
			if(AfterType.IsFloatType()) {
				return "sitofp";
			}
		}
		else if(BeforeType.IsFloatType()) {
			if(AfterType.IsIntType()) {
				return "fptosi";
			}
		}
		throw new RuntimeException("Can't use this cast " + BeforeType.ShortName + " to " + AfterType.ShortName);
	}
	private String GetSizeOfType(BType Type) {
		if(this.IsPrimitiveType(Type)) {
			return "ptrtoint (" + this.GetTypeExpr(Type) + "* getelementptr (" + this.GetTypeExpr(Type) + "* null, i64 1) to i64)";
		}
		else if(Type instanceof BClassType) {
			return "ptrtoint (" + this.GetTypeExpr(Type) + " getelementptr (" + this.GetTypeExpr(Type) + " null, i64 1) to i64)";
		}
		else {
			return null;
		}
	}

	private String ConvertLLVMString(String StringValue) {
		@Var char[] CharArray = StringValue.toCharArray();
		@Var StringBuilder sb = new StringBuilder();
		@Var int i = 0;
		while(i < CharArray.length) {
			char ch = CharArray[i];
			if(ch == '\n') {
				sb.append("\\0A");
			}
			else if(ch == '\t') {
				sb.append("\\09");
			}
			else if(ch == '"') {
				sb.append("\\22");
			}
			else if(ch == '\\') {
				sb.append("\\5C");
			}
			else {
				sb.append(ch);
			}
			i = i + 1;
		}
		sb.append("\\00");
		return sb.toString();
	}

	private int GetLLVMStringLen(String FormedString) {
		@Var char[] CharArray = FormedString.toCharArray();
		@Var int i = 0;
		@Var int Len = 0;
		while(i < CharArray.length) {
			char ch = CharArray[i];
			if(ch == '\\') {
				i = i + 3;
			}
			else {
				i = i + 1;
			}
			Len = Len + 1;
		}
		return Len;
	}

	@Override public void VisitAndNode(BunAndNode Node) {
		@Var int LabelNum = this.CurrentScope.GetTempLabelNumber();
		@Var String RightLabel = "And__" + LabelNum + ".Right";
		@Var String EndLabel = "And__" + LabelNum + ".End";

		this.GenerateExpression(Node.LeftNode());
		@Var String LeftResult = this.CurrentScope.PopValue();

		this.Source.AppendNewLine("br i1 ");
		this.Source.Append(LeftResult);
		this.Source.Append(", ");
		this.Source.Append("label %" + RightLabel + ", ");
		this.Source.Append("label %" + EndLabel);
		@Var String LeftLabel = this.CurrentScope.GetCurrentLabel();

		this.Source.AppendLineFeed();
		this.Source.Append(RightLabel + ":");
		this.CurrentScope.SetLabel(RightLabel);
		this.GenerateExpression(Node.RightNode());
		@Var String RightResult = this.CurrentScope.PopValue();
		this.Source.AppendNewLine("br label %" + EndLabel);
		RightLabel = this.CurrentScope.GetCurrentLabel();

		this.Source.AppendLineFeed();
		this.Source.Append(EndLabel + ":");
		this.CurrentScope.SetLabel(EndLabel);
		@Var String AllResult = this.CurrentScope.CreateTempLocalSymbol();
		this.Source.AppendNewLine(AllResult);
		this.Source.Append(" = phi i1 ");
		this.Source.Append("[ false, %" + LeftLabel + " ], ");
		this.Source.Append("[ " + RightResult + ", %" + RightLabel + " ]");

		this.CurrentScope.PushValue(AllResult);
	}

	@Override public void VisitArrayLiteralNode(BunArrayLiteralNode Node) {
		@Var int ArraySize = Node.GetListSize();
		@Var BType ElementType = ((BGenericType)Node.Type).ParamType;
		@Var String ExtFuncName;
		if(ElementType.IsIntType()) {
			ExtFuncName = "BIntArray_Construct";
		}
		else if(ElementType.IsFloatType()) {
			ExtFuncName = "BFloatArray_Construct";
		}
		else if(ElementType.IsBooleanType()) {
			ExtFuncName = "BBooleanArray_Construct";
		}
		else {
			ExtFuncName = "BObjArray_Construct";
		}
		this.DeclareExtrnalFunction(ExtFuncName, "%BArray*", "(i8*, i64)");

		if(ArraySize > 0) {
			@Var StringBuilder sb = new StringBuilder();
			@Var String GlobalConst = this.CreateTempGlobalSymbol();
			sb.append(GlobalConst);
			sb.append(" = private constant ");
			@Var String ArrayType = "[" + ArraySize + " x " + this.GetTypeExpr(ElementType) + "]";
			sb.append(ArrayType);

			sb.append(" [");
			@Var int i = 0;
			while(i < ArraySize) {
				if (i > 0) {
					sb.append(", ");
				}
				@Var BNode SubNode = Node.GetListAt(i);
				sb.append(this.GetTypeExpr(SubNode.Type));
				sb.append(" ");
				this.GenerateExpression(SubNode);
				sb.append(this.CurrentScope.PopValue());
				i = i + 1;
			}
			sb.append("]");
			this.Header.AppendNewLine(sb.toString());

			this.CallExternalFunction(ExtFuncName, "(i8* bitcast (" + ArrayType + "* " + GlobalConst + " to i8*), i64 " + ArraySize + ")");
		}
		else {
			this.CallExternalFunction(ExtFuncName, "(i8* null, i64 " + ArraySize + ")");
		}
	}

	@Override public void VisitBinaryNode(BinaryOperatorNode Node) {
		this.GenerateExpression(Node.LeftNode());
		@Var String Left = this.CurrentScope.PopValue();
		this.GenerateExpression(Node.RightNode());
		@Var String Right = this.CurrentScope.PopValue();

		if(this.IsPrimitiveType(Node.LeftNode().Type)) {
			@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(TempVar);
			this.Source.Append(" = ");
			this.Source.Append(this.GetBinaryOpcode(Node));
			this.Source.Append(" ");
			this.Source.Append(this.GetTypeExpr(Node.LeftNode().Type));
			this.Source.Append(" ");
			this.Source.Append(Left);
			this.Source.Append(", ");
			this.Source.Append(Right);

			this.CurrentScope.PushValue(TempVar);
		}
		else if(Node.LeftNode().Type.IsStringType()) {
			if(Node.SourceToken.EqualsText('+') && Node.RightNode().Type.IsStringType()) {
				this.DeclareExtrnalFunction("BString_StrCat", "%BString*", "(%BString*, %BString*)");
				this.CallExternalFunction("BString_StrCat", "(" + this.GetTypeExpr(Node.LeftNode().Type) + " " + Left + ", " + this.GetTypeExpr(Node.RightNode().Type) + " " + Right + ")");
			}
		}
		else {
			BunLogger._LogError(Node.SourceToken, "Unknown binary \"" + Node.SourceToken.GetText() + "\" for this type");
		}
	}

	@Override public void VisitBreakNode(BunBreakNode Node) {
		this.Source.AppendNewLine("br label %" + this.CurrentScope.PeekBreakLabel());
		this.CurrentScope.TerminateBlock();
	}

	@Override public void VisitBlockNode(BunBlockNode Node) {
		this.GenerateStmtListNode(Node);
	}

	@Override public void VisitBooleanNode(BunBooleanNode Node) {
		if (Node.BooleanValue) {
			this.CurrentScope.PushValue(this.TrueLiteral);
		} else {
			this.CurrentScope.PushValue(this.FalseLiteral);
		}
	}

	@Override public void VisitCastNode(BunCastNode Node) {
		/*FIXME*/
		@Var BType BeforeType = Node.ExprNode().Type;
		@Var BType AfterType = Node.Type;
		if(BeforeType == AfterType || BeforeType.IsVarType()) {
			this.GenerateExpression(Node.ExprNode());
		}
		else if(!(BeforeType.IsVoidType()) && AfterType.IsVoidType()) {
			this.GenerateExpression(Node.ExprNode());
			this.CurrentScope.PopValue();
		}
		else {
			this.GenerateExpression(Node.ExprNode());
			@Var String Expr = this.CurrentScope.PopValue();

			@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(TempVar);
			this.Source.Append(" = ");
			this.Source.Append(this.GetCastOpCode(BeforeType, AfterType));
			this.Source.Append(" ");
			this.Source.Append(this.GetTypeExpr(BeforeType));
			this.Source.Append(" ");
			this.Source.Append(Expr);
			this.Source.Append(" to ");
			this.Source.Append(this.GetTypeExpr(AfterType));

			this.CurrentScope.PushValue(TempVar);
		}
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		@Var LLVMScope PushedScope = this.CurrentScope;
		this.CurrentScope = new LLVMScope();

		this.DefineClass(Node.ClassName(), Node);
		@Var String ClassSymbol = this.ToClassSymbol(Node.ClassName());
		this.Source.AppendNewLine(ClassSymbol);
		this.Source.OpenIndent(" = type {");
		this.Source.AppendNewLine("i8*");
		this.VisitFieldList(Node, !WithInitValue);
		this.Source.CloseIndent("}");

		@Var String ProtoSymbol = "@" + Node.ClassName() + ".Proto";
		this.Source.AppendNewLine(ProtoSymbol);
		this.Source.Append(" = ");
		if(!Node.IsExport) {
			this.Source.Append("private ");
		}
		this.Source.Append("constant ");
		this.Source.Append(ClassSymbol);
		this.Source.OpenIndent(" {");
		if(!Node.SuperType().Equals(BClassType._ObjectType)) {
			this.Source.AppendNewLine("i8* bitcast (");
			this.Source.Append(this.GetTypeExpr(Node.SuperType()));
			this.Source.Append(" @" + Node.SuperType().ShortName + ".Proto");
			this.Source.Append(" to i8*)");
		}
		else {
			this.Source.AppendNewLine("i8* null");
		}
		this.VisitFieldList(Node, WithInitValue);
		this.Source.CloseIndent("}");

		this.CurrentScope = PushedScope;
	}

	@Override public void VisitComparatorNode(ComparatorNode Node) {
		this.GenerateExpression(Node.LeftNode());
		@Var String Left = this.CurrentScope.PopValue();
		this.GenerateExpression(Node.RightNode());
		@Var String Right = this.CurrentScope.PopValue();

		if(this.IsPrimitiveType(Node.LeftNode().Type)) {
			@Var String Result = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(Result);
			this.Source.Append(" = ");
			this.Source.Append(this.GetCompareOpCodeAndCondition(Node));
			this.Source.Append(" ");
			this.Source.Append(this.GetTypeExpr(Node.LeftNode().Type));
			this.Source.Append(" ");
			this.Source.Append(Left);
			this.Source.Append(", ");
			this.Source.Append(Right);
			this.CurrentScope.PushValue(Result);
		}
		else if(Node.LeftNode().Type.IsStringType()) {
			if(Node.SourceToken.EqualsText("==") && Node.RightNode().Type.IsStringType()) {
				this.DeclareExtrnalFunction("BString_EqualString", "i1", "(%BString*, %BString*)");
				this.CallExternalFunction("BString_EqualString", "(" + this.GetTypeExpr(Node.LeftNode().Type) + " " + Left + ", " + this.GetTypeExpr(Node.RightNode().Type) + " " + Right + ")");
			}
			else if(Node.SourceToken.EqualsText("!=") && Node.RightNode().Type.IsStringType()) {
				this.DeclareExtrnalFunction("BString_EqualString", "i1", "(%BString*, %BString*)");
				this.CallExternalFunction("BString_EqualString", "(" + this.GetTypeExpr(Node.LeftNode().Type) + " " + Left + ", " + this.GetTypeExpr(Node.RightNode().Type) + " " + Right + ")");
				@Var String CmpResult = this.CurrentScope.PopValue();

				@Var String NotResult = this.CurrentScope.CreateTempLocalSymbol();
				this.Source.AppendNewLine(NotResult);
				this.Source.Append(" = ");
				this.Source.Append("xor");
				this.Source.Append(" ");
				this.Source.Append(this.GetTypeExpr(BType.BooleanType));
				this.Source.Append(" 1, ");
				this.Source.Append(CmpResult);

				this.CurrentScope.PushValue(NotResult);
			}
		}
		else {
			@Var String LeftAddress = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(LeftAddress);
			this.Source.Append(" = ptrtoint ");
			this.Source.Append(this.GetTypeExpr(Node.LeftNode().Type));
			this.Source.Append(" ");
			this.Source.Append(Left);
			this.Source.Append(" to i64");

			@Var String RightAddress = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(RightAddress);
			this.Source.Append(" = ptrtoint ");
			this.Source.Append(this.GetTypeExpr(Node.RightNode().Type));
			this.Source.Append(" ");
			this.Source.Append(Right);
			this.Source.Append(" to i64");

			@Var String Result = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(Result);
			this.Source.Append(" = ");
			this.Source.Append(this.GetCompareOpCodeAndCondition(Node));
			this.Source.Append(" ");
			this.Source.Append(this.GetTypeExpr(BType.IntType));
			this.Source.Append(" ");
			this.Source.Append(LeftAddress);
			this.Source.Append(", ");
			this.Source.Append(RightAddress);
			this.CurrentScope.PushValue(Result);
		}
	}

	@Override public void VisitErrorNode(ErrorNode Node) {
	}

	@Override public void VisitFloatNode(BunFloatNode Node) {
		this.CurrentScope.PushValue("" + Node.FloatValue);
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		@Var BFuncType FuncType = Node.GetFuncType();
		if(FuncType == null) {
			BunLogger._LogError(Node.SourceToken, "Can't interpret this function call");
			return;
		}
		@Var BType ReturnType = FuncType.GetReturnType();
		@Var BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		if(FuncNameNode != null) {
			@Var String FuncName = FuncNameNode.GetSignature();
			this.DefineGlobalSymbol(FuncName);
			this.CurrentScope.PushValue(this.ToGlobalSymbol(FuncName));
		}
		else {
			this.GenerateExpression(Node.FunctorNode());
		}
		@Var String CallFunc = this.CurrentScope.PopValue();
		this.GenerateListNode(" (", Node, ", ", ")");
		@Var String Args = this.CurrentScope.PopValue();

		@Var String TempVar = "";
		this.Source.AppendNewLine("");
		if(!ReturnType.IsVoidType()) {
			TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.Append(TempVar);
			this.Source.Append(" = ");
		}
		this.Source.Append("call ");
		this.Source.Append(this.GetTypeExpr(FuncType));
		this.Source.Append(" ");
		this.Source.Append(CallFunc);
		this.Source.Append(Args);

		if(!ReturnType.IsVoidType()) {
			this.CurrentScope.PushValue(TempVar);
		}
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		@Var LLVMScope PushedScope = this.CurrentScope;
		this.CurrentScope = new LLVMScope();

		this.Source = this.AppendNewSourceBuilder();

		this.Source.AppendNewLine("define private ");
		this.Source.Append(this.GetTypeExpr(Node.ReturnType()));
		@Var String FuncName;
		if(Node.FuncName() == null) {
			FuncName = this.CreateTempFuncName(Node.ResolvedFuncType);
		}
		else {
			@Var String StringifiedName = Node.ResolvedFuncType.StringfySignature(Node.FuncName());
			this.DefineGlobalSymbol(StringifiedName);
			FuncName = this.ToGlobalSymbol(StringifiedName);
		}
		this.Source.Append(" " + FuncName + " ");
		this.VisitFuncParamNode("(", Node, ")");
		//@Var String Args = this.CurrentScope.PopValue();
		//this.Source.Append(Args);
		this.Source.OpenIndent(" {");
		this.Source.AppendLineFeed();
		this.Source.Append("Entry:");
		this.CurrentScope.SetLabel("Entry");

		//this.Source = this.AppendNewSourceBuilder();
		//this.Source.Indent();
		this.GenerateExpression(Node.BlockNode());
		if(!this.CurrentScope.IsBlockTerminated()) {
			this.AppendDefaultReturn(Node.ReturnType());
		}
		this.Source.CloseIndent("}");

		if(Node.IsExport) {
			if(Node.FuncName().equals("main")) {
				this.Source.AppendNewLine("define i32 @main (i32 %argc, i8** %argv)");
				this.Source.OpenIndent(" {");
				if(Node.ResolvedFuncType.GetFuncParamSize() != 0) {
					this.Source.AppendNewLine("%_argc = zext i32 %argc to i64");
				}
				this.Source.AppendNewLine("call ");
				this.Source.Append(this.GetTypeExpr(Node.ResolvedFuncType));
				this.Source.Append(" ");
				this.Source.Append(FuncName);
				this.Source.Append(" (");
				if(Node.ResolvedFuncType.GetFuncParamSize() != 0) {
					this.Source.Append("i64 %_argc, i8** %argv");
				}
				this.Source.Append(")");
				this.Source.AppendNewLine("ret i32 0");
				this.Source.CloseIndent("}");
			}
			else {
				this.Header.AppendNewLine("@" + Node.FuncName());
				this.Header.Append(" = constant ");
				this.Header.Append(this.GetTypeExpr(Node.ResolvedFuncType));
				this.Header.Append(" ");
				this.Header.Append(FuncName);
			}
		}

		if(Node.ParentFunctionNode != null || Node.ParentNode instanceof BunLetVarNode) {
			this.Source = this.Source.Pop();
		}
		this.CurrentScope = PushedScope;
		//Node.ParentFunctionNode != null
		if(Node.FuncName() == null) {
			this.CurrentScope.PushValue(FuncName);
		}
	}

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		this.GenerateExpression(Node.RecvNode());
		@Var String Recv = this.CurrentScope.PopValue();
		this.GenerateExpression(Node.IndexNode());
		@Var String Index = this.CurrentScope.PopValue();

		if(Node.RecvNode().Type.IsArrayType()) {
			@Var BType ElementType = ((BGenericType)Node.RecvNode().Type).ParamType;
			if(ElementType.IsIntType()) {
				this.DeclareExtrnalFunction("BIntArray_Get", "i64", "(%BArray*, i64)");
				this.CallExternalFunction("BIntArray_Get", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ")");
			}
			else if(ElementType.IsFloatType()) {
				this.DeclareExtrnalFunction("BFloatArray_Get", "double", "(%BArray*, i64)");
				this.CallExternalFunction("BFloatArray_Get", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ")");
			}
			else if(ElementType.IsBooleanType()) {
				this.DeclareExtrnalFunction("BBooleanArray_Get", "i1", "(%BArray*, i64)");
				this.CallExternalFunction("BBooleanArray_Get", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ")");
			}
			else {
				this.DeclareExtrnalFunction("BObjArray_Get", "i8*", "(%BArray*, i64)");
				this.CallExternalFunction("BObjArray_Get", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ")");
			}
		}
		else if(Node.RecvNode().Type.IsStringType()) {
			this.DeclareExtrnalFunction("BString_Get", "%BString*", "(%BString*, i64)");
			this.CallExternalFunction("BString_Get", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ")");
		}
	}

	@Override public void VisitGetNameNode(GetNameNode Node) {
		if(Node.ResolvedNode.IsTopLevel()/* global Let */) {
			if(this.IsUserDefinedGlobalSymbol(Node.GetUniqueName(this))) {
				@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
				this.Source.AppendNewLine(TempVar);
				this.Source.Append(" = load ");
				this.Source.Append(this.GetTypeExpr(Node.Type) + "*");
				this.Source.Append(" " + this.ToGlobalSymbol(Node.GetUniqueName(this)));
				this.CurrentScope.PushValue(TempVar);
			}
			else {
				this.GenerateExpression(Node.ResolvedNode.InitValueNode());
			}
		}
		else if(Node.ResolvedNode.IsReadOnly()) {
			//if() /* local Let */{
			//}
			//else /* Argument */{
			this.CurrentScope.PushValue(this.ToLocalSymbol(Node.GetUniqueName(this)));
			//}
		}
		else /* Var */{
			@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(TempVar);
			this.Source.Append(" = load ");
			this.Source.Append(this.GetTypeExpr(Node.Type) + "*");
			this.Source.Append(" " + this.ToLocalSymbol(Node.GetUniqueName(this)));
			//if(!this.IsPrimitiveType(Node.Type)) {
			//@llvm.gcread
			//}
			this.CurrentScope.PushValue(TempVar);
		}
	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GetObjectElementPointer(Node.RecvNode(), Node.GetName());
		@Var String Element = this.CurrentScope.PopValue();

		@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
		this.Source.AppendNewLine(TempVar);
		this.Source.Append(" = load ");
		this.Source.Append(this.GetTypeExpr(Node.Type) + "*");
		this.Source.Append(" ");
		this.Source.Append(Element);
		//@llvm.gcread
		this.CurrentScope.PushValue(TempVar);
	}

	@Override public void VisitGroupNode(GroupNode Node) {
		this.GenerateExpression(Node.ExprNode());
	}

	@Override public void VisitIfNode(BunIfNode Node) {
		@Var int LabelNum = this.CurrentScope.GetTempLabelNumber();
		@Var String ThenLabel = "If__" + LabelNum + ".Then";
		@Var String ElseLabel = "If__" + LabelNum + ".Else";
		@Var String EndLabel = "If__" + LabelNum + ".End";
		@Var boolean IsEndReachable = false;

		this.GenerateExpression(Node.CondNode());
		@Var String Cond = this.CurrentScope.PopValue();

		this.Source.AppendNewLine("br i1 ");
		this.Source.Append(Cond);
		this.Source.Append(", ");
		this.Source.Append("label %" + ThenLabel + ", ");
		if(Node.ElseNode() != null) {
			this.Source.Append("label %" + ElseLabel);
		}
		else {
			this.Source.Append("label %" + EndLabel);
			IsEndReachable = true;
		}

		this.Source.AppendLineFeed();
		this.Source.Append(ThenLabel + ":");
		this.CurrentScope.SetLabel(ThenLabel);
		this.GenerateExpression(Node.ThenNode());
		if(!this.CurrentScope.IsBlockTerminated()) {
			this.Source.AppendNewLine("br label %" + EndLabel);
			IsEndReachable = true;
		}

		if(Node.ElseNode() != null) {
			this.Source.AppendLineFeed();
			this.Source.Append(ElseLabel + ":");
			this.CurrentScope.SetLabel(ElseLabel);
			this.GenerateExpression(Node.ElseNode());
			if(!this.CurrentScope.IsBlockTerminated()) {
				this.Source.AppendNewLine("br label %" + EndLabel);
				IsEndReachable = true;
			}
		}
		if(IsEndReachable) {
			this.Source.AppendLineFeed();
			this.Source.Append(EndLabel + ":");
			this.CurrentScope.SetLabel(EndLabel);
		}
	}

	@Override public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		// TODO
	}

	@Override public void VisitIntNode(BunIntNode Node) {
		this.CurrentScope.PushValue("" + Node.IntValue);
	}

	@Override public void VisitLetNode(BunLetVarNode Node) {
		if(Node.IsParamNode()) {
			this.VisitParamNode(Node);
		}
		else {
			@Var LLVMScope PushedScope = this.CurrentScope;
			this.CurrentScope = new LLVMScope();

			@Var BNode InitNode = Node.InitValueNode();
			if((InitNode instanceof BunFunctionNode)) {
			}
			else if((InitNode instanceof BunArrayLiteralNode) || (InitNode instanceof BunMapLiteralNode) || (InitNode instanceof BunStringNode)) {
				/*FIXME*/
				return;
			}
			else if(!(InitNode instanceof ConstNode)) {
				BunLogger._LogError(Node.SourceToken, "Init value is not a constant");
				return;
			}

			this.GenerateExpression(InitNode);
			@Var String Init = this.CurrentScope.PopValue();

			this.DefineGlobalSymbol(Node.GetUniqueName(this));
			this.Header.AppendNewLine(this.ToGlobalSymbol(Node.GetUniqueName(this)));
			this.Header.Append(" = ");
			if(!Node.IsExport()) {
				this.Header.Append("private ");
			}
			this.Header.Append("constant ");
			this.Header.Append(this.GetTypeExpr(Node.DeclType()));
			this.Header.Append(" ");
			this.Header.Append(Init);

			this.CurrentScope = PushedScope;
			//this.CurrentScope.PushValue(this.ToGlobalSymbol(Node.GetUniqueName(this)));
		}
	}

	@Override public void VisitFormNode(BunFormNode Node) {
		@Var StringBuilder sb = new StringBuilder();

		@Var String TempVar = "";
		@Var String Macro = Node.GetFormText();
		@Var BFuncType FuncType = Node.GetFuncType();
		if(!FuncType.GetReturnType().IsVoidType()) {
			TempVar = this.CurrentScope.CreateTempLocalSymbol();
			sb.append(TempVar);
			sb.append(" = ");
		}
		@Var int fromIndex = 0;
		@Var int BeginNum = Macro.indexOf("$[", fromIndex);
		while(BeginNum != -1) {
			@Var int EndNum = Macro.indexOf("]", BeginNum + 2);
			if(EndNum == -1) {
				break;
			}
			sb.append(Macro.substring(fromIndex, BeginNum));
			@Var int Index = (int)LibBunSystem._ParseInt(Macro.substring(BeginNum+2, EndNum));
			if(Node.AST[Index] != null) {
				sb.append(this.GetTypeExpr(Node.AST[Index].Type));
				sb.append(" ");
				this.GenerateExpression(Node.AST[Index]);
				sb.append(this.CurrentScope.PopValue());
			}
			fromIndex = EndNum + 1;
			BeginNum = Macro.indexOf("$[", fromIndex);
		}
		sb.append(Macro.substring(fromIndex));
		this.Source.AppendNewLine(sb.toString());

		BeginNum = Macro.indexOf("call ");
		while(BeginNum != -1) {
			@Var int EndNum = Macro.indexOf("(", BeginNum + 5);
			if(EndNum == -1) {
				break;
			}
			@Var String ReturnType = Macro.substring(BeginNum + 5, EndNum - 1);

			BeginNum = EndNum;
			EndNum = Macro.indexOf(")", BeginNum + 1);
			if(EndNum == -1) {
				break;
			}
			@Var String ParamType = Macro.substring(BeginNum, EndNum + 1);

			BeginNum = Macro.indexOf("@", EndNum + 1);
			if(BeginNum == -1) {
				break;
			}
			EndNum = Macro.indexOf(" ", BeginNum + 1);
			if(EndNum == -1) {
				break;
			}
			@Var String FuncName = Macro.substring(BeginNum + 1, EndNum);
			this.DeclareExtrnalFunction(FuncName, ReturnType, ParamType);

			BeginNum = Macro.indexOf("call ", EndNum + 1);
		}
		if(!FuncType.GetReturnType().IsVoidType()) {
			this.CurrentScope.PushValue(TempVar);
		}
	}

	@Override public void VisitMapLiteralNode(BunMapLiteralNode Node) {
		// TODO
	}

	@Override public void VisitMethodCallNode(MethodCallNode Node) {
		// TODO
	}

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		if(Node.Type instanceof BClassType) {
			this.DeclareExtrnalFunction("GC_malloc", "i8*", "(i64)");
			//this.DeclareExtrnalFunction("free", "void", "(i8*)");
			this.DeclareExtrnalFunction("memcpy", "void", "(i8*, i8*, i64)");

			@Var String AllocateSize = this.GetSizeOfType(Node.Type);
			this.CallExternalFunction("GC_malloc", "(i64 " + AllocateSize + ")");
			@Var String AllocatedAddress = this.CurrentScope.PopValue();

			this.CallExternalFunction("memcpy", "(i8* " + AllocatedAddress + ", i8* bitcast (" + this.GetTypeExpr(Node.Type) + " @" + Node.Type.ShortName + ".Proto to i8*), i64 " + AllocateSize + ")");

			@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(TempVar);
			this.Source.Append(" = bitcast i8* ");
			this.Source.Append(AllocatedAddress);
			this.Source.Append(" to ");
			this.Source.Append(this.GetTypeExpr(Node.Type));

			/* if(this.IsUserDefinedGlobalSymbol(Node.Type.ShortName)) {
				@Var StringBuilder sb = new StringBuilder();
				sb.append("call void ");
				sb.append(this.ToConstructorSymbol(Node.Type.ShortName));
				sb.append(" (");
				sb.append(this.GetTypeExpr(Node.Type));
				sb.append(" " + TempVar);
				if(Node.GetListSize() > 0) {
					this.GenerateListNode(", ", Node, ", ", ")");
					sb.append(this.Writer.PopValue());
				}
				else {
					sb.append(")");
				}
				this.Source.AppendNewLine(sb.toString());
			} */
			this.CurrentScope.PushValue(TempVar);
		}
		else if(Node.Type.IsStringType()) {
			// FIXME
		}
	}

	@Override public void VisitNotNode(BunNotNode Node) {
		this.GenerateExpression(Node.AST[BunNotNode._Recv]);
		@Var String Recv = this.CurrentScope.PopValue();

		@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
		this.Source.AppendNewLine(TempVar);
		this.Source.Append(" = ");
		this.Source.Append("xor");
		this.Source.Append(" ");
		this.Source.Append(this.GetTypeExpr(Node.AST[BunNotNode._Recv].Type));
		this.Source.Append(" 1, ");
		this.Source.Append(Recv);

		this.CurrentScope.PushValue(TempVar);
	}

	@Override public void VisitNullNode(BunNullNode Node) {
		this.CurrentScope.PushValue(this.NullLiteral);
	}

	@Override public void VisitOrNode(BunOrNode Node) {
		@Var int LabelNum = this.CurrentScope.GetTempLabelNumber();
		@Var String RightLabel = "Or__" + LabelNum + ".Right";
		@Var String EndLabel = "Or__" + LabelNum + ".End";

		this.GenerateExpression(Node.LeftNode());
		@Var String LeftResult = this.CurrentScope.PopValue();

		this.Source.AppendNewLine("br i1 ");
		this.Source.Append(LeftResult);
		this.Source.Append(", ");
		this.Source.Append("label %" + EndLabel + ", ");
		this.Source.Append("label %" + RightLabel);
		@Var String LeftLabel = this.CurrentScope.GetCurrentLabel();

		this.Source.AppendLineFeed();
		this.Source.Append(RightLabel + ":");
		this.CurrentScope.SetLabel(RightLabel);
		this.GenerateExpression(Node.RightNode());
		@Var String RightResult = this.CurrentScope.PopValue();
		this.Source.AppendNewLine("br label %" + EndLabel);
		RightLabel = this.CurrentScope.GetCurrentLabel();

		this.Source.AppendLineFeed();
		this.Source.Append(EndLabel + ":");
		this.CurrentScope.SetLabel(EndLabel);
		@Var String AllResult = this.CurrentScope.CreateTempLocalSymbol();
		this.Source.AppendNewLine(AllResult);
		this.Source.Append(" = phi i1 ");
		this.Source.Append("[ true, %" + LeftLabel + " ], ");
		this.Source.Append("[ " + RightResult + ", %" + RightLabel + " ]");

		this.CurrentScope.PushValue(AllResult);
	}

	@Override public void VisitReturnNode(BunReturnNode Node) {
		if (Node.HasReturnExpr()) {
			this.GenerateExpression(Node.ExprNode());
			@Var String Expr = this.CurrentScope.PopValue();

			this.Source.AppendNewLine("ret ");
			this.Source.Append(this.GetTypeExpr(Node.ExprNode().Type));
			this.Source.Append(" ");
			this.Source.Append(Expr);
		}
		else {
			this.Source.AppendNewLine("ret void");
		}
		this.CurrentScope.TerminateBlock();
	}

	//	@Override public void VisitSetIndexNode(SetIndexNode Node) {
	// FIXME
	//		this.GenerateExpression(Node.RecvNode());
	//		@Var String Recv = this.CurrentScope.PopValue();
	//		this.GenerateExpression(Node.IndexNode());
	//		@Var String Index = this.CurrentScope.PopValue();
	//		this.GenerateExpression(Node.ExprNode());
	//		@Var String Expr = this.CurrentScope.PopValue();
	//
	//		if(Node.RecvNode().Type.IsArrayType()) {
	//			@Var BType ElementType = ((BGenericType)Node.RecvNode().Type).ParamType;
	//			if(ElementType.IsIntType()) {
	//				this.DeclareExtrnalFunction("BIntArray_Set", "void", "(%BArray*, i64, i64)");
	//				this.CallExternalFunction("BIntArray_Set", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ", " + this.GetTypeExpr(Node.ExprNode().Type) + " " + Expr + ")");
	//			}
	//			else if(ElementType.IsFloatType()) {
	//				this.DeclareExtrnalFunction("BFloatArray_Set", "void", "(%BArray*, i64, double)");
	//				this.CallExternalFunction("BFloatArray_Set", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ", " + this.GetTypeExpr(Node.ExprNode().Type) + " " + Expr + ")");
	//			}
	//			else if(ElementType.IsBooleanType()) {
	//				this.DeclareExtrnalFunction("BBooleanArray_Set", "void", "(%BArray*, i64, i1)");
	//				this.CallExternalFunction("BBooleanArray_Set", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ", " + this.GetTypeExpr(Node.ExprNode().Type) + " " + Expr + ")");
	//			}
	//			else {
	//				this.DeclareExtrnalFunction("BObjArray_Set", "void", "(%BArray*, i64, i8*)");
	//				this.CallExternalFunction("BObjArray_Set", "(" + this.GetTypeExpr(Node.RecvNode().Type) + " " + Recv + ", " + this.GetTypeExpr(Node.IndexNode().Type) + " " + Index + ", i8* bitcast (" + this.GetTypeExpr(Node.ExprNode().Type) + " " + Expr + " to i8*))");
	//			}
	//		}
	//	}

	@Override public void VisitAssignNode(AssignNode Node) {
		//FIXME:
		//		this.GenerateExpression(Node.ExprNode());
		//		@Var String Expr = this.CurrentScope.PopValue();
		//
		//		this.Source.AppendNewLine("store ");
		//		this.Source.Append(this.GetTypeExpr(Node.ExprNode().Type));
		//		this.Source.Append(" ");
		//		this.Source.Append(Expr);
		//		this.Source.Append(", ");
		//		this.Source.Append(this.GetTypeExpr(Node.ExprNode().Type) + "*");
		//		this.Source.Append(" ");
		//		//this.GenerateExpression(Node.LeftNode());
		//		this.Source.Append(this.ToLocalSymbol(Node.NameNode().GetUniqueName(this)));
		//		//if(!this.IsPrimitiveType(Node.ExprNode().Type)) {
		//		//@llvm.gcwrite
		//		//}
	}

	@Override public void VisitStringNode(BunStringNode Node) {
		@Var String StringConst = this.CreateTempGlobalSymbol();
		this.Header.AppendNewLine(StringConst);
		this.Header.Append(" = private constant ");
		@Var String StringValue = this.ConvertLLVMString(Node.StringValue);
		@Var int StrLen = this.GetLLVMStringLen(StringValue);
		@Var String StringType = "[" + StrLen + " x i8]";
		this.Header.Append(StringType);
		this.Header.Append(" c\"" + StringValue + "\"");

		this.DeclareExtrnalFunction("BString_Construct", "%BString*", "(i8*, i64)");
		this.CallExternalFunction("BString_Construct", "(i8* bitcast (" + StringType + "* " + StringConst + " to i8*), i64 " + (StrLen-1) + ")");
	}

	//FIXME
	//	@Override public void VisitSetFieldNode(SetFieldNode Node) {
	//		this.GenerateExpression(Node.ExprNode());
	//		@Var String Expr = this.CurrentScope.PopValue();
	//		this.GetObjectElementPointer(Node.RecvNode(), Node.GetName());
	//		@Var String Element = this.CurrentScope.PopValue();
	//
	//		this.Source.AppendNewLine("store ");
	//		this.Source.Append(this.GetTypeExpr(Node.ExprNode().Type));
	//		this.Source.Append(" ");
	//		this.Source.Append(Expr);
	//		this.Source.Append(", ");
	//		this.Source.Append(this.GetTypeExpr(Node.ExprNode().Type) + "*");
	//		this.Source.Append(" ");
	//		this.Source.Append(Element);
	//		//@llvm.gcwrite
	//	}

	@Override public void VisitThrowNode(BunThrowNode Node) {
		// TODO
	}

	@Override public void VisitTryNode(BunTryNode Node) {
		// TODO
	}

	@Override public void VisitUnaryNode(UnaryOperatorNode Node) {
		this.GenerateExpression(Node.RecvNode());
		@Var String Recv = this.CurrentScope.PopValue();

		if(Node.SourceToken.EqualsText('+')) {
			this.CurrentScope.PushValue(Recv);
		}
		else if(Node.SourceToken.EqualsText('-')){
			if(Node.RecvNode() instanceof ConstNode) {
				this.CurrentScope.PushValue("-" + Recv);
			}
			else {
				@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
				this.Source.AppendNewLine(TempVar);
				this.Source.Append(" = ");
				if(Node.RecvNode().IsUntyped()) {
					BunLogger._LogError(Node.SourceToken, "Unary \"-\" is untyped");
				}
				else if(Node.RecvNode().Type.IsIntType()) {
					this.Source.Append("sub");
					this.Source.Append(" i64 0, ");
				}
				else if(Node.RecvNode().Type.IsFloatType()) {
					this.Source.Append("fsub");
					this.Source.Append(" double 0.0, ");
				}
				else {
					BunLogger._LogError(Node.SourceToken, "Unknown unary \"-\" for this type");
				}
				this.Source.Append(Recv);

				this.CurrentScope.PushValue(TempVar);
			}
		}
		else if(Node.SourceToken.EqualsText('~')){
			if(Node.RecvNode().IsUntyped()) {
				BunLogger._LogError(Node.SourceToken, "Unary \"~\" is untyped");
				return;
			}
			else if(!Node.RecvNode().Type.IsIntType()){
				BunLogger._LogError(Node.SourceToken, "Unknown unary \"~\" for this type");
				return;
			}

			@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.AppendNewLine(TempVar);
			this.Source.Append(" = xor ");
			this.Source.Append(this.GetTypeExpr(Node.RecvNode().Type));
			this.Source.Append(" -1, ");
			this.Source.Append(Recv);

			this.CurrentScope.PushValue(TempVar);
		}
		else {
			BunLogger._LogError(Node.SourceToken, "Unknown unary \"" + Node.SourceToken.GetText() + "\" for this type");
		}
	}

	@Override public void VisitVarBlockNode(BunVarBlockNode Node) {
		this.VisitVarDeclNode(Node.VarDeclNode());
		this.GenerateStmtListNode(Node);
	}

	@Override public void VisitWhileNode(BunWhileNode Node) {
		@Var int LabelNum = this.CurrentScope.GetTempLabelNumber();
		@Var String CondLabel = "While__" + LabelNum + ".Cond";
		@Var String BodyLabel = "While__" + LabelNum + ".Body";
		@Var String EndLabel = "While__" + LabelNum + ".End";

		this.Source.AppendNewLine("br label %" + CondLabel);

		this.Source.AppendLineFeed();
		this.Source.Append(CondLabel + ":");
		this.CurrentScope.SetLabel(CondLabel);
		this.GenerateExpression(Node.CondNode());
		@Var String Cond = this.CurrentScope.PopValue();

		this.Source.AppendNewLine("br i1 ");
		this.Source.Append(Cond);
		this.Source.Append(", ");
		this.Source.Append("label %" + BodyLabel + ", ");
		this.Source.Append("label %" + EndLabel);

		this.Source.AppendLineFeed();
		this.Source.Append(BodyLabel + ":");
		this.CurrentScope.SetLabel(BodyLabel);
		this.CurrentScope.PushBreakLabel(EndLabel);
		this.GenerateExpression(Node.BlockNode());
		if(!this.CurrentScope.IsBlockTerminated()) {
			this.Source.AppendNewLine("br label %" + CondLabel);
		}
		this.CurrentScope.PopBreakLabel();

		this.Source.AppendLineFeed();
		this.Source.Append(EndLabel + ":");
		this.CurrentScope.SetLabel(EndLabel);
	}


	@Override protected void GenerateExpression(BNode Node) {
		Node.Accept(this);
		/* if(this.IsNeededSurroud(Node)) {
			this.GenerateExpression("", Node, "");
		}
		else {
			this.GenerateExpression(Node);
		} */
	}

	@Override public void GenerateStmtListNode(BunBlockNode BlockNode) {
		@Var int i = 0;
		while (i < BlockNode.GetListSize()) {
			@Var BNode SubNode = BlockNode.GetListAt(i);
			this.GenerateExpression(SubNode);
			i = i + 1;
		}
	}

	@Override protected void VisitParamNode(BunLetVarNode Node) {
		@Var String SymbolName = Node.GetGivenName();
		this.CurrentScope.DefineLocalSymbol(SymbolName);
		this.CurrentScope.PushValue(this.ToLocalSymbol(SymbolName));
	}

	@Override protected void VisitFuncParamNode(String OpenToken, BunFunctionNode VargNode, String CloseToken) {
		@Var StringBuilder sb = new StringBuilder();
		sb.append(OpenToken);
		@Var int i = 0;
		while(i < VargNode.GetListSize()) {
			@Var BunLetVarNode ParamNode = VargNode.GetParamNode(i);
			if (i > 0) {
				sb.append(this.Camma);
			}
			sb.append(this.GetTypeExpr(ParamNode.DeclType()));
			sb.append(" ");
			this.VisitParamNode(ParamNode);
			sb.append(this.CurrentScope.PopValue());
			i = i + 1;
		}
		sb.append(CloseToken);
		this.Source.Append(sb.toString());
	}

	@Override protected void VisitVarDeclNode(BunLetVarNode Node) {
		//@Var SourceBuilder EntryBlockBuilder = this.Source.Pop();
		@Var LibBunSourceBuilder VarDeclBuilder = this.Source; //FIXME

		@Var String VarName = Node.GetUniqueName(this);
		this.CurrentScope.DefineLocalVar(VarName);
		@Var String VarSymbol = this.ToLocalSymbol(VarName);
		VarDeclBuilder.AppendNewLine(VarSymbol);
		VarDeclBuilder.Append(" = alloca ");
		VarDeclBuilder.Append(this.GetTypeExpr(Node.DeclType()));
		//if(!this.IsPrimitiveType(Node.DeclType())) {
		//@llvm.gcroot
		//}

		this.GenerateExpression(Node.InitValueNode());
		@Var String Init = this.CurrentScope.PopValue();

		this.Source.AppendNewLine("store ");
		this.Source.Append(this.GetTypeExpr(Node.InitValueNode().Type));
		this.Source.Append(" ");
		this.Source.Append(Init);
		this.Source.Append(", ");
		this.Source.Append(this.GetTypeExpr(Node.DeclType()) + "*");
		this.Source.Append(" ");
		this.Source.Append(VarSymbol);
	}

	@Override protected void GenerateListNode(String OpenToken, AbstractListNode VargNode, String DelimToken, String CloseToken) {
		@Var StringBuilder sb = new StringBuilder();
		sb.append(OpenToken);
		@Var int i = 0;
		while(i < VargNode.GetListSize()) {
			@Var BNode ParamNode = VargNode.GetListAt(i);
			if (i > 0) {
				sb.append(DelimToken);
			}
			sb.append(this.GetTypeExpr(ParamNode.Type));
			sb.append(" ");
			this.GenerateExpression(ParamNode);
			sb.append(this.CurrentScope.PopValue());
			i = i + 1;
		}
		sb.append(CloseToken);
		this.CurrentScope.PushValue(sb.toString());
	}

	private void VisitFieldList(BunClassNode ClassNode, boolean WithInitValue) {
		if(ClassNode.SuperType() != BClassType._ObjectType) {
			BunClassNode SuperClassNode = this.ClassFieldMap.get(ClassNode.SuperType().ShortName);
			this.VisitFieldList(SuperClassNode, WithInitValue);
		}
		@Var int i = 0;
		while(i < ClassNode.GetListSize()) {
			@Var BunLetVarNode FieldNode = ClassNode.GetFieldNode(i);
			this.Source.Append(",");
			this.Source.AppendNewLine(this.GetTypeExpr(FieldNode.DeclType()));
			if(WithInitValue) {
				this.Source.Append(" ");
				this.GenerateExpression(FieldNode.InitValueNode());
				this.Source.Append(this.CurrentScope.PopValue());
			}
			i = i + 1;
		}
	}

	private void DefineExternalStruct(String TypeName) {
		if(!this.ExternalStructList.contains(TypeName)) {
			this.Header.AppendNewLine("%" + TypeName + " = type opaque");
			this.ExternalStructList.add(TypeName);
		}
	}

	private void DeclareExtrnalFunction(String FuncName, String ReturnType, String ParamType) {
		if(!this.ExternalFunctionMap.containsKey(FuncName)) {
			this.ExternalFunctionMap.put(FuncName, ReturnType + " " + ParamType + "*");
			this.Header.AppendNewLine("declare ");
			this.Header.Append(ReturnType);
			this.Header.Append(" @" + FuncName);
			this.Header.Append(" " + ParamType);
		}
	}
	private void CallExternalFunction(String FuncName, String Param) {
		@Var String FuncType = this.ExternalFunctionMap.get(FuncName);
		if(FuncType == null) {
			return;
		}
		@Var String TempVar = "";
		this.Source.AppendNewLine("");
		if(!FuncType.startsWith("void")) {
			TempVar = this.CurrentScope.CreateTempLocalSymbol();
			this.Source.Append(TempVar);
			this.Source.Append(" = ");
		}
		this.Source.Append("call ");
		this.Source.Append(FuncType);
		this.Source.Append(" @" + FuncName);
		this.Source.Append(" " + Param);

		if(!FuncType.startsWith("void")) {
			this.CurrentScope.PushValue(TempVar);
		}
	}

	private void GetObjectElementPointer(BNode RecvNode, String FieldName) {
		this.GenerateExpression(RecvNode);
		@Var String Recv = this.CurrentScope.PopValue();
		this.GetObjectElementOffset(RecvNode.Type, FieldName);
		@Var String Field = this.CurrentScope.PopValue();

		@Var String TempVar = this.CurrentScope.CreateTempLocalSymbol();
		this.Source.AppendNewLine(TempVar);
		this.Source.Append(" = getelementptr ");
		this.Source.Append(this.GetTypeExpr(RecvNode.Type));
		this.Source.Append(" ");
		this.Source.Append(Recv);
		this.Source.Append(", i64 0");
		this.Source.Append(", ");
		this.Source.Append(Field);

		this.CurrentScope.PushValue(TempVar);
	}
	private void GetObjectElementOffset(BType Type, String FieldName) {
		@Var String ClassName = Type.ShortName;
		@Var BunClassNode ClassNode = this.ClassFieldMap.get(ClassName);
		if(ClassNode != null) {
			@Var int Size = ClassNode.GetListSize();
			@Var int i = 0;
			while(i < Size) {
				if(ClassNode.GetFieldNode(i).GetGivenName().equals(FieldName)) {
					@Var int Offset = i + this.GetClassFieldSize(Type.RefType);
					this.CurrentScope.PushValue("i32 " + Offset);
					return;
				}
				i = i + 1;
			}
		}
		if(Type.RefType != null) {
			this.GetObjectElementOffset(Type.RefType, FieldName);
			return;
		}
		this.CurrentScope.PushValue("i32 -1");
	}
	private int GetClassFieldSize(BType Type) {
		if(Type != null) {
			@Var String ClassName = Type.ShortName;
			@Var BunClassNode ClassNode = this.ClassFieldMap.get(ClassName);
			if(ClassNode != null) {
				return ClassNode.GetListSize() + this.GetClassFieldSize(Type.RefType);
			}
		}
		return 1/*Element size of object header*/;
	}

	private void AppendDefaultReturn(BType ReturnType) {
		this.Source.AppendNewLine("ret ");
		if(!ReturnType.IsVoidType()) {
			this.Source.Append(this.GetTypeExpr(ReturnType));
			this.Source.Append(" ");
			if(ReturnType.IsFloatType()) {
				this.Source.Append("" + 0.0);
			}
			else if(ReturnType.IsBooleanType()) {
				this.Source.Append(this.FalseLiteral);
			}
			else {
				this.Source.Append("" + 0);
			}
		}
		else {
			this.Source.Append("void");
		}

	}
}
