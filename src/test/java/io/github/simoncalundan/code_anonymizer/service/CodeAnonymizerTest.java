package io.github.simoncalundan.code_anonymizer.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CodeAnonymizerServiceTest {

    private final CodeAnonymizerServiceImpl anonymizerService = new CodeAnonymizerServiceImpl();

    @Test
    void testEmptyCodeReturnsEmptyString() {
        String result = anonymizerService.anonymizeCode("");
        assertEquals("", result);
    }

    @Test
    void testNullCodeReturnsEmptyString() {
        String result = anonymizerService.anonymizeCode(null);
        assertEquals("", result);
    }

    @Test
    void testBasicVariableAnonymization() {
        String sourceCode = "int userAge = 30;\nString userName = \"John\";";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode);

        assertNotEquals(sourceCode, anonymizedCode);
        assertTrue(anonymizedCode.contains("var1"));
        assertTrue(anonymizedCode.contains("var2"));
        assertFalse(anonymizedCode.contains("userAge"));
        assertFalse(anonymizedCode.contains("userName"));
    }

    @Test
    void testKeywordsNotReplaced() {
        String sourceCode = "if (true) {\n  int x = 10;\n}";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode);

        assertTrue(anonymizedCode.contains("if (true)"),
                "The keyword 'if' should not be replaced");
        assertFalse(anonymizedCode.contains("x"),
                "The variable name should be anonymized");
    }

    @Test
    void testConsistentAnonymization() {
        String sourceCode = "int x = 10; int y = x + 5;";
        String firstPass = anonymizerService.anonymizeCode(sourceCode);
        String secondPass = anonymizerService.anonymizeCode(sourceCode);

        assertEquals(firstPass, secondPass);
    }
    @Test
    void testComplexCodeStructure() {
        String sourceCode = "public class Example {\n" +
                "    private int count;\n" +
                "    public void calculateTotal(int base) {\n" +
                "        int total = base * count;\n" +
                "    }\n" +
                "}";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode);

        assertTrue(anonymizedCode.contains("public class"), "Class declaration should remain");
        assertTrue(anonymizedCode.contains("public void"), "Method declaration should remain");
        assertFalse(anonymizedCode.contains("count"), "Private variable should be anonymized");
        assertFalse(anonymizedCode.contains("total"), "Local variable should be anonymized");
        assertFalse(anonymizedCode.contains("base"), "Method parameter should be anonymized");
    }

    @Test
    void testLiteralsPreserved() {
        String sourceCode = "int x = 42; boolean flag = true; String message = \"Hello\";";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, true);
        System.out.println("source: " + sourceCode);
        System.out.println("anonymized: " + anonymizedCode);

        assertTrue(anonymizedCode.contains("42"), "Numeric literals should be preserved");
        assertTrue(anonymizedCode.contains("true"), "Boolean literals should be preserved");
        assertTrue(anonymizedCode.contains("\"Hello\""), "String literals should be preserved");

        // Additional test to ensure variables are anonymized
        assertFalse(anonymizedCode.contains("x"), "Variable names should be anonymized");
        assertFalse(anonymizedCode.contains("flag"), "Variable names should be anonymized");
        assertFalse(anonymizedCode.contains("message"), "Variable names should be anonymized");
    }

    @Test
    void testLiteralsNotPreserved() {
        String sourceCode = "int x = 42; boolean flag = true; String message = \"Hello\";";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, false);
        System.out.println("source " + sourceCode );
        System.out.println("anonymized " + anonymizedCode );

        assertTrue(anonymizedCode.contains("42"), "Numeric literals should be preserved");
        assertTrue(anonymizedCode.contains("true"), "Boolean literals should be preserved");
        assertFalse(anonymizedCode.contains("\"Hello\""), "String literals should be anonymized");
    }
}
