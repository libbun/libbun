package libbun.encode.jvm;

import java.util.HashMap;

import libbun.ast.AstNode;
import libbun.util.LibBunSystem;
import libbun.util.Var;

import org.objectweb.asm.Type;


class AsmClassLoader extends ClassLoader {
	protected final HashMap<String,AsmClassBuilder> ClassBuilderMap = new HashMap<String, AsmClassBuilder>();
	private final AsmJavaGenerator Generator;

	public AsmClassLoader(AsmJavaGenerator Generator) {
		this.Generator = Generator;
	}

	AsmClassBuilder NewClass(int ClassQualifer, AstNode Node, String ClassName, String SuperClass) {
		@Var String SourceFile = null;
		if(Node != null && Node.SourceToken != null) {
			SourceFile = Node.SourceToken.GetFileName();
		}
		AsmClassBuilder ClassBuilder = new AsmClassBuilder(this.Generator, ClassQualifer, SourceFile, ClassName, SuperClass);
		this.ClassBuilderMap.put(ClassName, ClassBuilder);
		return ClassBuilder;
	}

	AsmClassBuilder NewClass(int ClassQualifer, AstNode Node, String ClassName, Class<?> SuperClass) {
		return this.NewClass(ClassQualifer, Node, ClassName, Type.getInternalName(SuperClass));
	}

	@Override protected Class<?> findClass(String name) {
		//System.err.println("loading .. " + name);
		AsmClassBuilder ClassBuilder = this.ClassBuilderMap.get(name);
		if(ClassBuilder != null) {
			byte[] b = ClassBuilder.GenerateBytecode();
			if(LibBunSystem.DebugMode) {
				ClassBuilder.OutputClassFile();
			}
			this.ClassBuilderMap.remove(name);
			try {
				return this.defineClass(name, b, 0, b.length);
			}
			catch(Error e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return null;
	}

	public Class<?> LoadGeneratedClass(String ClassName) {
		try {
			return this.loadClass(ClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			LibBunSystem._Exit(1, "generation failed: " + ClassName);
		}
		return null;
	}

}