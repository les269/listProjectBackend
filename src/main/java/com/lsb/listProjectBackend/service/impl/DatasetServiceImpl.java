package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.DatasetDataMapper;
import com.lsb.listProjectBackend.mapper.DatasetMapper;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.*;
import com.lsb.listProjectBackend.service.DatasetService;
import com.lsb.listProjectBackend.service.GroupDatasetService;
import com.lsb.listProjectBackend.service.ImageService;
import com.lsb.listProjectBackend.service.ScrapyService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DatasetServiceImpl implements DatasetService {
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private GroupDatasetDataRepository groupDatasetDataRepository;
    @Autowired
    private GroupDatasetRepository groupDatasetRepository;
    @Autowired
    private GroupDatasetService groupDatasetService;
    @Autowired
    private ScrapyService scrapyService;
    @Autowired
    private DatasetDataRepository datasetDataRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private ReplaceValueMapRepository replaceValueMapRepository;

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

    public List<DatasetDataTO> getDatasetDataByNameList(List<String> nameList) {
        return datasetDataMapper.toDomainList(datasetDataRepository.findAllById(nameList));
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
        groupDatasetService.refreshGroupDataset(groupName);

        // 1. 執行歸檔
        doFiling(datasetConfig);
        // 2. 執行爬蟲
        // all 把當前group所擁有資料放入資料集
        if (configDatasetType == Global.ConfigDatasetType.all) {
            var data = groupDatasetDataRepository.findByGroupName(groupName)
                    .stream()
                    .map(GroupDatasetData::getJson).toList();
            DatasetData datasetData = new DatasetData();
            datasetData.setDatasetName(dataset.getName());
            datasetData.setData(data);
            datasetDataRepository.save(datasetData);
        }
        // file, folder 需要進行進行爬蟲
        else if (configDatasetType == Global.ConfigDatasetType.file || configDatasetType == Global.ConfigDatasetType.folder) {
            doScrapy(name, datasetConfig);
        }

        useReplaceValueMap(groupName);

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
        ScrapyConfigTO scrapyConfigTO = getDefaultScrapy(groupName);
        if (groupDatasetOptional.isPresent() && scrapyConfigTO != null) {
            GroupDatasetConfig groupDatasetConfig = groupDatasetOptional.get().getConfig();
            List<GroupDatasetData> saveGroupDataset = new ArrayList<>();
            for (String fileName : needScrapyList) {
                Map<String, Object> scrapyResult = scrapyService.doScrapyByJson(List.of(fileName), scrapyConfigTO.getData());
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
                                case Global.DatasetFieldType.fixedString ->
                                        Utils.replaceValue(datasetField.getFixedString(), x.getJson());
                            };
                            x.getJson().put(datasetField.getKey(), value);
                        });
                    });
        }
        DatasetData datasetData = new DatasetData();
        datasetData.setDatasetName(datasetName);
        datasetData.setData(groupDatasetDataList.stream().map(GroupDatasetData::getJson).toList());
        datasetDataRepository.save(datasetData);
    }

    private void doDownloadImage(String groupName, DatasetConfig datasetConfig) {
        if (datasetConfig.isAutoImageDownload()) {
            var groupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
            var groupDataset = getGroupDatasetConfig(groupName);
            if (groupDataset != null) {
                for (GroupDatasetData groupDatasetData : groupDatasetDataList) {
                    String imageUrl = (String) groupDatasetData.getJson().get(datasetConfig.getImageByKey());
                    String value = (String) groupDatasetData.getJson().get(groupDataset.getByKey());
                    String referer = (String) groupDatasetData.getJson().get("__image_referer");
                    if (!new File(groupDataset.getImageSaveFolder() + "\\" + value).exists()) {
                        imageService.downloadImageFromUrl(imageUrl, groupDataset.getImageSaveFolder(), value, new HashMap<>(), null, referer);
                    }
                }
            }
        }
    }

    private GroupDatasetConfig getGroupDatasetConfig(String groupName) {
        var groupDatasetOptional = groupDatasetRepository.findById(groupName);
        if (groupDatasetOptional.isPresent()) {
            GroupDataset groupDataset = groupDatasetOptional.get();
            return groupDataset.getConfig();
        }
        return null;
    }

    private ScrapyConfigTO getDefaultScrapy(String groupName) {
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
            var allFile = new ArrayList<>(Arrays.stream(Objects.requireNonNull(path.listFiles(File::isFile)))
                    .filter(file -> Utils.isBlank(datasetConfig.getFileExtension()) || Utils.checkFileExtension(file, datasetConfig.getFileExtension()))
                    .toList());
            //有歸檔時需要檢索資料夾裡面的資料
            if (datasetConfig.isFiling()) {
                //取得目錄底下的所有資料夾
                var folders = Arrays.stream(Objects.requireNonNull(path.listFiles(File::isDirectory))).toList();
                for (var folder : folders) {
                    //file是檔案,有限制副檔名時需要檢查副檔名是否相同
                    var subFiles = folder.listFiles(file ->
                            file.isFile() && (Utils.isBlank(datasetConfig.getFileExtension()) || Utils.checkFileExtension(file, datasetConfig.getFileExtension()))
                    );
                    if (subFiles == null) {
                        continue;
                    }
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

    private boolean shouldIncludeConfigFile(File file, DatasetConfig config) {
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

    private void useReplaceValueMap(String groupName) {
        Map<String, Map<String, Object>> replaceValueMap = new HashMap<>();
        var groupDataset = groupDatasetRepository.findById(groupName).orElse(null);
        if (groupDataset == null) {
            return;
        }
        var dataList = groupDatasetDataRepository.findByGroupName(groupName);
        if (dataList.isEmpty()) {
            return;
        }
        for (GroupDatasetField field : groupDataset.getConfig().getGroupDatasetFieldList()) {
            var fieldKey = field.getKey();
            var mapName = field.getReplaceValueMapName();
            if (Utils.isBlank(mapName)) {
                continue;
            }
            var map = replaceValueMap.computeIfAbsent(mapName, key ->
                    replaceValueMapRepository.findById(key).map(ReplaceValueMap::getMap).orElse(null)
            );
            if (map == null || map.isEmpty()) {
                continue;
            }
            for (var data : dataList) {
                var json = data.getJson();
                switch (field.getType()) {
                    case string -> {
                        var s = json.get(fieldKey).toString();
                        if (map.containsKey(s) && Utils.isNotBlank(map.get(s).toString())) {
                            json.put(fieldKey, map.get(s));
                        }
                    }
                    case stringArray -> {
                        if (json.get(fieldKey) instanceof List<?> list) {
                            var replacedList = list
                                    .stream()
                                    .map(Object::toString) // 確保元素轉為 String
                                    .map(s -> map.containsKey(s) && Utils.isNotBlank(map.get(s).toString()) ? map.get(s).toString() : s)
                                    .toList();
                            json.put(fieldKey, replacedList);
                        }
                    }
                }
            }
        }
        groupDatasetDataRepository.saveAll(dataList);
    }
}
