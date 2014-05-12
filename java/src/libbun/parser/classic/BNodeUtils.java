package libbun.parser.classic;

import libbun.ast.AbstractListNode;
import libbun.ast.BNode;
import libbun.ast.BlockNode;
import libbun.ast.decl.BunFunctionNode;
import libbun.ast.statement.BunBreakNode;
import libbun.ast.statement.BunIfNode;
import libbun.ast.statement.BunReturnNode;
import libbun.ast.statement.BunThrowNode;
import libbun.util.Var;

public class BNodeUtils {

	public final static boolean _IsBlockBreak(BNode Node) {
		if(Node instanceof BunReturnNode || Node instanceof BunThrowNode || Node instanceof BunBreakNode) {
			return true;
		}
		//System.out.println("@HasReturn"+ Node.getClass().getSimpleName());
		return false;
	}

	public final static boolean _HasFunctionBreak(BNode Node) {
		if(Node instanceof BunReturnNode || Node instanceof BunThrowNode) {
			return true;
		}
		if(Node instanceof BunIfNode) {
			@Var BunIfNode IfNode = (BunIfNode)Node;
			if(IfNode.HasElseNode()) {
				return BNodeUtils._HasFunctionBreak(IfNode.ThenNode()) && BNodeUtils._HasFunctionBreak(IfNode.ElseNode());
			}
			return false;
		}
		if(Node instanceof BlockNode) {
			@Var BlockNode blockNode = (BlockNode)Node;
			@Var int i = 0;
			while(i < blockNode.GetListSize()) {
				@Var BNode StmtNode = blockNode.GetListAt(i);
				//System.out.println("i="+i +", "+ StmtNode.getClass().getSimpleName());
				if(BNodeUtils._HasFunctionBreak(StmtNode)) {
					return true;
				}
				i = i + 1;
			}
		}
		//System.out.println("@HasReturn"+ Node.getClass().getSimpleName());
		return false;
	}



	public final static BunReturnNode _CheckIfSingleReturnNode(BunFunctionNode Node) {
		@Var BlockNode blockNode = Node.blockNode();
		if(blockNode.GetListSize() == 1) {
			@Var BNode ReturnNode= blockNode.AST[0];
			if(ReturnNode instanceof BunReturnNode) {
				return (BunReturnNode)ReturnNode;
			}
		}
		return null;
	}


	public final static int _AstListIndexOf(AbstractListNode LNode, BNode ChildNode) {
		@Var int i = 0;
		while(i < LNode.GetListSize()) {
			if(LNode.GetListAt(i) == ChildNode) {
				return i;
			}
			i = i + 1;
		}
		return -1;
	}

	public final static void _CopyAstList(AbstractListNode SourceListNode, int FromIndex, AbstractListNode DestListNode) {
		@Var int i = FromIndex;
		while(i < SourceListNode.GetListSize()) {
			DestListNode.Append(SourceListNode.GetListAt(i));
			i = i + 1;
		}
	}

	public final static void _MoveAstList(AbstractListNode SourceListNode, int FromIndex, AbstractListNode DestListNode) {
		BNodeUtils._CopyAstList(SourceListNode, FromIndex, DestListNode);
		SourceListNode.ClearListToSize(FromIndex);
	}

}
