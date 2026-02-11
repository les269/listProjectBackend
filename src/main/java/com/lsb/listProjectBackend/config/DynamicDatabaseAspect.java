package com.lsb.listProjectBackend.config;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.lsb.listProjectBackend.entity.local.Setting;
import com.lsb.listProjectBackend.repository.local.SettingRepository;
import com.lsb.listProjectBackend.utils.Global;

/**
 * 透過 AOP 在方法呼叫前後切換 dynamic 資料庫。
 *
 * 使用方式：
 * 
 * <pre>
 *  &#64;UseDynamic
 *  public void someServiceMethod() { ... }
 * </pre>
 *
 * 實際要使用哪一個 DB key，由 local DB 的 setting 決定：
 * name = "current_dynamic_database", value = 對應的 database_config.config_name
 */
@Aspect
@Component
public class DynamicDatabaseAspect {

    private static final Logger log = LoggerFactory.getLogger(DynamicDatabaseAspect.class);

    private final SettingRepository settingRepository;

    public DynamicDatabaseAspect(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Around("@within(com.lsb.listProjectBackend.aop.UseDynamic) || @annotation(com.lsb.listProjectBackend.aop.UseDynamic) || @annotation(com.lsb.listProjectBackend.aop.UseDynamicTx)")
    public Object switchDatabase(ProceedingJoinPoint pjp) throws Throwable {
        String dbKey = resolveCurrentDatabaseKey();
        DynamicDataSourceContextHolder.setDataSourceKey(dbKey);
        DynamicDataSourceContextHolder.incrementDepth();
        log.debug("Switched dynamic datasource to key: {}", dbKey);
        try {
            return pjp.proceed();
        } finally {
            DynamicDataSourceContextHolder.decrementDepthAndClearIfOutermost();
            log.debug("Cleared dynamic datasource key (if outermost)");
        }
    }

    /**
     * 從 local.setting 讀取目前選擇的 dynamic DB key。
     * 若無設定或值為空，則回退到預設 key。
     */
    private String resolveCurrentDatabaseKey() {
        try {
            Optional<Setting> settingOpt = settingRepository.findById(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
            if (settingOpt.isPresent()) {
                String value = settingOpt.get().getValue();
                if (value != null && !value.isBlank()) {
                    return value;
                }
            } else {
                Setting setting = new Setting();
                setting.setName(Global.CURRENT_DYNAMIC_DB_SETTING_NAME);
                setting.setValue(Global.DEFAULT_DYNAMIC_DB_KEY);
                setting.setEnabled(true);
                settingRepository.save(setting);
            }
        } catch (Exception e) {
            log.warn("Failed to resolve current dynamic database from setting, fallback to default", e);
        }
        return Global.DEFAULT_DYNAMIC_DB_KEY;
    }
}
