package com.lsb.listProjectBackend.controller.spider;

import com.lsb.listProjectBackend.domain.spider.BrowserVerificationTO.*;
import com.lsb.listProjectBackend.service.spider.BrowserVerificationService;
import com.lsb.listProjectBackend.service.spider.BrowserVerificationStorageService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/spider/browser-verification")
@CrossOrigin(origins = "${spider.browser-verification.frontend-origin:http://localhost:5173}")
@RequiredArgsConstructor
@Tag(name = "Browser verification", description = "持久化 profile 人工驗證：開窗 → 查詢瀏覽器狀態 → 測試 Jsoup → 儲存。視窗開在後端電腦。")
public class BrowserVerificationController {
    private final BrowserVerificationService browser;
    private final BrowserVerificationStorageService storage;

    @PostMapping
    @Operation(summary = "1. 開啟驗證瀏覽器", description = "需先設定 SPIDER_BROWSER_VERIFICATION_ENABLED=true。"
            + "在後端電腦開啟 Chrome 並沿用該網站專用 profile。請手動完成驗證，再查詢狀態；不會驗證後自動存 DB 或關窗。"
            + "readySelector 可選填正常頁面必須存在的 CSS selector。")
    @ApiResponse(responseCode = "200", description = "視窗已開啟，回傳 sessionId 與到期時間")
    @ApiResponse(responseCode = "400", description = "URL 格式不符或網域不在 allowed-hosts")
    @ApiResponse(responseCode = "409", description = "已有驗證視窗開啟")
    @ApiResponse(responseCode = "502", description = "無法開啟瀏覽器或載入頁面")
    @ApiResponse(responseCode = "503", description = "尚未啟用瀏覽器驗證")
    public Session start(@RequestBody StartRequest request) {
        return browser.start(request.url(), request.readySelector());
    }

    @GetMapping("/{sessionId}/status")
    @Operation(summary = "2. 查詢瀏覽器驗證狀態", description = "可重複查詢。不重新整理 Chrome、不發出 Jsoup 請求，"
            + "也不儲存 DB。BROWSER_READY 代表瀏覽器初步判定正常，仍需獨立測試 Jsoup。"
            + "challengeResponseCount 記錄帶 challenge 標頭的主頁面回應次數；可用來觀察驗證循環。")
    @ApiResponse(responseCode = "200", description = "目前狀態與最近一次 Jsoup 測試結果；不包含 cookie 值")
    @ApiResponse(responseCode = "404", description = "找不到 session（新 session 開始後會取代上一個，重啟後端也會清除記錄）")
    public Status status(@Parameter(description = "第一步回傳的 sessionId", required = true)
            @PathVariable String sessionId) {
        return browser.status(sessionId);
    }

    @PostMapping("/{sessionId}/test-jsoup")
    @Operation(summary = "3. 獨立測試 Jsoup", description = "只有瀏覽器狀態為 BROWSER_READY 才會送出一次 Jsoup 請求。"
            + "回傳狀態中的 jsoup.state 為 PASSED、FAILED 或 NOT_TESTED；不存 DB、不關窗、不重新導向 Chrome。"
            + "瀏覽器成功但 Jsoup 失敗時，可在這裡分別看到兩種結果。")
    @ApiResponse(responseCode = "200", description = "測試結果，請檢查 browserState 與 jsoup.state，200 不代表測試一定成功")
    @ApiResponse(responseCode = "404", description = "找不到 session")
    @ApiResponse(responseCode = "410", description = "Session 已結束，請重新開始")
    public Status testJsoup(@Parameter(description = "第一步回傳的 sessionId", required = true)
            @PathVariable String sessionId) {
        return browser.testJsoup(sessionId);
    }

    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "4. 確認驗證並儲存", description = "瀏覽器為 BROWSER_READY 後執行。再次以 Jsoup 測試 cookie 與 User-Agent，"
            + "成功才存入目前選取的 dynamic DB 並關聯至指定 spider，接著關閉視窗。refId 必須是已存在的目標 ID。")
    @ApiResponse(responseCode = "200", description = "已儲存並關聯，回傳清單 ID 與頁面測試結果，不回傳 cookie")
    @ApiResponse(responseCode = "400", description = "缺少 refId 或 type")
    @ApiResponse(responseCode = "404", description = "找不到 session 或指定 spider")
    @ApiResponse(responseCode = "409", description = "尚未完成驗證或 Jsoup 仍被攔截，未儲存，可繼續驗證後重試")
    @ApiResponse(responseCode = "410", description = "Session 已到期或視窗已關閉，請重新開始")
    @ApiResponse(responseCode = "502", description = "驗證請求失敗，未儲存")
    public Saved complete(@Parameter(description = "第一步回傳的 sessionId", required = true)
            @PathVariable String sessionId, @RequestBody CompleteRequest request) {
        var verified = browser.verify(sessionId);
        var saved = storage.save(request, verified);
        browser.release(sessionId);
        return saved;
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "5. 取消驗證", description = "不需儲存 DB 時執行，關閉驗證瀏覽器並釋放資源。"
            + "網站專用 profile 仍保留在本機，下一次可沿用。")
    @ApiResponse(responseCode = "200", description = "已取消")
    @ApiResponse(responseCode = "404", description = "找不到 session")
    public void cancel(@Parameter(description = "第一步回傳的 sessionId", required = true)
            @PathVariable String sessionId) {
        browser.cancel(sessionId);
    }
}
