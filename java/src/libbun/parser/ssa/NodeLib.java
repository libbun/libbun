package libbun.parser.ssa;

import libbun.ast.AstNode;
import libbun.ast.binary.AssignNode;
import libbun.ast.decl.BunLetVarNode;
import libbun.ast.decl.BunVarBlockNode;
import libbun.ast.expression.GetNameNode;
import libbun.encode.LibBunGenerator;
import libbun.type.BType;

public class NodeLib {
	static public boolean IsVariableNode(AstNode Node) {
		if(Node instanceof BunLetVarNode) {
			return true;
		}
		else if(Node instanceof BunVarBlockNode) {
			return true;
		}
		else if(Node instanceof AssignNode) {
			return ((AssignNode)Node).LeftNode() instanceof GetNameNode;
		}
		else if(Node instanceof PHINode) {
			return true;
		}
		return false;
	}

	static public BType GetType(AstNode Node) {
		if(Node instanceof BunLetVarNode) {
			BunLetVarNode LNode = (BunLetVarNode) Node;
			return LNode.DeclType();
		}
		else if(Node instanceof BunVarBlockNode) {
			BunVarBlockNode VNode = (BunVarBlockNode) Node;
			return VNode.VarDeclNode().DeclType();
		}
		else if(Node instanceof AssignNode) {
			AssignNode SNode = (AssignNode) Node;
			return SNode.RightNode().Type;
		}
		else if(Node instanceof PHINode) {
			PHINode PNode = (PHINode) Node;
			return PNode.Type;
		}
		return BType.VarType;
	}

	static public String GetVarName(AstNode Node, LibBunGenerator Generator) {
		if(Node instanceof BunLetVarNode) {
			BunLetVarNode LNode = (BunLetVarNode) Node;
			return LNode.GetGivenName();
		}
		else if(Node instanceof BunVarBlockNode) {
			BunVarBlockNode VNode = (BunVarBlockNode) Node;
			return VNode.VarDeclNode().GetGivenName();
		}
		else if(Node instanceof AssignNode) {
			AssignNode SNode = (AssignNode) Node;
			return ((GetNameNode)SNode.LeftNode()).GetUniqueName(Generator);
		}
		else if(Node instanceof PHINode) {
			PHINode PNode = (PHINode) Node;
			return PNode.GetName();
		}
		return null;
	}

	static public int GetVarIndex(AstNode Node) {
		if(Node instanceof BunLetVarNode) {
			return 0;
		}
		else if(Node instanceof BunVarBlockNode) {
			return 0;
		}
		else if(Node instanceof AssignNode) {
			AssignNode SNode = (AssignNode) Node;
			return ((GetNameNode)SNode.LeftNode()).VarIndex;
		}
		else if(Node instanceof PHINode) {
			PHINode PNode = (PHINode) Node;
			return PNode.GetVarIndex();
		}
		return -1;
	}
}