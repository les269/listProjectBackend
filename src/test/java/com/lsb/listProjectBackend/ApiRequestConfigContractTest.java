package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.mapper.connection.ApiRequestConfigMapper;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class ApiRequestConfigContractTest {
    private final ObjectMapper json = new ObjectMapper();
    private final ApiRequestConfigMapper mapper = Mappers.getMapper(ApiRequestConfigMapper.class);

    @Test
    void preservesFrontendSettingsThroughJsonAndEntityMapping() {
        String source = """
                {
                  "apiId": "example", "description": "Example API",
                  "createdTime": "2026-11-09T10:00:00", "updatedTime": "2026-11-10T12:30:00",
                  "config": {
                    "httpMethod": "post", "endpointUrl": "https://example.com",
                    "requestBody": "", "bodyType": "form-data",
                    "httpParams": [
                      {"seq": 1, "name": "flag", "value": "", "enabled": true},
                      {"seq": 2, "name": "flag", "value": "two", "enabled": false}
                    ],
                    "bodyFormFields": [{"seq": 3, "name": "title", "value": "hello", "enabled": true}],
                    "timeoutMs": 5000, "enabled": false, "successMessage": "Done"
                  }
                }
                """;
        ApiRequestConfigTO input = json.readValue(source, ApiRequestConfigTO.class);
        ApiRequestConfigTO result = mapper.toDomain(mapper.toEntity(input));
        assertEquals(json.readTree(source), json.readTree(json.writeValueAsString(result)));
        assertEquals(1, result.config().getHttpParams().getFirst().getSeq());
        assertEquals(2, result.config().getHttpParams().get(1).getSeq());
        assertEquals(3, result.config().getBodyFormFields().getFirst().getSeq());
        assertTrue(result.config().getHttpParams().getFirst().isEnabled());
        assertFalse(result.config().getHttpParams().get(1).isEnabled());
        assertEquals("hello", result.config().getBodyFormFields().getFirst().getValue());
    }

    @Test
    void preservesTimestampsThroughJsonAndEntityMapping() {
        var input = json.readValue("""
                {"apiId":"a","description":"","config":{},
                 "createdTime":"2026-11-09T10:00:00","updatedTime":"2026-11-10T12:30:00"}
                """, ApiRequestConfigTO.class);
        var result = mapper.toDomain(mapper.toEntity(input));
        assertEquals(java.time.LocalDateTime.of(2026, 11, 9, 10, 0), result.createdTime());
        assertEquals(java.time.LocalDateTime.of(2026, 11, 10, 12, 30), result.updatedTime());
        var encoded = json.readTree(json.writeValueAsString(result));
        assertEquals("2026-11-09T10:00:00", encoded.get("createdTime").asText());
        assertEquals("2026-11-10T12:30:00", encoded.get("updatedTime").asText());
        assertFalse(encoded.has("date"));
    }

    @Test
    void acceptsOptionalSettingsAndOmittedTimestamps() {
        var config = json.readValue("""
                {"apiId":"a","description":"",
                 "config":{"httpMethod":"get","endpointUrl":"https://example.com","requestBody":""}}
                """, ApiRequestConfigTO.class);
        var result = mapper.toDomain(mapper.toEntity(config));
        assertNull(result.createdTime());
        assertNull(result.updatedTime());
        var encoded = json.readTree(json.writeValueAsString(result));
        assertFalse(encoded.has("createdTime"));
        assertFalse(encoded.has("updatedTime"));
        assertFalse(encoded.has("date"));
        assertNull(config.config().getEnabled());
        assertNull(config.config().getTimeoutMs());
        assertNull(config.config().getBodyType());
    }

    @Test
    void supportsAllFrontendBodyTypeValues() {
        for (var type : Global.ApiRequestBodyType.values()) {
            String encoded = json.writeValueAsString(type);
            assertEquals('"' + type.getValue() + '"', encoded);
            assertEquals(type, json.readValue(encoded, Global.ApiRequestBodyType.class));
        }
    }

}
