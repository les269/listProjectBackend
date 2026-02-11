package com.lsb.listProjectBackend.config;

/**
 * 依 Thread 儲存目前的 dynamic 資料庫 key，供 {@link DynamicRoutingDataSource} 使用。
 * <p>
 * <b>併發：</b>雖然欄位是 static，實際存的是 {@link ThreadLocal}，每個 request
 * 對應的 thread 各自一份 key／depth。兩個 API 同時進行不會互相讀到或覆蓋，不會衝突。
 * <p>
 * <b>巢狀清理：</b>Service A ({@code @UseDynamic}) 呼叫 Service B
 * ({@code @UseDynamic})
 * 時，A 為最外層、B 為內層。先進入 A → 再進入 B → B 結束時 depth 減一但不 clear →
 * A 結束時 depth 歸零才 clear。亦即「第一層（最外層）結束才清」。
 */
public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public static void setDataSourceKey(String key) {
        CONTEXT_HOLDER.set(key);
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    /** 進入一層 @UseDynamic；depth 用於巢狀時只在最外層 clear。 */
    public static void incrementDepth() {
        DEPTH.set(DEPTH.get() + 1);
    }

    /** 離開一層 @UseDynamic；若 depth 歸零才 clear key。 */
    public static void decrementDepthAndClearIfOutermost() {
        int d = DEPTH.get() - 1;
        DEPTH.set(d);
        if (d <= 0) {
            DEPTH.remove();
            CONTEXT_HOLDER.remove();
        }
    }

    /** 直接清除（保留給非 AOP 手動 set 的場合）。 */
    public static void clear() {
        DEPTH.remove();
        CONTEXT_HOLDER.remove();
    }
}
