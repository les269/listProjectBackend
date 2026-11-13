package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.domain.connection.SingalCallApiReqTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.*;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetRepository;
import com.lsb.listProjectBackend.service.connection.ApiRequestConfigService;
import com.lsb.listProjectBackend.service.dataset.GroupDatasetServiceImpl;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupDatasetApiResponseTest {
    private final GroupDatasetRepository groups = mock(GroupDatasetRepository.class);
    private final GroupDatasetDataRepository rows = mock(GroupDatasetDataRepository.class);
    private final ApiRequestConfigService apis = mock(ApiRequestConfigService.class);
    private final GroupDatasetServiceImpl service = new GroupDatasetServiceImpl(groups, apis, rows, null);
    private final ApiRequestConfigTO api = new ApiRequestConfigTO("api", "", null, null, null);
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setup() {
        var reference = new GroupDatasetApi();
        reference.setApiId("api");
        var config = new GroupDatasetConfig();
        config.setType(Global.GroupDatasetConfigType.api);
        config.setByKey("id");
        config.setGroupDatasetApiList(List.of(reference));
        var group = new GroupDataset();
        group.setConfig(config);
        when(groups.findById("group")).thenReturn(Optional.of(group));
        when(apis.getListById(List.of("api"))).thenReturn(List.of(api));
    }

    @Test
    void storesObjectsAndObjectArraysAsRows() throws Exception {
        for (String response : List.of("{\"id\":\"one\",\"value\":true}",
                "[{\"id\":\"one\",\"value\":true}]")) {
            clearInvocations(rows);
            when(apis.singalCallApi(new SingalCallApiReqTO(api.apiId(), json.createObjectNode()))).thenReturn(json.readTree(response));
            service.refreshGroupDataset("group");
            var captor = org.mockito.ArgumentCaptor.forClass(Iterable.class);
            verify(rows).saveAll(captor.capture());
            var stored = (GroupDatasetData) captor.getValue().iterator().next();
            assertEquals("group", stored.getGroupName());
            assertEquals("one", stored.getPrimeValue());
            assertEquals(true, stored.getJson().get("value"));
        }
    }

    @Test
    void savesOnceAfterAllApiCallsAndKeepsSuccessfulResultsWhenOneFails() throws Exception {
        var failedApi = new ApiRequestConfigTO("failed", "", null, null, null);
        var lastApi = new ApiRequestConfigTO("last", "", null, null, null);
        when(apis.getListById(List.of("api"))).thenReturn(List.of(api, failedApi, lastApi));
        when(apis.singalCallApi(new SingalCallApiReqTO(api.apiId(), json.createObjectNode()))).thenReturn(json.readTree("[{\"id\":\"one\"},{\"id\":\"two\"}]"));
        when(apis.singalCallApi(new SingalCallApiReqTO(failedApi.apiId(), json.createObjectNode()))).thenThrow(new IllegalStateException("API unavailable"));
        when(apis.singalCallApi(new SingalCallApiReqTO(lastApi.apiId(), json.createObjectNode()))).thenReturn(json.readTree("{\"id\":\"three\"}"));

        service.refreshGroupDataset("group");

        var captor = org.mockito.ArgumentCaptor.forClass(Iterable.class);
        var order = inOrder(apis, rows);
        order.verify(apis).getListById(List.of("api"));
        order.verify(apis).singalCallApi(new SingalCallApiReqTO(api.apiId(), json.createObjectNode()));
        order.verify(apis).singalCallApi(new SingalCallApiReqTO(failedApi.apiId(), json.createObjectNode()));
        order.verify(apis).singalCallApi(new SingalCallApiReqTO(lastApi.apiId(), json.createObjectNode()));
        order.verify(rows).saveAll(captor.capture());
        verifyNoMoreInteractions(rows);
        var savedIds = new java.util.ArrayList<String>();
        for (Object row : captor.getValue()) {
            savedIds.add(((GroupDatasetData) row).getPrimeValue());
        }
        assertEquals(List.of("one", "two", "three"), savedIds);
    }

    @Test
    void skipsDatabaseWriteWhenNoApiReturnsRows() throws Exception {
        when(apis.singalCallApi(new SingalCallApiReqTO(api.apiId(), json.createObjectNode()))).thenReturn(json.readTree("[]"));
        service.refreshGroupDataset("group");
        verifyNoInteractions(rows);
    }

    @Test
    void doesNotStoreScalarOrMixedArrayResponses() throws Exception {
        for (String response : List.of("true", "42", "\"text\"", "[{\"id\":\"one\"},false]")) {
            when(apis.singalCallApi(new SingalCallApiReqTO(api.apiId(), json.createObjectNode()))).thenReturn(json.readTree(response));
            service.refreshGroupDataset("group");
        }
        verifyNoInteractions(rows);
    }
}
