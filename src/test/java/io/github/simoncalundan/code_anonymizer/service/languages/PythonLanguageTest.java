package io.github.simoncalundan.code_anonymizer.service.languages;

import io.github.simoncalundan.code_anonymizer.service.CodeAnonymizerService;
import io.github.simoncalundan.code_anonymizer.service.CodeAnonymizerServiceImpl;
import io.github.simoncalundan.code_anonymizer.service.LanguageInterpreterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PythonInterpreterTest {
    public static final String TEST_FILE_NAME = "test.py";

    private final LanguageInterpreterService languageService = new LanguageInterpreterService();

    private final CodeAnonymizerService anonymizerService = new CodeAnonymizerServiceImpl(
            languageService);

    private void compareResults(String sourceCode, String anonymizedCode) {
        log.info("Source code = \n{}", sourceCode);
        log.info("anonymize code =\n{}", anonymizedCode);
    }

    @Test
    void testEmptyCodeReturnsEmptyString() {
        String result = anonymizerService.anonymizeCode("", TEST_FILE_NAME, false, true);
        assertEquals("", result);
    }

    @Test
    void testNullCodeReturnsEmptyString() {
        String result = anonymizerService.anonymizeCode(null, TEST_FILE_NAME, false, true);
        assertEquals("", result);
    }

    @Test
    void testBasicVariableAnonymization() {
        String sourceCode = "user_age = 30\nuser_name = \"John\"";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertNotEquals(sourceCode, anonymizedCode);
        assertTrue(anonymizedCode.contains("var1"));
        assertTrue(anonymizedCode.contains("var2"));
        assertFalse(anonymizedCode.contains("user_age"));
        assertFalse(anonymizedCode.contains("user_name"));
    }

    @Test
    void testKeywordsNotReplaced() {
        String sourceCode = "if True:\n    x = 10";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("if True"),
                "The keyword 'if' and 'True' should not be replaced");
        assertFalse(anonymizedCode.contains("x"),
                "The variable name should be anonymized");
    }

    @Test
    void testConsistentAnonymization() {
        String sourceCode = "x = 10\ny = x + 5";
        String firstPass = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        String secondPass = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);

        assertEquals(firstPass, secondPass);
    }

    @Test
    void testComplexCodeStructure() {
        String sourceCode = """
                class Example:
                    def __init__(self):
                        self.count = 0
                
                    def calculate_total(self, base):
                        total = base * self.count
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("class"), "Class declaration should remain");
        assertTrue(anonymizedCode.contains("def"), "Method declaration should remain");
        assertFalse(anonymizedCode.contains("count"), "Instance variable should be anonymized");
        assertFalse(anonymizedCode.contains("total"), "Local variable should be anonymized");
        assertFalse(anonymizedCode.contains("base"), "Method parameter should be anonymized");
    }

    @Test
    void testLiteralsPreserved() {
        String sourceCode = "x = 42\nflag = True\nmessage = \"Hello\"";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("42"), "Numeric literals should be preserved");
        assertTrue(anonymizedCode.contains("True"), "Boolean literals should be preserved");
        assertTrue(anonymizedCode.contains("\"Hello\""), "String literals should be preserved");

        assertFalse(anonymizedCode.contains("x"), "Variable names should be anonymized");
        assertFalse(anonymizedCode.contains("flag"), "Variable names should be anonymized");
        assertFalse(anonymizedCode.contains("message"), "Variable names should be anonymized");
    }

    @Test
    void testLiteralsNotPreserved() {
        String sourceCode = "x = 42\nflag = True\nmessage = \"Hello\"";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("42"), "Numeric literals should be preserved");
        assertTrue(anonymizedCode.contains("True"), "Boolean literals should be preserved");
        assertFalse(anonymizedCode.contains("\"Hello\""), "String literals should be anonymized");
    }

    @Test
    void testBuiltInTypesPreservation() {
        String sourceCode = """
                items = list()
                numbers = set()
                mapping = dict()
                my_tuple = tuple()
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("list"));
        assertTrue(anonymizedCode.contains("set"));
        assertTrue(anonymizedCode.contains("dict"));
        assertTrue(anonymizedCode.contains("tuple"));
    }

    @Test
    void testDecoratorsPreservation() {
        String sourceCode = """
                class TestClass:
                    @property
                    def value(self):
                        return 42
                
                    @classmethod
                    def from_string(cls, text):
                        return cls()
                
                    @staticmethod
                    def helper():
                        return True
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("@property"));
        assertTrue(anonymizedCode.contains("@classmethod"));
        assertTrue(anonymizedCode.contains("@staticmethod"));
    }

    @Test
    void testStripSingleLineComments() {
        String sourceCode = """
                age = 25  # User age
                name = "John"  # User name""";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, false);
        compareResults(sourceCode, anonymizedCode);

        assertFalse(anonymizedCode.contains("# User age"));
        assertFalse(anonymizedCode.contains("# User name"));
        assertFalse(anonymizedCode.contains("age"));
        assertFalse(anonymizedCode.contains("name"));
    }

    @Test
    void testPreserveDocstrings() {
        String sourceCode = """
                \"\"\"User information module\"\"\"
                age = 25
                
                def get_name():
                    \"\"\"Returns the user's full name
                    from the database
                    \"\"\"
                    name = "John"
                    return name""";

        String expectedCode = """
                \"\"\"User information module\"\"\"
                var1 = 25
                
                def var2():
                    \"\"\"Returns the user's full name
                    from the database
                    \"\"\"
                    var3 = "John"
                    return var3""";

        String anonymizedCode = anonymizerService.anonymizeCode(
                sourceCode,
                TEST_FILE_NAME,
                true,  // preserveStringLiterals
                true   // preserveComments
        );

        compareResults(sourceCode, anonymizedCode);

        String normalizedExpected = expectedCode.replaceAll("\r\n", "\n");
        String normalizedActual = anonymizedCode.replaceAll("\r\n", "\n");

        assertEquals(normalizedExpected, normalizedActual,
                "Anonymized code should match expected output exactly");

        assertTrue(anonymizedCode.contains("\"\"\"User information module\"\"\""),
                "Module docstring should be preserved");
        assertTrue(anonymizedCode.contains("\"\"\"Returns the user's full name"),
                "Function docstring should be preserved");
    }

    @Test
    void testStripDocstrings() {
        //TODO - test pass, incorrect result
        String sourceCode = """
                \"\"\"User information module\"\"\"
                age = 25
                def get_name():
                    \"\"\"Returns the user's full name
                    from the database
                    \"\"\"
                    name = "John"
                    return name""";
        String anonymizedCode = anonymizerService.anonymizeCode(
                sourceCode,
                TEST_FILE_NAME,
                false,  // preserveStringLiterals - change to false to strip all strings including docstrings
                false   // preserveComments
        );
        compareResults(sourceCode, anonymizedCode);

        // The docstrings should be replaced with anonymized string literals
        assertFalse(anonymizedCode.contains("\"\"\"User information module\"\"\""));
        assertFalse(anonymizedCode.contains("\"\"\"Returns the user's full name"));
        assertTrue(anonymizedCode.contains("\"var1\""), "Docstring should be replaced with anonymized string");

        // Other variables should still be anonymized
        assertFalse(anonymizedCode.contains("age"));
        assertFalse(anonymizedCode.contains("get_name"));
        assertFalse(anonymizedCode.contains("name"));
    }

    @Test
    void testPreserveSingleLineComments() {
        String sourceCode = """
                age = 25  # User age
                name = "John"  # User name""";

        String expectedCode = """
                var1 = 25  # User age
                var2 = "John"  # User name""";

        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        String normalizedExpected = expectedCode.replaceAll("\r\n", "\n").trim();
        String normalizedActual = anonymizedCode.replaceAll("\r\n", "\n").trim();

        assertEquals(normalizedExpected, normalizedActual,
                "Anonymized code should match expected output exactly");

        assertTrue(anonymizedCode.contains("# User age"));
        assertTrue(anonymizedCode.contains("# User name"));
        assertFalse(anonymizedCode.contains("age ="));
        assertFalse(anonymizedCode.contains("name ="));
    }

    @Test
    void testMixedCommentsPreservation() {
        String sourceCode = """
                \"\"\"User data module\"\"\"
                age = 25  # Age in years
                # Current user
                name = "John"  # Another comment""";  // Changed to regular comment

        String expectedCode = """
                \"\"\"User data module\"\"\"
                var1 = 25  # Age in years
                # Current user
                var2 = "John"  # Another comment""";

        String anonymizedCode = anonymizerService.anonymizeCode(
                sourceCode,
                TEST_FILE_NAME,
                true,  // preserveStringLiterals
                true   // preserveComments
        );
        compareResults(sourceCode, anonymizedCode);

        String normalizedExpected = expectedCode.replaceAll("\r\n", "\n").trim();
        String normalizedActual = anonymizedCode.replaceAll("\r\n", "\n").trim();

        assertEquals(normalizedExpected, normalizedActual,
                "Anonymized code should match expected output exactly");
    }
}