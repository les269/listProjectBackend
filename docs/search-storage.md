# 搜尋值儲存

在 dynamic 資料庫新增兩個資料表，SQLite 與 PostgreSQL 的初始化 SQL 均包含此結構。

| 資料表 | 欄位 | 用途 |
| --- | --- | --- |
| `search_bookmark` | `id`、`name`、`created_time`、`updated_time` | 儲存名稱，`id` 為字串主鍵 |
| `search_bookmark_item` | `id`、`value`、`seq`、`created_time`、`updated_time` | 儲存搜尋值，`(id, value)` 為複合主鍵，`id` 參照 `search_bookmark.id` |

兩表以相同的 `id` 一對多關聯；先建立名稱，再以同一個 ID 建立多筆不同的搜尋值。同一 ID 下不可有重複的 value。刪除時先刪除搜尋值，再刪除名稱。除 `seq` 外，欄位皆不可為 NULL，ID 由呼叫端指定。

`seq` 是整數排序值，API 使用同名欄位。新增搜尋值時，伺服器指定為同一書籤 ID 內最大 `seq + 1`，沒有非空序號時從 `1` 開始，忽略新增請求的 `seq`。更新既有 `(id, value)` 時可指定 `seq`，省略則保留原值。

刪除單筆搜尋值後，同一書籤內的剩餘項目依原 `seq` 升冪重編為 `1..N`；舊資料的空序號排在最後，序號相同時依 `value` 排序。刪除與重編在同一交易內執行，不影響其他書籤。`/item/get` 依此順序回傳，`/item/all` 不指定排序。

目前初始化流程只建立不存在的資料表，不會自動替既有表新增 `seq`。既有資料庫可執行一次 [補上 seq 欄位](migrations/search-bookmark-item-seq.sql)，欄位已存在時勿重複執行。此遷移只新增欄位，不處理先前主鍵或日期欄位的變更。

若已加入 `seq` 卻仍出現 `no such column`，先確認實際查詢的資料庫。預設 SQLite 位於使用者的 `AppData/Roaming/listProjectData/dynamic.sqlite`，可能和設定選定的 DB 不同。資料源切面須先設定 routing key，再開始交易，直到交易完成才清除，確保連線使用選定的 DB。

`created_time` 為建立時間，`updated_time` 為更新時間，皆使用 TIMESTAMP。Entity 以 `createdTime`、`updatedTime` 對應，由 Hibernate 自動維護，建立時間不隨更新改變。直接執行 SQL 新增時，兩欄預設為目前時間；直接執行 SQL 更新時需自行更新 `updated_time`。

初始化 SQL 的 `CREATE TABLE IF NOT EXISTS` 不會修改既有資料表；若先前已建立這兩個表，需另行遷移補上時間欄位，並將搜尋值表的主鍵從 `id` 改為 `(id, value)`。

可使用 `SearchBookmarkItemRepository.findAllById(id)` 查詢書籤下所有搜尋值，查詢可使用 `(id, value)` 複合主鍵索引。資料存取沿用現有 dynamic 資料源與交易管理方式。

```sql
SELECT n.id, n.name, v.value
FROM search_bookmark n
JOIN search_bookmark_item v ON v.id = n.id
WHERE n.id = ?;
```

SQLite 連線需要啟用 `PRAGMA foreign_keys = ON` 才會執行外鍵檢查。

## CRUD API

以下路徑皆位於 `/list-project-backend/api/search-bookmark`，沿用目前選定的 dynamic 資料庫。

