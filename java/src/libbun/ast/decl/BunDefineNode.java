package libbun.ast.decl;

import libbun.ast.AstNode;
import libbun.ast.literal.BunAsmNode;
import libbun.ast.literal.BunStringNode;
import libbun.parser.classic.LibBunGamma;
import libbun.type.BFormFunc;
import libbun.type.BFuncType;
import libbun.type.BType;
import libbun.util.BField;
import libbun.util.Var;

public class BunDefineNode extends TopLevelNode {
	@BField public BunLetVarNode DefineNode;

	public BunDefineNode(AstNode ParentNode, BunLetVarNode DefineNode) {
		super(ParentNode, 0);
		this.DefineNode = DefineNode;
	}
	@Override public AstNode dup(boolean TypedClone, AstNode ParentNode) {
		return this.dupField(TypedClone, new BunDefineNode(ParentNode, this.DefineNode));
	}

	private String GetFormText() {
		@Var AstNode Node = this.DefineNode.InitValueNode();
		if(Node instanceof BunStringNode) {
			return ((BunStringNode)Node).StringValue;
		}
		return "";
	}

	@Override public final void Perform(LibBunGamma Gamma) {
		@Var String Symbol = this.DefineNode.GetGivenName();
		@Var String FormText = this.GetFormText();
		@Var BType  FormType = this.DefineNode.DeclType();
		@Var String LibName = null;
		@Var int loc = FormText.indexOf("~");
		if(loc > 0) {
			LibName = FormText.substring(0, loc);
		}
		if(loc >= 0) {
			FormText = FormText.substring(loc+1);
		}
		if(FormType instanceof BFuncType) {
			@Var BFuncType FormFuncType = (BFuncType)FormType;
			@Var BFormFunc FormFunc = new BFormFunc(Symbol, FormFuncType, LibName, FormText);
			if(Symbol.equals("_")) {
				Gamma.Generator.SetConverterFunc(FormFuncType.GetRecvType(), FormFuncType.GetReturnType(), FormFunc);
			}
			else {
				//				System.out.println("Func: " + FormFunc + " by " + FormFunc.GetSignature());
				Gamma.Generator.SetDefinedFunc(FormFunc);
			}
		}
		else {
			//let symbol = asm "macro": type;
			@Var BunAsmNode AsmNode = new BunAsmNode(null, LibName, FormText, FormType);
			AsmNode.SourceToken = this.DefineNode.getTokenAt(BunLetVarNode._NameInfo);
			AsmNode.Type = FormType;
			this.DefineNode.GivenType = FormType;
			this.DefineNode.GivenName = FormText;
			this.DefineNode.SetNode(BunLetVarNode._InitValue, AsmNode);
			Gamma.SetSymbol(Symbol, this.DefineNode);
		}
	}
}
