package com.lsb.listProjectBackend.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExperessionUtilsTest {

    // ---------- 基本加減乘除 ----------

    @Test
    void arithmetic_add() {
        Object result = ExperessionUtils.calculateExpression("1 + 2", null);
        assertEquals(3, result);
    }

    @Test
    void arithmetic_subtract() {
        Object result = ExperessionUtils.calculateExpression("10 - 3", null);
        assertEquals(7, result);
    }

    @Test
    void arithmetic_multiply() {
        Object result = ExperessionUtils.calculateExpression("4 * 5", null);
        assertEquals(20, result);
    }

    @Test
    void arithmetic_divide() {
        Object result = ExperessionUtils.calculateExpression("10 / 4", null);
        assertEquals(2, result);
    }

    @Test
    void arithmetic_modulo() {
        Object result = ExperessionUtils.calculateExpression("10 % 3", null);
        assertEquals(1, result);
    }

    @Test
    void arithmetic_withVariables() {
        var vars = Map.of("a", (Object) new BigDecimal("10"), "b", (Object) new BigDecimal("3"));
        Object result = ExperessionUtils.calculateExpression("#a + #b", vars);
        assertEquals(new BigDecimal("13"), result);
    }

    @Test
    void arithmetic_decimal() {
        var vars = Map.of("price", (Object) new BigDecimal("19.99"), "qty", (Object) new BigDecimal("3"));
        Object result = ExperessionUtils.calculateExpression("#price * #qty", vars);
        assertEquals(new BigDecimal("59.97"), result);
    }

    @Test
    void arithmetic_ternary() {
        var vars = Map.of("x", (Object) new BigDecimal("5"));
        Object result = ExperessionUtils.calculateExpression("#x > 3 ? 1 : 0", vars);
        assertEquals(1, result);
    }

    // ---------- SafeFunctions ----------

    @Test
    void fn_abs_negative() {
        var vars = Map.of("v", (Object) new BigDecimal("-7.5"));
        Object result = ExperessionUtils.calculateExpression("#abs(#v)", vars);
        assertEquals(new BigDecimal("7.5"), result);
    }

    @Test
    void fn_max() {
        var vars = Map.of("a", (Object) new BigDecimal("3"), "b", (Object) new BigDecimal("8"));
        Object result = ExperessionUtils.calculateExpression("#max(#a, #b)", vars);
        assertEquals(new BigDecimal("8"), result);
    }

    @Test
    void fn_min() {
        var vars = Map.of("a", (Object) new BigDecimal("3"), "b", (Object) new BigDecimal("8"));
        Object result = ExperessionUtils.calculateExpression("#min(#a, #b)", vars);
        assertEquals(new BigDecimal("3"), result);
    }

    @Test
    void fn_clamp_within() {
        var vars = Map.of("v", (Object) new BigDecimal("5"));
        Object result = ExperessionUtils.calculateExpression("#clamp(#v, 0, 10)", vars);
        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    void fn_clamp_below_min() {
        var vars = Map.of("v", (Object) new BigDecimal("-3"));
        Object result = ExperessionUtils.calculateExpression("#clamp(#v, 0, 10)", vars);
        assertEquals(new BigDecimal("0"), result);
    }

    @Test
    void fn_clamp_above_max() {
        var vars = Map.of("v", (Object) new BigDecimal("15"));
        Object result = ExperessionUtils.calculateExpression("#clamp(#v, 0, 10)", vars);
        assertEquals(new BigDecimal("10"), result);
    }

    @Test
    void fn_floor() {
        var vars = Map.of("v", (Object) new BigDecimal("3.9"));
        Object result = ExperessionUtils.calculateExpression("#floor(#v)", vars);
        assertEquals(new BigDecimal("3"), result);
    }

    @Test
    void fn_ceil() {
        var vars = Map.of("v", (Object) new BigDecimal("3.1"));
        Object result = ExperessionUtils.calculateExpression("#ceil(#v)", vars);
        assertEquals(new BigDecimal("4"), result);
    }

    @Test
    void fn_round_up() {
        var vars = Map.of("v", (Object) new BigDecimal("3.5"));
        Object result = ExperessionUtils.calculateExpression("#round(#v)", vars);
        assertEquals(new BigDecimal("4"), result);
    }

    @Test
    void fn_round_down() {
        var vars = Map.of("v", (Object) new BigDecimal("3.4"));
        Object result = ExperessionUtils.calculateExpression("#round(#v)", vars);
        assertEquals(new BigDecimal("3"), result);
    }

    @Test
    void fn_roundScale() {
        var vars = Map.of("v", (Object) new BigDecimal("3.14159"));
        Object result = ExperessionUtils.calculateExpression("#roundScale(#v, 2)", vars);
        assertEquals(new BigDecimal("3.14"), result);
    }

    @Test
    void fn_pow() {
        var vars = Map.of("b", (Object) new BigDecimal("2"));
        Object result = ExperessionUtils.calculateExpression("#pow(#b, 10)", vars);
        assertEquals(new BigDecimal("1024"), result);
    }

    @Test
    void fn_sqrt() {
        var vars = Map.of("v", (Object) new BigDecimal("9"));
        Object result = ExperessionUtils.calculateExpression("#sqrt(#v)", vars);
        assertEquals(0, ((BigDecimal) result).compareTo(new BigDecimal("3")));
    }

    @Test
    void fn_sum_list() {
        var vars = Map.of("list", (Object) List.of(
                new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        Object result = ExperessionUtils.calculateExpression("#sum(#list)", vars);
        assertEquals(new BigDecimal("6"), result);
    }

    @Test
    void fn_avg_list() {
        var vars = Map.of("list", (Object) List.of(
                new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        BigDecimal result = (BigDecimal) ExperessionUtils.calculateExpression("#avg(#list)", vars);
        assertEquals(0, result.compareTo(new BigDecimal("2")));
    }

    @Test
    void fn_count_list() {
        var vars = Map.of("list", (Object) List.of(
                new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30")));
        Object result = ExperessionUtils.calculateExpression("#count(#list)", vars);
        assertEquals(new BigDecimal("3"), result);
    }

    @Test
    void fn_ifNull_null_value() {
        var vars = Map.of("defaultVal", (Object) new BigDecimal("99"));
        // null 無法放入 Map.of，改用 HashMap
        var vars2 = new java.util.HashMap<String, Object>();
        vars2.put("v", null);
        vars2.put("d", new BigDecimal("99"));
        Object result = ExperessionUtils.calculateExpression("#ifNull(#v, #d)", vars2);
        assertEquals(new BigDecimal("99"), result);
    }

    @Test
    void fn_ifNull_non_null_value() {
        var vars = Map.of("v", (Object) new BigDecimal("5"), "d", (Object) new BigDecimal("99"));
        Object result = ExperessionUtils.calculateExpression("#ifNull(#v, #d)", vars);
        assertEquals(new BigDecimal("5"), result);
    }

    @Test
    void fn_isNull_true() {
        var vars = new java.util.HashMap<String, Object>();
        vars.put("v", null);
        Object result = ExperessionUtils.calculateExpression("#isNull(#v)", vars);
        assertEquals(true, result);
    }

    @Test
    void fn_notNull_true() {
        var vars = Map.of("v", (Object) new BigDecimal("1"));
        Object result = ExperessionUtils.calculateExpression("#notNull(#v)", vars);
        assertEquals(true, result);
    }

    // ---------- 安全性 ----------

    @Test
    void security_blank_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("", null));
    }

    @Test
    void security_tooLong_throws() {
        String longExpr = "1 + ".repeat(130); // > 500 chars
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression(longExpr, null));
    }

    @Test
    void security_typeReference_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("T(java.lang.Math).random()", null));
    }

    @Test
    void security_newInstance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("new java.util.ArrayList()", null));
    }

    @Test
    void security_methodCall_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("#v.getClass()", Map.of("v", (Object) "x")));
    }

    @Test
    void security_stringLiteral_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("'hello'", null));
    }

    @Test
    void security_arrayAccess_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("#v[0]", Map.of("v", (Object) List.of(1))));
    }

    @Test
    void security_systemExit_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> ExperessionUtils.calculateExpression("T(System).exit(0)", null));
    }

    // ---------- div / mod / percent ----------

    @Test
    void fn_div_exact() {
        var vars = Map.of("a", (Object) new BigDecimal("10"), "b", (Object) new BigDecimal("4"));
        BigDecimal result = (BigDecimal) ExperessionUtils.calculateExpression("#divide(#a, #b)", vars);
        assertEquals(0, result.compareTo(new BigDecimal("2.5")));
    }

    @Test
    void fn_div_decimal() {
        var vars = Map.of("a", (Object) new BigDecimal("1"), "b", (Object) new BigDecimal("3"));
        BigDecimal result = (BigDecimal) ExperessionUtils.calculateExpression("#divide(#a, #b)", vars);
        assertTrue(result.compareTo(new BigDecimal("0.33333")) >= 0
                && result.compareTo(new BigDecimal("0.33334")) < 0);
    }

    @Test
    void fn_mod() {
        var vars = Map.of("a", (Object) new BigDecimal("10"), "b", (Object) new BigDecimal("3"));
        Object result = ExperessionUtils.calculateExpression("#modulo(#a, #b)", vars);
        assertEquals(new BigDecimal("1"), result);
    }

    @Test
    void fn_percent_normal() {
        var vars = Map.of("part", (Object) new BigDecimal("25"), "total", (Object) new BigDecimal("200"));
        BigDecimal result = (BigDecimal) ExperessionUtils.calculateExpression("#percent(#part, #total)", vars);
        assertEquals(0, result.compareTo(new BigDecimal("12.5")));
    }

    @Test
    void fn_percent_zero_total_returns_zero() {
        var vars = Map.of("part", (Object) new BigDecimal("10"), "total", (Object) new BigDecimal("0"));
        Object result = ExperessionUtils.calculateExpression("#percent(#part, #total)", vars);
        assertEquals(new BigDecimal("0"), result);
    }

    // ---------- between ----------

    @Test
    void fn_between_inside() {
        var vars = Map.of("v", (Object) new BigDecimal("5"));
        Object result = ExperessionUtils.calculateExpression("#between(#v, 0, 10)", vars);
        assertEquals(true, result);
    }

    @Test
    void fn_between_on_boundary() {
        var vars = Map.of("v", (Object) new BigDecimal("0"));
        Object result = ExperessionUtils.calculateExpression("#between(#v, 0, 10)", vars);
        assertEquals(true, result);
    }

    @Test
    void fn_between_outside() {
        var vars = Map.of("v", (Object) new BigDecimal("11"));
        Object result = ExperessionUtils.calculateExpression("#between(#v, 0, 10)", vars);
        assertEquals(false, result);
    }

    // ---------- negate ----------

    @Test
    void fn_negate_positive() {
        var vars = Map.of("v", (Object) new BigDecimal("7"));
        Object result = ExperessionUtils.calculateExpression("#negate(#v)", vars);
        assertEquals(new BigDecimal("-7"), result);
    }

    @Test
    void fn_negate_negative() {
        var vars = Map.of("v", (Object) new BigDecimal("-3.5"));
        Object result = ExperessionUtils.calculateExpression("#negate(#v)", vars);
        assertEquals(new BigDecimal("3.5"), result);
    }

    // ---------- coalesce ----------

    @Test
    void fn_coalesce_first_non_null() {
        var vars = new java.util.HashMap<String, Object>();
        vars.put("a", null);
        vars.put("b", new BigDecimal("42"));
        vars.put("c", new BigDecimal("99"));
        Object result = ExperessionUtils.calculateExpression("#coalesce(#a, #b, #c)", vars);
        assertEquals(new BigDecimal("42"), result);
    }

    @Test
    void fn_coalesce_all_null_returns_zero() {
        var vars = new java.util.HashMap<String, Object>();
        vars.put("a", null);
        vars.put("b", null);
        Object result = ExperessionUtils.calculateExpression("#coalesce(#a, #b)", vars);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void fn_coalesce_single_non_null() {
        var vars = Map.of("v", (Object) new BigDecimal("5"));
        Object result = ExperessionUtils.calculateExpression("#coalesce(#v)", vars);
        assertEquals(new BigDecimal("5"), result);
    }

    // ---------- isEq / isGt / isGte / isLt / isLte ----------

    @Test
    void fn_isEq_true() {
        var vars = Map.of("a", (Object) new BigDecimal("5.0"), "b", (Object) new BigDecimal("5"));
        assertEquals(true, ExperessionUtils.calculateExpression("#isEq(#a, #b)", vars));
    }

    @Test
    void fn_isEq_false() {
        var vars = Map.of("a", (Object) new BigDecimal("5"), "b", (Object) new BigDecimal("6"));
        assertEquals(false, ExperessionUtils.calculateExpression("#isEq(#a, #b)", vars));
    }

    @Test
    void fn_isGt_true() {
        var vars = Map.of("a", (Object) new BigDecimal("7"), "b", (Object) new BigDecimal("3"));
        assertEquals(true, ExperessionUtils.calculateExpression("#isGt(#a, #b)", vars));
    }

    @Test
    void fn_isGt_false_equal() {
        var vars = Map.of("a", (Object) new BigDecimal("5"), "b", (Object) new BigDecimal("5"));
        assertEquals(false, ExperessionUtils.calculateExpression("#isGt(#a, #b)", vars));
    }

    @Test
    void fn_isGte_true_equal() {
        var vars = Map.of("a", (Object) new BigDecimal("5"), "b", (Object) new BigDecimal("5"));
        assertEquals(true, ExperessionUtils.calculateExpression("#isGte(#a, #b)", vars));
    }

    @Test
    void fn_isGte_false() {
        var vars = Map.of("a", (Object) new BigDecimal("3"), "b", (Object) new BigDecimal("5"));
        assertEquals(false, ExperessionUtils.calculateExpression("#isGte(#a, #b)", vars));
    }

    @Test
    void fn_isLt_true() {
        var vars = Map.of("a", (Object) new BigDecimal("2"), "b", (Object) new BigDecimal("9"));
        assertEquals(true, ExperessionUtils.calculateExpression("#isLt(#a, #b)", vars));
    }

    @Test
    void fn_isLt_false_equal() {
        var vars = Map.of("a", (Object) new BigDecimal("5"), "b", (Object) new BigDecimal("5"));
        assertEquals(false, ExperessionUtils.calculateExpression("#isLt(#a, #b)", vars));
    }

    @Test
    void fn_isLte_true_equal() {
        var vars = Map.of("a", (Object) new BigDecimal("5"), "b", (Object) new BigDecimal("5"));
        assertEquals(true, ExperessionUtils.calculateExpression("#isLte(#a, #b)", vars));
    }

    @Test
    void fn_isLte_false() {
        var vars = Map.of("a", (Object) new BigDecimal("8"), "b", (Object) new BigDecimal("5"));
        assertEquals(false, ExperessionUtils.calculateExpression("#isLte(#a, #b)", vars));
    }
}
