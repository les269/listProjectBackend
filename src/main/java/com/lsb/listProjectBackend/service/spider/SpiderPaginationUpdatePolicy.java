package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.entity.dynamic.spider.ParamSetting;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.UpdateControl;
import com.lsb.listProjectBackend.utils.DateUtils;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;

@Service
public class SpiderPaginationUpdatePolicy {
    public boolean shouldUpdate(LocalDateTime lastSuccessTime, SpiderPaginationConfig config,
            Map<String, Object> params, LocalDateTime now, boolean force) {
        if (force || config.getUpdateControl() == null || lastSuccessTime == null) {
            return true;
        }
        UpdateControl control = config.getUpdateControl();
        LocalDateTime deadline = null;
        if (control.getLastUpdateMode() == Global.LastUpdateMode.FIXED) {
            deadline = control.getLastUpdateDate();
            if (deadline == null)
                throw new IllegalArgumentException("lastUpdateDate is required");
        } else if (control.getLastUpdateMode() == Global.LastUpdateMode.USE_PARAM) {
            ParamSetting setting = timeParameter(config, control.getLastUpdateParamKey());
            Object value = params.get(setting.getParamKey());
            if (value == null)
                throw new IllegalArgumentException("Missing last-update parameter value");
            TemporalAccessor date = DateTimeFormatter.ofPattern(setting.getTimeFormat()).parse(value.toString());
            int year = date.isSupported(ChronoField.YEAR) ? date.get(ChronoField.YEAR)
                    : date.get(ChronoField.YEAR_OF_ERA);
            deadline = LocalDateTime.of(year, component(date, ChronoField.MONTH_OF_YEAR, 1),
                    component(date, ChronoField.DAY_OF_MONTH, 1), component(date, ChronoField.HOUR_OF_DAY, 0),
                    component(date, ChronoField.MINUTE_OF_HOUR, 0), component(date, ChronoField.SECOND_OF_MINUTE, 0),
                    component(date, ChronoField.NANO_OF_SECOND, 0));
        }
        Integer interval = null;
        Global.TimeUnit unit = null;
        if (control.getUpdateIntervalMode() == Global.UpdateIntervalMode.FIXED) {
            interval = control.getUpdateInterval();
            unit = control.getUpdateIntervalUnit();
        } else if (control.getUpdateIntervalMode() == Global.UpdateIntervalMode.USE_PARAM) {
            ParamSetting setting = timeParameter(config, control.getUpdateIntervalParamKey());
            interval = setting.getTimeInterval();
            unit = setting.getTimeIntervalUnit();
        }
        boolean hasInterval = control.getUpdateIntervalMode() != null
                && control.getUpdateIntervalMode() != Global.UpdateIntervalMode.NONE;
        if (!hasInterval) {
            return deadline == null || !now.isAfter(deadline);
        }
        if (interval == null || interval <= 0 || unit == null) {
            throw new IllegalArgumentException("Update interval must be positive with a time unit");
        }
        if (deadline != null && now.isAfter(deadline))
            return false;
        return !DateUtils.addTime(lastSuccessTime, interval, unit)
                .isAfter(now);
    }

    private ParamSetting timeParameter(SpiderPaginationConfig config, String key) {
        List<ParamSetting> settings = config.getParamSettings() == null ? List.of() : config.getParamSettings();
        return settings.stream().filter(setting -> key != null && key.equals(setting.getParamKey()))
                .filter(setting -> setting.getType() == Global.ParamType.TIME_RANGE).findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Update control requires a TIME_RANGE parameter: " + key));
    }

    private int component(TemporalAccessor value, ChronoField field, int fallback) {
        return value.isSupported(field) ? value.get(field) : fallback;
    }
}
