package app1.modifier;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CodeModifier {

    private static final String BUNDLE_NAME = "messages";

    private CodeModifier() {
    }

    public static void modify(List<Path> javaFiles, Map<String, String> extractedStrings) throws IOException {
        Map<String, String> valueToKey = buildValueToKeyIndex(extractedStrings);

        for (Path javaFile : javaFiles) {
            CompilationUnit compilationUnit = StaticJavaParser.parse(javaFile);
            boolean modified = false;

            for (StringLiteralExpr literal : compilationUnit.findAll(StringLiteralExpr.class)) {
                String propertyKey = valueToKey.get(literal.getValue());
                if (propertyKey != null) {
                    literal.replace(createResourceBundleCall(propertyKey));
                    modified = true;
                }
            }

            if (modified) {
                compilationUnit.addImport("java.util.ResourceBundle");
            }

            try (FileWriter writer = new FileWriter(javaFile.toFile(), StandardCharsets.UTF_8)) {
                writer.write(compilationUnit.toString());
            }
        }
    }

    private static Map<String, String> buildValueToKeyIndex(Map<String, String> extractedStrings) {
        Map<String, String> valueToKey = new HashMap<>();
        for (Map.Entry<String, String> entry : extractedStrings.entrySet()) {
            valueToKey.putIfAbsent(entry.getValue(), entry.getKey());
        }
        return valueToKey;
    }

    private static MethodCallExpr createResourceBundleCall(String propertyKey) {
        MethodCallExpr getBundleCall = new MethodCallExpr(
                new NameExpr("ResourceBundle"),
                "getBundle"
        );
        getBundleCall.addArgument(new StringLiteralExpr(BUNDLE_NAME));

        MethodCallExpr getStringCall = new MethodCallExpr(getBundleCall, "getString");
        getStringCall.addArgument(new StringLiteralExpr(propertyKey));
        return getStringCall;
    }
}
