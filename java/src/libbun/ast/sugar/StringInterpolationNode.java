package libbun.ast.sugar;

import libbun.ast.AstNode;
import libbun.ast.DesugarNode;
import libbun.ast.SyntaxSugarNode;
import libbun.ast.binary.BunAddNode;
import libbun.ast.literal.BunStringNode;
import libbun.parser.classic.LibBunTypeChecker;
import libbun.type.BType;
import libbun.util.Var;

public class StringInterpolationNode extends SyntaxSugarNode {

	public StringInterpolationNode(AstNode ParentNode) {
		super(ParentNode,0);
		this.Type = BType.StringType;
	}

	public final String GetStringLiteralAt(int Index) {
		@Var AstNode LiteralNode = this.AST[Index];
		if(LiteralNode instanceof BunStringNode) {
			return ((BunStringNode)LiteralNode).StringValue;
		}
		return null;
	}

	public final void SetStringLiteralAt(int Index, String Text) {
		@Var AstNode LiteralNode = this.AST[Index];
		if(LiteralNode instanceof BunStringNode) {
			((BunStringNode)LiteralNode).StringValue = Text;
		}
	}


	@Override public void PerformTyping(LibBunTypeChecker TypeChecker, BType ContextType) {
		@Var int i = 0;
		while(i < this.size()) {
			if(i % 2 == 0) {
				assert(this.AST[i] instanceof BunStringNode);
				TypeChecker.CheckTypeAt(this, i, BType.StringType);
			}
			else {
				TypeChecker.TryTypeAt(this, i, BType.VarType);
			}
			i = i + 1;
		}
		TypeChecker.typeNode(this, BType.StringType);
	}

	@Override public DesugarNode PerformDesugar(LibBunTypeChecker TypeChecker) {
		@Var AstNode LeftNode = this.CreateStringConcat(TypeChecker, this.AST[0], this.AST[1]);
		@Var int i = 2;
		while(i < this.size()) {
			LeftNode = this.CreateStringConcat(TypeChecker, LeftNode, this.AST[i]);
			i = i + 1;
		}
		return new DesugarNode(this, LeftNode);
	}

	private AstNode CreateStringConcat(LibBunTypeChecker TypeChecker, AstNode LeftNode, AstNode RightNode) {
		@Var BunAddNode BinaryNode = new BunAddNode(null);
		BinaryNode.SetLeftNode(this.EnforceStringTypedNode(TypeChecker, LeftNode));
		BinaryNode.SetRightNode(this.EnforceStringTypedNode(TypeChecker, RightNode));
		TypeChecker.typeNode(BinaryNode, BType.StringType);
		return BinaryNode;
	}

	private AstNode EnforceStringTypedNode(LibBunTypeChecker TypeChecker, AstNode Node) {
		if(!Node.Type.IsStringType()) {
			Node = TypeChecker.EnforceNodeType(Node, BType.StringType);
		}
		return Node;
	}

	public final static StringInterpolationNode _ToStringInterpolationNode(BunAddNode Node) {
		@Var AstNode LeftNode = _ToImpl(Node.LeftNode());
		@Var AstNode RightNode = _ToImpl(Node.RightNode());
		//		System.out.println("LeftNode " + LeftNode);
		//		System.out.println("RightNode " + RightNode);
		if(LeftNode instanceof BunStringNode) {
			@Var BunStringNode LeftStringNode = (BunStringNode)LeftNode;
			if(RightNode instanceof StringInterpolationNode) {
				((StringInterpolationNode)RightNode).AppendFirstText(LeftStringNode.StringValue);
				return ((StringInterpolationNode)RightNode);
			}
			@Var StringInterpolationNode InterNode = new StringInterpolationNode(Node.ParentNode);
			InterNode.appendNode(LeftStringNode);
			if(RightNode instanceof BunStringNode) {
				LeftStringNode.StringValue = LeftStringNode.StringValue + ((BunStringNode)RightNode).StringValue;
			}
			else {
				InterNode.appendNode(RightNode);
				InterNode.appendNode(new BunStringNode(null, null, ""));
			}
			return InterNode; //"ab" + n
		}
		StringInterpolationNode LeftInterNode = null;
		if(LeftNode instanceof StringInterpolationNode) {
			LeftInterNode = (StringInterpolationNode)LeftNode;
		}
		else {
			LeftInterNode = new StringInterpolationNode(Node.ParentNode);
			LeftInterNode.appendNode(new BunStringNode(null, null, ""));
			LeftInterNode.appendNode(LeftNode);
			LeftInterNode.appendNode(new BunStringNode(null, null, ""));
		}
		if(RightNode instanceof StringInterpolationNode) {
			LeftInterNode.Merge((StringInterpolationNode)RightNode);
			return LeftInterNode;
		}
		if(RightNode instanceof BunStringNode) {
			LeftInterNode.AppendLastText(((BunStringNode)RightNode).StringValue);
			return LeftInterNode;
		}
		LeftInterNode.appendNode(RightNode);
		LeftInterNode.appendNode(new BunStringNode(null, null, ""));
		return LeftInterNode; //"ab" + n
	}

	private final static AstNode _ToImpl(AstNode Node) {
		if(Node.Type.IsStringType()) {
			if(Node instanceof BunAddNode) {
				return _ToStringInterpolationNode((BunAddNode)Node);
			}
		}
		return Node;
	}

	private void AppendFirstText(String Text) {
		this.SetStringLiteralAt(0, Text + this.GetStringLiteralAt(0));
	}

	private void AppendLastText(String Text) {
		@Var int LastIndex = this.size()-1;
		this.SetStringLiteralAt(LastIndex, this.GetStringLiteralAt(LastIndex)+Text);
	}

	private void Merge(StringInterpolationNode RightNode) {
		this.AppendLastText(RightNode.GetStringLiteralAt(0));
		@Var int i = 1;
		while(i < RightNode.size()) {
			this.appendNode(RightNode.AST[i]);
			i = i + 1;
		}
	}

}
