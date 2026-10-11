package app1.extractor;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class StringLiteralFilters {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\d+}");
    private static final Predicate<String> IS_RELEVANT_VALUE = StringLiteralFilters::isRelevantString;

    private StringLiteralFilters() {
    }

    public static Predicate<StringLiteralExpr> isInternationalizableLiteral() {
        return literal -> !isAlreadyInternationalized(literal) && IS_RELEVANT_VALUE.test(literal.getValue());
    }

    private static boolean isAlreadyInternationalized(StringLiteralExpr literal) {
        return literal.findAncestor(MethodCallExpr.class)
                .map(MethodCallExpr::getNameAsString)
                .filter(methodName -> "getString".equals(methodName) || "getBundle".equals(methodName))
                .isPresent();
    }

    private static boolean isRelevantString(String value) {
        return !value.isEmpty()
                && !value.isBlank()
                && value.length() != 1
                && !isOnlySpecialCharacters(value);
    }

    private static boolean isOnlySpecialCharacters(String value) {
        return value.chars().noneMatch(Character::isLetterOrDigit);
    }

    static boolean containsPlaceholder(String value) {
        return PLACEHOLDER_PATTERN.matcher(value).find();
    }
}
