package com.lsb.listProjectBackend.entity.dynamic.dataset;

import com.lsb.listProjectBackend.converter.dataset.GroupDatasetApiListConverter;
import com.lsb.listProjectBackend.converter.dataset.GroupDatasetFieldListConverter;
import com.lsb.listProjectBackend.converter.dataset.GroupDatasetScrapyListConverter;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class GroupDatasetConfig {
    private String byKey;
    private String imageSaveFolder;
    @Enumerated(EnumType.STRING)
    private Global.GroupDatasetConfigType type;
    @Convert(converter = GroupDatasetScrapyListConverter.class)
    private List<GroupDatasetScrapy> groupDatasetScrapyList;
    @Convert(converter = GroupDatasetFieldListConverter.class)
    private List<GroupDatasetField> groupDatasetFieldList;
    @Convert(converter = GroupDatasetApiListConverter.class)
    private List<GroupDatasetApi> groupDatasetApiList;
}
