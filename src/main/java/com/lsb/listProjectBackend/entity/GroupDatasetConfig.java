package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.GroupDatasetFieldListConverter;
import com.lsb.listProjectBackend.converter.GroupDatasetScrapyListConverter;
import jakarta.persistence.Convert;
import lombok.Data;

import java.util.List;

@Data
public class GroupDatasetConfig {
    private String byKey;
    private String imageSaveFolder;
    @Convert(converter = GroupDatasetScrapyListConverter.class)
    private List<GroupDatasetScrapy> groupDatasetScrapyList;
    @Convert(converter = GroupDatasetFieldListConverter.class)
    private List<GroupDatasetField> groupDatasetFieldList;
}
