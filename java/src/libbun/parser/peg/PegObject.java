package libbun.parser.peg;

import libbun.ast.BNode;
import libbun.ast.PegNode;
import libbun.ast.binary.BinaryOperatorNode;
import libbun.common.CommonArray;
import libbun.encode.LibBunSourceBuilder;
import libbun.parser.classic.BToken;
import libbun.parser.common.BunSource;
import libbun.parser.common.BunToken;

public abstract class PegObject {
	BunSource debugSource;
	Peg createdPeg;
	int startIndex;
	int endIndex;
	CommonArray<PegObject> elementList;
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

	public final BNode eval(BunSource source, BNode parentNode) {
		BNode node = null;
		if(this.semanticAction != null) {
			node = this.semanticAction.Invoke(source, parentNode, this);
		}
		else {
			node = new PegNode(parentNode, this.size());
			if(this.size() > 0) {
				this.copySubTo(source, node);
			}
		}
		node.SourceToken = source.newToken(this.startIndex, this.endIndex);
		return node;
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

	public final BNode get(BunSource source, BNode parentNode, int index) {
		PegObject po = this.elementList.ArrayValues[index];
		return po.eval(source, parentNode);
	}

	public BNode copySubTo(BunSource source, BNode node) {
		node.expandAstToSize(this.size());
		for(int i = 0; i < this.size(); i++) {
			node.AST[i] = this.get(source, node, i);
			if(node.AST[i] != null) {
				node.AST[i].ParentNode = node;
			}
		}
		return node;
	}

	public BNode copyBinaryAsRightAssoc(BunSource source, BinaryOperatorNode node) {
		node.expandAstToSize(2);
		node.AST[0] = this.get(source, node, 0);
		return node.SetRightBinaryNode(this.get(source, node, 1));
	}
}

class PegParsedNode extends PegObject {

	PegParsedNode(Peg createdPeg, int startIndex, int endIndex) {
		super(createdPeg, startIndex, endIndex);
		this.elementList = null;
	}


	@Override public void set(int index, PegObject childNode) {
		if(this.elementList == null) {
			this.elementList = new CommonArray<PegObject>(new PegObject[2]);
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