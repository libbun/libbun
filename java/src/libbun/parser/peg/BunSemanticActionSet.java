// ***************************************************************************
// Copyright (c) 2013-2014, Libbun project authors. All rights reserved.
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


package libbun.parser.peg;
import libbun.ast.BNode;
import libbun.ast.BunBlockNode;
import libbun.ast.GroupNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.binary.BunAddNode;
import libbun.ast.binary.BunAndNode;
import libbun.ast.binary.BunBitwiseAndNode;
import libbun.ast.binary.BunBitwiseOrNode;
import libbun.ast.binary.BunBitwiseXorNode;
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
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunRequireNode;
import libbun.ast.expression.FuncCallNode;
import libbun.ast.expression.GetFieldNode;
import libbun.ast.expression.GetIndexNode;
import libbun.ast.expression.GetNameNode;
import libbun.ast.expression.MethodCallNode;
import libbun.ast.expression.NewObjectNode;
import libbun.ast.literal.BunArrayNode;
import libbun.ast.literal.BunAsmNode;
import libbun.ast.literal.BunFalseNode;
import libbun.ast.literal.BunFloatNode;
import libbun.ast.literal.BunIntNode;
import libbun.ast.literal.BunNullNode;
import libbun.ast.literal.BunStringNode;
import libbun.ast.literal.BunTrueNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.ast.statement.BunTryNode;
import libbun.ast.statement.BunWhileNode;
import libbun.ast.sugar.BunAssertNode;
import libbun.ast.unary.BunCastNode;
import libbun.ast.unary.BunComplementNode;
import libbun.ast.unary.BunMinusNode;
import libbun.ast.unary.BunNotNode;
import libbun.ast.unary.BunPlusNode;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;
import libbun.type.BType;
import libbun.util.LibBunSystem;
import libbun.util.Var;

// Syntax

class NullFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		BNode node = new BunNullNode(parentNode);
		node.SourceToken = po.getToken();
		return node;

	}
}

class TrueFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		BNode node = new BunTrueNode(parentNode);
		node.SourceToken = po.getToken();
		return node;
	}
}

class FalseFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		BNode node = new BunFalseNode(parentNode);
		node.SourceToken = po.getToken();
		return node;
	}
}

class StringFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		@Var BunToken token = po.getToken();
		BNode node = new BunStringNode(parentNode, LibBunSystem._UnquoteString(token.GetText()));
		node.SourceToken = token;
		return node;
	}
}


class IntFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		@Var BunToken token = po.getToken();
		BNode node = new BunIntNode(parentNode, LibBunSystem._ParseInt(token.GetText()));
		node.SourceToken = token;
		return node;
	}
}

class FloatFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		@Var BunToken token = po.getToken();
		BNode node = new BunFloatNode(parentNode, LibBunSystem._ParseFloat(token.GetText()));
		node.SourceToken = token;
		return node;

	}
}

//class StringInterpolationTokenFunction extends BTokenFunction {
//	@Override public boolean Invoke(BSourceContext SourceContext) {
//		int StartIndex = SourceContext.GetPosition();
//		while(SourceContext.HasChar()) {
//			@Var char ch = SourceContext.GetCurrentChar();
//			if(ch == '$' && SourceContext.GetCharAtFromCurrentPosition(+1) == '{') {
//				SourceContext.Tokenize(StartIndex, SourceContext.GetPosition() + 2);
//				return true;
//			}
//			SourceContext.MoveNext();
//		}
//		SourceContext.Tokenize(StartIndex, SourceContext.GetPosition());
//		return true;
//	}
//}
//
//class StringInterpolationFunction extends SemanticFunction {
//	public final static BTokenFunction StringInterpolationToken = new StringInterpolationTokenFunction();
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var StringInterpolationNode FormatNode = new StringInterpolationNode(parentNode);
//		@Var BToken Token = po.getToken();
//		TokenContext = TokenContext.SubContext(Token.StartIndex + 1, Token.EndIndex - 1);
//		while(true) {
//			@Var BToken SubToken = TokenContext.ParseTokenBy(StringInterpolationToken);
//			if(SubToken == null) {
//				FormatNode.Append(new BunStringNode(FormatNode, Token, ""));
//				break;
//			}
//			if(SubToken.EndsWith("${")) {
//				SubToken.EndIndex = SubToken.EndIndex - 2;
//				if(SubToken.size() == 0) {
//					FormatNode.Append(new BunStringNode(FormatNode, Token, ""));
//				}
//				else {
//					FormatNode.Append(new BunStringNode(FormatNode, SubToken, LibBunSystem._UnquoteString(SubToken.GetText())));
//				}
//				//				SubToken = TokenContext.GetToken();
//				//System.out.println("#before: ###" + SubToken.GetText()+"###");
//				@Var BNode SubNode = TokenContext.Parse(FormatNode, "$Expression$", BTokenContext._Required);
//				if(SubNode.IsErrorNode()) {
//					return SubNode;
//				}
//				SubToken = TokenContext.GetToken();
//				//System.out.println("#node: " + SubNode);
//				//System.out.println("#after: ###" + SubToken.GetText()+"###");
//				if(!SubToken.EqualsText('}')) {
//					return new ErrorNode(parentNode, SubToken, "syntax error in string interpolation");
//				}
//				FormatNode.Append(SubNode);
//			}
//			else {
//				FormatNode.Append(new BunStringNode(FormatNode, SubToken, LibBunSystem._UnquoteString(SubToken.GetText())));
//				break;
//			}
//		}
//		return FormatNode;
//	}
//}

class SymbolFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		@Var BunToken token = po.getToken();
		return new GetNameNode(parentNode, token, token.GetText());
	}
}

class BunNotFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunNotNode(parentNode));
	}
}

class BunPlusFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunPlusNode(parentNode));
	}
}
class BunMinusFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunMinusNode(parentNode));
	}
}
class BunComplementFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunComplementNode(parentNode));
	}
}


class BunAndFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunAndNode(parentNode));
	}
}

class BunOrFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunOrNode(parentNode));
	}
}

class BunAddFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunAddNode(parentNode));
	}
}

class BunSubFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunSubNode(parentNode));
	}
}

class BunMulFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunMulNode(parentNode));
	}
}

class BunDivFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunSubNode(parentNode));
	}
}

class BunModFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunModNode(parentNode));
	}
}

class BunBitwiseAndFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunBitwiseAndNode(parentNode));
	}
}

class BunBitwiseOrFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunBitwiseOrNode(parentNode));
	}
}

class BunBitwiseXorFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunBitwiseXorNode(parentNode));
	}
}

class BunLeftShiftFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLeftShiftNode(parentNode));
	}
}

class BunRightShiftFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunRightShiftNode(parentNode));
	}
}

class BunEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunEqualsNode(parentNode));
	}
}

class BunNotEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunNotEqualsNode(parentNode));
	}
}

class BunLessThanFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLessThanNode(parentNode));
	}
}

class BunLessThanEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLessThanEqualsNode(parentNode));
	}
}

class BunGreaterThanFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunGreaterThanNode(parentNode));
	}
}

class BunGreaterThanEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunGreaterThanEqualsNode(parentNode));
	}
}

class InstanceOfFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunInstanceOfNode(parentNode));
	}
}

class AssignFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new AssignNode(parentNode));
	}
}

class RaAndFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunAndNode(parentNode));
	}
}

class RaOrFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunOrNode(parentNode));
	}
}

class RaAddFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunAddNode(parentNode));
	}
}

class RaSubFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunSubNode(parentNode));
	}
}

class RaMulFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunMulNode(parentNode));
	}
}

class RaDivFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunSubNode(parentNode));
	}
}

class RaModFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunModNode(parentNode));
	}
}

class RaBitwiseAndFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunBitwiseAndNode(parentNode));
	}
}

class RaBitwiseOrFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunBitwiseOrNode(parentNode));
	}
}

class RaBitwiseXorFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunBitwiseXorNode(parentNode));
	}
}

class RaLeftShiftFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunLeftShiftNode(parentNode));
	}
}

class RaRightShiftFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunRightShiftNode(parentNode));
	}
}

class RaEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunEqualsNode(parentNode));
	}
}

class RaNotEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunNotEqualsNode(parentNode));
	}
}

class RaLessThanFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunLessThanNode(parentNode));
	}
}

class RaLessThanEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunLessThanEqualsNode(parentNode));
	}
}

class RaGreaterThanFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunGreaterThanNode(parentNode));
	}
}

class RaGreaterThanEqualsFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunGreaterThanEqualsNode(parentNode));
	}
}

class RaInstanceOfFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new BunInstanceOfNode(parentNode));
	}
}

class RaAssignFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copyBinaryAsRightAssoc(source, new AssignNode(parentNode));
	}
}

//
//class DefinedTypeFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var BToken Token = po.getToken();
//		@Var BType Type = parentNode.Getp().GetType(Token.GetText(), Token, false/*IsCreation*/);
//		if(Type != null) {
//			@Var BunTypeNode TypeNode = new BunTypeNode(parentNode, Token, Type);
//			return TokenContext.ParseAfter(parentNode, TypeNode, "$TypeRight$", BTokenContext._Optional);
//		}
//		return null;
//	}
//}
//
//class OpenTypeFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var BToken MaybeToken   = null;
//		@Var BToken MutableToken = null;
//		if(TokenContext.IsToken("maybe")) {
//			MaybeToken   = po.getToken();
//		}
//		if(TokenContext.MatchToken("mutable")) {
//			MutableToken   = po.getToken();
//		}
//		@Var BToken Token = po.getToken();
//		@Var BType Type = parentNode.Getp().GetType(Token.GetText(), Token, true/*IsCreation*/);
//		if(Type != null) {
//			@Var BunTypeNode TypeNode = new BunTypeNode(parentNode, Token, Type);
//			@Var BNode Node = TokenContext.ParseAfter(parentNode, TypeNode, "$TypeRight$", BTokenContext._Optional);
//			if(Node instanceof BunTypeNode) {
//				@Var LibBunTypeChecker p = parentNode.Getp().Generator.TypeChecker;
//				if(MutableToken != null) {
//					Node.Type = BTypePool._LookupMutableType(p, Node.Type, MutableToken);
//				}
//				if(MaybeToken != null) {
//					Node.Type = BTypePool._LookupNullableType(p, Node.Type, MaybeToken);
//				}
//			}
//			return Node;
//		}
//		return null; // Not Matched
//	}
//}
//
//class RightTypeFunction extends SemanticFunction {
//	@Override public BNode Invoke(BNode parentNode, BTokenContext TokenContext, BNode LeftTypeNode) {
//		@Var BToken SourceToken = TokenContext.GetToken();
//		if(LeftTypeNode.Type.GetParamSize() > 0) {
//			if(TokenContext.MatchToken("<")) {  // Generics
//				@Var BArray<BType> TypeList = new BArray<BType>(new BType[4]);
//				while(!TokenContext.StartsWithToken(">")) {
//					if(TypeList.size() > 0 && !TokenContext.MatchToken(",")) {
//						return null;
//					}
//					@Var BunTypeNode ParamTypeNode = (BunTypeNode) TokenContext.Parse(parentNode, "$OpenType$", BTokenContext._Optional);
//					if(ParamTypeNode == null) {
//						return LeftTypeNode;
//					}
//					TypeList.add(ParamTypeNode.Type);
//				}
//				LeftTypeNode = new BunTypeNode(parentNode, SourceToken, BTypePool._GetGenericType(LeftTypeNode.Type, TypeList, true));
//			}
//		}
//		while(TokenContext.MatchToken("[")) {  // Array
//			if(!TokenContext.MatchToken("]")) {
//				return null;
//			}
//			LeftTypeNode = new BunTypeNode(parentNode, SourceToken, BTypePool._GetGenericType1(BGenericType._ArrayType, LeftTypeNode.Type));
//		}
//		return LeftTypeNode;
//	}
//}
//
//class TypeAnnotationFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		if(TokenContext.MatchToken(":")) {
//			return TokenContext.Parse(parentNode, "$OpenType$", BTokenContext._Required);
//		}
//		return null;
//	}
//}

class GetFieldFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new GetFieldNode(parentNode));
	}
}

class MethodCallFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new MethodCallNode(parentNode, null, null));
	}
}

class GroupFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new GroupNode(parentNode));
	}
}

class CastFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		BunCastNode castNode = new BunCastNode(parentNode, BType.VarType, null);
		po.copySubTo(source, castNode);
		castNode.CastType();  // due to old implementation that cannot be fixed easily.
		return castNode;
	}
}

class FuncCallFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new FuncCallNode(parentNode, null));
	}
}

class GetIndexFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new GetIndexNode(parentNode, null));
	}
}

class ArrayLiteralFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunArrayNode(parentNode));
	}
}

class NewObjectFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new NewObjectNode(parentNode));
	}
}

class MapLiteralFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		//		return po
		//		@Var BNode LiteralNode = new BunMapEntryNode(parentNode, null);
		//		LiteralNode = TokenContext.Match(LiteralNode, BunMapEntryNode._Key, "$Expression$", BTokenContext._Required);
		//		LiteralNode = TokenContext.MatchToken(LiteralNode, ":", BTokenContext._Required);
		//		LiteralNode = TokenContext.Match(LiteralNode, BunMapEntryNode._Value, "$Expression$", BTokenContext._Required);
		//		return LiteralNode;
		//		return po.
		//		@Var BNode LiteralNode = new BunMapLiteralNode(parentNode);
		//		LiteralNode = TokenContext.MatchNtimes(LiteralNode, "{", "$MapEntry$", ",", "}");
		//		return LiteralNode;
		return null; // TODO
	}
}

class BlockFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunBlockNode(parentNode, null));
	}
}

class AnnotationFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		// TODO Auto-generated method stub
		return null;
	}
}


class IfFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunIfNode(parentNode));
	}
}

class WhileFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunWhileNode(parentNode));
	}
}

class BreakFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunBreakNode(parentNode));
	}
}

class ReturnFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunReturnNode(parentNode));
	}
}

class TryFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunTryNode(parentNode));
	}
}

class ThrowFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunThrowNode(parentNode));
	}
}

class VarFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLetVarNode(parentNode, 0, null, null));
	}
}

class LetFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLetVarNode(parentNode, BunLetVarNode._IsReadOnly, null, null));
	}
}

class ParamFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunLetVarNode(parentNode, BunLetVarNode._IsReadOnly, null, null));
	}
}

class FunctionFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunFunctionNode(parentNode, 0));
	}
}


//class ExportFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var BToken NameToken = po.getToken();
//		@Var BNode Node = TokenContext.Parse(parentNode, "function", BTokenContext._Optional);
//		if(Node instanceof BunFunctionNode) {
//			((BunFunctionNode)Node).IsExport = true;
//			return Node;
//		}
//		Node = TokenContext.Parse(parentNode, "let", BTokenContext._Optional);
//		if(Node instanceof BunLetVarNode) {
//			((BunLetVarNode)Node).NameFlag = ((BunLetVarNode)Node).NameFlag | BunLetVarNode._IsExport;
//			return Node;
//		}
//		Node = TokenContext.Parse(parentNode, "class", BTokenContext._Optional);
//		if(Node instanceof BunClassNode) {
//			((BunClassNode)Node).IsExport = true;
//			return Node;
//		}
//		return new ErrorNode(parentNode, NameToken, "export function, class, or let");
//	}
//}
//
//class ImportFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var BToken NameToken = po.getToken();
//		return new ErrorNode(parentNode, NameToken, "unsupported import");
//	}
//}

//class ClassFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var BNode ClassNode = new BunClassNode(parentNode);
//		ClassNode = TokenContext.MatchToken(ClassNode, "class", BTokenContext._Required);
//		ClassNode = TokenContext.Match(ClassNode, BunClassNode._NameInfo, "$Name$", BTokenContext._Required);
//		if(TokenContext.MatchNewLineToken("extends")) {
//			ClassNode = TokenContext.Match(ClassNode, BunClassNode._TypeInfo, "$OpenType$", BTokenContext._Required);
//		}
//		ClassNode = TokenContext.MatchNtimes(ClassNode, "{", "$FieldDecl$", null, "}");
//		return ClassNode;
//	}
//}
//
//class FieldFunction extends SemanticFunction {
//	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
//		@Var boolean Rememberd = TokenContext.SetParseFlag(false);
//		@Var BNode VarNode = new BunLetVarNode(parentNode, 0, null, null);
//		VarNode = TokenContext.MatchToken(VarNode, "var", BTokenContext._Required);
//		VarNode = TokenContext.Match(VarNode, BunLetVarNode._NameInfo, "$Name$", BTokenContext._Required);
//		VarNode = TokenContext.Match(VarNode, BunLetVarNode._TypeInfo, "$TypeAnnotation$", BTokenContext._Optional);
//		if(TokenContext.MatchToken("=")) {
//			VarNode = TokenContext.Match(VarNode, BunLetVarNode._InitValue, "$Expression$", BTokenContext._Required);
//		}
//		VarNode = TokenContext.Match(VarNode, BNode._Nop, ";", BTokenContext._Required);
//		TokenContext.SetParseFlag(Rememberd);
//		return VarNode;
//	}
//}


class AssertFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunAssertNode(parentNode));
	}
}

class AsmFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunAsmNode(parentNode, null, null, null));
	}
}


class BunDefineFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		//		@Var BNode LetNode = new BunLetVarNode(parentNode, BunLetVarNode._IsReadOnly, null, null);
		//		LetNode = TokenContext.MatchToken(LetNode, "define", BTokenContext._Required);
		//		LetNode = TokenContext.Match(LetNode, BunLetVarNode._NameInfo, "$LongName$", BTokenContext._Required);
		//		LetNode = TokenContext.Match(LetNode, BunLetVarNode._InitValue, "$StringLiteral$", BTokenContext._Required);
		//		LetNode = TokenContext.Match(LetNode, BunLetVarNode._TypeInfo, "$TypeAnnotation$", BTokenContext._Required);
		//		if(LetNode instanceof BunLetVarNode) {
		//			return new BunDefineNode(parentNode, (BunLetVarNode)LetNode);
		//		}
		//		return LetNode;
		return null;
	}
}

class RequireFunction extends SemanticFunction {
	@Override public BNode Invoke(BunSource source, BNode parentNode, PegObject po) {
		return po.copySubTo(source, new BunRequireNode(parentNode));
	}
}

public class BunSemanticActionSet {
	public final static SemanticFunction Null = new NullFunction();
	public final static SemanticFunction True = new TrueFunction();
	public final static SemanticFunction False = new FalseFunction();

	public final static SemanticFunction IntLiteral = new IntFunction();
	public final static SemanticFunction FloatLiteral = new FloatFunction();
	public final static SemanticFunction StringLiteral = new StringFunction();
	//	public final static SemanticFunction StringInterpolation = new StringInterpolationFunction();
	//
	//	public final static SemanticFunction Type = new DefinedTypeFunction();
	//	public final static SemanticFunction OpenType = new OpenTypeFunction();
	//	public final static SemanticFunction TypeSuffix = new RightTypeFunction();
	//	public final static SemanticFunction TypeAnnotation = new TypeAnnotationFunction();

	public final static SemanticFunction Not = new BunNotFunction();
	public final static SemanticFunction Plus = new BunPlusFunction();
	public final static SemanticFunction Minus = new BunMinusFunction();
	public final static SemanticFunction Complement = new BunComplementFunction();

	public final static SemanticFunction Assign = new AssignFunction();

	public final static SemanticFunction Equals = new BunEqualsFunction();
	public final static SemanticFunction NotEquals = new BunNotEqualsFunction();
	public final static SemanticFunction LessThan = new BunLessThanFunction();
	public final static SemanticFunction LessThanEquals = new BunLessThanEqualsFunction();
	public final static SemanticFunction GreaterThan = new BunGreaterThanFunction();
	public final static SemanticFunction GreaterThanEquals = new BunGreaterThanEqualsFunction();

	public final static SemanticFunction And = new BunAndFunction();
	public final static SemanticFunction Or = new BunOrFunction();

	public final static SemanticFunction Add = new BunAddFunction();
	public final static SemanticFunction Sub = new BunSubFunction();
	public final static SemanticFunction Mul = new BunMulFunction();
	public final static SemanticFunction Div = new BunDivFunction();
	public final static SemanticFunction Mod = new BunModFunction();

	public final static SemanticFunction BitwiseAnd = new BunBitwiseAndFunction();
	public final static SemanticFunction BitwiseOr = new BunBitwiseOrFunction();
	public final static SemanticFunction BitwiseXor = new BunBitwiseXorFunction();
	public final static SemanticFunction LeftShift = new BunLeftShiftFunction();
	public final static SemanticFunction RightShift = new BunRightShiftFunction();

	public final static SemanticFunction RaEquals = new RaEqualsFunction();
	public final static SemanticFunction RaNotEquals = new RaNotEqualsFunction();
	public final static SemanticFunction RaLessThan = new RaLessThanFunction();
	public final static SemanticFunction RaLessThanEquals = new RaLessThanEqualsFunction();
	public final static SemanticFunction RaGreaterThan = new RaGreaterThanFunction();
	public final static SemanticFunction RaGreaterThanEquals = new RaGreaterThanEqualsFunction();

