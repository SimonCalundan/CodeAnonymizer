package io.github.simoncalundan.code_anonymizer;

import io.github.simoncalundan.code_anonymizer.cli.AnonymizeCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
@Slf4j
public class CodeAnonymizerApplication implements CommandLineRunner {
    private final AnonymizeCommand anonymizeCommand;
    private final CommandLine.IFactory factory;

    public CodeAnonymizerApplication(AnonymizeCommand anonymizeCommand, CommandLine.IFactory factory) {
        this.anonymizeCommand = anonymizeCommand;
        this.factory = factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(CodeAnonymizerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        int exitCode = new CommandLine(anonymizeCommand, factory).execute(args);
        System.exit(exitCode);
    }
}
