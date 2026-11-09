# Playwright 人工驗證與狀態觀測

後端開啟可見 Chrome，使用者手動驗證。每個網站 origin 有獨立、持久化的 profile，
再次開啟會沿用 cookie、localStorage 等瀏覽器資料。瀏覽器狀態與 Jsoup 測試結果分開回報。
只有呼叫 complete 且測試成功後，才將 cookie 與 User-Agent 存到目前選取的 dynamic DB，關聯至指定 spider。

## 啟用

後端電腦需有可互動桌面和已安裝的 Chrome：

```powershell
$env:SPIDER_BROWSER_VERIFICATION_ENABLED = 'true'
.\gradlew.bat bootRun
```

設定前綴 `spider.browser-verification`：

| 設定 | 預設值 | 說明 |
| --- | --- | --- |
| enabled | false | 啟用開窗功能 |
| channel | chrome | 可改為已安裝的 `msedge`；空字串使用 Playwright Chromium |
| allowed-hosts | [anidb.net] | 起始 URL 的精確 host 清單，只接受 HTTPS、443 埠 |
| profile-directory | .browser-verification/profiles | 相對後端工作目錄，也可指定絕對路徑 |
| session-timeout-seconds | 600 | 限制為 30–1800 秒，到期關窗但保留 profile |
| frontend-origin | http://localhost:5173 | 自行串接前端時允許的跨來源網址 |

profile 的子目錄名稱來自 origin（scheme、host、port）的雜湊；同一網站不同路徑共用，
不同 host 分開。`https://anidb.net/` 與 `https://anidb.net:443/anime/1` 使用同一份 profile。
目前未區分同網站的不同帳號，請把這項功能當成單人本機操作。
不會使用或修改平常 Chrome 的使用者 profile。

預設 `.browser-verification/` 已加入 Git ignore。profile 包含 cookie 等敏感資料，
不要納入版本控制；自訂儲存位置時也應排除。取消、到期與成功儲存都只關窗、不刪除 profile。
若需重新開始全新瀏覽器狀態，先關閉驗證視窗，再手動移走該網站的 profile 目錄。
cookie 是否能跨次使用仍受其期限與網站判定影響；持久化不代表能保證通過 Cloudflare。

若使用 bundled Chromium，先執行 `.\gradlew.bat installPlaywrightChromium` 並將 channel 設為空字串。
應用不會在啟動時自動下載瀏覽器。

## Swagger UI 測試流程

啟動後開啟：

- Swagger UI：<http://localhost:8081/list-project-backend/swagger-ui.html>
- OpenAPI JSON：<http://localhost:8081/list-project-backend/v3/api-docs>

找到 **Browser verification** 分組，使用 Try it out / Execute。
以下 API 路徑都需加上 context path `/list-project-backend`。

### 1. 開窗

`POST /api/spider/browser-verification`

```json
{ "url": "https://anidb.net/" }
```

回傳 `sessionId`、`url`、`expiresAt`。瀏覽器開在**後端所在電腦**，同時只允許一個驗證視窗。
人工操作時保留初始分頁。可選填 `readySelector`，內容必須是正常頁面實際存在、Playwright 和 Jsoup 都支援的 CSS selector。
例如你自行確認正常頁面有 `#normal-content` 才使用該值；不要照貼不存在的 selector。
省略時以主頁面 HTTP 狀態、標題與非空 `cf_clearance` 做初步判定。

### 2. 觀察瀏覽器狀態

`GET /api/spider/browser-verification/{sessionId}/status`

後端每秒讀取一次頁面狀態；呼叫此 API 也會更新觀測結果。
**不重新整理頁面、不點擊驗證、不發出 Jsoup 請求、不寫入 DB。**

主要欄位：

| 欄位 | 意義 |
| --- | --- |
| browserState | 瀏覽器目前的判定 |
| currentUrl / title | 初始分頁目前的 URL（移除 query、fragment）與標題 |
| browserHttpStatus | 最近一次主頁面 navigation response 的 HTTP 狀態 |
| hasClearance | 起始 URL 適用的 cookie 中是否有非空 cf_clearance，不回傳值 |
| readySelectorMatched | 有設定 selector 時，是否出現在頁面中；未設定時為 null |
| navigationCount | 本次 session 觀察到的主頁面回應次數，不含子資源與 iframe |
| challengeResponseCount | 上述回應中帶 cf-mitigated: challenge 的次數；不是 CAPTCHA 點擊次數 |
| message / observedAt | 判定說明與最近觀測時間 |
| jsoup | 最近一次 Jsoup 測試結果，與 browserState 分開 |

