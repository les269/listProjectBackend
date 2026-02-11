package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.GroupDatasetApiListConverter;
import com.lsb.listProjectBackend.converter.GroupDatasetFieldListConverter;
import com.lsb.listProjectBackend.converter.GroupDatasetScrapyListConverter;
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
