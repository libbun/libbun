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

package libbun.encode.release;

import libbun.ast.AstNode;
import libbun.ast.BlockNode;
import libbun.ast.GroupNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.ast.binary.BunAddNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BunBitwiseAndNode;
import libbun.ast.binary.BunBitwiseOrNode;
import libbun.ast.binary.BunBitwiseXorNode;
import libbun.ast.binary.BunDivNode;
import libbun.ast.binary.BunEqualsNode;
import libbun.ast.binary.BunGreaterThanEqualsNode;
import libbun.ast.binary.BunGreaterThanNode;
import libbun.ast.binary.BunInstanceOfNode;
import libbun.ast.binary.BunLeftShiftNode;
import libbun.ast.binary.BunLessThanEqualsNode;
import libbun.ast.binary.BunLessThanNode;
import libbun.ast.binary.BunModNode;
import libbun.ast.binary.BunMulNode;
import libbun.ast.binary.BunNotEqualsNode;
import libbun.ast.binary.BunOrNode;
import libbun.ast.binary.BunRightShiftNode;
import libbun.ast.binary.BunSubNode;
import libbun.ast.decl.BunClassNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.error.ErrorNode;
import libbun.ast.expression.BunFuncNameNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunBooleanNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunMapEntryNode;
import libbun.ast.literal.BunMapNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunComplementNode;
import libbun.ast.unary.BunMinusNode;
import libbun.ast.unary.BunNotNode;
import libbun.ast.unary.BunPlusNode;
import libbun.ast.unary.UnaryOperatorNode;
import libbun.common.CommonArray;
import libbun.common.CommonMap;
import libbun.encode.LibBunSourceBuilder;
import libbun.encode.LibBunSourceGenerator;
import libbun.parser.classic.LibBunLangInfo;
import libbun.parser.common.BunLogger;
import libbun.type.BClassType;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;
import libbun.util.ZenMethod;

public class CSharpGenerator extends LibBunSourceGenerator {

	@BField private final CommonArray<BunFunctionNode> ExportFunctionList = new CommonArray<BunFunctionNode>(new BunFunctionNode[4]);
	@BField private final static String NameSpaceName = "LibBunGenerated";
	@BField private final static String MainClassName = "LibBunMain";
	@BField private boolean IsMainClassOpened = false;

	public CSharpGenerator() {
		this("C#-5.0");
		this.SetNativeType(BType.BooleanType, "bool");
		this.SetNativeType(BType.IntType, "long");
		this.SetNativeType(BType.FloatType, "double");
		this.SetNativeType(BType.StringType, "string");
		this.SetNativeType(BType.VarType, "dynamic");
	}

	protected CSharpGenerator(String LangVersion) {
		super(new LibBunLangInfo(LangVersion, "cs"));
		this.LoadInlineLibrary("inline.cs", "//");
		this.SetReservedName("this", "@this");
		this.ImportLibrary("System");
		this.ImportLibrary("System.Diagnostics");
		this.ImportLibrary("System.Linq");
		this.ImportLibrary("System.Text");
		this.Source.AppendNewLine("namespace ", CSharpGenerator.NameSpaceName);
		this.Source.OpenIndent(" {");
	}

	private void OpenMainClass(){
		if(!this.IsMainClassOpened){
			this.Source.AppendNewLine("public static partial class ", CSharpGenerator.MainClassName, " {");
			this.Source.OpenIndent();
			this.IsMainClassOpened = true;
		}
	}

	private void CloseMainClass(){
		if(this.IsMainClassOpened){
			this.Source.CloseIndent("}");
			this.IsMainClassOpened = false;
		}
	}

	@Override protected void GenerateImportLibrary(String LibName) {
		this.Header.AppendNewLine("using ", LibName, ";");
	}

	@Override @ZenMethod protected void Finish(String FileName) {
		this.CloseMainClass();
		this.Source.CloseIndent("}"); // end of namespace
		this.Source.AppendLineFeed();
	}

	@Override public void VisitArrayLiteralNode(BunArrayNode Node) {
		if(Node.size() == 0) {
			this.AppendCSharpTypeName("new ", Node.Type, "()");
		}
		else {
			this.ImportLibrary("System.Collections.Generic");
			this.AppendCSharpTypeName("new ", Node.Type, "");
			if(Node.size() > 0) {
				this.GenerateListNode("{ ", Node, 0, ", ", " }");
			}else{
				this.Source.Append("()");
			}
		}
	}

