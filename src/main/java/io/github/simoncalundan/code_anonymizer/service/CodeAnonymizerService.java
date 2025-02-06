package io.github.simoncalundan.code_anonymizer.service;

public interface CodeAnonymizerService {
//    String anonymizeCode(String sourceCode, String fileName);
    String anonymizeCode(String sourceCode, String fileName, boolean preserveStringLiterals);
}
