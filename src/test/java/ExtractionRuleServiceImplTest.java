import com.jayway.jsonpath.JsonPath;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionCondition;
import com.lsb.listProjectBackend.entity.dynamic.spider.ExtractionRule;
import com.lsb.listProjectBackend.entity.dynamic.spider.ValuePipeline;
import com.lsb.listProjectBackend.service.spider.ExtractionRuleServiceImpl;
import com.lsb.listProjectBackend.utils.Global;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExtractionRuleServiceImplTest {

    @Test
    public void sortedRules_shouldSortBySeqAndPutNullSeqAtEnd() {
        ExtractionRuleServiceImpl service = new ExtractionRuleServiceImpl();

        ExtractionRule seq3 = new ExtractionRule();
        seq3.setSeq(3);
        seq3.setKey("c");

        ExtractionRule seq1 = new ExtractionRule();
        seq1.setSeq(1);
        seq1.setKey("a");

        ExtractionRule seqNull = new ExtractionRule();
        seqNull.setSeq(null);
        seqNull.setKey("z");

        List<ExtractionRule> result = service.sortedRules(List.of(seq3, seqNull, seq1));

        assertEquals("a", result.get(0).getKey());
        assertEquals("c", result.get(1).getKey());
        assertEquals("z", result.get(2).getKey());
    }

    @Test
    public void allPipelines_shouldFlattenRulesPipelines() {
        ExtractionRuleServiceImpl service = new ExtractionRuleServiceImpl();

        ValuePipeline p1 = new ValuePipeline();
        p1.setSeq(1);
        ValuePipeline p2 = new ValuePipeline();
        p2.setSeq(2);

        ExtractionRule r1 = new ExtractionRule();
        r1.setPipelines(List.of(p1));
        ExtractionRule r2 = new ExtractionRule();
        r2.setPipelines(List.of(p2));

        List<ValuePipeline> result = service.allPipelines(List.of(r1, r2));

        assertIterableEquals(List.of(p1, p2), result);
    }

    @Test
    public void checkCondition_shouldEvaluateContainsAndNotContains() {
        ExtractionRuleServiceImpl service = new ExtractionRuleServiceImpl();
        var result = JsonPath.parse("{\"name\":\"Tom\",\"tags\":[\"a\",\"b\"]}");

        ExtractionCondition containsCondition = new ExtractionCondition();
        containsCondition.setKey("$.name");
        containsCondition.setValue("to");
        containsCondition.setIgnoreCase(true);

        ExtractionRule containsRule = new ExtractionRule();
        containsRule.setConditionType(Global.ExtractionStepCondition.CONTAINS);
        containsRule.setConditionValue(containsCondition);

        assertTrue(service.checkCondition(containsRule, result));

        ExtractionCondition notContainsCondition = new ExtractionCondition();
        notContainsCondition.setKey("$.tags");
        notContainsCondition.setValue("z");

        ExtractionRule notContainsRule = new ExtractionRule();
        notContainsRule.setConditionType(Global.ExtractionStepCondition.NOT_CONTAINS);
        notContainsRule.setConditionValue(notContainsCondition);

        assertTrue(service.checkCondition(notContainsRule, result));
        notContainsCondition.setValue("a");
        assertFalse(service.checkCondition(notContainsRule, result));
    }
}

