package io.github.simoncalundan.code_anonymizer.service.languages;

import io.github.simoncalundan.code_anonymizer.model.CommentStyle;
import io.github.simoncalundan.code_anonymizer.service.LanguageInterpreter;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class PythonInterpreter implements LanguageInterpreter {
    private static final Set<String> KEYWORDS = Set.of(
            // Control flow
            "if", "elif", "else", "for", "while", "break", "continue", "return",
            "try", "except", "finally", "raise", "with", "match", "case",
            // Function and class definition
            "def", "class", "lambda", "pass", "yield", "async", "await",
            // Module-related
            "import", "from", "as", "global", "nonlocal",
            // Logical operators
            "and", "or", "not", "is", "in",
            // Type-related
            "type", "isinstance", "issubclass", "super", "object",
            // Built-in types
            "int", "float", "str", "bool", "list", "tuple", "dict", "set",
            "bytes", "bytearray", "complex", "frozenset",
            // Built-in functions
            "len", "print", "range", "map", "filter", "zip", "enumerate",
            "min", "max", "sum", "any", "all", "sorted", "reversed",
            // Built-in exceptions
            "Exception", "TypeError", "ValueError", "RuntimeError", "AttributeError",
            "IndexError", "KeyError", "NameError", "OSError", "FileNotFoundError",
            // Context managers
            "open", "iter", "next",
            // Decorators
            "property", "staticmethod", "classmethod",
            // Special methods
            "__init__", "__str__", "__repr__", "__len__", "__get__", "__set__",
            "__call__", "__enter__", "__exit__", "__iter__", "__next__",
            // Common third-party imports
            "self", "cls",
            // Literal values
            "True", "False", "None",
            // Type hints
            "Optional", "Union", "List", "Dict", "Set", "Tuple", "Any",
            "Callable", "Iterator", "Iterable", "Generator"
    );

    @Override
    public Set<String> getReservedKeywords() {
        return KEYWORDS;
    }

    @Override
    public CommentStyle getCommentStyle() {
        return CommentStyle.PYTHON_STYLE;
    }

    @Override
    public boolean isValidIdentifier(String name) {
        if (name == null || name.isEmpty() || KEYWORDS.contains(name)) {
            return false;
        }

        // Python identifier rules:
        // - First character must be a letter or underscore
        // - Subsequent characters can be letters, numbers, or underscores
        char firstChar = name.charAt(0);
        if (!Character.isLetter(firstChar) && firstChar != '_') {
            return false;
        }

        // Check remaining characters
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }

        return true;
    }
}
