package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.DatasetDataMapper;
import com.lsb.listProjectBackend.mapper.DatasetMapper;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.DatasetDataRepository;
import com.lsb.listProjectBackend.repository.DatasetRepository;
import com.lsb.listProjectBackend.repository.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.GroupDatasetRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import com.zaxxer.hikari.util.FastList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DatasetService {
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private GroupDatasetDataRepository groupDatasetDataRepository;
    @Autowired
    private GroupDatasetRepository groupDatasetRepository;
    @Autowired
    private ScrapyService scrapyService;
    @Autowired
    private DatasetDataRepository datasetDataRepository;
    @Autowired
    private ImageService imageService;

    private final DatasetMapper datasetMapper = DatasetMapper.INSTANCE;
    private final DatasetDataMapper datasetDataMapper = DatasetDataMapper.INSTANCE;
    private final GroupDatasetDataMapper groupDatasetDataMapper = GroupDatasetDataMapper.INSTANCE;

    public List<DatasetTO> getAllDataset() {
        return datasetMapper.toDomainList(datasetRepository.findAll());
    }

    public void updateDataset(DatasetTO req) {
        Dataset datasetData = datasetMapper.toEntity(req);
        datasetRepository.save(datasetData);
    }

    public void deleteDataset(String name) {
        datasetRepository.deleteById(name);
    }

    public DatasetTO getDataset(String name) {
        return datasetMapper.toDomain(datasetRepository.findById(name).orElse(null));
    }

    public DatasetDataTO getDatasetDataByName(String name) {
        return datasetDataMapper.toDomain(datasetDataRepository.findById(name).orElse(null));
    }

    public boolean existDataset(String name) {
        return datasetRepository.existsById(name);
    }

    public void refreshData(String name) throws Exception {
        var dataset = datasetRepository.findById(name).orElse(null);
        if (dataset == null) {
            return;
        }
        DatasetConfig datasetConfig = dataset.getConfig();
        String groupName = datasetConfig.getGroupName();
        Global.ConfigDatasetType configDatasetType = datasetConfig.getType();

        // 1. 執行歸檔
        doFiling(datasetConfig);
        // 2. 執行爬蟲
        // all 把當前group所擁有資料放入資料集
        if (configDatasetType == Global.ConfigDatasetType.all) {
            var data = groupDatasetDataRepository.findByGroupName(groupName)
                    .stream()
                    .map(GroupDatasetData::getJson).toList();
            DatasetData datasetData = new DatasetData();
            datasetData.setDatasetConfigName(dataset.getName());
            datasetData.setData(data);
            datasetDataRepository.save(datasetData);
        }
        // file, folder 需要進行進行爬蟲
        else if (configDatasetType == Global.ConfigDatasetType.file || configDatasetType == Global.ConfigDatasetType.folder) {
            doScrapy(name, datasetConfig);
        }
        // 下載圖片
        doDownloadImage(groupName, datasetConfig);
    }

    private void doFiling(DatasetConfig datasetConfig) {
        boolean filing = datasetConfig.isFiling();
        String filingRegular = datasetConfig.getFilingRegular();
        String pathString = switch (datasetConfig.getType()) {
            case Global.ConfigDatasetType.file -> datasetConfig.getFilePath();
            case Global.ConfigDatasetType.folder -> datasetConfig.getFolderPath();
            case all -> "";
        };
        if (!filing || Utils.isBlank(pathString, filingRegular)) {
            return;
        }
        File path = new File(pathString);
        Pattern filingRegularPattern = Pattern.compile(datasetConfig.getFilingRegular());
        List<File> files = Arrays.stream(Objects.requireNonNull(
                path.listFiles(file -> shouldIncludeConfigFile(file, datasetConfig))
        )).toList();
        for (File file : files) {
            var fileName = file.getName();
            Matcher matcher = filingRegularPattern.matcher(fileName);
            if (matcher.find()) {
                new File(path + "\\" + matcher.group()).mkdir();
                file.renameTo(new File(path + "\\" + matcher.group() + "\\" + file.getName()));
            }
        }
    }

    private void doScrapy(String datasetName, DatasetConfig datasetConfig) throws Exception {
        //群組名稱
        String groupName = datasetConfig.getGroupName();
        // 取得群組所有資料
        List<GroupDatasetData> allGroupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
        // 取得存在的value
        List<String> primeValueList = allGroupDatasetDataList.stream().map(GroupDatasetData::getPrimeValue).toList();
        // 取得所有的檔案
        List<File> allFile = getAllFile(datasetConfig);
        // 取得所有檔案名稱
        List<String> allFileName = allFile.stream()
                .map(Utils::getFileNameWithoutExtension)
                .toList();
        // 判斷在DB是否存在資料,不存在則需要爬蟲
        List<String> needScrapyList = allFile.stream()
                .map(Utils::getFileNameWithoutExtension)
                .filter(x -> !primeValueList.contains(x))
                .toList();
        // 找出檔案名稱在存在groupDatasetData有資料並且去除副檔名
        List<GroupDatasetData> notNeedScrapyList = allGroupDatasetDataList.stream()
                .filter(x -> allFileName.contains(x.getPrimeValue()))
                .toList();
        List<GroupDatasetData> groupDatasetDataList = groupDatasetDataMapper.toCopyEntityList(notNeedScrapyList);

        // 檢查是否有這GroupDataset, 並判斷該GroupDataset的Scrapy是否有可預設使用的爬蟲
        var groupDatasetOptional = groupDatasetRepository.findById(datasetConfig.getGroupName());
        // 把缺少資訊的資料進行爬蟲
        if (groupDatasetOptional.isPresent()) {
            ScrapyConfigTO scrapyConfigTO = getDefaultScrapy(groupName);
            GroupDatasetConfig groupDatasetConfig = groupDatasetOptional.get().getConfig();
            List<GroupDatasetData> saveGroupDataset = new ArrayList<>();
            for (String fileName : needScrapyList) {
                Map<String, Object> scrapyResult = scrapyService.scrapyByJson(scrapyConfigTO.getData(), List.of(fileName));
                Thread.sleep(100);
                scrapyResult.put(groupDatasetConfig.getByKey(), fileName);
                GroupDatasetData groupDatasetData = new GroupDatasetData();
                groupDatasetData.setGroupName(datasetConfig.getGroupName());
                groupDatasetData.setPrimeValue(fileName);
                groupDatasetData.setJson(scrapyResult);
                saveGroupDataset.add(groupDatasetData);
            }
            groupDatasetDataRepository.saveAll(saveGroupDataset);
            groupDatasetDataList.addAll(groupDatasetDataMapper.toCopyEntityList(saveGroupDataset));
        }
        // 設定自定義的資料
        for (File file : allFile) {
            String fileName = Utils.getFileNameWithoutExtension(file);
            groupDatasetDataList.stream().filter(x -> fileName.equals(x.getPrimeValue()))
                    .forEach(x -> {
                        datasetConfig.getFieldList().forEach(datasetField -> {
                            var value = switch (datasetField.getType()) {
                                case Global.DatasetFieldType.fileName -> file.getName();
                                case Global.DatasetFieldType.fileSize -> file.length();
                                case Global.DatasetFieldType.path -> file.getAbsoluteFile();
                                case Global.DatasetFieldType.fixedString -> datasetField.getFixedString();
                            };
                            x.getJson().put(datasetField.getKey(), value);
                        });
                    });
        }
        DatasetData datasetData = new DatasetData();
        datasetData.setDatasetConfigName(datasetName);
        datasetData.setData(groupDatasetDataList.stream().map(GroupDatasetData::getJson).toList());
        datasetDataRepository.save(datasetData);
    }

    public void doDownloadImage(String groupName, DatasetConfig datasetConfig) {
        if (datasetConfig.isAutoImageDownload()) {
            var groupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
            var groupDataset = getGroupDatasetConfig(groupName);
            if (groupDataset != null) {
                for (GroupDatasetData groupDatasetData : groupDatasetDataList) {
                    String imageUrl = (String) groupDatasetData.getJson().get(datasetConfig.getImageByKey());
                    String value = (String) groupDatasetData.getJson().get(groupDataset.getByKey());
                    String referer = (String) groupDatasetData.getJson().get("__image_referer");
                    if (!new File(datasetConfig.getImageSaveFolder() + "\\" + value).exists()) {
                        imageService.downloadImageFromUrl(imageUrl, datasetConfig.getImageSaveFolder(), value, new HashMap<>(), null, referer);
                    }
                }
            }
        }
    }

    public GroupDatasetConfig getGroupDatasetConfig(String groupName) {
        var groupDatasetOptional = groupDatasetRepository.findById(groupName);
        if (groupDatasetOptional.isPresent()) {
            GroupDataset groupDataset = groupDatasetOptional.get();
            return groupDataset.getConfig();
        }
        return null;
    }


    public ScrapyConfigTO getDefaultScrapy(String groupName) {
        GroupDatasetConfig groupDatasetConfig = getGroupDatasetConfig(groupName);
        var groupDatasetScrapyOptional = groupDatasetConfig.getGroupDatasetScrapyList()
                .stream()
                .filter(GroupDatasetScrapy::isDefault)
                .findFirst();
        if (groupDatasetScrapyOptional.isPresent()) {
            GroupDatasetScrapy groupDatasetScrapy = groupDatasetScrapyOptional.get();
            return scrapyService.getConfig(groupDatasetScrapy.getName());
        }
        return null;
    }

    private List<File> getAllFile(DatasetConfig datasetConfig) {
        if (datasetConfig.getType() == Global.ConfigDatasetType.file) {
            var path = new File(datasetConfig.getFilePath());
            //取得目錄底下所有檔案
            var allFile = Arrays.stream(Objects.requireNonNull(path.listFiles(File::isFile)))
                    .filter(file -> Utils.isBlank(datasetConfig.getFileExtension()) || Utils.checkFileExtension(file, datasetConfig.getFileExtension()))
                    .toList();
            //有歸檔時需要檢索資料夾裡面的資料
            if (datasetConfig.isFiling()) {
                //取得目錄底下的所有資料夾
                var folders = Arrays.stream(Objects.requireNonNull(path.listFiles(File::isDirectory))).toList();
                for (var folder : folders) {
                    //file是檔案,有限制副檔名時需要檢查副檔名是否相同
                    var subFiles = folder.listFiles(file ->
                            file.isFile() && (Utils.isBlank(datasetConfig.getFileExtension()) || Utils.checkFileExtension(file, datasetConfig.getFileExtension()))
                    );
                    allFile.addAll(List.of(subFiles));
                }
            }
            return allFile;
        } else if (datasetConfig.getType() == Global.ConfigDatasetType.folder) {
            var path = new File(datasetConfig.getFolderPath());
            var folders = Arrays.stream(Objects.requireNonNull(path.listFiles(File::isDirectory))).toList();
            if (datasetConfig.isFiling()) {
                List<File> allFile = new ArrayList<>();
                for (var folder : folders) {
                    allFile.addAll(List.of(Objects.requireNonNull(folder.listFiles(File::isDirectory))));
                }
                return allFile;
            } else {
                return folders;
            }
        }
        return new ArrayList<>();
    }

    public static boolean shouldIncludeConfigFile(File file, DatasetConfig config) {
        return switch (config.getType()) {
            case file -> {
                if (file.isFile()) {
                    if (Utils.isNotBlank(config.getFileExtension())) {
                        yield Utils.checkFileExtension(file, config.getFileExtension());
                    }
                    yield true;
                }
                yield false;
            }
            case folder -> file.isDirectory();
            default -> false;
        };
    }
}