	@Override public void VisitMapLiteralNode(BunMapNode Node) {
		this.AppendCSharpTypeName("new ", Node.Type, "");
		if(Node.GetListSize() > 0) {
			@Var int i = 0;
			this.Source.OpenIndent(" {");
			while(i < Node.GetListSize()) {
				@Var BunMapEntryNode Entry = Node.getMapEntryNode(i);
				this.Source.AppendNewLine("{");
				this.GenerateExpression("", Entry.KeyNode(), ", ", Entry.ValueNode(), "},");
				i = i + 1;
			}
			this.Source.CloseIndent("}");
		}else{
			this.Source.Append("()");
		}
	}

	@Override public void VisitNewObjectNode(NewObjectNode Node) {
		this.Source.Append("new " + this.NameClass(Node.Type));
		this.GenerateListNode("(", Node, ", ", ")");
	}

	@Override public void VisitGetIndexNode(GetIndexNode Node) {
		this.GenerateExpression(Node.RecvNode());
		if(Node.RecvNode().Type == BType.StringType){
			this.GenerateExpression(".Substring(", Node.IndexNode(), ", 1)");
		}else if(Node.IndexNode().Type == BType.IntType){
			this.GenerateExpression("[(int)", Node.IndexNode(), "]");
		}else{
			this.GenerateExpression("[", Node.IndexNode(), "]");
		}
	}

	@Override public void VisitFuncCallNode(FuncCallNode Node) {
		@Var BunFuncNameNode FuncNameNode = Node.FuncNameNode();
		if(FuncNameNode != null) {
			this.Source.Append((Node.FuncNameNode().FuncName));
		}
		else {
			this.GenerateExpression(Node.FunctorNode());
		}
		this.GenerateListNode("(", Node, ", ", ")");
	}

	@Override public void VisitMethodCallNode(MethodCallNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(".");
		this.Source.Append(Node.MethodName());
		this.GenerateListNode("(", Node, ", ", ")");
	}

	@Override public void VisitThrowNode(BunThrowNode Node) {
		this.ImportLibrary("@Throw");
		this.Source.Append("Lib.Throw(");
		this.GenerateExpression(Node.ExprNode());
		this.Source.Append(")");
	}

	@Override public void VisitTryNode(BunTryNode Node) {
		this.Source.Append("try ");
		this.GenerateExpression(Node.TryblockNode());
		if(Node.HasCatchblockNode()) {
			this.ImportLibrary("@SoftwareFault");
			@Var String VarName = Node.ExceptionName();
			this.Source.AppendNewLine("catch (Exception ", VarName, ")");
			this.GenerateExpression(Node.CatchblockNode());
		}
		if(Node.HasFinallyblockNode()) {
			this.Source.AppendNewLine("finally ");
			this.GenerateExpression(Node.FinallyblockNode());
		}
	}

	protected final void AppendCSharpTypeName(String Prefix, BType Type, String Suffix) {
		this.Source.Append(Prefix);
		this.GenerateTypeName(Type);
		this.Source.Append(Suffix);
	}

	@Override
	protected void GenerateTypeName(BType Type) {
		if(Type.IsArrayType()) {
			this.ImportLibrary("System.Collections.Generic");
			this.AppendCSharpTypeName("List<", Type.GetParamType(0), ">");
		}
		else if(Type.IsMapType()) {
			this.ImportLibrary("System.Collections.Generic");
			this.AppendCSharpTypeName("Dictionary<string, ", Type.GetParamType(0), ">");
		}
		else if(Type instanceof BFuncType) {
			this.AppendFuncTypeClass((BFuncType)Type);
		}
		else if(Type instanceof BClassType) {
			this.Source.Append(this.NameClass(Type));
		}
		else{
			String name = this.GetNativeTypeName(Type);
			if(name.equals("return")){
				this.Source.Append("void");
			}else{
				this.Source.Append(name);
			}
		}
	}


	@BField private final CommonMap<String> FuncNameMap = new CommonMap<String>(null);

