package io.github.simoncalundan.code_anonymizer.service;

import io.github.simoncalundan.code_anonymizer.model.CommentStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

@Service
@Slf4j
public class CodeAnonymizerServiceImpl implements CodeAnonymizerService {
    private final LanguageInterpreterService languageService;

    public CodeAnonymizerServiceImpl(LanguageInterpreterService languageService) {
        this.languageService = languageService;
    }

    @Override
    public String anonymizeCode(String sourceCode, String fileName, boolean preserveStringLiterals, boolean preserveComments) {
        LanguageInterpreter interpreter = languageService.getInterpreterForFile(fileName);
        log.info("Interpreter gathered from file {}", interpreter);
        Set<String> keywords = interpreter.getReservedKeywords();
        log.info("Starting code anonymization (preserveStringLiterals: {}, preserveComments: {})", preserveStringLiterals, preserveComments);

        if (sourceCode == null || sourceCode.isEmpty()) {
            log.warn("Received empty or null source code");
            return "";
        }

        // Handle comments based on preservation flag
        Map<String, String> commentMap = new HashMap<>();
        String processedCode = sourceCode;

        if (preserveComments) {
            // Extract and preserve comments while handling string literals
            Map<String, Object> extractionResult = extractComments(sourceCode, interpreter.getCommentStyle());
            processedCode = (String) extractionResult.get("processedCode");
            commentMap = (Map<String, String>) extractionResult.get("commentMap");
        } else {
            // Strip comments completely
            processedCode = stripComments(sourceCode, interpreter.getCommentStyle(), false);
        }

        // Process string literals
        Map<String, String> stringLiterals = new HashMap<>();
        Pattern stringLiteralPattern = Pattern.compile("(\"(?:[^\"\\\\]|\\\\.)*\")");
        Matcher stringMatcher = stringLiteralPattern.matcher(processedCode);
        StringBuffer stringProcessedCode = new StringBuffer();
        int stringCounter = 1;

        while (stringMatcher.find()) {
            String literal = stringMatcher.group(1);
            String placeholder = "‹" + stringCounter++ + "›";
            if (preserveStringLiterals) {
                stringLiterals.put(placeholder, literal);
            } else {
                stringLiterals.put(placeholder, "\"var" + (stringCounter - 1) + "\"");
            }
            stringMatcher.appendReplacement(stringProcessedCode, Matcher.quoteReplacement(placeholder));
        }
        stringMatcher.appendTail(stringProcessedCode);
        processedCode = stringProcessedCode.toString();

        // Anonymize identifiers
        Map<String, String> nameMapping = new HashMap<>();
        int counter = 1;
        Pattern identifierPattern = Pattern.compile(
                "\\b(?!(?:" + String.join("|", keywords) + ")\\b)" +
                        "(?<!§¤)([a-zA-Z_][a-zA-Z0-9_]*)\\b"
        );

        Matcher matcher = identifierPattern.matcher(processedCode);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String originalName = matcher.group(1);
            if (!nameMapping.containsKey(originalName)) {
                String anonymizedName = "var" + counter++;
                nameMapping.put(originalName, anonymizedName);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(nameMapping.get(originalName)));
        }
        matcher.appendTail(result);
        String finalCode = result.toString();

        // Restore string literals
        for (Map.Entry<String, String> entry : stringLiterals.entrySet()) {
            finalCode = finalCode.replace(entry.getKey(), entry.getValue());
        }

        // Restore comments if they were preserved
        if (preserveComments) {
            for (Map.Entry<String, String> entry : commentMap.entrySet()) {
                finalCode = finalCode.replace(entry.getKey(), entry.getValue());
            }
        }

