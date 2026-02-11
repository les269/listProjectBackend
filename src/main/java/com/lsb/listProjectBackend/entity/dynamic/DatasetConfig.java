package com.lsb.listProjectBackend.entity.dynamic;

import com.lsb.listProjectBackend.converter.DatasetFieldListConverter;
import com.lsb.listProjectBackend.utils.Global;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
public class DatasetConfig {
    @Enumerated(EnumType.STRING)
    private Global.DatasetConfigType type;
    private String groupName;
    private String filePath;
    private String fileExtension;
    private String folderPath;
    private boolean filing;
    private String filingRegular;
    @Convert(converter = DatasetFieldListConverter.class)
    private List<DatasetField> fieldList;
    private boolean autoImageDownload;
    private String imageByKey;
    private String scrapyText;
    private String scrapyPagination;

}
