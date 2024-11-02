package com.lsb.listProjectBackend.entity;

import com.lsb.listProjectBackend.converter.DatasetFieldListConverter;
import com.lsb.listProjectBackend.converter.DatasetScrapyListConverter;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class DatasetConfig {
    @Enumerated(EnumType.STRING)
    private Global.ConfigDatasetType type;
    private String groupName;
    private String byKey;
    private String filePath;
    private String fileExtension;
    private String folderPath;
    private boolean filing;
    private String filingRegular;
    @Convert(converter = DatasetFieldListConverter.class)
    private List<DatasetField> fieldList;
    private boolean autoImageDownload;
    private String imageByKey;
    private String imageSaveFolder;
    @Convert(converter = DatasetScrapyListConverter.class)
    private List<DatasetScrapy> datasetScrapyList;
}