        log.info("File successfully anonymized!");
        return finalCode;
    }

    /**
     * Extracts comments from code while properly handling string literals.
     * Returns a map containing the processed code and the extracted comments.
     */
    private Map<String, Object> extractComments(String code, CommentStyle style) {
        Map<String, String> commentMap = new HashMap<>();
        Map<String, String> literalMap = new HashMap<>();

        // Use a special prefix that won't be matched by identifier pattern
        final String COMMENT_PREFIX = "§¤COMMENT¤";

        // Phase 1: Mask string literals to prevent false positives in comments
        Matcher litMatcher = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"").matcher(code);
        StringBuffer litBuffer = new StringBuffer();
        int litCounter = 0;

        while (litMatcher.find()) {
            String token = "§LIT_" + litCounter + "§";
            literalMap.put(token, litMatcher.group());
            litMatcher.appendReplacement(litBuffer, Matcher.quoteReplacement(token));
            litCounter++;
        }
        litMatcher.appendTail(litBuffer);
        String maskedCode = litBuffer.toString();

        // Phase 2: Extract comments from the code with masked string literals
        Pattern commentPattern = style.getMultiLineStart() != null ?
                Pattern.compile("(?:" + Pattern.quote(style.getSingleLine()) + ".*)|(?s:" +
                        Pattern.quote(style.getMultiLineStart()) + ".*?" +
                        Pattern.quote(style.getMultiLineEnd()) + ")") :
                Pattern.compile(Pattern.quote(style.getSingleLine()) + ".*");

        Matcher commentMatcher = commentPattern.matcher(maskedCode);
        StringBuffer commentBuffer = new StringBuffer();
        int commentCounter = 0;

        while (commentMatcher.find()) {
            String placeholder = COMMENT_PREFIX + commentCounter + "§";
            commentMap.put(placeholder, commentMatcher.group());
            commentMatcher.appendReplacement(commentBuffer, placeholder);
            commentCounter++;
        }
        commentMatcher.appendTail(commentBuffer);

        // Phase 3: Restore string literals in the comment-masked code
        String processedCode = commentBuffer.toString();
        for (Map.Entry<String, String> entry : literalMap.entrySet()) {
            processedCode = processedCode.replace(entry.getKey(), entry.getValue());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processedCode", processedCode);
        result.put("commentMap", commentMap);
        return result;
    }

    private String stripComments(String code, CommentStyle style, boolean preserveComments) {
        if (preserveComments) {
            return code;
        }
        // First handle string literals to avoid processing comments inside strings
        Map<String, String> literalMap = new HashMap<>();
        Pattern literalPattern = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");
        Matcher litMatcher = literalPattern.matcher(code);
        StringBuffer tempBuffer = new StringBuffer();
        int litIndex = 0;
        while (litMatcher.find()) {
            String literal = litMatcher.group();
            String token = "§STR" + litIndex++ + "§";
            literalMap.put(token, literal);
            litMatcher.appendReplacement(tempBuffer, Matcher.quoteReplacement(token));
        }
        litMatcher.appendTail(tempBuffer);
        String codeWithoutLiterals = tempBuffer.toString();
        // Process line by line
        StringBuilder result = new StringBuilder();
        String[] lines = codeWithoutLiterals.split("\n");
        boolean inMultiLineComment = false;
        for (String line : lines) {
            if (!inMultiLineComment) {
                // Check for single line comments
                int commentIndex = line.indexOf(style.getSingleLine());
                if (commentIndex != -1) {
                    // Keep only the code before the comment
                    line = line.substring(0, commentIndex);
                }
                // Check for start of multi-line comment
                if (style.getMultiLineStart() != null) {
                    int multiLineStart = line.indexOf(style.getMultiLineStart());
                    if (multiLineStart != -1) {
                        int multiLineEnd = line.indexOf(style.getMultiLineEnd(), multiLineStart);
                        if (multiLineEnd != -1) {
                            // Single-line block comment
                            line = line.substring(0, multiLineStart) +
                                    line.substring(multiLineEnd + style.getMultiLineEnd().length());
                        } else {
                            // Start of multi-line comment
                            line = line.substring(0, multiLineStart);
                            inMultiLineComment = true;
                        }
                    }
                }
            } else {
                // Looking for end of multi-line comment
                int multiLineEnd = line.indexOf(style.getMultiLineEnd());
                if (multiLineEnd != -1) {
                    line = line.substring(multiLineEnd + style.getMultiLineEnd().length());
                    inMultiLineComment = false;
                } else {
                    // Still in multi-line comment
                    continue;
                }
            }
            // Add non-empty lines to result
            if (!line.trim().isEmpty()) {
                result.append(line).append("\n");
            }
        }
        // Restore string literals
        String processedCode = result.toString();
        for (Map.Entry<String, String> entry : literalMap.entrySet()) {
            processedCode = processedCode.replace(entry.getKey(), entry.getValue());
        }
        return processedCode;
    }

}