import com.lsb.listProjectBackend.entity.dynamic.spider.*;
import com.lsb.listProjectBackend.service.spider.SpiderPaginationUpdatePolicy;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpiderPaginationUpdatePolicyTest {
    private final SpiderPaginationUpdatePolicy policy = new SpiderPaginationUpdatePolicy();
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 4, 12, 0);

    @Test
    void intervalBecomesDueAtExactBoundary() {
        var config = config();
        config.getUpdateControl().setUpdateIntervalMode(Global.UpdateIntervalMode.FIXED);
        config.getUpdateControl().setUpdateInterval(1);
        config.getUpdateControl().setUpdateIntervalUnit(Global.TimeUnit.DAY);
        assertFalse(policy.shouldUpdate(now.minusHours(23), config, Map.of(), now, false));
        assertTrue(policy.shouldUpdate(now.minusDays(1), config, Map.of(), now, false));
    }

    @Test
    void expiredDeadlineStillAllowsFirstFetchAndForce() {
        var config = config();
        config.getUpdateControl().setLastUpdateMode(Global.LastUpdateMode.FIXED);
        config.getUpdateControl().setLastUpdateDate(now.minusDays(1));
        assertFalse(policy.shouldUpdate(now.minusDays(2), config, Map.of(), now, false));
        assertTrue(policy.shouldUpdate(null, config, Map.of(), now, false));
        assertTrue(policy.shouldUpdate(now.minusDays(2), config, Map.of(), now, true));
    }

    @Test
    void usesTimeParameterValueForDeadlineAndStepForInterval() {
        var config = config();
        var param = new ParamSetting();
        param.setParamKey("date");
        param.setType(Global.ParamType.TIME_RANGE);
        param.setTimeFormat("yyyy-MM-dd");
        param.setTimeInterval(2);
        param.setTimeIntervalUnit(Global.TimeUnit.DAY);
        config.setParamSettings(List.of(param));
        var control = config.getUpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.USE_PARAM);
        control.setLastUpdateParamKey("date");
        control.setUpdateIntervalMode(Global.UpdateIntervalMode.USE_PARAM);
        control.setUpdateIntervalParamKey("date");
        assertFalse(policy.shouldUpdate(now.minusDays(3), config,
                Map.of("date", "2026-11-03"), now, false));
        assertFalse(policy.shouldUpdate(now.minusDays(1), config,
                Map.of("date", "2026-11-05"), now, false));
        assertTrue(policy.shouldUpdate(now.minusDays(2), config,
                Map.of("date", "2026-11-05"), now, false));
    }

    @Test
    void noneDoesNotRestrictUpdates() {
        assertTrue(policy.shouldUpdate(now, config(), Map.of(), now, false));
    }

    private SpiderPaginationConfig config() {
        var config = new SpiderPaginationConfig();
        var control = new UpdateControl();
        control.setLastUpdateMode(Global.LastUpdateMode.NONE);
        control.setUpdateIntervalMode(Global.UpdateIntervalMode.NONE);
        config.setUpdateControl(control);
        return config;
    }

}
