package io.github.simoncalundan.code_anonymizer.service;

import io.github.simoncalundan.code_anonymizer.service.languages.PythonInterpreter;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LanguageInterpreterService {
    private final Map<String, LanguageInterpreter> interpreters = Map.of(
            "java", new JavaInterpreter(),
            "py", new PythonInterpreter()
    );

    public LanguageInterpreter getInterpreterForFile(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return interpreters.getOrDefault(extension, new DefaultInterpreter());
    }
}
