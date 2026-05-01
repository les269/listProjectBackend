package com.lsb.listProjectBackend.entity.dynamic.dataset;

import com.lsb.listProjectBackend.utils.Global;
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
    private List<GroupDatasetScrapy> groupDatasetScrapyList;
    private List<GroupDatasetField> groupDatasetFieldList;
    private List<GroupDatasetApi> groupDatasetApiList;
}
