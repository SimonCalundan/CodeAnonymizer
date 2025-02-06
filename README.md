# CodeAnonymizer

A command-line tool that anonymizes source code while preserving its structural integrity and logic. CodeAnonymizer replaces variable and method names with generic alternatives, making it ideal for sharing code snippets without exposing sensitive information.

## Key Features
- **Smart Variable Anonymization**: Replaces variable and method names while maintaining code readability
- **Structure Preservation**: Retains the original code structure and logic
- **Multi-Language Support**: Compatible with multiple programming languages
- **Terminal Output**: Displays anonymized code directly in your terminal
- **Configurable Settings**: Flexible options for string literal preservation

## Installation
1. Ensure Java 11+ is installed
2. Install Maven from https://maven.apache.org/download.cgi
3. Clone and build:
```bash
git clone https://github.com/SimonCalundan/CodeAnonymizer.git
cd CodeAnonymizer
mvn clean package
```

## Usage
```bash
cd target
java -jar code-anonymizer.jar <file-path> [options]
```

### Options
| Option | Description | Default |
|--------|-------------|---------|
| `--preserve-strings` | Maintain original string literals | `true` |

### Examples
```bash
# Basic usage
java -jar code-anonymizer.jar mycode.java

# Without string preservation
java -jar code-anonymizer.jar mycode.java --preserve-strings=false

# Save output to file
java -jar code-anonymizer.jar mycode.java > anonymized.java
```

## Contributing
Contributions welcome! See [Contributing Guidelines](CONTRIBUTING.md).

## License
MIT License - see [LICENSE](LICENSE).