	void AppendFuncTypeClass(BFuncType FuncType) {
		@Var String ClassName = this.FuncNameMap.GetOrNull(FuncType.GetUniqueName());
		if(ClassName == null){
			@Var LibBunSourceBuilder MainBuilder = this.Source;
			this.Source = new LibBunSourceBuilder(this, null);
			@Var boolean HasReturnValue = !FuncType.GetReturnType().equals(BType.VoidType);
			if(HasReturnValue){
				this.Source.Append("Func");
			}else{
				this.Source.Append("Action");
			}
			this.Source.Append("<");
			@Var int i = 0;
			while(i < FuncType.GetFuncParamSize()) {
				if(i > 0) {
					this.Source.Append(", ");
				}
				this.GenerateTypeName(FuncType.GetFuncParamType(i));
				i = i + 1;
			}
			if(HasReturnValue){
				this.Source.Append(", ");
				this.GenerateTypeName(FuncType.GetReturnType());
			}
			this.Source.Append(">");
			ClassName = this.Source.toString();
			this.Source = MainBuilder;
			this.FuncNameMap.put(FuncType.GetUniqueName(), ClassName);
		}
		this.Source.Append(ClassName);
	}

	@Override public void VisitFunctionNode(BunFunctionNode Node) {
		@Var boolean IsLambda = (Node.FuncName() == null);
		if(IsLambda){
			this.GenerateLambdaFunction(Node);
			return;
		}
		if(!Node.Type.IsVoidType()) {
			@Var String FuncName = Node.GetUniqueName(this);
			this.Source = this.InsertNewSourceBuilder();
			FuncName = this.GenerateFunctionAsClass(FuncName, Node);
			this.Source.AppendLineFeed();
			this.Source = this.Source.Pop();
			this.Source.Append(FuncName);
		}
		else {
			this.GenerateFunctionAsClass(Node.FuncName(), Node);
			if(Node.IsExport()) {
				if(Node.FuncName().equals("main")) {
					this.ImportLibrary("@main");
				}
				else {
					this.ExportFunctionList.add(Node);
				}
			}
		}
	}

	private void GenerateLambdaFunction(BunFunctionNode Node){
		this.GenerateListNode("(", Node.ParamNode(), ", ", ") => ");
		if(Node.blockNode().GetListSize() == 1){
			@Var AstNode FirstNode = Node.blockNode().GetListAt(0);
			if(FirstNode instanceof BunReturnNode){
				this.GenerateExpression(((BunReturnNode)FirstNode).ExprNode());
				return;
			}
		}
		this.GenerateExpression(Node.blockNode());
	}

	private String GenerateFunctionAsClass(String FuncName, BunFunctionNode Node) {
		@Var BunLetVarNode FirstParam = Node.getParamSize() == 0 ? null : (BunLetVarNode)Node.GetParamNode(0);
		@Var boolean IsInstanceMethod = FirstParam != null && FirstParam.GetGivenName().equals("this");

		this.OpenMainClass();
		this.Source.AppendNewLine("public static ");
		this.GenerateTypeName(Node.ReturnType());
		this.Source.Append(" ");
		this.Source.Append(FuncName);
		if(IsInstanceMethod){
			this.GenerateListNode("(this ", Node.ParamNode(), ", ", ")");
		}else{
			this.GenerateListNode("(", Node.ParamNode(), ", ", ")");
		}

		this.GenerateExpression(Node.blockNode());

		return FuncName;
	}

	@Override public void VisitInstanceOfNode(BunInstanceOfNode Node) {
		this.GenerateExpression(Node.AST[BunInstanceOfNode._Left]);
		this.Source.Append(" is ");
		this.GenerateTypeName(Node.TargetType());
	}

	private void GenerateClass(String Qualifier, String ClassName, BType SuperType) {
		if(Qualifier != null && Qualifier.length() > 0) {
			this.Source.AppendNewLine(Qualifier);
			this.Source.AppendWhiteSpace("partial class ", ClassName);
		}
		else {
			this.Source.AppendNewLine("partial class ", ClassName);
		}
		if(SuperType != null && !SuperType.Equals(BClassType._ObjectType) && !SuperType.IsFuncType()) {
			this.Source.Append(" : ");
			this.GenerateTypeName(SuperType);
		}
	}

	private void GenerateClassField(String Qualifier, BType FieldType, String FieldName, String Value) {
		if(Qualifier.length() > 1){
			this.Source.AppendNewLine(Qualifier);
			this.Source.Append("public ");
		}else{
			this.Source.AppendNewLine("public ");
		}
		this.GenerateTypeName(FieldType);
		this.Source.Append(" ", FieldName);
		if(Value != null) {
			this.Source.Append(" = ", Value, ";");
		}
	}

	@Override public void VisitClassNode(BunClassNode Node) {
		@Var BType SuperType = Node.ClassType.GetSuperType();
		@Var String ClassName = this.NameClass(Node.ClassType);
		this.CloseMainClass();
		this.GenerateClass("public", ClassName, SuperType);
		this.Source.OpenIndent(" {");
		@Var int i = 0;
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.GenerateClassField("", FieldNode.DeclType(), FieldNode.GetGivenName(), null);
			this.Source.Append(";");
			i = i + 1;
		}
		this.Source.AppendNewLine();

