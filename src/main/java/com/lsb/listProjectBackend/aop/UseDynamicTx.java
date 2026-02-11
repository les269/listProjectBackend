package com.lsb.listProjectBackend.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.transaction.annotation.Transactional;

/**
 * 等同 {@link UseDynamic} +
 * {@code @Transactional(transactionManager = "dynamicTransactionManager")}。
 * <p>
 * 用於需要 dynamic 資料庫且要開啟交易的方法。未指定 TM 的 {@code @Transactional} 會綁定
 * {@code localTransactionManager}，dynamic repo 不會加入該交易，可能導致非預期行為。
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@UseDynamic
@Transactional(transactionManager = "dynamicTransactionManager")
public @interface UseDynamicTx {
}
