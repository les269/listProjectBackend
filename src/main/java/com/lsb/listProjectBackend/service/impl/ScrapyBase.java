package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.LsbException;
import com.lsb.listProjectBackend.entity.CssSelect;
import com.lsb.listProjectBackend.entity.ReplaceValueMap;
import com.lsb.listProjectBackend.entity.ScrapyData;
import com.lsb.listProjectBackend.repository.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class ScrapyBase {

    @Autowired
    private ReplaceValueMapRepository replaceValueMapRepository;

    public void useCssSelect(String htmlString, List<CssSelect> select, Map<String, Object> result) {
        try {
            Document doc = Jsoup.parse(htmlString);
            for (CssSelect cssSelect : select.stream().sorted((a, b) -> a.getSeq() > b.getSeq() ? 1 : -1).toList()) {
                var value = Utils.replaceValue(cssSelect.getValue(), result);
                List<String> textList = doc.select(value)
                        .stream()
                        .map(x -> {
                            if (Utils.isNotBlank(cssSelect.getAttr())) {
                                return x.attr(cssSelect.getAttr());
                            }
                            if (cssSelect.isOnlyOwn()) {
                                return x.ownText();
                            }
                            return x.text();
                        })
                        .filter(Utils::isNotBlank)
                        .map(x -> {
                            if (Utils.isNotBlank(cssSelect.getReplaceRegular())) {
                                return x.replaceAll(cssSelect.getReplaceRegular(), cssSelect.getReplaceRegularTo());
                            }
                            return x;
                        })
                        .map(String::trim)
                        .toList();
                //分割單一字串
                if (Utils.isNotBlank(cssSelect.getSplitText()) && textList.size()==1){
                    textList = Stream.of(textList.getFirst().split(cssSelect.getSplitText()))
                            .map(String::trim)
                            .toList();
                }
                if (Utils.isNotBlank(cssSelect.getReplaceString()) && !textList.isEmpty()) {
                    String replacedValue = Utils.replaceValue(cssSelect.getReplaceString(), textList);
                    result.put(cssSelect.getKey(), cssSelect.isConvertToArray() ? List.of(replacedValue) : replacedValue);
                    continue;
                }
                // 判斷是否合併已有資料
                mergeResult(result, cssSelect.getKey(), textList, cssSelect.isConvertToArray());
            }
            Map<String, Map<String, Object>> replaceValueMap = new HashMap<>();
            for (CssSelect cssSelect : select) {
                resultReplaceValue(cssSelect, result, replaceValueMap);
            }
        } catch (Exception e) {
            log.error("An error occurred", e);
            throw new LsbException(e.getMessage());
        }
    }

    private void mergeResult(Map<String, Object> result, String key, List<String> newTextList, boolean convertToArray) {
        if (newTextList.isEmpty()) {
            result.put(key, convertToArray ? List.of() : "");
            return;
        }

        if (convertToArray) {
            List<String> mergedList = new ArrayList<>(newTextList);
            if (result.containsKey(key) && result.get(key) instanceof List) {
                mergedList.addAll((List<String>) result.get(key));
            }
            result.put(key, mergedList.stream().distinct().toList());
        } else {
            result.put(key, newTextList.size() == 1 ? newTextList.get(0) : newTextList);
        }
    }

    private void resultReplaceValue(CssSelect cssSelect, Map<String, Object> result, Map<String, Map<String, Object>> replaceValueMap) {
        // 提前返回，如果沒有 replaceValueMapName
        var name = cssSelect.getReplaceValueMapName();
        if (Utils.isBlank(name)) return;
        // 獲取 key
        var key = cssSelect.getKey();

        Map<String, Object> map = replaceValueMap.computeIfAbsent(name, k ->
                replaceValueMapRepository.findById(k)
                        .map(ReplaceValueMap::getMap)
                        .orElseGet(HashMap::new)
        );

        // 處理 result 中的 key，確保其值為 List<String>
        if (result.containsKey(key) && result.get(key) instanceof List<?> list) {
            List<String> replacedList = list.stream()
                    .map(Object::toString) // 確保元素轉為 String
                    .map(s -> map.containsKey(s) && Utils.isNotBlank(map.get(s).toString()) ? map.get(s).toString() : s)
                    .toList();
            result.put(key, replacedList);
        }
    }

    protected Connection getConnection(String url) {
        return Jsoup.connect(url)
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7,zh-CN;q=0.6,ja;q=0.5")
                .header("Accept", "*/*")
                .header("Content-Type", "text/html; charset=UTF-8");
    }
}
