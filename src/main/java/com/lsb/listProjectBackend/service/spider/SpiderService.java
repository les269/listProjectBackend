package com.lsb.listProjectBackend.service.spider;

import java.util.List;

import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.domain.spider.SpiderTestTO;

public interface SpiderService {

    /**
     * 執行爬蟲流程 - 基於 URL
     *
     * @param spiderId 爬蟲配置 ID
     * @param url      輸入 URL
     * @return 爬蟲結果
     */
    String executeByUrl(String spiderId, String url) throws Exception;

    /**
     * 執行爬蟲流程 - 基於主鍵陣列
     *
     * @param spiderId     爬蟲配置 ID
     * @param primeKeyList 主鍵陣列
     * @return 爬蟲結果
     */
    String executeByPrimeKeyList(String spiderId, List<String> primeKeyList) throws Exception;

    /**
     * 預覽抽取結果（單一設定，不連線）
     *
     * @param req 抽取設定
     * @return 預覽結果
     */
    String previewExtraction(SpiderItemTO req);

}
