package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.repository.dynamic.common.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 每批只查詢一次；回傳的替換表快照供整批唯讀共用。 */
@UseDynamic
@Service
@RequiredArgsConstructor
public class ReplaceValueMapLoader {
    private final ReplaceValueMapRepository repository;

    public Map<String, Map<String, Object>> load(List<ValuePipeline> pipelines) {
        Set<String> names = new LinkedHashSet<>();
        collectNames(pipelines, names);
        if (names.isEmpty()) {
            return Map.of();
        }
        var tables = repository.findAllById(names);
        Map<String, Map<String, Object>> maps = new LinkedHashMap<>();
        for (var table : tables) {
            if (table.getMap() != null) {
                maps.put(table.getName(), Collections.unmodifiableMap(new LinkedHashMap<>(table.getMap())));
            }
        }
        return Collections.unmodifiableMap(maps);
    }

    private void collectNames(List<ValuePipeline> pipelines, Set<String> names) {
        if (pipelines == null) {
            return;
        }
        for (var pipeline : pipelines) {
            if (pipeline == null || !pipeline.isEnabled()) {
                continue;
            }
            if (pipeline.getType() == Global.ValuePipelineType.REPLACE_VALUE_MAP
                    && Utils.isNotBlank(pipeline.getUseReplaceValueMap())) {
                names.add(pipeline.getUseReplaceValueMap());
            }
            if (pipeline.getType() == Global.ValuePipelineType.EXTRACT_OBJECTS
                    && pipeline.getExtractObjectsConfig() != null
                    && pipeline.getExtractObjectsConfig().getFields() != null) {
                for (var field : pipeline.getExtractObjectsConfig().getFields()) {
                    if (field != null) {
                        collectNames(field.getPipelines(), names);
                    }
                }
            }
        }
    }
}
