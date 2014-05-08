package libbun.ast.expression;

import libbun.ast.BNode;
import libbun.ast.LocalDefinedNode;
import libbun.parser.common.BunToken;
import libbun.type.BFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;

public class BunFuncNameNode extends LocalDefinedNode {
	@BField public final String FuncName;
	@BField public final BType RecvType;
	@BField public final int FuncParamSize;

	public BunFuncNameNode(BNode ParentNode, BunToken SourceToken, String FuncName, BFuncType FuncType) {
		super(ParentNode, 0);
		this.SourceToken = SourceToken;
		this.FuncName = FuncName;
		this.RecvType = FuncType.GetRecvType();
		this.FuncParamSize = FuncType.GetFuncParamSize();
		this.Type = FuncType;
	}

	public BunFuncNameNode(BNode ParentNode, BunToken sourceToken, String FuncName, BType RecvType, int FuncParamSize) {
		super(ParentNode, 0);
		this.SourceToken = sourceToken;
		this.FuncName = FuncName;
		this.RecvType = RecvType;
		this.FuncParamSize = FuncParamSize;
	}

	@Override public BNode Dup(boolean TypedClone, BNode ParentNode) {
		if(TypedClone) {
			return this;
		}
		else {
			return this.DupField(TypedClone, new GetNameNode(ParentNode, null, this.FuncName));
		}
	}

	public final String GetSignature() {
		return BFunc._StringfySignature(this.FuncName, this.FuncParamSize, this.RecvType);
	}
}
