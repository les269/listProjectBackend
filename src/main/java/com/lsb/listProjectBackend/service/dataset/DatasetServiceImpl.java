package com.lsb.listProjectBackend.service.dataset;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.dataset.*;
import com.lsb.listProjectBackend.entity.dynamic.dataset.Dataset;
import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetConfig;
import com.lsb.listProjectBackend.entity.dynamic.dataset.DatasetData;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDataset;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetConfig;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetData;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetDataPK;
import com.lsb.listProjectBackend.entity.dynamic.dataset.GroupDatasetSpider;
import com.lsb.listProjectBackend.domain.spider.SpiderReqTO;
import com.lsb.listProjectBackend.service.spider.SpiderService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import com.lsb.listProjectBackend.mapper.dataset.DatasetDataMapper;
import com.lsb.listProjectBackend.mapper.dataset.DatasetMapper;
import com.lsb.listProjectBackend.mapper.dataset.GroupDatasetDataMapper;
import com.lsb.listProjectBackend.repository.dynamic.dataset.DatasetDataRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.DatasetRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetDataRepository;
import com.lsb.listProjectBackend.repository.dynamic.dataset.GroupDatasetRepository;
import com.lsb.listProjectBackend.service.file.ImageService;
import com.lsb.listProjectBackend.service.scrapy.ScrapyPaginationService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class DatasetServiceImpl implements DatasetService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final DatasetRepository datasetRepository;
    private final GroupDatasetDataRepository groupDatasetDataRepository;
    private final GroupDatasetRepository groupDatasetRepository;
    private final GroupDatasetService groupDatasetService;
    private final SpiderService spiderService;
    private final ScrapyPaginationService scrapyPaginationService;
    private final DatasetDataRepository datasetDataRepository;
    private final ImageService imageService;

    private final DatasetMapper datasetMapper;
    private final DatasetDataMapper datasetDataMapper;
    private final GroupDatasetDataMapper groupDatasetDataMapper;

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
                var groupConfig = getGroupDatasetConfig(groupName);
                var data = groupDatasetDataRepository.findByGroupName(groupName)
                        .stream()
                        .map(row -> datasetRowJson(row, groupConfig)).toList();
                DatasetData datasetData = new DatasetData();
                datasetData.setDatasetName(dataset.getName());
                datasetData.setData(data);
                datasetDataRepository.save(datasetData);
            }
            case file, folder, text, pagination -> doSpider(name, datasetConfig, configDatasetType);
        }

        endTime = System.currentTimeMillis();
        System.out.println("scrapy time: " + (endTime - startTime) + " ms");
        startTime = System.currentTimeMillis();
        // 下載圖片
        doDownloadImage(groupName);
        endTime = System.currentTimeMillis();
        System.out.println("doDownloadImage time: " + (endTime - startTime) + " ms");
    }

    @Override
    public Map<String, Object> quickRefresh(DatasetQuickRefreshTO to) throws Exception {
        String primeKey = to.primeKey();
        String byKey = to.byKey();
        Dataset dataset = datasetRepository.findById(to.datasetName()).orElse(null);
        if (dataset == null) {
            return null;
        }
        // 1.爬蟲獲取資料
        if (Utils.isBlank(to.spiderId())) {
            throw new IllegalArgumentException("spiderId is required");
        }
        var groupName = dataset.getConfig().getGroupName();
        var groupConfig = getGroupDatasetConfig(groupName);
        if (groupConfig == null || Utils.isBlank(groupConfig.getByKey())
                || !groupConfig.getByKey().equals(byKey) || Utils.isBlank(primeKey)) {
            throw new IllegalArgumentException("A valid primeKey and the group's byKey are required");
        }
        String response = Global.QuickRefreshType.url.equals(to.quickRefreshType())
                ? spiderService.executeByUrl(to.spiderId(), to.url())
                : spiderService.executeByPrimeKeyList(to.spiderId(), to.params());
        Map<String, Object> responseJson = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
        if (responseJson == null) {
            throw new IllegalArgumentException("Spider response must be a JSON object");
        }

        // 2.根據primeKey跟group name去更新 group_dataset_data
        GroupDatasetData groupDatasetData = groupDatasetDataRepository
                .findById(new GroupDatasetDataPK(groupName, primeKey)).orElseGet(GroupDatasetData::new);
        Map<String, Object> json = new LinkedHashMap<>();
        if (groupDatasetData.getJson() != null) {
            json.putAll(groupDatasetData.getJson());
        }
        json.putAll(responseJson);
        json.put(byKey, primeKey);
        groupDatasetData.setGroupName(groupName);
        groupDatasetData.setPrimeValue(primeKey);
        groupDatasetData.setJson(json);
        groupDatasetDataRepository.save(groupDatasetData);

        // 3.刷新datasetData
        refreshData(to.datasetName());
        return getDatasetDataByName(to.datasetName())
                .data().stream()
                .filter(x -> primeKey.equals(x.get(byKey)))
                .findFirst()
                .orElse(new HashMap<>());
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
                path.listFiles(file -> shouldIncludeConfigFile(file, datasetConfig)))).toList();
        for (File file : files) {
            var fileName = file.getName();
            Matcher matcher = filingRegularPattern.matcher(fileName);
            if (matcher.find()) {
                new File(path + "\\" + matcher.group()).mkdir();
                file.renameTo(new File(path + "\\" + matcher.group() + "\\" + file.getName()));
            }
        }
    }

    private void doSpider(String datasetName, DatasetConfig datasetConfig, Global.DatasetConfigType type)
            throws Exception {
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
            Map<String, String> keyRedirectUrlMap = to.config().getKeyRedirectUrlMap();
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
        // 使用群組設定中的預設 Spider。
        var groupDatasetOptional = groupDatasetRepository.findById(groupName);
        GroupDatasetConfig config = groupDatasetOptional.map(GroupDataset::getConfig).orElse(null);
        GroupDatasetSpider spider = config != null && config.getType() == Global.GroupDatasetConfigType.spider
                ? getDefaultSpider(config) : null;
        if (spider != null && !targetList.isEmpty()) {
            GroupDatasetConfig groupDatasetConfig = groupDatasetOptional.get().getConfig();
            List<GroupDatasetData> saveGroupDataset = new ArrayList<>();

            String spiderId = spider.getSpiderId();
            var requests = targetList.stream()
                    .map(target -> new SpiderReqTO(spiderId,
                            type == Global.DatasetConfigType.pagination ? target.getLast() : null, target))
                    .toList();
            List<String> spiderResults = spiderService.executeBatch(spiderId, requests);

            int resultIndex = 0;
            for (List<String> target : targetList) {
                log.info("spider key is {}", target.getFirst());
                Map<String, Object> spiderResult = objectMapper.readValue(spiderResults.get(resultIndex++),
                        new TypeReference<Map<String, Object>>() {});

                spiderResult.put(groupDatasetConfig.getByKey(), target.getFirst());
                if (type == Global.DatasetConfigType.pagination) {
                    spiderResult.put("__item_url", target.getLast());
                }
                GroupDatasetData groupDatasetData = new GroupDatasetData();
                groupDatasetData.setGroupName(groupName);
                groupDatasetData.setPrimeValue(target.getFirst());
                groupDatasetData.setJson(spiderResult);
                saveGroupDataset.add(groupDatasetData);
            }
            groupDatasetDataRepository.saveAll(saveGroupDataset);
            groupDatasetDataList.addAll(groupDatasetDataMapper.toCopyEntityList(saveGroupDataset));
        }

        // 設定自定義資料
        if (type == Global.DatasetConfigType.file || type == Global.DatasetConfigType.folder) {
            setCustomDataForFiles(groupDatasetDataList, datasetConfig);
        } else if (type == Global.DatasetConfigType.text || type == Global.DatasetConfigType.pagination) {
            setCustomDataForText(groupDatasetDataList, datasetConfig);
        }

        // 保存資料至 DatasetData
        DatasetData datasetData = new DatasetData();
        datasetData.setDatasetName(datasetName);
        datasetData.setData(groupDatasetDataList.stream().map(row -> datasetRowJson(row, config)).toList());
        datasetDataRepository.save(datasetData);
    }

    private Map<String, Object> datasetRowJson(GroupDatasetData row, GroupDatasetConfig config) {
        Map<String, Object> json = new LinkedHashMap<>();
        if (row.getJson() != null) {
            json.putAll(row.getJson());
        }
        if (config != null && Utils.isNotBlank(config.getByKey())) {
            json.put(config.getByKey(), row.getPrimeValue());
        }
        return json;
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
                                value = value.replaceAll(datasetField.getReplaceRegular(),
                                        datasetField.getReplaceRegularTo());
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
                            Global.DatasetFieldType.fileSize ->
                        "";
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

    private void doDownloadImage(String groupName) {
        log.info("doDownloadImage start");
        var groupDataset = getGroupDatasetConfig(groupName);
        if (groupDataset != null && groupDataset.isAutoImageDownload()) {
            var groupDatasetDataList = groupDatasetDataRepository.findByGroupName(groupName);
            var path = groupDataset.getImageSaveFolder();
            Set<String> nameList = Arrays.stream(Objects.requireNonNull(new File(path).list()))
                    .parallel()
                    .map(x -> Utils.windowsFileNameReplace(x).toLowerCase())
                    .collect(Collectors.toSet());
            groupDatasetDataList = groupDatasetDataList.stream()
                    .filter(x -> !nameList.contains(Utils.windowsFileNameReplace(x.getPrimeValue()).toLowerCase()))
                    .filter(x -> Utils.isNotBlank((String) x.getJson().get(groupDataset.getImageByKey())))
                    .toList();
            for (GroupDatasetData groupDatasetData : groupDatasetDataList) {
                String imageUrl = (String) groupDatasetData.getJson().get(groupDataset.getImageByKey());
                String fileName = groupDatasetData.getPrimeValue();
                String referer = (String) groupDatasetData.getJson().get("__image_referer");
                imageService.downloadImageFromUrl(imageUrl, path, Utils.windowsFileNameReplace(fileName),
                        new HashMap<>(), null, referer);
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

    private GroupDatasetSpider getDefaultSpider(GroupDatasetConfig config) {
        if (config.getGroupDatasetSpiderList() == null) {
            return null;
        }
        return config.getGroupDatasetSpiderList().stream()
                .filter(GroupDatasetSpider::isDefault).findFirst().orElse(null);
    }

    private List<File> getAllFile(DatasetConfig datasetConfig) {
        if (datasetConfig.getType() == Global.DatasetConfigType.file) {
            var path = new File(datasetConfig.getFilePath());
            // 取得目錄底下所有檔案
            var allFile = new ArrayList<>(Arrays.stream(Objects.requireNonNull(path.listFiles(File::isFile)))
                    .filter(file -> Utils.isBlank(datasetConfig.getFileExtension())
                            || Utils.checkFileExtension(file, datasetConfig.getFileExtension()))
                    .toList());
            // 有歸檔時需要檢索資料夾裡面的資料
            if (datasetConfig.isFiling()) {
                // 取得目錄底下的所有資料夾
                var folders = Arrays.stream(Objects.requireNonNull(path.listFiles(File::isDirectory))).toList();
                for (var folder : folders) {
                    // file是檔案,有限制副檔名時需要檢查副檔名是否相同
                    var subFiles = folder
                            .listFiles(file -> file.isFile() && (Utils.isBlank(datasetConfig.getFileExtension())
                                    || Utils.checkFileExtension(file, datasetConfig.getFileExtension())));
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


}