| 方法 | 路徑 | 輸入 | 回傳 |
| --- | --- | --- | --- |
| GET | `/all` | 無 | 書籤陣列 |
| GET | `/get` | Query `id` | 單筆書籤 |
| POST | `/update` | JSON `id`、`name` | 儲存後的書籤 |
| DELETE | `/delete` | Query `id` | 空回應，連同對應搜尋值刪除 |
| GET | `/item/all` | 無 | 搜尋值陣列 |
| GET | `/item/get` | Query `id` | 該 ID 下所有搜尋值的陣列，沒有資料時為 `[]` |
| POST | `/item/update` | JSON `id`、`value`、可選 `seq` | 儲存後的搜尋值 |
| DELETE | `/item/delete` | Query `id`、`value` | 空回應，只刪除該 `(id, value)` 搜尋值，保留書籤與其他搜尋值 |
| PUT | `/item/order` | JSON `id`、`values` | 依新順序排列的搜尋值陣列 |

`/update` 依 ID 新增或更新書籤；`/item/update` 依 `(id, value)` 新增或更新搜尋值。先儲存書籤 `{"id":"bookmark-1","name":"我的搜尋"}`，再儲存搜尋值 `{"id":"bookmark-1","value":"關鍵字"}`。同一 ID 搭配不同 value 會新增另一筆搜尋值，保留原本的值；再次儲存相同 `(id, value)` 會使用既有資料，保留建立時間。

回傳物件包含 `createdTime`、`updatedTime`，由伺服器產生；請求中即使提供時間也會忽略。更新時保留原建立時間，更新時間由 Hibernate 維護。

ID、書籤名稱不可空白，搜尋值不可為 `null`（允許空字串），格式不符回傳 400。單筆書籤查詢不存在，或儲存搜尋值時書籤不存在，回傳 404。按 ID 查詢搜尋值是列表查詢，沒有資料時回傳空陣列。刪除不存在的 ID 為成功的空操作。書籤列表與所有搜尋值列表未指定排序，按 ID 查詢搜尋值則依 seq 排序。刪除書籤與所有搜尋值在同一 dynamic 交易內執行，先刪除搜尋值再刪除書籤。

刪除書籤不會自動清除 Theme 設定中的 `searchBookmarkId`；呼叫端應先解除或改選書籤。

單筆搜尋值刪除範例：`DELETE /list-project-backend/api/search-bookmark/item/delete?id=bookmark-1&value=keyword`。`value` 必填，允許空字串；省略時回傳 400，不會改成整批刪除。Query 參數須依 URL 編碼規則傳送。

### 一次更新排序

```http
PUT /list-project-backend/api/search-bookmark/item/order
Content-Type: application/json
```

```json
{
  "id": "bookmark-1",
  "values": ["搜尋值C", "搜尋值A", "搜尋值B"]
}
```

傳入該書籤全部搜尋值的完整順序，後端依陣列順序指定 `seq = 1..N`，在同一 dynamic 交易內更新並回傳排序後的項目（含日期）。先驗證全部資料再修改；缺少項目、重複、包含其他書籤的值或 `null` 時回傳 400，不做部分更新。書籤不存在回傳 404。`values: []` 僅適用於沒有 item 的書籤，不表示清空資料。空字串 value 合法，必須照實包含。既有單筆更新介面保留相容，拖曳排序應使用此 API。

## Theme 設定

`ThemeOtherSetting.searchBookmarkId` 保存 `search_bookmark.id`，JSON 欄位名稱為 `searchBookmarkId`，未指定時為 `null`，表示未選擇搜尋書籤。既有 Theme API 的 `themeOtherSetting` 可攜帶此欄位：

```json
{
  "themeOtherSetting": {
    "searchBookmarkId": "bookmark-1"
  }
}
```

每個 Theme 可選擇一個搜尋書籤，多個 Theme 可共用同一書籤。目前直接儲存在設定 JSON 中，不新增 mapping 表，也不需要新增 Theme 資料表欄位。此 JSON 參照不受資料庫外鍵約束，尚未加入書籤存在檢查或刪除時清理參照的行為。

若未來需要一個 Theme 關聯多個書籤，或頻繁反查使用中的 Theme、以外鍵限制刪除，再考慮獨立關聯表；屆時應選定單一關聯儲存來源，避免 JSON 與 mapping 表各存一份而不一致。
