# OpenAPI 給前端 AI 使用

本專案使用 springdoc 自動掃描 Spring MVC Controller 與 DTO，產生 OpenAPI；不包含 Swagger UI。

## 取得規格

啟動後端（`./gradlew.bat bootRun`），使用預設設定時：

- JSON：<http://localhost:8081/list-project-backend/v3/api-docs>
- YAML：<http://localhost:8081/list-project-backend/v3/api-docs.yaml>

若調整連接埠或 context path，請同步修改網址。

前端 AI 能存取後端時，直接提供上述 JSON 網址。無法存取時，先下載檔案，再提供給 AI：

```powershell
Invoke-WebRequest -Uri 'http://localhost:8081/list-project-backend/v3/api-docs' -OutFile openapi.json
```

可使用以下提示：

> 請讀取 openapi.json，依照其中的 paths、schemas 與 required 欄位實作前端 API 呼叫。遇到未描述的回傳結構或業務規則，請先列出缺少的資訊，不要自行猜測。

## 文件維護

修改 Controller 或 DTO 後，重啟後端並重新取得規格即可。初期不必逐支 API 加註解；需要改善說明時，再加入 `@Operation`、`@Schema` 與範例。

自動產生的文件只能反映 Java 型別與註解。回傳 `String`、`Map` 或動態 JSON 的端點，以及錯誤格式、業務規則，仍可能需要補充描述及實際範例。

若某個環境不需要提供文件，可設定 `springdoc.api-docs.enabled=false`。

## Whitelabel Error Page／404

請確認完整網址包含 `/list-project-backend/v3/api-docs`。目前沒有安裝 Swagger UI，因此 `/swagger-ui.html` 不提供頁面。

若規格網址也回傳 404，先停止目前執行的後端，重新載入 Gradle 專案，再啟動。可直接從專案根目錄執行，確保使用 Gradle 的最新依賴：

```powershell
.\gradlew.bat bootRun
```

VS Code 若仍使用舊的依賴，執行命令面板的 `Java: Clean Java Language Server Workspace`，讓 Java 專案重新匯入後再啟動。

若狀態碼為 500，請查看後端 console 的例外；那代表規格產生過程發生錯誤，不能只依 Whitelabel 頁面的標題判斷原因。
