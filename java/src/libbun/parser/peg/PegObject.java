package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.encode.LibBunSourceBuilder;
import libbun.parser.classic.BToken;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;
import libbun.util.BArray;
import libbun.util.LibBunSystem;

public abstract class PegObject {
	BunSource debugSource;
	Peg createdPeg;
	int startIndex;
	int endIndex;
	BArray<PegObject> elementList;
	SemanticFunction semanticAction;

	PegObject(Peg createdPeg, int startIndex, int endIndex) {
		this.createdPeg = createdPeg;
		this.startIndex = startIndex;
		this.endIndex   = endIndex;
	}

	public final boolean isErrorNode() {
		return this instanceof PegFailureNode;
	}

	public abstract void set(int index, PegObject childNode);
	abstract void stringfy(BunToken source, LibBunSourceBuilder sb);

	String toString(BunToken source) {
		LibBunSourceBuilder sb = new LibBunSourceBuilder(null, null);
		this.stringfy(source, sb);
		return sb.toString();
	}

	void setSemanticAction(SemanticFunction f) {
		this.semanticAction = f;
	}

	public final BNode eval(BNode parentNode) {
		if(this.semanticAction != null) {
			return this.semanticAction.Invoke(parentNode, this);
		}
		LibBunSystem._Exit(1, "undefined semantic action: " + this);
		return null;
	}

	public final BToken getToken() {
		return this.debugSource.newToken(this.startIndex, this.endIndex);
	}

	int size() {
		if(this.elementList != null) {
			return this.elementList.size();
		}
		return 0;
	}

	public final BNode get(BNode parentNode, int index) {
		PegObject po = this.elementList.ArrayValues[index];
		return po.eval(parentNode);
	}

	public BNode copySub(BNode node) {
		node.expandAstToSize(this.size());
		for(int i = 0; i < this.size(); i++) {
			node.AST[i] = this.get(node, i);
			if(node.AST[i] != null) {
				node.AST[i].ParentNode = node;
			}
		}
		return node;
	}

	public BNode copySubAsBinary(BinaryOperatorNode node) {
		node.expandAstToSize(2);
		node.AST[0] = this.get(node, 0);
		node.SetRightBinaryNode(this.get(node, 1));
		return node;
	}
}

class PegParsedNode extends PegObject {

	PegParsedNode(Peg createdPeg, int startIndex, int endIndex) {
		super(createdPeg, startIndex, endIndex);
		this.elementList = null;
	}


	@Override public void set(int index, PegObject childNode) {
		if(this.elementList == null) {
			this.elementList = new BArray<PegObject>(new PegObject[2]);
		}
		this.elementList.add(childNode);
	}

	@Override public String toString() {
		if(this.elementList != null) {
			String s = "{'" + this.debugSource.substring(this.startIndex, this.endIndex) + "' ";
			for(int i = 0; i < this.elementList.size(); i++) {
				if(i > 0) {
					s = s + ",";
				}
				s = s + this.elementList.ArrayValues[i].toString();
			}
			return s + "}";
		}
		else {
			if(this.endIndex > this.startIndex) {
				return this.debugSource.substring(this.startIndex, this.endIndex);
			}
			return "{}";
		}
	}

	@Override void stringfy(BunToken source, LibBunSourceBuilder sb) {
		if(this.elementList == null) {
			sb.AppendNewLine(source.substring(this.startIndex, this.endIndex), "   ## by " + this.createdPeg);
		}
		else {
			sb.AppendNewLine("node ''", source.substring(this.startIndex, this.endIndex));
			sb.OpenIndent("'' {            ## by " + this.createdPeg);
			for(int i = 0; i < this.elementList.size(); i++) {
				this.elementList.ArrayValues[i].stringfy(source, sb);
			}
			sb.CloseIndent("}");
		}
	}
}

class PegFailureNode extends PegObject {
	String errorMessage;
	PegFailureNode(Peg createdPeg, int startIndex, String errorMessage) {
		super(createdPeg, startIndex, startIndex);
		this.errorMessage = errorMessage;
	}
	@Override public void set(int index, PegObject childNode) {

	}

	@Override public String toString() {
		return "!!ERROR: " + this.errorMessage + "!!";
	}

	@Override void stringfy(BunToken source, LibBunSourceBuilder sb) {
		if(this.debugSource != null) {
			sb.AppendNewLine(this.debugSource.formatErrorLineMarker("error", this.startIndex, this.errorMessage + "   ## by " + this.createdPeg));
		}
		else {
			sb.AppendNewLine("Nothing");
		}
	}

}