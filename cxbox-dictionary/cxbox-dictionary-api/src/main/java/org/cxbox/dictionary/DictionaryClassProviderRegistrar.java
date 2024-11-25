package org.cxbox.dictionary;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class DictionaryClassProviderRegistrar extends AbstractProcessor {

	private static final boolean ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS = false;

	private static final String DICT_CANONICAL_NAME = "org.cxbox.dictionary.Dictionary";

	private static final String DICT_TYPE_PROVIDER_CANONICAL_NAME = "org.cxbox.dictionary.DictionaryClassProvider";

	private Types typeUtils;

	private Elements elementUtils;

	private Filer filer;

	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Running " + getClass().getSimpleName());
		if (roundEnv.processingOver()) {
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}
		if (roundEnv.getRootElements() == null || roundEnv.getRootElements().isEmpty()) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "No sources to process");
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
		}

		// Target interface
		TypeElement dictInterface = elementUtils.getTypeElement(DICT_CANONICAL_NAME);
		TypeElement dictTypeProviderInterface = elementUtils.getTypeElement(DICT_TYPE_PROVIDER_CANONICAL_NAME);

		if (dictInterface == null || dictTypeProviderInterface == null) {
			return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS; // Interface not found in the current round
		}

		// Process each class that implements Dict
		for (Element element : roundEnv.getRootElements()) {
			if (element instanceof TypeElement typeElement) {
				for (TypeMirror iface : typeElement.getInterfaces()) {
					if (typeUtils.isSameType(iface, dictInterface.asType())) {
						generateProcessorForImplementation(typeElement, dictInterface, dictTypeProviderInterface);
					}
				}
			}
		}

		return ALLOW_OTHER_PROCESSORS_TO_CLAIM_ANNOTATIONS;
	}

	private void generateProcessorForImplementation(TypeElement implementation, TypeElement dictInterface, TypeElement dictTypeProviderInterface) {

		String packageName = elementUtils.getPackageOf(implementation).getQualifiedName().toString();
		String className = implementation.getSimpleName() + dictTypeProviderInterface.getSimpleName().toString();
		String qualifiedClassName = packageName + "." + className;

		try {
			// Create the source file
			JavaFileObject file = filer.createSourceFile(qualifiedClassName, dictInterface);

			try (PrintWriter writer = new PrintWriter(file.openWriter())) {
				writer.println("package " + packageName + ";");
				writer.println();
				writer.println("import com.google.auto.service.AutoService;");
				writer.println("import " + dictInterface.getQualifiedName() + ";");
				writer.println("import " + dictTypeProviderInterface.getQualifiedName() + ";");
				writer.println();
				writer.println("@AutoService(" + dictTypeProviderInterface.getSimpleName() + ".class)");
				writer.println(
						"public final class " + className + " implements " + dictTypeProviderInterface.getSimpleName() + " {");
				writer.println();
				writer.println("    @Override");
				writer.println("    public Class<? extends " + dictInterface.getSimpleName() + "> getDictionaryType() {");
				writer.println("        return " + implementation.getQualifiedName() + ".class;");
				writer.println("    }");
				writer.println("}");
			}
		} catch (IOException e) {
			messager.printMessage(
					Diagnostic.Kind.ERROR,
					"Failed to generate " + qualifiedClassName + ": " + e.getMessage()
			);
		}
	}

}
