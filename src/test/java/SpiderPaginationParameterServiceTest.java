import com.lsb.listProjectBackend.entity.dynamic.spider.EndTimeConfig;
import com.lsb.listProjectBackend.entity.dynamic.spider.ParamSetting;
import com.lsb.listProjectBackend.service.spider.SpiderPaginationParameterService;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpiderPaginationParameterServiceTest {
    private final SpiderPaginationParameterService service = new SpiderPaginationParameterService();
    private final LocalDateTime now = LocalDateTime.of(2026, 11, 4, 12, 0);

    @Test
    void noParametersExecutesOnce() {
        assertEquals(List.of(Map.of()), service.expand(null, now));
    }

    @Test
    void expandsCartesianProductInSequenceOrderWithPadding() {
        ParamSetting region = new ParamSetting();
        region.setSeq(1);
        region.setParamKey("region");
        region.setType(Global.ParamType.STATIC_LIST);
        region.setStaticList(List.of("tw", "jp"));
        ParamSetting number = number("001", "005", 2);
        number.setSeq(2);
        number.setPadZeros(true);
        assertEquals(List.of(
                Map.of("region", "tw", "number", "001"),
                Map.of("region", "tw", "number", "003"),
                Map.of("region", "tw", "number", "005"),
                Map.of("region", "jp", "number", "001"),
                Map.of("region", "jp", "number", "003"),
                Map.of("region", "jp", "number", "005")),
                service.expand(List.of(number, region), now));
    }

    @Test
    void supportsLargeNumbersWithoutOverflowAndRejectsZeroStep() {
        var setting = number("99999999999999999999", "100000000000000000000", 1);
        assertEquals(2, service.expand(List.of(setting), now).size());
        setting.setNumStep(0);
        assertThrows(IllegalArgumentException.class, () -> service.expand(List.of(setting), now));
    }

    @Test
    void expandsTimeToRelativeEndInclusively() {
        ParamSetting time = new ParamSetting();
        time.setParamKey("date");
        time.setType(Global.ParamType.TIME_RANGE);
        time.setStartTime(now.minusDays(3));
        time.setTimeFormat("yyyy-MM-dd");
        time.setTimeInterval(1);
        time.setTimeIntervalUnit(Global.TimeUnit.DAY);
        EndTimeConfig end = new EndTimeConfig();
        end.setEndTimeType(Global.EndTimeType.RELATIVE_OFFSET);
        end.setOffsetInterval(-1);
        end.setOffsetUnit(Global.TimeUnit.DAY);
        time.setEndTimeConfig(end);
        assertEquals(List.of("2026-11-01", "2026-11-02", "2026-11-03"),
                service.expand(List.of(time), now).stream().map(value -> value.get("date")).toList());
    }

    @Test
    void rejectsDuplicateKeysAndDoesNotExpandEmptyRanges() {
        ParamSetting setting = number("5", "1", 1);
        assertEquals(0, service.expand(List.of(setting), now).size());
        assertThrows(IllegalArgumentException.class, () -> service.expand(List.of(setting, setting), now));
    }

    private ParamSetting number(String start, String end, int step) {
        ParamSetting setting = new ParamSetting();
        setting.setParamKey("number");
        setting.setType(Global.ParamType.NUMBER_RANGE);
        setting.setStartNumStr(start);
        setting.setEndNumStr(end);
        setting.setNumStep(step);
        return setting;
    }
}
