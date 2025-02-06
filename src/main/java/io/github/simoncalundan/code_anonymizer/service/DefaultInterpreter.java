package io.github.simoncalundan.code_anonymizer.service;


import io.github.simoncalundan.code_anonymizer.model.CommentStyle;

import java.util.HashSet;
import java.util.Set;

public class DefaultInterpreter implements LanguageInterpreter {
    private static final Set<String> DEFAULT_KEYWORDS = new HashSet<>();

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
