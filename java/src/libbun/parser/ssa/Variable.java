package libbun.parser.ssa;

import libbun.ast.AstNode;

class Variable {
	public AstNode  Node;
	public String Name;
	public int    Index;
	public Variable(String Name, int Index, AstNode Node) {
		this.Node = Node;
		this.Index = Index;
		this.Name = Name;
	}
	@Override
	public String toString() {
		return "Variable { Name : " + this.Name + ", Index : " + this.Index + "}";
	}
}