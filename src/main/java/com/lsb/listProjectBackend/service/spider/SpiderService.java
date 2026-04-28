package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;

import java.util.List;
import java.util.Map;

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

}
