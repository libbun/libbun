package libbun.ast;

import libbun.util.BField;
import libbun.util.LibBunSystem;
import libbun.util.Var;

public abstract class AbstractListNode extends AstNode {
	@BField public int vargStartIndex;
	public AbstractListNode(AstNode ParentNode, int Size) {
		super(ParentNode, Size);
		this.vargStartIndex = Size;
	}

	public final int GetListSize() {
		return this.size() - this.vargStartIndex;
	}

	public final AstNode GetListAt(int Index) {
		return this.AST[this.vargStartIndex + Index];
	}

	public final void SetListAt(int Index, AstNode Node) {
		this.SetNode(Index + this.vargStartIndex, Node);
	}

	public final void InsertListAt(int Index, AstNode Node) {
		if(this.AST == null || Index < 0 || this.AST.length == Index) {
			this.appendNode(Node);
		} else {
			@Var AstNode[] newAST = LibBunSystem._NewNodeArray(this.AST.length + 1);
			@Var AstNode[] oldAST = this.AST;
			Index = this.vargStartIndex + Index;
			this.AST = newAST;
			LibBunSystem._ArrayCopy(oldAST, 0, newAST, 0, Index);
			this.SetNode(Index, Node);
			LibBunSystem._ArrayCopy(oldAST, Index, newAST, Index + 1, oldAST.length - Index);
		}
	}

	public final AstNode RemoveListAt(int Index) {
		@Var AstNode Removed = this.GetListAt(Index);
		@Var AstNode[] newAST = LibBunSystem._NewNodeArray(this.AST.length - 1);
		@Var int RemovedIndex = this.vargStartIndex + Index;
		LibBunSystem._ArrayCopy(this.AST, 0, newAST, 0, RemovedIndex);
		LibBunSystem._ArrayCopy(this.AST, RemovedIndex + 1, newAST, RemovedIndex, this.AST.length - (RemovedIndex + 1));
		this.AST = newAST;
		return Removed;
	}

	public final void ClearListToSize(int Size) {
		if(Size < this.GetListSize()) {
			@Var int newsize = this.vargStartIndex + Size;
			if(newsize == 0) {
				this.AST = null;
			}
			else {
				@Var AstNode[] newAST = LibBunSystem._NewNodeArray(newsize);
				LibBunSystem._ArrayCopy(this.AST, 0, newAST, 0, newsize);
				this.AST = newAST;
			}
		}
	}

}
