package com.lsb.listProjectBackend.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.DatasetDataMapper;
import com.lsb.listProjectBackend.mapper.DatasetMapper;
import com.lsb.listProjectBackend.mapper.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.*;
import com.lsb.listProjectBackend.service.*;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
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
    private ScrapyPaginationService scrapyPaginationService;
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
        Global.DatasetConfigType configDatasetType = datasetConfig.getType();
        groupDatasetService.refreshGroupDataset(groupName);
        long startTime = System.currentTimeMillis();
        // 1. 執行歸檔
        doFiling(datasetConfig);
        long endTime = System.currentTimeMillis();
        System.out.println("doFiling time: " + (endTime - startTime) + " ms");
        startTime = System.currentTimeMillis();
        // 2. 執行爬蟲
        // all 把當前group所擁有資料放入資料集
        switch (configDatasetType) {
            case all -> {
                var data = groupDatasetDataRepository.findByGroupName(groupName)
                        .stream()
                        .map(GroupDatasetData::getJson).toList();
                DatasetData datasetData = new DatasetData();
                datasetData.setDatasetName(dataset.getName());
                datasetData.setData(data);
                datasetDataRepository.save(datasetData);
            }
            case file, folder, text, pagination -> doScrapy(name, datasetConfig, configDatasetType);
        }

        endTime = System.currentTimeMillis();
        System.out.println("scrapy time: " + (endTime - startTime) + " ms");
        startTime = System.currentTimeMillis();

        useReplaceValueMap(groupName);
        endTime = System.currentTimeMillis();
        System.out.println("useReplaceValueMap time: " + (endTime - startTime) + " ms");
        startTime = System.currentTimeMillis();
        // 下載圖片
        doDownloadImage(groupName, datasetConfig);
        endTime = System.currentTimeMillis();
        System.out.println("doDownloadImage time: " + (endTime - startTime) + " ms");
    }

    private void doFiling(DatasetConfig datasetConfig) {
        boolean filing = datasetConfig.isFiling();
        String filingRegular = datasetConfig.getFilingRegular();
        String pathString = switch (datasetConfig.getType()) {
            case Global.DatasetConfigType.file -> datasetConfig.getFilePath();
            case Global.DatasetConfigType.folder -> datasetConfig.getFolderPath();
            case text, pagination, all -> "";
        };
        if (!filing || Utils.isBlank(pathString, filingRegular)) {
            return;
        }
        File path = new File(pathString);
        if (!path.exists()) {
            return;
        }
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

    private void doScrapy(String datasetName, DatasetConfig datasetConfig, Global.DatasetConfigType type) throws Exception {
        // 群組名稱
        String groupName = datasetConfig.getGroupName();
        // 取得群組所有資料
        List<GroupDatasetData> allGroupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
        // 取得存在的 primeValue
        Set<String> primeValueSet = allGroupDatasetDataList.stream()
                .map(GroupDatasetData::getPrimeValue)
                .collect(Collectors.toSet());

        List<List<String>> targetList = new ArrayList<>();
        List<GroupDatasetData> groupDatasetDataList = new ArrayList<>();

        // 根據不同類型處理資料來源 (File 或 Text)
        if (type == Global.DatasetConfigType.file || type == Global.DatasetConfigType.folder) {
            if (type == Global.DatasetConfigType.file && !new File(datasetConfig.getFilePath()).exists()) {
                return;
            }
            if (type == Global.DatasetConfigType.folder && !new File(datasetConfig.getFolderPath()).exists()) {
                return;
            }
            // 取得所有檔案
            List<File> allFiles = getAllFile(datasetConfig);
            Set<String> allFileNameSet = allFiles.stream()
                    .map(Utils::getFileNameWithoutExtension)
                    .collect(Collectors.toSet());

            targetList = allFileNameSet.stream()
                    .filter(x -> !primeValueSet.contains(x))
                    .map(List::of)
                    .toList();

            groupDatasetDataList.addAll(allGroupDatasetDataList.stream()
                    .filter(x -> allFileNameSet.contains(x.getPrimeValue()))
                    .toList());
        } else if (type == Global.DatasetConfigType.text) {
            List<List<String>> textList = Utils.textToList(datasetConfig.getScrapyText(), ",");
            Set<String> firstTextSet = textList.stream().map(List::getFirst).collect(Collectors.toSet());

            targetList = textList.stream()
                    .filter(x -> !primeValueSet.contains(x.getFirst()))
                    .toList();

            groupDatasetDataList.addAll(allGroupDatasetDataList.stream()
                    .filter(x -> firstTextSet.contains(x.getPrimeValue()))
                    .toList());
        } else if (type == Global.DatasetConfigType.pagination) {
            if (scrapyPaginationService.checkForUpdate(datasetConfig.getScrapyPagination())) {
                scrapyPaginationService.updateRedirectData(datasetConfig.getScrapyPagination());
            }
            var to = scrapyPaginationService.get(datasetConfig.getScrapyPagination());
            Map<String, String> keyRedirectUrlMap = to.getConfig().getKeyRedirectUrlMap();
            Set<String> keys = keyRedirectUrlMap.keySet();
            // 過濾未獲取過的資料
            targetList = keyRedirectUrlMap.entrySet().stream()
                    .filter(x -> !primeValueSet.contains(x.getKey()))
                    .map(x -> List.of(x.getKey(), x.getValue()))
                    .toList();
            // 過濾已經取得的資料
            groupDatasetDataList.addAll(allGroupDatasetDataList.stream()
                    .filter(x -> keys.contains(x.getPrimeValue()))
                    .toList());
        }
        // 檢查是否有這 GroupDataset, 並判斷該 GroupDataset 的 Scrapy 是否有可預設使用的爬蟲
        var groupDatasetOptional = groupDatasetRepository.findById(groupName);
        ScrapyConfigTO scrapyConfigTO = getDefaultScrapy(groupName);
        if (groupDatasetOptional.isPresent() && scrapyConfigTO != null && !targetList.isEmpty()) {
            GroupDatasetConfig groupDatasetConfig = groupDatasetOptional.get().getConfig();
            List<GroupDatasetData> saveGroupDataset = new ArrayList<>();
            //TODO: 前端可以進行設定時間參數
            RateLimiter rateLimiter = RateLimiter.create(2.0);
            for (List<String> target : targetList) {
                rateLimiter.acquire();

                log.info("scrapy key is {}", target.getFirst());
                Map<String, Object> scrapyResult = type == Global.DatasetConfigType.pagination ?
                        scrapyService.doScrapyByUrl(target.getLast(), scrapyConfigTO.getData()) :
                        scrapyService.doScrapyByJson(target, scrapyConfigTO.getData());

                scrapyResult.put(groupDatasetConfig.getByKey(), target.getFirst());
                if (type == Global.DatasetConfigType.pagination) {
                    scrapyResult.put("__item_url", target.getLast());
                }
                GroupDatasetData groupDatasetData = new GroupDatasetData();
                groupDatasetData.setGroupName(groupName);
                groupDatasetData.setPrimeValue(target.getFirst());
                groupDatasetData.setJson(scrapyResult);
                saveGroupDataset.add(groupDatasetData);
            }
            groupDatasetDataRepository.saveAll(saveGroupDataset);
            groupDatasetDataList.addAll(groupDatasetDataMapper.toCopyEntityList(saveGroupDataset));
        }

        //設定自定義資料
        if (type == Global.DatasetConfigType.file || type == Global.DatasetConfigType.folder) {
            setCustomDataForFiles(groupDatasetDataList, datasetConfig);
        } else if (type == Global.DatasetConfigType.text || type == Global.DatasetConfigType.pagination) {
            setCustomDataForText(groupDatasetDataList, datasetConfig);
        }

        // 保存資料至 DatasetData
        DatasetData datasetData = new DatasetData();
        datasetData.setDatasetName(datasetName);
        datasetData.setData(groupDatasetDataList.stream().map(GroupDatasetData::getJson).toList());
        datasetDataRepository.save(datasetData);
    }

    private void setCustomDataForFiles(List<GroupDatasetData> groupDatasetDataList, DatasetConfig datasetConfig) {
        if (datasetConfig.getFieldList().isEmpty()) {
            return;
        }
        for (File file : getAllFile(datasetConfig)) {
            String fileName = Utils.getFileNameWithoutExtension(file);
            groupDatasetDataList.stream()
                    .filter(x -> fileName.equals(x.getPrimeValue()))
                    .forEach(x -> {
                        datasetConfig.getFieldList().forEach(datasetField -> {
                            var value = switch (datasetField.getType()) {
                                case Global.DatasetFieldType.fileName -> file.getName();
                                case Global.DatasetFieldType.fileSize -> file.length() + "";
                                case Global.DatasetFieldType.path -> file.getAbsolutePath();
                                case Global.DatasetFieldType.fixedString ->
                                        Utils.replaceValue(datasetField.getFixedString(), x.getJson());
                            };
                            if (Utils.isNotBlank(datasetField.getReplaceRegular())) {
                                value = value.replaceAll(datasetField.getReplaceRegular(), datasetField.getReplaceRegularTo());
                            }
                            x.getJson().put(datasetField.getKey(), value);
                        });
                    });
        }
    }

    private void setCustomDataForText(List<GroupDatasetData> groupDatasetDataList, DatasetConfig datasetConfig) {
        if (datasetConfig.getFieldList().isEmpty()) {
            return;
        }
        groupDatasetDataList.forEach(x -> {
            datasetConfig.getFieldList().forEach(datasetField -> {
                var value = switch (datasetField.getType()) {
                    case Global.DatasetFieldType.fileName, Global.DatasetFieldType.path,
                         Global.DatasetFieldType.fileSize -> "";
                    case Global.DatasetFieldType.fixedString ->
                            Utils.replaceValue(datasetField.getFixedString(), x.getJson());
                };
                if (Utils.isNotBlank(datasetField.getReplaceRegular())) {
                    value = value.replaceAll(datasetField.getReplaceRegular(), datasetField.getReplaceRegularTo());
                }
                x.getJson().put(datasetField.getKey(), value);
            });
        });
    }

    private void doDownloadImage(String groupName, DatasetConfig datasetConfig) {
        log.info("doDownloadImage start");
        if (datasetConfig.isAutoImageDownload()) {
            var groupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
            var groupDataset = getGroupDatasetConfig(groupName);
            if (groupDataset != null) {
                var path = groupDataset.getImageSaveFolder();
                Set<String> nameList = Arrays.stream(Objects.requireNonNull(new File(path).list()))
                        .parallel()
                        .map(x -> Utils.windowsFileNameReplace(x).toLowerCase())
                        .collect(Collectors.toSet());
                groupDatasetDataList = groupDatasetDataList.stream()
                        .filter(x -> !nameList.contains(Utils.windowsFileNameReplace(x.getPrimeValue()).toLowerCase()))
                        .filter(x -> Utils.isNotBlank((String) x.getJson().get(datasetConfig.getImageByKey())))
                        .toList();
                for (GroupDatasetData groupDatasetData : groupDatasetDataList) {
                    String imageUrl = (String) groupDatasetData.getJson().get(datasetConfig.getImageByKey());
                    String fileName = groupDatasetData.getPrimeValue();
                    String referer = (String) groupDatasetData.getJson().get("__image_referer");
                    imageService.downloadImageFromUrl(imageUrl, path, Utils.windowsFileNameReplace(fileName), new HashMap<>(), null, referer);
                }
            }
        }
        log.info("doDownloadImage end");
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
        if (datasetConfig.getType() == Global.DatasetConfigType.file) {
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
        } else if (datasetConfig.getType() == Global.DatasetConfigType.folder) {
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
        if (dataList.isEmpty()
        ) {
            return;
        }
        if (groupDataset.getConfig()
                .getGroupDatasetFieldList()
                .stream()
                .map(GroupDatasetField::getReplaceValueMapName)
                .allMatch(Utils::isBlank)) {
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
