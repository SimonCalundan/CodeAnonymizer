package io.github.simoncalundan.code_anonymizer.service;

import io.github.simoncalundan.code_anonymizer.model.CommentStyle;

import java.util.Set;

public interface LanguageInterpreter {
    Set<String> getReservedKeywords();

    CommentStyle getCommentStyle();

    boolean isValidIdentifier(String name);
}
