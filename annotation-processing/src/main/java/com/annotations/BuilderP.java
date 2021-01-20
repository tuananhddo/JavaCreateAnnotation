package com.annotations;

import com.google.auto.service.AutoService;
import jdk.nashorn.internal.runtime.logging.Logger;
import org.apache.commons.io.FileUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.annotations.BuilderPProperty")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Logger
public class BuilderP extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "HMMMM");

        for (TypeElement annotation : annotations) {

            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
//            processingEnv.getMessager().appendMessage(Diagnostic.Kind.WARNING, String.valueOf(annotations.size()));

            Map<Boolean, List<Element>> annotatedMethods = annotatedElements.stream().collect(Collectors.partitioningBy(element -> ((ExecutableType) element.asType()).getParameterTypes().size() == 1 && element.getSimpleName().toString().startsWith("set")));

            List<Element> setters = annotatedMethods.get(true);
            List<Element> otherMethods = annotatedMethods.get(false);

            otherMethods.forEach(element -> processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@BuilderProperty must be applied to a setXxx method with a single argument", element));

            if (setters.isEmpty()) {
                continue;
            }

            String className = ((TypeElement) setters.get(0).getEnclosingElement()).getQualifiedName().toString();

            Map<String, String> setterMap = setters.stream().collect(Collectors.toMap(setter -> setter.getSimpleName().toString(), setter -> ((ExecutableType) setter.asType()).getParameterTypes().get(0).toString()));

            try {
                writeBuilderFile(className, setterMap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    private void writeBuilderFile(String className, Map<String, String> setterMap) throws IOException {
        StringBuilder builder = new StringBuilder();
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "BuilderB";
        String builderSimpleClassName = builderClassName.substring(lastDot + 1);

        if (packageName != null) {
            builder.append("package ");
            builder.append(packageName);
            builder.append(";");
            builder.append(System.getProperty("line.separator"));
        }

        builder.append("public class ");
        builder.append(builderSimpleClassName);
        builder.append(" {");
        builder.append(System.getProperty("line.separator"));

        builder.append("    private ");
        builder.append(simpleClassName);
        builder.append(" object = new ");
        builder.append(simpleClassName);
        builder.append("();");
        builder.append(System.getProperty("line.separator"));

        builder.append("    public ");
        builder.append(simpleClassName);
        builder.append(" build() {");
        builder.append(System.getProperty("line.separator"));
        builder.append("        return object;");
        builder.append(System.getProperty("line.separator"));
        builder.append("    }");
        builder.append(System.getProperty("line.separator"));

        setterMap.entrySet().forEach(setter -> {
            String methodName = setter.getKey();
            String argumentType = setter.getValue();

            builder.append("    public ");
            builder.append(builderSimpleClassName);
            builder.append(" ");
            builder.append(methodName);

            builder.append("(");

            builder.append(argumentType);
            builder.append(" value) {");
            builder.append(System.getProperty("line.separator"));
            builder.append("        object.");
            builder.append(methodName);
            builder.append("(value);");
            builder.append(System.getProperty("line.separator"));
            builder.append("        return this;");
            builder.append(System.getProperty("line.separator"));
            builder.append("    }");
            builder.append(System.getProperty("line.separator"));
        });

        builder.append("}");
        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(builderClassName);
        try {
            Writer writer = builderFile.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        }catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }
    }


}
