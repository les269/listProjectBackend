package com.lsb.listProjectBackend.entity.dynamic.dataset;

import com.lsb.listProjectBackend.utils.Global;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties({"autoImageDownload", "imageByKey"})
public class DatasetConfig {
    @Enumerated(EnumType.STRING)
    private Global.DatasetConfigType type;
    private String groupName;
    private String filePath;
    private String fileExtension;
    private String folderPath;
    private boolean filing;
    private String filingRegular;
    private List<DatasetField> fieldList;
    private String scrapyText;
    private String scrapyPagination;

}
