# 分頁爬蟲

執行入口為 `SpiderService.executePagination(spiderPaginationId, force)`。

```http
POST /api/spider/execute-pagination?spiderPaginationId=anime&force=false
```

回傳本次成功寫入的 `SpiderPaginationDataTO` 列表；跳過更新的資料不包含在回傳值。
每頁成功後即儲存，後續失敗會拋出錯誤，但保留已成功的頁面。

## 資料與翻頁

- `paramSettings` 依 `seq` 排序，展開為參數的所有組合。沒有參數時執行一次。
- `groupIdTemplate`、`startUrl` 使用 `{{$.year}}` 等 JSONPath 範本注入目前參數。
- 每頁獨立擷取一份物件，從 `dataIdKey`（普通 key 或 JSONPath）取得單筆資料 ID。
- 整份擷取結果存入 `data`；主鍵為 `(spiderPaginationId, groupId, dataId)`。
- `nextPageUrlKey` 為下一頁 URL 的 key 或 JSONPath；空白設定、缺少欄位、null、空字串或空列表皆終止翻頁。
- 下一頁可用相對 URL；同一組參數重複 URL 時會停止並回報錯誤。
- `dataIdKey`、`nextPageUrlKey` 可讀取單一值或單元素列表；多元素列表會回報錯誤。
- Headers、Cookies 使用 `SPIDER_PAGINATION` 類型與 `spiderPaginationId` 關聯。
- `requestIntervalMs` 在後續請求前等待，跨參數組也計入；不控制不同執行任務間的流量。

## 更新控制

新資料或沒有 `lastSuccessTime` 的資料先取得首次成功結果。
既有資料依以下規則決定是否覆寫：

- `lastUpdateMode = FIXED`：超過 `lastUpdateDate` 後停止更新。
- `lastUpdateMode = USE_PARAM`：以指定 TIME_RANGE 參數當前的格式化值作為截止時間；缺少的月／日補 1，時間補 0。
- `updateIntervalMode = FIXED`：到達 `lastSuccessTime + updateInterval/updateIntervalUnit` 才更新。
- `updateIntervalMode = USE_PARAM`：使用指定 TIME_RANGE 參數的 `timeInterval/timeIntervalUnit` 作為重抓間隔。
- `NONE` 不設該項限制；`force=true` 略過期限與間隔。

必須先請求並擷取才能得知 dataId，因此更新控制限制的是儲存覆寫，仍會請求頁面以取得下一頁網址。
時間沿用後端 `LocalDateTime` 與伺服器時間，日期範圍含起點與終點，數字與時間步長需為正數。

## 既有資料庫

建表 SQL 只適用新表；既有表需遷移主鍵，不能只增加 `group_id` 欄位。
請先備份並替既有紀錄決定正確的 groupId，舊資料無法一律從 dataId 反推分組。

PostgreSQL：加入並填妥 `group_id`，設為 NOT NULL，再將原主鍵改為
`PRIMARY KEY (spider_pagination_id, group_id, data_id)`。

SQLite：建立具有新三欄主鍵的表，將舊資料連同指定的 groupId 複製過去，確認後切換表名。
舊設定 JSON 中的 `dataIdTemplate` 需改為 `groupIdTemplate`，另補上 `dataIdKey`。

單筆查詢、刪除及存在檢查 API 現在都需要 `spiderPaginationId`、`groupId`、`dataId` 三個參數。