		i = 0;

		this.Source.AppendNewLine("public ", this.NameClass(Node.ClassType), "()");
		this.Source.OpenIndent(" {");
		//this.CurrentBuilder.AppendNewLine("super();");
		while (i < Node.GetListSize()) {
			@Var BunLetVarNode FieldNode = Node.GetFieldNode(i);
			this.Source.AppendNewLine("this.", FieldNode.GetGivenName(), "=");
			this.GenerateExpression(FieldNode.InitValueNode());
			this.Source.Append(";");
			i = i + 1;
		}
		i = 0;

		this.Source.CloseIndent("}"); /* end of constructor*/
		this.Source.CloseIndent("}"); /* end of class */
	}

	@Override public void VisitErrorNode(ErrorNode Node) {
		BunLogger._LogError(Node.SourceToken, Node.ErrorMessage);
		this.ImportLibrary("@Error");
		this.Source.Append("Lib.Error(");
		this.Source.Append(LibBunSystem._QuoteString(Node.ErrorMessage));
		this.Source.Append(")");
	}

	@Override
	protected void GenerateStatementEnd(AstNode Node) {
		if(!this.Source.EndsWith(';') && !this.Source.EndsWith('}')){
			this.Source.Append(";");
		}
	}

	@Override
	public void VisitNullNode(BunNullNode Node) {
		this.Source.Append("null");
	}

	@Override
	public void VisitBooleanNode(BunBooleanNode Node) {
		this.Source.Append(Node.BooleanValue ? "true" : "false");
	}

	@Override
	public void VisitIntNode(BunIntNode Node) {
		this.Source.Append(""+Node.IntValue);
	}

	@Override
	public void VisitFloatNode(BunFloatNode Node) {
		this.Source.Append(""+Node.FloatValue);
	}

	@Override
	public void VisitStringNode(BunStringNode Node) {
		this.Source.AppendQuotedText(Node.StringValue);
	}

	@Override public void VisitNotNode(BunNotNode Node) {
		this.Source.Append("!(");
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(")");
	}

	@Override public void VisitPlusNode(BunPlusNode Node) {
		this.Source.Append("+");
		this.GenerateExpression(Node.RecvNode());
	}

	@Override public void VisitMinusNode(BunMinusNode Node) {
		this.Source.Append("-");
		this.GenerateExpression(Node.RecvNode());
	}

	@Override public void VisitComplementNode(BunComplementNode Node) {
		this.Source.Append("~");
		this.GenerateExpression(Node.RecvNode());
	}

	private void GenerateBinaryOperatorExpression(BinaryOperatorNode Node, String Operator) {
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.Source.Append("(");
		}
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" ", Operator, " ");
		this.GenerateExpression(Node.RightNode());
		if (Node.ParentNode instanceof BinaryOperatorNode) {
			this.Source.Append(")");
		}
	}

	@Override public void VisitAndNode(BunAndNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "&&");
	}

	@Override public void VisitOrNode(BunOrNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "||");
	}

	@Override public void VisitAddNode(BunAddNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "+");
	}

	@Override public void VisitSubNode(BunSubNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "-");
	}

	@Override public void VisitMulNode(BunMulNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "*");
	}

	@Override public void VisitDivNode(BunDivNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "/");
	}

	@Override public void VisitModNode(BunModNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "%");
	}

	@Override public void VisitLeftShiftNode(BunLeftShiftNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "<<");
	}

	@Override public void VisitRightShiftNode(BunRightShiftNode Node) {
		this.GenerateBinaryOperatorExpression(Node, ">>");
	}

	@Override public void VisitBitwiseAndNode(BunBitwiseAndNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "&");
	}

	@Override public void VisitBitwiseOrNode(BunBitwiseOrNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "|");
	}

	@Override public void VisitBitwiseXorNode(BunBitwiseXorNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "^");
	}

	@Override public void VisitEqualsNode(BunEqualsNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "==");
	}

	@Override public void VisitNotEqualsNode(BunNotEqualsNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "!=");
	}

	@Override public void VisitLessThanNode(BunLessThanNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "<");
	}

	@Override public void VisitLessThanEqualsNode(BunLessThanEqualsNode Node) {
		this.GenerateBinaryOperatorExpression(Node, "<=");
	}

	@Override public void VisitGreaterThanNode(BunGreaterThanNode Node) {
		this.GenerateBinaryOperatorExpression(Node, ">");
	}

	@Override public void VisitGreaterThanEqualsNode(BunGreaterThanEqualsNode Node) {
		this.GenerateBinaryOperatorExpression(Node, ">=");
	}

	@Override public void VisitGroupNode(GroupNode Node) {
		this.GenerateExpression("(", Node.ExprNode(), ")");
	}


	private void VisitStmtList(BlockNode blockNode) {
		@Var int i = 0;
		while (i < blockNode.GetListSize()) {
			@Var AstNode SubNode = blockNode.GetListAt(i);
			this.GenerateStatement(SubNode);
			i = i + 1;
		}
	}

	@Override public void VisitBlockNode(BlockNode Node) {
		this.Source.OpenIndent("{");
		this.VisitStmtList(Node);
		this.Source.CloseIndent("}");
	}

	protected void VisitLocalVariableDecl(BunLetVarNode Node){
		if(Node.DeclType().IsFuncType()){
			this.GenerateTypeName(Node.DeclType());
		}else{
			this.Source.Append("var");
		}
		this.Source.Append(" ", Node.GetUniqueName(this));
		this.GenerateExpression(" = ", Node.InitValueNode(), ";");
	}

	@Override public void VisitLetNode(BunLetVarNode Node) {
		if(Node.IsParamNode()) {
			if(Node.Type.Equals(BType.VarType) && ((BunFunctionNode)Node.ParentNode).FuncName() == null){
				this.Source.Append(Node.GetUniqueName(this));
			}else{
				this.GenerateTypeName(Node.DeclType());
				this.Source.Append(" ", Node.GetUniqueName(this));
			}
		}
		else if(Node.isTopLevel()) {
			this.OpenMainClass();
			this.Source.AppendNewLine("public static ");
			this.GenerateTypeName(Node.getTypeAt(BunLetVarNode._InitValue));
			this.Source.Append(" ", Node.GetUniqueName(this), " = ");
			this.GenerateExpression(Node.InitValueNode());
			this.Source.Append(";");
		}
		else {
			this.VisitLocalVariableDecl(Node);
		}
	}

	@Override
	public void VisitVarblockNode(BunVarBlockNode Node) {
		@Var BunLetVarNode VarNode = Node.VarDeclNode();
		this.VisitLocalVariableDecl(VarNode);
		this.VisitStmtList(Node);
	}

	@Override
	public void VisitIfNode(BunIfNode Node) {
		this.GenerateExpression("if (", Node.CondNode(), ")");
		this.GenerateExpression(Node.ThenNode());
		if (Node.HasElseNode()) {
			this.Source.AppendNewLine("else ");
			this.GenerateExpression(Node.ElseNode());
		}
	}

	@Override
	public void VisitReturnNode(BunReturnNode Node) {
		this.Source.Append("return");
		if (Node.HasReturnExpr()) {
			this.Source.Append(" ");
			this.GenerateExpression(Node.ExprNode());
		}
	}

	@Override
	public void VisitWhileNode(BunWhileNode Node) {
		this.GenerateExpression("while (", Node.CondNode(), ")");
		if(Node.HasNextNode()) {
			Node.blockNode().appendNode(Node.NextNode());
		}
		this.GenerateExpression(Node.blockNode());
	}

	@Override
	public void VisitBreakNode(BunBreakNode Node) {
		this.Source.Append("break");
	}

	@Override public void VisitGetNameNode(GetNameNode Node) {
		@Var AstNode ResolvedNode = Node.ResolvedNode;
		if(ResolvedNode == null && !this.LangInfo.AllowUndefinedSymbol) {
			BunLogger._LogError(Node.SourceToken, "undefined symbol: " + Node.GivenName);
		}
		this.Source.Append(Node.GetUniqueName(this));
	}

	@Override public void VisitGetFieldNode(GetFieldNode Node) {
		this.GenerateExpression(Node.RecvNode());
		this.Source.Append(".", Node.GetName());
	}

	@Override
	public void VisitUnaryNode(UnaryOperatorNode Node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void VisitCastNode(BunCastNode Node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void VisitBinaryNode(BinaryOperatorNode Node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void VisitAssignNode(AssignNode Node) {
		this.GenerateExpression(Node.LeftNode());
		this.Source.Append(" = ");
		this.GenerateExpression(Node.RightNode());
	}


}
