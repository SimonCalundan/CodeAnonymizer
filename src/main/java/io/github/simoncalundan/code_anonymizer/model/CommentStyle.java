package io.github.simoncalundan.code_anonymizer.model;

public enum CommentStyle {
    JAVA_STYLE("//", "/*", "*/"),
    PYTHON_STYLE("#", "\"\"\"", "\"\"\""),
    DEFAULT("//", "/*", "*/");

    private final String singleLine;
    private final String multiLineStart;
    private final String multiLineEnd;

    CommentStyle(String singleLine, String multiLineStart, String multiLineEnd) {
        this.singleLine = singleLine;
        this.multiLineStart = multiLineStart;
        this.multiLineEnd = multiLineEnd;
    }

    public String getSingleLine() {
        return singleLine;
    }

    public String getMultiLineStart() {
        return multiLineStart;
    }

    public String getMultiLineEnd() {
        return multiLineEnd;
    }
}