| browserState | 說明 |
| --- | --- |
| LOADING | 尚無目前頁面的正常回應或內容未就緒 |
| CHALLENGED | 目前是驗證頁，即使有舊 cookie 也不算成功 |
| BLOCKED | HTTP 錯誤或已知封鎖頁 |
| WAITING_FOR_VERIFICATION | 等待 cookie 或指定正常內容 selector |
| WRONG_ORIGIN | 已離開目標網站，不會判定成功 |
| BROWSER_READY | 正常回應、非驗證標題、cookie 與選填 selector 都已符合；仍需測試 Jsoup |
| ERROR | 無法讀取頁面狀態，請檢查視窗或 selector |
| WINDOW_CLOSED / EXPIRED / CANCELLED / SAVED | 視窗已結束；其餘欄位保留最後觀測值 |

如果 BROWSER_READY 後又變回 CHALLENGED，且 challengeResponseCount 增加，就能觀察到驗證反覆出現。
這是診斷資料，不能單憑計數確認 Cloudflare 攔截的具體原因。
未指定正常內容 selector 的判定屬通用啟發式檢查，不能保證任意網站的內容正確。

### 3. 獨立測試 Jsoup

`POST /api/spider/browser-verification/{sessionId}/test-jsoup`

只有 BROWSER_READY 才發出一次對起始 URL 的 Jsoup 請求。回傳 HTTP 200 表示成功取得診斷結果，
**需檢查 body 中的 `jsoup.state`，不能只看 API 的 HTTP 200**：

- NOT_TESTED：尚未測試，或瀏覽器尚未就緒而跳過測試。
- PASSED：Jsoup 取得正常回應與選填 selector，目前可沿用 cookie + User-Agent。
- FAILED：瀏覽器可正常開頁，但 Jsoup 遭攔截、重新導向、缺少 selector 或網路失敗。

`jsoup.httpStatus`、`title`、`message`、`checkedAt` 提供測試結果，不會顯示 cookie。
網路失敗時 httpStatus 為 null。此步驟不關窗、不寫 DB，Jsoup 失敗不會把 browserState 改成 CHALLENGED。
主頁面再次 navigation 時會將先前的 Jsoup 結果重設為 NOT_TESTED，避免沿用舊頁面的結論。

### 4. 確認並儲存

`POST /api/spider/browser-verification/{sessionId}/complete`

```json
{ "refId": "已存在的 spiderItemId", "type": "SPIDER" }
```

分頁改用 `type: "SPIDER_PAGINATION"` 與已存在的 pagination ID。
此 API 再次檢查瀏覽器與 Jsoup；通過才以交易儲存 cookie/header 清單和 mapping，然後關窗。
成功回傳 `cookieId`、`headersId`、`httpStatus`、`title`。既有 spider 流程會讀取原有 mapping，無需 migration。
此時 status 為 SAVED，後續爬取仍需自行啟動，尚未實作自動續跑。

未就緒或 Jsoup 被攔截時回傳 409，網路失敗為 502，不存 DB、保留視窗。
目標不存在回傳 404，也不關窗。瀏覽器已結束時回傳 410，請重新開窗。

### 5. 取消

`DELETE /api/spider/browser-verification/{sessionId}`：關閉視窗，不存 DB，保留網站 profile。
對已結束的同一 session 可重複取消。status 仍能查詢最後狀態，直到下一次開窗取代或後端重啟；之後舊 ID 為 404。

## 資料與部署限制

- 不自動解 CAPTCHA、不修改自動化識別特徵；持久化與觀測並不保證 Cloudflare 接受瀏覽器。
- 本機 Chrome 測試成功不代表已確認 AniDB 人工驗證成功，需要實際操作並觀察 status。
- 遠端伺服器／Docker 的視窗不會出現在你的電腦；遠端畫面串流尚未實作。
- DB 沿用原有明文 cookie_list / headers 與查詢 API，未新增加密或權限模型，僅供目前本機管理用途。
- 每次成功儲存建立獨立清單，保留既有非同名 cookie 與其他 header；同名 cookie 和 User-Agent 取代舊值與 pipeline。
  共享清單不修改，舊清單不自動刪除。
- DB cookie 是起始 URL 的快照，未保存 domain/path/expiry，不自動刷新；只應關聯到相同網站的 spider。
- 本機 profile 與 DB 是不同儲存：即使沒有 complete，瀏覽器本身仍可能把資料寫入專用 profile。

## 安裝與持久化測試

可選的本機 Chrome 測試：使用 headless、暫存專用 profile 和本機攔截回應，不連接 AniDB、不寫 DB。
包含關閉再開啟 Chrome 後 cookie 與 localStorage 的保留測試。

```powershell
$env:PLAYWRIGHT_SMOKE_TEST = 'true'
.\gradlew.bat test --tests '*PlaywrightBrowserSmokeTest'
Remove-Item Env:PLAYWRIGHT_SMOKE_TEST
```

官方參考：[持久化 context](https://playwright.dev/java/docs/api/class-browsertype#browser-type-launch-persistent-context)、
[Java 執行緒與事件派送](https://playwright.dev/java/docs/multithreading)、
[瀏覽器 channel](https://playwright.dev/java/docs/browsers)。
