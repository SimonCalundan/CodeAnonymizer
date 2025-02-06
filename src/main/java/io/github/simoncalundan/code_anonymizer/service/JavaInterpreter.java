package io.github.simoncalundan.code_anonymizer.service;

import io.github.simoncalundan.code_anonymizer.model.CommentStyle;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class JavaInterpreter implements LanguageInterpreter {
    private static final Set<String> KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while"
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
