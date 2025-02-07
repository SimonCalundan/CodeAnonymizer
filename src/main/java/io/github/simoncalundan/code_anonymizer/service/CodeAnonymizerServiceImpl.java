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

        // Store original comment locations before processing
        Map<String, String> commentMap = new HashMap<>();
        String processedCode = sourceCode;
        if (!preserveComments) {
            processedCode = stripComments(sourceCode, interpreter.getCommentStyle(), preserveComments);
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
                        "([a-zA-Z_][a-zA-Z0-9_]*)\\b"
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

        log.info("File successfully anonymized!");
        return finalCode;
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
