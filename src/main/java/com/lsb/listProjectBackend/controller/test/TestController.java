package com.lsb.listProjectBackend.controller.test;

import com.lsb.listProjectBackend.domain.common.LsbException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/test")
public class TestController {
    private final ObjectMapper objectMapper;
    private final Path jsonPath;

    public TestController(ObjectMapper objectMapper,
            @Value("${test.json.path:src/test/test.json}") String jsonPath) {
        this.objectMapper = objectMapper;
        this.jsonPath = Path.of(jsonPath);
    }

    @GetMapping("/json")
    public JsonNode json() throws IOException {
        try (var input = Files.newInputStream(jsonPath)) {
            JsonNode result = objectMapper.reader()
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(input);
            if (result == null || result.isMissingNode()) {
                throw new LsbException("JSON file is empty");
            }
            return result;
        } catch (NoSuchFileException e) {
            throw new LsbException("JSON test file not found", HttpStatus.NOT_FOUND);
        } catch (JacksonException e) {
            throw new LsbException("Invalid JSON test file", e);
        }
    }
}
