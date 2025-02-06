package io.github.simoncalundan.code_anonymizer.service;


import io.github.simoncalundan.code_anonymizer.model.CommentStyle;

import java.util.HashSet;
import java.util.Set;

public class DefaultInterpreter implements LanguageInterpreter {
    private static final Set<String> DEFAULT_KEYWORDS = Set.of(
            // Control flow
            "if", "else", "while", "for", "do", "break", "continue", "return",
            "switch", "case", "default",

            // Common type keywords
            "int", "string", "float", "double", "boolean", "char", "null", "void",

            // Object-oriented
            "class", "interface", "public", "private", "protected", "static", "final",
            "extends", "implements", "new",

            // Exception handling
            "try", "catch", "finally", "throw", "throws",

            // Logical operators
            "true", "false", "and", "or", "not",

            // Common functions/classes
            "String", "Math", "Arrays", "Object", "Exception", "Error",

            // Import/package related
            "import", "package", "from", "as",

            // Variable declaration
            "var", "let", "const",

            // Common collections
            "List", "Map", "Set", "Array"
    );

    @Override
    public Set<String> getReservedKeywords() {
        return DEFAULT_KEYWORDS;
    }

    @Override
    public CommentStyle getCommentStyle() {
        return CommentStyle.DEFAULT;
    }

    @Override
    public boolean isValidIdentifier(String name) {
        return name != null && !name.isEmpty() &&
                Character.isJavaIdentifierStart(name.charAt(0)) &&
                name.chars().allMatch(Character::isJavaIdentifierPart);
    }
}
