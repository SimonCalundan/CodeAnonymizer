package io.github.simoncalundan.code_anonymizer.cli;

import io.github.simoncalundan.code_anonymizer.service.CodeAnonymizerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component
@CommandLine.Command(name = "anonymize", description = "Anonymize source code files")
public class AnonymizeCommand implements Runnable {

    private final CodeAnonymizerService anonymizerService;

    @CommandLine.Parameters(index = "0", description = "The file to anonymize")
    private String filePath;

    @CommandLine.Option(names = {"--preserve-strings"},
            description = "Preserve string literals (default: true)")
    private boolean preserveStringLiterals = true;

    public AnonymizeCommand(CodeAnonymizerService anonymizerService) {
        this.anonymizerService = anonymizerService;
    }

    @Override
    public void run() {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("Error: File does not exist - {}", filePath);
            return;
        }
        if (!file.isFile()) {
            log.error("Error: Path is not a file - {}", filePath);
            return;
        }
        if (!file.canRead()) {
            log.error("Error: Cannot read file - {}", filePath);
            return;
        }

        try {
            String sourceCode = Files.readString(file.toPath());
            if (sourceCode.trim().isEmpty()) {
                log.error("Error: File is empty");
                return;
            }
            String anonymizedCode = anonymizerService.anonymizeCode(sourceCode, preserveStringLiterals);
            System.out.println(anonymizedCode);
            log.info("Code anonymized successfully!");
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
