package com.lsb.listProjectBackend.config.datasource;

/**
 * 以 ThreadLocal 儲存「目前 thread 要連哪個 dynamic 資料庫」的 key。
 *
 * 為什麼用 ThreadLocal：
 * Web 應用每個 HTTP Request 由獨立的 thread 處理。ThreadLocal 讓每個 thread 各自持有
 * 一份 key，互不影響。即使 100 個請求同時進來，A 設定 key="db1"、B 設定 key="db2"，
 * 彼此看到的值完全獨立，不會互相覆蓋。
 *
 * 為什麼需要 depth（巢狀深度）：
 * 若 Service A (@UseDynamic) 呼叫 Service B (@UseDynamic)，AOP 會攔截兩次：
 * 進入 A → depth: 0→1，設定 key
 * 進入 B → depth: 1→2，設定 key（同一個 key，無影響）
 * 離開 B → depth: 2→1，depth > 0，不 clear key（A 還在用！）
 * 離開 A → depth: 1→0，depth = 0，clear key
 * 若 B 離開時就 clear，則 A 後續的 JPA 操作找不到 key，會 fallback 到 default DB，
 * 導致查詢錯誤的資料庫。depth 計數正是為了防止這個問題。
 */
public class DynamicDataSourceContextHolder {

    /** 儲存目前 thread 的 DB routing key。null 表示使用 default DataSource。 */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /** 記錄目前 thread 進入 @UseDynamic 方法的巢狀層數，用來判斷是否為最外層。 */
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    /**
     * 設定目前 thread 要使用的 DB key。
     * 由 {@link DynamicDatabaseAspect} 在方法執行前呼叫。
     *
     * @param key 對應 {@code database_config.config_name} 的字串
     */
    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }

    /**
     * 讀取目前 thread 的 DB key。
     * 由 {@link DynamicRoutingDataSource#determineCurrentLookupKey()} 呼叫，
     * 用來決定這次 JPA 操作要用哪個 DataSource。
     *
     * @return DB key 字串；若尚未設定則回傳 null（Spring 會使用 defaultTargetDataSource）
     */
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 進入一層 {@code @UseDynamic} 方法，depth 加一。
     * 必須在 {@link #setDataSourceKey} 之後呼叫。
     */
    public static void incrementDepth() {
        DEPTH.set(DEPTH.get() + 1);
    }

    /**
     * 離開一層 {@code @UseDynamic} 方法，depth 減一。
     * 只有當 depth 降回 0（最外層結束）時，才真正清除 ThreadLocal，
     * 避免巢狀情境下內層提早清除導致外層找不到 key。
     */
    public static void decrementDepthAndClearIfOutermost() {
        int d = DEPTH.get() - 1;
        DEPTH.set(d);
        if (d <= 0) {
            DEPTH.remove();
            CONTEXT_HOLDER.remove();
        }
    }

    /**
     * 強制清除目前 thread 的 key 與 depth（不論層數）。
     * 保留給非 AOP 場合手動呼叫，例如非同步任務、測試環境清理等。
     */
    public static void clear() {
        DEPTH.remove();
        CONTEXT_HOLDER.remove();
    }
}
