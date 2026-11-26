package com.lsb.listProjectBackend;

import com.lsb.listProjectBackend.domain.dataset.DatasetQuickRefreshTO;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.entity.dynamic.dataset.*;
import com.lsb.listProjectBackend.mapper.dataset.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.dynamic.dataset.*;
import com.lsb.listProjectBackend.service.dataset.*;
import com.lsb.listProjectBackend.service.spider.SpiderService;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroupDatasetSpiderRefreshTest {
    @TempDir
    java.nio.file.Path directory;
    private final DatasetRepository datasets = mock(DatasetRepository.class);
    private final GroupDatasetRepository groups = mock(GroupDatasetRepository.class);
    private final GroupDatasetDataRepository rows = mock(GroupDatasetDataRepository.class);
    private final DatasetDataRepository datasetRows = mock(DatasetDataRepository.class);
    private final GroupDatasetService groupService = mock(GroupDatasetService.class);
    private final SpiderService spiders = mock(SpiderService.class);
    private final GroupDatasetDataMapper copies = mock(GroupDatasetDataMapper.class);
    private final DatasetServiceImpl service = new DatasetServiceImpl(datasets, rows, groups, groupService,
            spiders, null, datasetRows, null, null, null, copies);
    private final GroupDatasetConfig config = new GroupDatasetConfig();

    @BeforeEach
    void setup() {
        var spider = new GroupDatasetSpider();
        spider.setSpiderId("detail");
        spider.setDefault(true);
        config.setType(Global.GroupDatasetConfigType.spider);
        config.setByKey("id");
        config.setGroupDatasetSpiderList(List.of(spider));
        config.setGroupDatasetFieldList(List.of());
        var group = new GroupDataset();
        group.setConfig(config);
        when(groups.findById("group")).thenReturn(Optional.of(group));
        var datasetConfig = new DatasetConfig();
        datasetConfig.setType(Global.DatasetConfigType.text);
        datasetConfig.setGroupName("group");
        datasetConfig.setScrapyText("one\ntwo");
        datasetConfig.setFieldList(List.of());
        var dataset = new Dataset();
        dataset.setName("dataset");
        dataset.setConfig(datasetConfig);
        when(datasets.findById("dataset")).thenReturn(Optional.of(dataset));
        when(rows.findByGroupName("group")).thenReturn(List.of());
        when(copies.toCopyEntityList(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void executesDefaultSpiderBatchThenSavesAllNewRowsOnce() throws Exception {
        var requests = List.of(new SpiderReqTO("detail", null, List.of("one")),
                new SpiderReqTO("detail", null, List.of("two")));
        when(spiders.executeBatch("detail", requests)).thenReturn(List.of("{\"title\":\"First\"}",
                "{\"title\":\"Second\"}"));

        service.refreshData("dataset");

        var saved = ArgumentCaptor.forClass(Iterable.class);
        var order = inOrder(spiders, rows, datasetRows);
        order.verify(rows).findByGroupName("group");
        order.verify(spiders).executeBatch("detail", requests);
        order.verify(rows).saveAll(saved.capture());
        order.verify(datasetRows).save(any(DatasetData.class));
        var values = new ArrayList<GroupDatasetData>();
        saved.getValue().forEach(value -> values.add((GroupDatasetData) value));
        assertEquals(List.of("one", "two"), values.stream().map(GroupDatasetData::getPrimeValue).toList());
        assertEquals("one", values.getFirst().getJson().get("id"));
        assertEquals("First", values.getFirst().getJson().get("title"));
    }

    @Test
    void emptySpiderResultStillSavesPrimaryKeyAndOtherRows() throws Exception {
        var requests = List.of(new SpiderReqTO("detail", null, List.of("one")),
                new SpiderReqTO("detail", null, List.of("two")));
        when(spiders.executeBatch("detail", requests)).thenReturn(List.of("{}", "{\"title\":\"Second\"}"));

        service.refreshData("dataset");

        var saved = ArgumentCaptor.forClass(Iterable.class);
        verify(rows).saveAll(saved.capture());
        var values = new ArrayList<GroupDatasetData>();
        saved.getValue().forEach(value -> values.add((GroupDatasetData) value));
        assertEquals(List.of("one", "two"), values.stream().map(GroupDatasetData::getPrimeValue).toList());
        assertEquals(Map.of("id", "one"), values.getFirst().getJson());
        assertEquals("Second", values.getLast().getJson().get("title"));
    }

    @Test
    void missingDefaultSpiderDoesNotExecuteOrSaveNewGroupRows() throws Exception {
        config.setGroupDatasetSpiderList(null);
        service.refreshData("dataset");
        verifyNoInteractions(spiders);
        verify(rows, never()).saveAll(any());
        verify(datasetRows).save(any(DatasetData.class));
    }

    @Test
    void quickRefreshReadsSpiderId() {
        var json = new tools.jackson.databind.ObjectMapper();
        var newRequest = json.readValue("""
                {"datasetName":"dataset","spiderId":"detail","quickRefreshType":"url","url":"https://example.test"}
                """, DatasetQuickRefreshTO.class);
        assertEquals("detail", newRequest.spiderId());
    }

    @Test
    void quickRefreshUsesSpider() throws Exception {
        var singleRefresh = spy(service);
        doNothing().when(singleRefresh).refreshData("dataset");
        doReturn(new com.lsb.listProjectBackend.domain.dataset.DatasetDataTO("dataset",
                List.of(Map.of("id", "one", "title", "Updated")), null, null))
                .when(singleRefresh).getDatasetDataByName("dataset");
        when(spiders.executeByUrl("detail", "https://example.test/one"))
                .thenReturn("{\"id\":\"one\",\"title\":\"Updated\"}");

        var result = singleRefresh.quickRefresh(new DatasetQuickRefreshTO("id", "one", "detail", "dataset",
                "https://example.test/one", Global.QuickRefreshType.url, List.of()));

        assertEquals("Updated", result.get("title"));
        verify(spiders).executeByUrl("detail", "https://example.test/one");
        verify(rows).save(any(GroupDatasetData.class));
    }

    @Test
    void quickRefreshRequiresSpiderSelectionBeforeWritingRows() {
        assertThrows(IllegalArgumentException.class, () -> service.quickRefresh(new DatasetQuickRefreshTO(
                "id", "one", null, "dataset", null, Global.QuickRefreshType.params, List.of("one"))));
        verifyNoInteractions(spiders, rows);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"id\":\"wrong\",\"title\":\"Updated\"}"})
    void quickRefreshPreservesExistingFieldsAndSourcePrimaryKey(String response) throws Exception {
        var existing = row("one", Map.of("id", "one", "title", "Original", "image", "cover.jpg"));
        when(rows.findById(new GroupDatasetDataPK("group", "one"))).thenReturn(Optional.of(existing));
        when(spiders.executeByPrimeKeyList("detail", List.of("one"))).thenReturn(response);
        var singleRefresh = spy(service);
        doNothing().when(singleRefresh).refreshData("dataset");
        doAnswer(invocation -> new com.lsb.listProjectBackend.domain.dataset.DatasetDataTO("dataset",
                List.of(existing.getJson()), null, null)).when(singleRefresh).getDatasetDataByName("dataset");

        var result = singleRefresh.quickRefresh(new DatasetQuickRefreshTO("id", "one", "detail", "dataset",
                null, Global.QuickRefreshType.params, List.of("one")));

        assertEquals("one", result.get("id"));
        assertEquals("cover.jpg", result.get("image"));
        assertEquals(response.equals("{}") ? "Original" : "Updated", result.get("title"));
        verify(rows).save(existing);
    }

    @Test
    void quickRefreshRejectsWrongKeyBeforeCrawlingOrWriting() {
        assertThrows(IllegalArgumentException.class, () -> service.quickRefresh(new DatasetQuickRefreshTO(
                "wrongKey", "one", "detail", "dataset", null, Global.QuickRefreshType.params, List.of("one"))));
        verifyNoInteractions(spiders, rows);
    }

    @Test
    void quickRefreshRejectsNullResponseWithoutOverwritingExistingData() throws Exception {
        when(spiders.executeByPrimeKeyList("detail", List.of("one"))).thenReturn("null");
        assertThrows(IllegalArgumentException.class, () -> service.quickRefresh(new DatasetQuickRefreshTO(
                "id", "one", "detail", "dataset", null, Global.QuickRefreshType.params, List.of("one"))));
        verifyNoInteractions(rows);
    }

    @ParameterizedTest
    @ValueSource(strings = {"text", "file", "folder", "all"})
    void listRestoresStoredPrimaryKeyEvenWhenJsonOrCustomFieldHasWrongKey(String source) throws Exception {
        var datasetConfig = datasets.findById("dataset").orElseThrow().getConfig();
        datasetConfig.setType(Global.DatasetConfigType.valueOf(source));
        datasetConfig.setScrapyText("one");
        datasetConfig.setFilePath(directory.toString());
        datasetConfig.setFolderPath(directory.toString());
        if (source.equals("file")) {
            java.nio.file.Files.createFile(directory.resolve("one.zip"));
        } else if (source.equals("folder")) {
            java.nio.file.Files.createDirectory(directory.resolve("one"));
        }
        var custom = new DatasetField();
        custom.setType(Global.DatasetFieldType.fixedString);
        custom.setKey("id");
        custom.setFixedString("wrong");
        datasetConfig.setFieldList(List.of(custom));
        var existing = row("one", Map.of("title", "Original"));
        when(rows.findByGroupName("group")).thenReturn(List.of(existing));

        service.refreshData("dataset");

        var saved = ArgumentCaptor.forClass(DatasetData.class);
        verify(datasetRows).save(saved.capture());
        assertEquals(List.of(Map.of("id", "one", "title", "Original")), saved.getValue().getData());
        verifyNoInteractions(spiders);
    }

    private GroupDatasetData row(String key, Map<String, Object> json) {
        var row = new GroupDatasetData();
        row.setGroupName("group");
        row.setPrimeValue(key);
        row.setJson(new LinkedHashMap<>(json));
        return row;
    }
}
