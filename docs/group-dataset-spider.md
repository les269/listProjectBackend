# GroupDataset Spider 設定

對應 [前端 issue #66](https://github.com/les269/list-project-angular/issues/66)。

`config.groupDatasetFieldList` 的欄位設定支援 `required: boolean`，省略時為 `false`。
此欄位記錄必填標記，目前不會自動驗證資料內容。

`POST /api/group-dataset/update` 的 `config` 改為 `type: "spider"` 與
`groupDatasetSpiderList`。每筆使用 `spiderId` 指向 `spider_config.spider_id`：

```json
{
  "groupName": "example",
  "config": {
    "type": "spider",
    "byKey": "id",
    "imageSaveFolder": "",
    "groupDatasetSpiderList": [
      {
        "seq": 1,
        "spiderId": "detail",
        "label": "詳細資料",
        "isDefault": true,
        "visibleJson": true,
        "visibleUrl": true
      }
    ],
    "groupDatasetFieldList": [],
    "groupDatasetApiList": []
  }
}
```

`GET /api/group-dataset/get`、`GET /api/group-dataset/all` 回傳相同設定。
前端將元件與模型改為 Spider 時，選項來源應改為 SpiderConfig，識別值使用
`spiderId`。`visibleJson` 仍表示顯示參數輸入，`visibleUrl` 表示顯示 URL 輸入。

資料集刷新時，`type: "spider"` 使用清單中第一筆 `isDefault: true` 的 Spider。
file、folder、text 來源以主鍵參數執行；pagination 來源使用目標 URL 執行。
批次開始前載入設定、Cookie 與 Header，迴圈內僅執行請求與記憶體處理，
所有新資料最後一次批次儲存。請求間隔使用 SpiderConfig 的 `requestIntervalMs`。
沒有設定預設 Spider 時，只使用已存在的群組資料。

`POST /api/dataset/quick-refresh` 使用必填的 `spiderId`，取代 `scrapyName`；
`quickRefreshType: "params"` 使用 `params`，`"url"` 使用 `url`。
未選擇 Spider 時不執行爬蟲或寫入資料。

快速刷新以爬蟲回傳物件合併既有 JSON，保留未回傳的頂層欄位；回傳 `{}` 不清空
舊資料。請求 `byKey` 須符合群組設定且 `primeKey` 不可為空，儲存時固定使用來源
主鍵。一般清單重新整理（含 `all`）會從資料庫 `primeValue` 補正 JSON 主鍵，
避免舊 JSON 缺少主鍵或自訂欄位覆蓋主鍵後無法識別資料。檔案、資料夾、文字與
分頁的來源篩選規則仍適用，不會因此把所有群組資料加入每個資料集。

這項補正需執行資料集刷新才會更新已儲存的清單；不會恢復先前已被覆蓋的其他欄位。
爬蟲空結果仍可能建立只有主鍵的群組資料，自動重試與成功狀態辨識尚未包含在本次修正。

## 既有 SQLite／PostgreSQL 資料

兩種資料庫都將這份設定存放在 `group_dataset.config` 的 JSON 中，無須新增欄位
或修改資料表。既有 JSON 必須先完成來源對應與遷移；目前尚無自動轉換機制。

目前 enum 仍保留 `scrapy`，但群組模型已沒有 `groupDatasetScrapyList`，
Dataset 刷新只會從 `type: "spider"` 選取預設爬蟲，不能視為相容讀取舊設定。
Scrapy 的 `name` 指向 `scrapy_config`，Spider 的 `spiderId` 指向另一張表，
不可直接替換 JSON 鍵名或假設識別值相同。升級既有群組時：

1. 備份既有 dynamic 資料庫，另存每個群組的舊來源、標籤、排序、預設與顯示選項，
   建立 Scrapy 名稱到 Spider ID 的明確對照。目前工作目錄沒有先前文件所稱的
   `group-dataset-spider-sqlite.sql`／`group-dataset-spider-postgresql.sql`，
   尚不能宣稱兩種資料庫的遷移已完成。無法對應的來源須保留備份並列為待處理，
   不應直接清空舊清單後視為完成遷移。
2. 啟動新版後端，建立或確認對應的 SpiderConfig 與 SpiderItem。
3. 將選定的 Spider ID、標籤、排序與顯示選項填入 `groupDatasetSpiderList`，
   設定預設項目，並將 `type` 改為 `"spider"`。
4. 呼叫 update API，再讀取群組確認設定；驗證資料集刷新。

本次不自動轉換既有 Scrapy 的抽取規則，也不修改本機資料庫。
`DatasetConfig.scrapyPagination` 仍使用原有分頁來源；SpiderPagination 來源切換
不包含在這次 GroupDataset 設定調整中。

## 2026-11-25 前端交接核對結果

核對基準為本後端工作目錄（包含未提交變更），不是已部署版本或前端所述的
`HEAD 43ef42b`。未存取前端原始碼、本機資料庫或實際站點。

| 前端核對事項 | 後端現況與限制 |
| --- | --- |
| 群組新欄位 | 已有 `type: "spider"`、`groupDatasetSpiderList`、`spiderId`，並保留 `seq`、`label`、`isDefault`、`visibleJson`、`visibleUrl`。JSON 與持久化序列化往返測試通過；不代表舊資料已遷移。 |
| 預設來源 | Dataset 刷新選取清單陣列中第一筆 `isDefault: true`，不依 `seq` 排序；沒有預設來源時不爬取。`GroupDatasetServiceImpl.refreshGroupDataset` 本身仍只有 API 分支，須區分群組刷新與 Dataset 刷新入口。 |
| 快速刷新 | 已使用 `spiderId`；`quickRefreshType: "params"` 將 `params` 傳給主鍵執行，URL 模式傳 `url`。結果合併既有資料並保護主鍵；後端沒有將 `otherSetting.useSpider` 的舊名稱轉為 ID。 |
| 舊識別對應 | 未提供自動對照或轉換；需由了解原抽取規則與新 Spider 設定的人員建立對照。無法對應時應先處理設定，避免直接重新儲存造成關聯遺失。 |
| `INITIAL_KEYS` | 已有執行邏輯，讀取最初的主鍵清單；跨步驟、空 URL、`EXTRACT_OBJECTS` 子欄位均可使用。URL 執行與預覽未提供初始主鍵，取得 `[]`。 |
| 直接爬取回應 | `use-url`／`use-prime-key` 從 `{}` 累積結果並回傳 `result.jsonString()`，沒有額外 envelope 或再次 JSON 編碼。Controller 的 Java 回傳型別仍為 `String`，未明定 `produces = application/json`；HTTP Content-Type 與前端預設 JSON 解析仍需整合驗收。 |
| 已刪除 Spider | 執行入口查無設定時拋 `LsbException`，HTTP 404；錯誤 body 為訊息文字。群組儲存尚未驗證引用的 ID 是否存在。 |
| 主鍵數量 | `primeKeySize` 有設定欄位，但執行入口尚未檢查數量是否相符，也沒有完整的 null／空陣列驗證。 |
| URL 專用 Spider | `isUrlBased: true` 時，主鍵執行會回 HTTP 400 與訊息文字；群組的 `visibleJson` 尚未與此設定交叉驗證。 |
| 刪除防護 | Item／Config 的 delete 直接呼叫 repository，沒有伺服器端綁定檢查。`/api/spider-item/in-use` 實際檢查項目是否存在；查綁定應用 `/api/spider-mapping/in-use-by-spider-item-id`，但先查再刪仍不能防止競態。 |
| 解除綁定補號 | mapping delete 在三個參數皆符合時刪除指定資料，並於同一筆交易內將該 Spider 的剩餘 `executionOrder` 依原順序從 0 連續補號；找不到指定資料時不變更。`update-list` 仍只 saveAll，沒有重建整份清單的語意。 |
| `skipWhenUsingUrl` | URL 模式只在首次處理 URL 前判斷並跳過標記項目；首次項目處理後，後續項目不再依此旗標跳過。並非「URL 模式一律跳過」。 |
| 測試 trace | 現有預覽只回擷取結果，未找到每個步驟的結構化 trace 回應契約。執行失敗會寫後端 log 並繼續，呼叫端可能得到部分結果或 `{}`；前端不可把所有成功回應都當成完整爬取成功。 |
| URL 的 Unix 毫秒 `time` | 已提供 `CALCULATE` 運算式 `#nowMillis()`，寫入 `time` 後可在下一步 URL 使用 `{{$.time}}`。這與資料更新時間 `@UpdateTimestamp` 無關，設定範例見下節。 |

主要程式依據：

- `entity/dynamic/dataset/GroupDatasetConfig.java`、`GroupDatasetSpider.java`
- `service/dataset/DatasetServiceImpl.java`、`GroupDatasetServiceImpl.java`
- `controller/spider/SpiderController.java`、`SpiderItemController.java`
- `service/spider/SpiderServiceImpl.java`、`SpiderItemServiceImpl.java`、`SpiderConfigServiceImpl.java`、`SpiderMappingServiceImpl.java`
- `utils/ValuePipelineUtils.java`、`config/GlobalDefaultExceptionHandler.java`

上述 Java 路徑皆相對於 `src/main/java/com/lsb/listProjectBackend/`。

本次驗證命令：

```powershell
.\gradlew.bat test --tests '*GroupDatasetSpiderContractTest' --tests '*GroupDatasetSpiderRefreshTest' --tests '*SpiderInitialKeysTest' --tests '*SpiderEmptyUrlTest' --tests '*SpiderFailureContinuationTest' --tests '*SpiderBatchExecutionTest' --offline
```

首次核對結果：33 項測試，31 項通過、2 項失敗。失敗為
`rejectsOldScrapyTypeAfterSwitchToSpider`（enum 仍接受 `scrapy`）與
`sqliteMigrationClearsOldReferencesAndPreservesOtherSettingsWhenRepeated`
（遷移 SQL 不存在）。首次核對未變更執行程式、未執行資料遷移、未驗證 PostgreSQL
或 HTTP 端到端往返；前端提到的另份 11 項核對文件未提供全文，不能宣稱已逐條驗收。

後續主鍵保護修正：`GroupDatasetSpiderRefreshTest` 共 14 項通過，涵蓋快速刷新
空結果／衝突主鍵的合併、錯誤 key／null 結果拒絕，以及 file／folder／text／all
清單主鍵補正。執行命令為 `.\gradlew.bat test --tests '*GroupDatasetSpiderRefreshTest' --offline`；
未重新執行全部測試，前述兩項既有失敗未在此次處理。

## 空 URL 初始化與 URL 時間參數

`SpiderExecutionService.executeRequest` 收到 null、空字串或純空白 URL 時，不建立
HTTP 連線，也不等待請求間隔，直接執行擷取規則及 pipeline。`JSON_PATH` 使用
`{}`，`SELECT` 使用空 HTML；規則仍受 condition 控制，pipeline 也須啟用。

例如在主鍵執行流程中，將初始化項目的 `itemSetting` 設為：

```json
{
  "url": "",
  "urlType": "BY_PARAMS",
  "mode": "JSON_PATH",
  "skipWhenUsingUrl": false,
  "extractionRuleList": [
    {
      "key": "time",
      "jsonPath": "$.missing",
      "pipelines": [
        {
          "type": "CALCULATE",
          "enabled": true,
          "seq": 1,
          "calculateConfig": {
            "expression": "#nowMillis()",
            "variablePaths": []
          }
        }
      ]
    }
  ]
}
```

下一個項目使用 `urlType: "BY_PARAMS"` 與
`url: "https://example.com/api?time={{$.time}}"`，即可帶入初始化時產生的
Unix 毫秒整數字串。`#nowMillis()` 取後端當下時間，不受時區影響；需要秒數時才用
`#nowSeconds()`。同一次流程要共用時間，應引用已存的 `time`，避免重複計算。

注意：`use-url` 入口目前會把傳入 URL 套到第一個未跳過的項目，覆蓋該項目的
設定 URL。因此，傳入非空 URL 時，即使第一項設定為空 URL，仍會發請求；目前不能
保證這個入口在使用傳入 URL 前先跑一個不發請求的初始化步驟。
`skipWhenUsingUrl: true` 會跳過整個項目及其 pipeline，也不能用來達成先初始化。
上述範例適用主鍵流程；若需求是 URL 入口也必須先初始化，仍需調整執行流程。
