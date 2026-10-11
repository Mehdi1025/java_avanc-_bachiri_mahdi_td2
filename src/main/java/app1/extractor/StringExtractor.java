package app1.extractor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class StringExtractor {

    private static final Predicate<StringLiteralExpr> IS_INTERNATIONALIZABLE =
            StringLiteralFilters.isInternationalizableLiteral();

    private StringExtractor() {
    }

    public static List<String> extractFromFile(Path javaFile) {
        try {
            return StaticJavaParser.parse(javaFile)
                    .findAll(StringLiteralExpr.class)
                    .stream()
                    .filter(IS_INTERNATIONALIZABLE)
                    .map(StringLiteralExpr::getValue)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de parser le fichier : " + javaFile, e);
        }
    }

    public static Map<String, String> extractFromFiles(List<Path> javaFiles) {
        List<String> uniqueStrings = javaFiles.stream()
                .flatMap(path -> extractFromFile(path).stream())
                .distinct()
                .toList();

        AtomicInteger counter = new AtomicInteger(1);
        return uniqueStrings.stream()
                .collect(Collectors.toMap(
                        ignored -> "msg_" + counter.getAndIncrement(),
                        value -> value,
                        (existing, duplicate) -> existing,
                        LinkedHashMap::new
                ));
    }
}
