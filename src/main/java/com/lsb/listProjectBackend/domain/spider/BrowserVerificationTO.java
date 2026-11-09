package com.lsb.listProjectBackend.domain.spider;

import java.time.Instant;
import io.swagger.v3.oas.annotations.media.Schema;

public final class BrowserVerificationTO {
    private BrowserVerificationTO() { }

    public enum TargetType { SPIDER, SPIDER_PAGINATION }

    public enum BrowserState {
        LOADING, WAITING_FOR_VERIFICATION, CHALLENGED, BLOCKED, WRONG_ORIGIN,
        BROWSER_READY, WINDOW_CLOSED, EXPIRED, CANCELLED, SAVED, ERROR
    }

    public enum JsoupState { NOT_TESTED, PASSED, FAILED }

    @Schema(name = "BrowserVerificationStartRequest", example = "{\"url\":\"https://anidb.net/\"}")
    public record StartRequest(
            @Schema(description = "待驗證 URL，需為 allowed-hosts 中的 HTTPS 網址", example = "https://anidb.net/",
                    requiredMode = Schema.RequiredMode.REQUIRED) String url,
            @Schema(description = "選填：正常頁面必須出現的 CSS selector，避免只憑 cookie 判定成功。"
                    + "需使用該網站實際存在的 selector，留空則使用回應狀態、標題和 cookie 初步判定。") String readySelector) { }

    @Schema(name = "BrowserVerificationSession")
    public record Session(String sessionId, String url, Instant expiresAt) { }

    @Schema(name = "BrowserVerificationJsoupCheck")
    public record JsoupCheck(JsoupState state, Integer httpStatus, String title, String message, Instant checkedAt) { }

    @Schema(name = "BrowserVerificationStatus")
    public record Status(String sessionId, BrowserState browserState, String currentUrl, String title,
                         Integer browserHttpStatus, boolean hasClearance, Boolean readySelectorMatched,
                         int navigationCount, int challengeResponseCount, String message,
                         Instant observedAt, Instant expiresAt, JsoupCheck jsoup) { }

    /** SPIDER refId is a spiderItemId; SPIDER_PAGINATION refId is a pagination ID. */
    @Schema(name = "BrowserVerificationCompleteRequest")
    public record CompleteRequest(
            @Schema(description = "已存在的 spiderItemId（SPIDER）或 pagination ID（SPIDER_PAGINATION），請替換範例值",
                    example = "your-spider-item-id", requiredMode = Schema.RequiredMode.REQUIRED) String refId,
            @Schema(description = "指定要關聯的目標類型", example = "SPIDER",
                    requiredMode = Schema.RequiredMode.REQUIRED) TargetType type) { }

    @Schema(name = "BrowserVerificationSaved")
    public record Saved(String cookieId, String headersId, int httpStatus, String title) { }
}
