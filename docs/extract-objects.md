# EXTRACT_OBJECTS

`ValuePipeline.extractObjectsConfig` 對齊前端的 `mode` 與 `fields`。
每個外層元素獨立產生一個物件；各欄位依 `seq` 執行自己的 Pipelines。

HTML 範例：外層 ExtractionRule 設定 `key = "items"`、`selector = "tbody tr"`，Pipeline 使用：

```json
{
  "seq": 1,
  "type": "EXTRACT_OBJECTS",
  "enabled": true,
  "extractObjectsConfig": {
    "mode": "SELECT",
    "fields": [
      { "seq": 1, "key": "name", "selector": "td.name a", "pipelines": [] },
      {
        "seq": 2,
        "key": "url",
        "selector": "td.name a",
        "pipelines": [
          { "seq": 1, "type": "EXTRACT_ATTR", "enabled": true, "attributeName": "href" },
          { "seq": 2, "type": "FIRST_VALUE", "enabled": true }
        ]
      }
    ]
  }
}
```

- `SELECT`：每個外層 Element 為一筆；子 selector 限定在該 Element 內，空 selector 代表該元素本身。
- 沒有子 Pipeline 時：未選到為 null、一個節點為文字字串、多個節點為文字陣列。
- 有子 Pipeline 時：初始值為所選節點的文字陣列，並傳入 Elements，沿用現有 Pipeline 行為；要取單值可加 `FIRST_VALUE`。
- `JSON_PATH`：將前一個 Pipeline／外層 JSONPath 取得的陣列逐項轉物件，子 jsonPath 相對於各項目；空 jsonPath 代表 `$`。非陣列非 null 輸入作為一筆。
- `CURRENT_DATA_KEY` 讀取該筆已完成的欄位；`FETCH_CURRENT_DATA` 讀取上一層的結果物件。Headers 與 Cookies 沿用父層。
- 欄位 key 為輸出物件的屬性名稱，須非空且不重複。
- 子結果不自動清洗；整個物件列表也不自動去重，保留相同資料列。使用者選擇的子 Pipeline 仍會照原定義處理值。
- `EXTRACT_OBJECTS` 可以作為子 Pipeline，處理巢狀資料。

此功能產生物件陣列；分頁執行器的跨頁累積與逐筆儲存流程需要另行接上。
