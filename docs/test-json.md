# JSON 爬蟲測試資料

`GET /api/test/json` 讀取後端工作目錄下的 `src/test/test.json`，將內容解析成 JSON 物件或陣列後直接回傳，供爬蟲當作測試來源。

將測試資料放入 `src/test/test.json`，從專案根目錄啟動後可使用：

```text
http://localhost:8081/list-project-backend/api/test/json
```

每次請求都會重新讀取檔案，修改資料後不必重啟。檔案不存在時回傳 404；空白檔案、JSON 格式錯誤或包含多份 JSON 文件時回傳 400。

可透過 `test.json.path` 設定其他路徑，例如啟動參數 `--test.json.path=C:/data/test.json`。檔案由後端讀取，不需要上傳，也不接受請求指定檔案路徑。
