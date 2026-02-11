package com.lsb.listProjectBackend.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 標註在 service / controller 方法或類別上，表示該方法／類別會使用 dynamic 資料庫。
 * <p>
 * 實際要使用哪一個資料庫，由 AOP 從 local DB 的設定
 * （例如 setting.current_dynamic_database）決定，而不是寫死在註解上。
 * <p>
 * 若有寫入且需交易，請改用 {@link UseDynamicTx}，否則 dynamic 操作不會加入交易。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseDynamic {
}
