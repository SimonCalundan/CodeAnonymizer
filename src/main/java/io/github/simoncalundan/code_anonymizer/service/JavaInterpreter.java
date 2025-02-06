package io.github.simoncalundan.code_anonymizer.service;

import io.github.simoncalundan.code_anonymizer.model.CommentStyle;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class JavaInterpreter implements LanguageInterpreter {
    private static final Set<String> KEYWORDS = Set.of(
            // Control flow
            "if", "else", "for", "do", "while", "break", "continue", "return",
            "switch", "case", "default", "try", "catch", "finally", "throw", "throws",

            // Modifiers and declarations
            "public", "private", "protected", "static", "final", "abstract", "synchronized",
            "transient", "volatile", "native", "strictfp", "const",

            // Type keywords
            "class", "interface", "enum", "extends", "implements", "package",
            "import", "instanceof", "super", "this", "new", "void",
            "boolean", "byte", "char", "short", "int", "long", "float", "double",

            // Standard library classes
            "String", "Object", "System", "Math", "Runtime", "Thread", "Exception",
            "StringBuilder", "StringBuffer",

            // Collections framework
            "List", "ArrayList", "LinkedList", "Set", "HashSet", "TreeSet",
            "Map", "HashMap", "TreeMap", "Queue", "Deque", "Stack", "Vector",

            // Wrapper classes
            "Integer", "Double", "Boolean", "Character", "Byte",

            // Common annotations
            "Override", "Deprecated", "SuppressWarnings", "FunctionalInterface",
            "Service", "Component", "Repository", "Controller", "Autowired",

            // Generic type parameters
            "T", "E", "K", "V", "N", "S", "U",

            // Functional interfaces
            "Function", "Consumer", "Supplier", "Predicate", "BiFunction",
            "BiConsumer", "BiPredicate", "Runnable", "Callable",

            // Literal values
            "true", "false", "null"
    );

    @Override
    public Set<String> getReservedKeywords() {
        return KEYWORDS;
    }

    @Override
    public CommentStyle getCommentStyle() {
        return CommentStyle.JAVA_STYLE;
    }

    @Override
    public boolean isValidIdentifier(String name) {
        if (name == null || name.isEmpty() || KEYWORDS.contains(name)) {
            return false;
        }
        char firstChar = name.charAt(0);
        return Character.isJavaIdentifierStart(firstChar) &&
                name.chars().allMatch(Character::isJavaIdentifierPart);
    }
}
