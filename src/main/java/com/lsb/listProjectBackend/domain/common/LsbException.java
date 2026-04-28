package com.lsb.listProjectBackend.domain.common;

import org.springframework.http.HttpStatus;

/**
 * 應用程式統一業務例外。
 * <p>
 * 使用方式：
 * 
 * <pre>
 * // 400 Bad Request（預設）
 * throw new LsbException("訊息");
 *
 * // 指定 HTTP 狀態碼
 * throw new LsbException("找不到資源", HttpStatus.NOT_FOUND);
 *
 * // 包裝底層例外（保留 stack trace）
 * throw new LsbException("描述", e);
 * </pre>
 */
public class LsbException extends RuntimeException {

    private final HttpStatus status;

    /** 預設 400 Bad Request。 */
    public LsbException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /** 指定 HTTP 狀態碼。 */
    public LsbException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /** 包裝底層例外，預設 400 Bad Request。 */
    public LsbException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /** 包裝底層例外並指定 HTTP 狀態碼。 */
    public LsbException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
