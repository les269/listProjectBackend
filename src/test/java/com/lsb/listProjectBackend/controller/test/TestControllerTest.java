package com.lsb.listProjectBackend.controller.test;

import com.lsb.listProjectBackend.config.GlobalDefaultExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestControllerTest {
    @TempDir
    Path directory;

    private MockMvc mvc() {
        return MockMvcBuilders.standaloneSetup(new TestController(
                        new ObjectMapper(), directory.resolve("test.json").toString()))
                .setControllerAdvice(new GlobalDefaultExceptionHandler()).build();
    }

    @Test
    void returnsParsedObjectAndReadsFileAgainOnEachRequest() throws Exception {
        Path file = directory.resolve("test.json");
        Files.writeString(file, """
                {"items":[{"name":"測試","count":2}],"enabled":true}
                """);
        var mvc = mvc();
        mvc.perform(get("/api/test/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("測試"))
                .andExpect(jsonPath("$.items[0].count").value(2))
                .andExpect(jsonPath("$.enabled").value(true));
        Files.writeString(file, "[{\"id\":3}]");
        mvc.perform(get("/api/test/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void returnsNotFoundWhenFileDoesNotExist() throws Exception {
        mvc().perform(get("/api/test/json")).andExpect(status().isNotFound());
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        Files.writeString(directory.resolve("test.json"), "{broken");
        mvc().perform(get("/api/test/json")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        Files.writeString(directory.resolve("test.json"), "   ");
        mvc().perform(get("/api/test/json")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMultipleJsonDocuments() throws Exception {
        Files.writeString(directory.resolve("test.json"), "{} {}");
        mvc().perform(get("/api/test/json")).andExpect(status().isBadRequest());
    }
}
