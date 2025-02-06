package io.github.simoncalundan.code_anonymizer.service;

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

    // removed hardcoded keywords and overloaded anonomizeCode method

    public String anonymizeCode(String sourceCode, String fileName, boolean preserveStringLiterals) {
        LanguageInterpreter interpreter = languageService.getInterpreterForFile(fileName);
        Set<String> keywords = interpreter.getReservedKeywords();
        log.info("Starting code anonymization (preserveStringLiterals: {}", preserveStringLiterals);
        if (sourceCode == null || sourceCode.isEmpty()) {
            log.warn("Received empty or null source code");
            return "";
        }

        try {
            Map<String, String> nameMapping = new HashMap<>();
            int counter = 1;

            Map<String, String> stringLiterals = new HashMap<>();
            String processedCode = sourceCode;

            Pattern stringLiteralPattern = Pattern.compile("(\"[^\"]*\")");
            Matcher stringMatcher = stringLiteralPattern.matcher(sourceCode);
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

            Pattern identifierPattern = Pattern.compile(
                    "\\b(?!(?:" + String.join("|", keywords) + ")\\b)" +
                            "([a-zA-Z_][a-zA-Z0-9_]*)\\b(?!\\s*\\()"
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

            for (Map.Entry<String, String> entry : stringLiterals.entrySet()) {
                finalCode = finalCode.replace(entry.getKey(), entry.getValue());
            }

            log.info("Anonymized code copied to clipboard!");
            return finalCode;
        } catch (Exception ex) {
            log.error("Error during code anonymization", ex);
            throw ex;
        }
    }
}
