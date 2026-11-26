# 替換表 Pipeline

`REPLACE_VALUE_MAP` 使用既有替換表，設定範例：

```json
{
  "seq": 1,
  "type": "REPLACE_VALUE_MAP",
  "enabled": true,
  "useReplaceValueMap": "性別對照表"
}
```

查表時將輸入字串轉為小寫，例如替換表 `{"male":"男"}` 能將 `MALE`、`Male`、`male` 轉成 `男`。陣列逐項替換，保留順序；未命中、找不到替換表、替換值為 null 或空白時保留原值。非字串資料保留原值。此步驟本身不去重，外層擷取流程仍沿用原有的結果正規化。

單筆執行、批次、分頁及預覽會先收集啟用的擷取、Cookie、Header pipelines 所需表名，包含多層 `EXTRACT_OBJECTS` 欄位，去重後在執行迴圈外一次查詢。每次爬蟲建立自己的結果 context，但 `replaceValueMaps` 共用同一份唯讀快照；下一批執行重新載入。

整批 100 筆只載入一次；沒有使用替換表時不查詢。Pipeline 工具本身只處理記憶體資料，不存取資料庫。自訂呼叫 `ValuePipelineUtils` 時，需由呼叫端先載入並傳入 `ValuePipelineContext.replaceValueMaps`。

`GroupDatasetField.replaceValueMapName` 已移除，資料集刷新不再依群組欄位執行替換；舊設定中的此欄位會忽略，不會自動轉成 pipeline。需要替換的來源請設定 `REPLACE_VALUE_MAP`。API 的 `responsePipelines` 尚未實作，因此目前不會自動轉換 API 回應。
