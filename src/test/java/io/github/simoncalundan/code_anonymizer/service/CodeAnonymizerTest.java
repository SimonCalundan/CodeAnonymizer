package io.github.simoncalundan.code_anonymizer.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CodeAnonymizerServiceTest {
    public static final String TEST_FILE_NAME = "test.java";

    private final LanguageInterpreterService languageService = new LanguageInterpreterService();

    private final CodeAnonymizerServiceImpl anonymizerService = new CodeAnonymizerServiceImpl(
            languageService);

    private void compareResults(String sourceCode, String anonymizedCode) {
        log.info("Source code = {}", sourceCode);
        log.info("anonymize code = {}", anonymizedCode);
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
        String sourceCode = "int userAge = 30;\nString userName = \"John\";";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);


        assertNotEquals(sourceCode, anonymizedCode);
        assertTrue(anonymizedCode.contains("var1"));
        assertTrue(anonymizedCode.contains("var2"));
        assertFalse(anonymizedCode.contains("userAge"));
        assertFalse(anonymizedCode.contains("userName"));
    }

    @Test
    void testKeywordsNotReplaced() {
        String sourceCode = "if (true) {\n  int x = 10;\n}";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("if (true)"),
                "The keyword 'if' should not be replaced");
        assertFalse(anonymizedCode.contains("x"),
                "The variable name should be anonymized");
    }

    @Test
    void testConsistentAnonymization() {
        String sourceCode = "int x = 10; int y = x + 5;";
        String firstPass = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        String secondPass = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);

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
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("public class"), "Class declaration should remain");
        assertTrue(anonymizedCode.contains("public void"), "Method declaration should remain");
        assertFalse(anonymizedCode.contains("count"), "Private variable should be anonymized");
        assertFalse(anonymizedCode.contains("total"), "Local variable should be anonymized");
        assertFalse(anonymizedCode.contains("base"), "Method parameter should be anonymized");
    }

    @Test
    void testLiteralsPreserved() {
        String sourceCode = "int x = 42; boolean flag = true; String message = \"Hello\";";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

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
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, false, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("42"), "Numeric literals should be preserved");
        assertTrue(anonymizedCode.contains("true"), "Boolean literals should be preserved");
        assertFalse(anonymizedCode.contains("\"Hello\""), "String literals should be anonymized");
    }

    @Test
    public void testCollectionsAndInterfacesPreservation() {
        String sourceCode = """
                public class TestClass {
                    private List<String> items = new ArrayList<>();
                    private Set<Integer> numbers = new HashSet<>();
                    private Map<String, Object> mapping = new HashMap<>();
                }
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        // Verify collections and interfaces are preserved
        assertTrue(anonymizedCode.contains("List"));
        assertTrue(anonymizedCode.contains("ArrayList"));
        assertTrue(anonymizedCode.contains("Set"));
        assertTrue(anonymizedCode.contains("HashSet"));
        assertTrue(anonymizedCode.contains("Map"));
        assertTrue(anonymizedCode.contains("HashMap"));
    }

    @Test
    public void testUtilityClassesPreservation() {
        String sourceCode = """
                public class TestClass {
                    String text = new String("test");
                    Integer number = Integer.valueOf(42);
                    Double decimal = Double.parseDouble("3.14");
                    StringBuilder builder = new StringBuilder();
                }
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("String"));
        assertTrue(anonymizedCode.contains("Integer"));
        assertTrue(anonymizedCode.contains("Double"));
        assertTrue(anonymizedCode.contains("StringBuilder"));
    }

    @Test
    public void testAnnotationsPreservation() {
        String sourceCode = """
                @Service
                public class TestClass {
                    @Override
                    @SuppressWarnings("unused")
                    @Deprecated
                    public void testMethod() {}
                }
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("@Service"));
        assertTrue(anonymizedCode.contains("@Override"));
        assertTrue(anonymizedCode.contains("@SuppressWarnings"));
        assertTrue(anonymizedCode.contains("@Deprecated"));
    }

    @Test
    public void testGenericTypeParametersPreservation() {
        String sourceCode = """
                public class TestClass<T, K, V> {
                    public <E> void testMethod(List<E> items) {
                        List<T> list = new ArrayList<T>();
                        Map<K, V> map = new HashMap<>();
                    }
                }
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("<T, K, V>"));
        assertTrue(anonymizedCode.contains("<E>"));
        assertTrue(anonymizedCode.contains("List<T>"));
        assertTrue(anonymizedCode.contains("Map<K, V>"));

        assertTrue(anonymizedCode.contains("class var1"));
        assertTrue(anonymizedCode.contains("void var2"));

        assertTrue(anonymizedCode.contains("var3")); // items
        assertTrue(anonymizedCode.contains("var4")); // list
        assertTrue(anonymizedCode.contains("var5")); // map
    }

    @Test
    public void testFunctionalInterfacesPreservation() {
        String sourceCode = """
                public class TestClass {
                    private Function<String, Integer> parser = Integer::parseInt;
                    private Consumer<String> printer = System.out::println;
                    private Supplier<String> getter = () -> "test";
                    private Predicate<Integer> isPositive = num -> num > 0;
                }
                """;
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("Function"));
        assertTrue(anonymizedCode.contains("Consumer"));
        assertTrue(anonymizedCode.contains("Supplier"));
        assertTrue(anonymizedCode.contains("Predicate"));
    }

    @Test
    void testPreserveInlineComments() {
        String sourceCode = """
                int age = 25; // User age
                String name = "John"; // User name""";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("// User age"));
        assertTrue(anonymizedCode.contains("// User name"));
        assertFalse(anonymizedCode.contains("int age"));
        assertFalse(anonymizedCode.contains("String name"));
    }

    @Test
    void testStripInlineComments() {
        String sourceCode = """
                int age = 25; // User age
                String name = "John"; // User name""";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, false);
        compareResults(sourceCode, anonymizedCode);

        assertFalse(anonymizedCode.contains("// User age"));
        assertFalse(anonymizedCode.contains("// User name"));
        assertFalse(anonymizedCode.contains("age"));
        assertFalse(anonymizedCode.contains("name"));
    }

    @Test
    void testPreserveBlockComments() {
        String sourceCode = """
                /* User information section */
                int age = 25;
                /* Store user's full name
                 * in this variable */
                String name = "John";""";

        String expectedCode = """
                /* User information section */
                int var1 = 25;
                /* Store user's full name
                 * in this variable */
                String var2 = "John";""";

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

        assertTrue(anonymizedCode.contains("/* User information section */"),
                "First block comment should be preserved");
        assertTrue(anonymizedCode.contains("/* Store user's full name"),
                "Second block comment should be preserved");
        assertFalse(anonymizedCode.contains("age"),
                "Original variable name 'age' should be anonymized");
        assertTrue(anonymizedCode.contains("var1"),
                "First variable should be anonymized to 'var1'");
        assertTrue(anonymizedCode.contains("var2"),
                "Second variable should be anonymized to 'var2'");
    }

    @Test
    void testStripBlockComments() {
        String sourceCode = """
                /* User information section */
                int age = 25;
                /* Store user's full name
                 * in this variable */
                String name = "John";""";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, false);
        compareResults(sourceCode, anonymizedCode);

        assertFalse(anonymizedCode.contains("/* User information section */"));
        assertFalse(anonymizedCode.contains("/* Store user's full name"));
        assertFalse(anonymizedCode.contains("age"));
        assertFalse(anonymizedCode.contains("name"));
    }

    @Test
    void testMixedCommentsPreservation() {
        String sourceCode = """
                /* User data */
                int age = 25; // Age in years
                // Current user
                String name = "John"; /* Full name */""";
        String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, TEST_FILE_NAME, true, true);
        compareResults(sourceCode, anonymizedCode);

        assertTrue(anonymizedCode.contains("/* User data */"));
        assertTrue(anonymizedCode.contains("// Age in years"));
        assertTrue(anonymizedCode.contains("// Current user"));
        assertTrue(anonymizedCode.contains("/* Full name */"));
    }
}
