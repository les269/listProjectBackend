package com.lsb.listProjectBackend.service.spider;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.spider.ExecutionContext;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationDataPK;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.service.common.HeadersService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderPaginationExecutionService {
    private final SpiderPaginationService paginationService;
    private final SpiderPaginationDataService dataService;
    private final SpiderPaginationParameterService parameterService;
    private final SpiderExecutionService executionService;
    private final HeadersService headersService;
    private final CookieListService cookieListService;
    private final SpiderPaginationUpdatePolicy updatePolicy;

    public List<SpiderPaginationDataTO> execute(String spiderPaginationId) throws Exception {
        return execute(spiderPaginationId, false);
    }

    /** 執行入口：準備設定、展開參數，再依序執行各組分頁。 */
    public List<SpiderPaginationDataTO> execute(String spiderPaginationId, boolean force) throws Exception {
        ExecutionContext context = createExecutionContext(spiderPaginationId, force);
        Map<List<String>, SpiderPaginationDataTO> existing = new LinkedHashMap<>();
        dataService.getBySpiderPaginationId(spiderPaginationId)
                .forEach(record -> existing.put(List.of(record.groupId(), record.dataId()), record));
        Map<List<String>, SpiderPaginationDataTO> pending = new LinkedHashMap<>();
        Set<String> completedGroups = new HashSet<>();
        List<Map<String, Object>> combinations = parameterService.expand(
                context.getConfig().getParamSettings(), LocalDateTime.now());
        try {
            for (Map<String, Object> params : combinations) {
                executeGroup(context, params, existing, pending, completedGroups);
            }
        } catch (Exception failure) {
            try {
                persistPages(context, existing, pending, completedGroups);
            } catch (Exception saveFailure) {
                failure.addSuppressed(saveFailure);
            }
            throw failure;
        }
        return persistPages(context, existing, pending, completedGroups);
    }

    /**
     * 批次儲存本次結果；不保留歷史時，只清理完整抓取成功的參數組。
     *
     * @param context 本次執行的 pagination ID、設定與連線資訊
     * @param records 資料庫既有資料加上本次抓取結果；key 為 [groupId, dataId]，value 為該筆資料的最新內容
     * @param pending 本次抓到、待寫入的資料；key 同為 [groupId, dataId]，包含失敗組別已抓到的部分結果
     * @param completedGroups 本次已完整抓完所有分頁的 groupId，不包含被跳過或抓取失敗的組別
     * @return 本次寫入後的資料，包含資料庫產生的時間欄位
     */
    private List<SpiderPaginationDataTO> persistPages(ExecutionContext context,
            Map<List<String>, SpiderPaginationDataTO> records,
            Map<List<String>, SpiderPaginationDataTO> pending, Set<String> completedGroups) {
        List<SpiderPaginationDataTO> updates = new ArrayList<>(pending.values());
        if (context.getConfig().isRetainHistory()) {
            return updates.isEmpty() ? List.of() : dataService.updateAll(updates);
        }
        // 已完整抓完的組別中，舊資料存在但本次沒有抓到的 key，即為待刪除資料。
        // key.get(0) 是 groupId，key.get(1) 是 dataId。
        List<SpiderPaginationDataPK> idsToDelete = records.keySet().stream()
                .filter(key -> completedGroups.contains(key.get(0)) && !pending.containsKey(key))
                .map(key -> new SpiderPaginationDataPK(context.getSpiderPaginationId(), key.get(0), key.get(1)))
                .toList();
        return updates.isEmpty() && idsToDelete.isEmpty() ? List.of()
                : dataService.synchronize(updates, idsToDelete);
    }

    /** 設定、Headers 與 Cookies 每次執行只讀取一次，供所有參數組共用。 */
    private ExecutionContext createExecutionContext(String spiderPaginationId, boolean force) {
        SpiderPaginationTO pagination = paginationService.getById(spiderPaginationId);
        if (pagination == null) {
            throw new LsbException("Pagination config not found: " + spiderPaginationId, HttpStatus.NOT_FOUND);
        }
        SpiderPaginationConfig config = pagination.paginationConfig();
        validate(config);
        var headers = headersService.getByRefIdAndType(spiderPaginationId, Global.HeadersMapType.SPIDER_PAGINATION);
        var cookies = cookieListService.getByRefIdAndType(spiderPaginationId,
                Global.CookieListMapType.SPIDER_PAGINATION);
        return new ExecutionContext(spiderPaginationId, config, headers, cookies, force);
    }

    /** 一組參數決定 groupId 與起始 URL；重複網址檢查僅限於這一組。 */
    private void executeGroup(ExecutionContext context, Map<String, Object> params,
            Map<List<String>, SpiderPaginationDataTO> records,
            Map<List<String>, SpiderPaginationDataTO> pending, Set<String> completedGroups)
            throws Exception {
        String groupId = JsonUtils.replaceValueByJsonPath(context.getConfig().getGroupIdTemplate(), params);
        List<SpiderPaginationDataTO> existing = records.values().stream()
                .filter(record -> groupId.equals(record.groupId())).toList();
        LocalDateTime lastSuccessTime = null;
        if (!existing.isEmpty() && existing.stream().allMatch(record -> record.lastSuccessTime() != null)) {
            lastSuccessTime = existing.stream().map(SpiderPaginationDataTO::lastSuccessTime)
                    .min(LocalDateTime::compareTo).orElse(null);
        }
        if (!updatePolicy.shouldUpdate(lastSuccessTime, context.getConfig(), params, LocalDateTime.now(),
                context.isForce())) {
            return;
        }
        String url = JsonUtils.replaceValueByJsonPath(context.getConfig().getStartUrl(), params);
        // 同一組若再次執行，必須重新完整抓取才能視為成功。
        completedGroups.remove(groupId);
        var visited = new HashSet<String>();
        var nextPageUrlKey = context.getConfig().getNextPageUrlKey();
        List<SpiderPaginationDataTO> saved = new ArrayList<>();
        while (Utils.isNotBlank(url)) {
            if (!visited.add(url)) {
                throw new LsbException("Repeated pagination URL in group " + groupId + ": " + url);
            }
            DocumentContext result = fetchPage(context, url);
            // 直接使用擷取到的下一頁網址；沒有值時結束翻頁。
            Object next = JsonUtils.getValueOrNull(result, nextPageUrlKey);
            SpiderPaginationDataTO record = collectPage(context, groupId, url, result, records);
            var key = List.of(groupId, record.dataId());
            records.put(key, record);
            pending.put(key, record);
            saved.add(record);
            url = next != null ? next.toString() : null;
        }
        // 整組翻頁完成才標記成功，資料統一在執行迴圈外儲存。
        LocalDateTime completedTime = LocalDateTime.now();
        for (SpiderPaginationDataTO record : saved) {

            var successful = new SpiderPaginationDataTO(record.spiderPaginationId(), record.groupId(), record.dataId(),
                    record.data(), record.createdTime(), record.updatedTime(), completedTime);
            var key = List.of(groupId, record.dataId());
            records.put(key, successful);
            pending.put(key, successful);
        }
        completedGroups.add(groupId);
    }

    /** 每頁使用新的結果物件，避免缺少下一頁欄位時沿用前頁網址。 */
    private DocumentContext fetchPage(ExecutionContext context, String url) throws Exception {
        DocumentContext result = JsonPath.parse("{}");
        SpiderPaginationConfig config = context.getConfig();
        // 第一筆不等待；後續請求跨參數組也沿用同一個間隔狀態。
        Long interval = context.isHasRequested() ? config.getRequestIntervalMs() : null;
        executionService.executeRequest(url, config.getExtractionRuleMode(), config.getExtractionRuleList(),
                context.getHeaders(), context.getCookies(), result, interval);
        context.setHasRequested(true);
        return result;
    }

    /** 只建立頁面資料，不存取資料庫。 */
    private SpiderPaginationDataTO collectPage(ExecutionContext context,
            String groupId, String url, DocumentContext result,
            Map<List<String>, SpiderPaginationDataTO> records) {
        Object dataId = JsonUtils.getValueOrNull(result, context.getConfig().getDataIdKey());
        if (dataId == null) {
            throw new LsbException("Missing dataId at " + context.getConfig().getDataIdKey() + " for " + url);
        }
        var dataIdStr = dataId.toString();
        var existing = records.get(List.of(groupId, dataIdStr));
        Map<String, Object> data = new LinkedHashMap<>(result.<Map<String, Object>>read("$"));
        return new SpiderPaginationDataTO(context.getSpiderPaginationId(), groupId,
                dataIdStr, data,
                existing == null ? null : existing.createdTime(), null, null);
    }

    /** 開始連線前檢查必要設定。 */
    private void validate(SpiderPaginationConfig config) {
        if (Utils.isNull(config, config.getExtractionRuleMode(), config.getExtractionRuleList())
                || Utils.isBlank(config.getGroupIdTemplate(), config.getDataIdKey(), config.getStartUrl())) {
            throw new LsbException(
                    "Pagination requires groupIdTemplate, dataIdKey, startUrl, mode and extraction rules");
        }
    }
}