	public final static SemanticFunction RaAnd = new RaAndFunction();
	public final static SemanticFunction RaOr = new RaOrFunction();

	public final static SemanticFunction RaAdd = new RaAddFunction();
	public final static SemanticFunction RaSub = new RaSubFunction();
	public final static SemanticFunction RaMul = new RaMulFunction();
	public final static SemanticFunction RaDiv = new RaDivFunction();
	public final static SemanticFunction RaMod = new RaModFunction();

	public final static SemanticFunction RaBitwiseAnd = new RaBitwiseAndFunction();
	public final static SemanticFunction RaBitwiseOr = new RaBitwiseOrFunction();
	public final static SemanticFunction RaBitwiseXor = new RaBitwiseXorFunction();
	public final static SemanticFunction RaLeftShift = new RaLeftShiftFunction();
	public final static SemanticFunction RaRightShift = new RaRightShiftFunction();

	public final static SemanticFunction RaAssign = new RaAssignFunction();
	public final static SemanticFunction RaInstanceOf = new RaInstanceOfFunction();

	public final static SemanticFunction GetField = new GetFieldFunction();
	public final static SemanticFunction MethodCall = new MethodCallFunction();

	public final static SemanticFunction Group = new GroupFunction();
	public final static SemanticFunction Cast = new CastFunction();
	public final static SemanticFunction FuncCall = new FuncCallFunction();

	public final static SemanticFunction GetIndex = new GetIndexFunction();
	public final static SemanticFunction ArrayLiteral = new ArrayLiteralFunction();
	//	public final static SemanticFunction MapEntry = new MapEntryFunction();
	public final static SemanticFunction MapLiteral = new MapLiteralFunction();
	public final static SemanticFunction NewObject = new NewObjectFunction();

	public final static SemanticFunction Block = new BlockFunction();
	public final static SemanticFunction Annotation = new AnnotationFunction();

	public final static SemanticFunction If = new IfFunction();
	public final static SemanticFunction While = new WhileFunction();
	public final static SemanticFunction Break = new BreakFunction();
	public final static SemanticFunction Return = new ReturnFunction();
	public final static SemanticFunction Try = new TryFunction();
	public final static SemanticFunction Throw = new ThrowFunction();

	public final static SemanticFunction Name = new SymbolFunction();
	public final static SemanticFunction Var = new VarFunction();
	public final static SemanticFunction Param = new ParamFunction();
	public final static SemanticFunction Function = new FunctionFunction();
	//	public final static SemanticFunction Prototype = new PrototypeFunction();

	public final static SemanticFunction Let = new LetFunction();
	//	public final static SemanticFunction Export = new ExportFunction();
	//	public final static SemanticFunction Import = new ImportFunction();
	//
	//	public final static SemanticFunction Class = new ClassFunction();
	//	public final static SemanticFunction ClassField = new FieldFunction();

	public final static SemanticFunction Assert = new AssertFunction();
	public final static SemanticFunction Require = new RequireFunction();

	public final static SemanticFunction Asm = new AsmFunction();
	public final static SemanticFunction Define = new BunDefineFunction();

