package libbun.encode.jvm;

import libbun.ast.AstNode;
import libbun.ast.LocalDefinedNode;
import libbun.type.BType;

public class JavaStaticFieldNode extends LocalDefinedNode {
	Class<?> StaticClass;
	String FieldName;
	JavaStaticFieldNode(AstNode ParentNode, Class<?> StaticClass, BType FieldType, String FieldName) {
		super(ParentNode, 0);
		this.StaticClass = StaticClass;
		this.Type = FieldType;
		this.FieldName = FieldName;
	}
}
