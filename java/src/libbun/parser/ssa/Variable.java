package libbun.parser.ssa;


import libbun.parser.ast.ZNode;
import libbun.util.Field;
import libbun.util.ZArray;


public class Variable {
	@Field public String Name;
	@Field public int Index;
	@Field private final ZArray<ZNode> Defs;
	@Field private final ZArray<ZNode> Uses;
	@Field public final ZNode Node;
	public Variable(String Name, ZNode Node) {
		this.Name = Name;
		this.Node = Node;
		this.Index = -1;
		this.Defs = new ZArray<ZNode>(new ZNode[0]);
		this.Uses = new ZArray<ZNode>(new ZNode[0]);
	}

	public void Use(ZNode Node) {
		this.Uses.add(Node);
	}

	public void Def(ZNode Node) {
		this.Defs.add(Node);
	}
}