	public static void LoadGrammar(PegParser p) {
		p.setSemanticAction("Null", Null);
		p.setSemanticAction("True", True);
		p.setSemanticAction("False", False);

		p.setSemanticAction("Int", IntLiteral);
		p.setSemanticAction("Float", FloatLiteral);
		p.setSemanticAction("String", StringLiteral);
		p.setSemanticAction("Name", Name);
		p.setSemanticAction("Symbol", Name);


		p.setSemanticAction("Plus", Plus);
		p.setSemanticAction("Minus", Minus);
		p.setSemanticAction("Complement", Complement);
		p.setSemanticAction("Not", Not);
		//		p.AppendSyntax("++ --", new Incl"));

		p.setSemanticAction("Assign", Assign);
		p.setSemanticAction("Equals", Equals);
		p.setSemanticAction("NotEquals", NotEquals);
		p.setSemanticAction("LessThan", LessThan);
		p.setSemanticAction("LessThanEquals", LessThanEquals);
		p.setSemanticAction("GreaterThan", GreaterThan);
		p.setSemanticAction("GreaterThanEquals", GreaterThanEquals);

		p.setSemanticAction("Add", Add);
		p.setSemanticAction("Sub", Sub);
		p.setSemanticAction("Mul", Mul);
		p.setSemanticAction("Div", Div);
		p.setSemanticAction("Mod", Mod);

		p.setSemanticAction("LeftShift", LeftShift);
		p.setSemanticAction("RightShift", RightShift);

		p.setSemanticAction("BitwiseAnd", BitwiseAnd);
		p.setSemanticAction("BitwiseOr", BitwiseOr);
		p.setSemanticAction("bitwiseXor", BitwiseXor);

		p.setSemanticAction("And", And);
		p.setSemanticAction("Or", Or);

		p.setSemanticAction("RaAssign", RaAssign);
		p.setSemanticAction("RaEquals", RaEquals);
		p.setSemanticAction("RaNotEquals", RaNotEquals);
		p.setSemanticAction("RaLessThan", RaLessThan);
		p.setSemanticAction("RaLessThanEquals", RaLessThanEquals);
		p.setSemanticAction("RaGreaterThan", RaGreaterThan);
		p.setSemanticAction("RaGreaterThanEquals", RaGreaterThanEquals);

		p.setSemanticAction("RaAdd", RaAdd);
		p.setSemanticAction("RaSub", RaSub);
		p.setSemanticAction("RaMul", RaMul);
		p.setSemanticAction("RaDiv", RaDiv);
		p.setSemanticAction("RaMod", RaMod);

		p.setSemanticAction("RaLeftShift", RaLeftShift);
		p.setSemanticAction("RaRightShift", RaRightShift);

		p.setSemanticAction("RaBitwiseAnd", RaBitwiseAnd);
		p.setSemanticAction("RaBitwiseOr", RaBitwiseOr);
		p.setSemanticAction("RabitwiseXor", RaBitwiseXor);

		p.setSemanticAction("RaAnd", RaAnd);
		p.setSemanticAction("RaOr", RaOr);


		p.setSemanticAction("Field", GetField);
		//		p.setSemanticAction(".", SetField);
		p.setSemanticAction("MethodCall", MethodCall);

		p.setSemanticAction("Group", Group);
		p.setSemanticAction("Cast", Cast);
		p.setSemanticAction("FuncCall", FuncCall);

		p.setSemanticAction("Indexer", GetIndex);
		//		p.setSemanticAction("[", SetIndex);
		p.setSemanticAction("ArrayLiteral", ArrayLiteral);
		//		p.setSemanticAction("$MapEntry$", MapEntry);
		p.setSemanticAction("MapLiteral", MapLiteral);
		p.setSemanticAction("NewObject", NewObject);

		//		p.setSemanticAction(";", StatementEnd);
		p.setSemanticAction("Block", Block);
		p.setSemanticAction("Annotation", Annotation);

		p.setSemanticAction("If", If);
		p.setSemanticAction("Return", Return);
		p.setSemanticAction("While", While);
		p.setSemanticAction("Break", Break);

		p.setSemanticAction("Var", Var);
		p.setSemanticAction("Param", Param);
		//		p.setSemanticAction("function", Prototype);
		p.setSemanticAction("Function", Function);

		p.setSemanticAction("Let", Let);
		//		p.setSemanticAction("export", Export);
		//
		////		p.SetTypeName(BClassType._ObjectType, null);
		//		p.setSemanticAction("class", Class);
		//		p.setSemanticAction("$FieldDecl$", ClassField);
		//		p.setSemanticAction("instanceof", BunPrecedence._Instanceof, InstanceOf);
		p.setSemanticAction("Instanceof", RaInstanceOf);

		p.setSemanticAction("Assert", Assert);
		p.setSemanticAction("Require", Require);

		p.setSemanticAction("Asm", Asm);
		//		p.setSemanticAction("$LongName$", DefineName);
		p.setSemanticAction("Define", Define);
		//		p.Generator.LangInfo.AppendGrammarInfo("zen-0.1");

		p.setSemanticAction("Try", Try);
		p.setSemanticAction("Throw", Throw);
		//		p.Generator.LangInfo.AppendGrammarInfo("zen-trycatch-0.1");
	}


}
