# CodeAnonymizer

A powerful command-line tool that anonymizes source code while preserving its structural integrity and logic. CodeAnonymizer automatically replaces variable and method names with generic alternatives, making it ideal for sharing code snippets without exposing sensitive information.

## Key Features

- **Smart Variable Anonymization**: Intelligently replaces variable and method names while maintaining code readability
- **Structure Preservation**: Retains the original code structure, logic, and functionality
- **Multi-Language Support**: Compatible with multiple programming languages
- **Clipboard Integration**: Automatically copies anonymized code to your clipboard
- **Configurable Settings**: Flexible options for string literal preservation and more

## Installation

1. Ensure you have Java 11 or higher installed
2. Clone the repository:
```bash
git clone https://github.com/yourusername/code-anonymizer.git
cd code-anonymizer
```

3. Build with Maven:
```bash
./mvnw clean package
```

## Usage

Basic command syntax:
```bash
java -jar code-anonymizer.jar <file-path> [options]
```

### Options

| Option | Description | Default |
|--------|-------------|---------|
| `--preserve-strings` | Maintain original string literals | `true` |

### Examples

Anonymize a Java file with default settings:
```bash
java -jar code-anonymizer.jar mycode.java
```

Anonymize code without preserving strings:
```bash
java -jar code-anonymizer.jar mycode.java --preserve-strings=false
```

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](CONTRIBUTING.md) before submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.