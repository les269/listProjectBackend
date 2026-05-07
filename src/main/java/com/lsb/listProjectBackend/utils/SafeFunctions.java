package com.lsb.listProjectBackend.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class SafeFunctions {

    // ---------- 基本 ----------

    public static BigDecimal abs(Object value) {
        return toBigDecimal(value).abs();
    }

    public static BigDecimal max(Object a, Object b) {
        return toBigDecimal(a).max(toBigDecimal(b));
    }

    public static BigDecimal min(Object a, Object b) {
        return toBigDecimal(a).min(toBigDecimal(b));
    }

    /** 限制 value 在 [min, max] 區間 */
    public static BigDecimal clamp(Object value, Object min, Object max) {
        return toBigDecimal(value).max(toBigDecimal(min)).min(toBigDecimal(max));
    }

    // ---------- 取整 ----------

    /** 無條件捨去至整數 */
    public static BigDecimal floor(Object value) {
        return toBigDecimal(value).setScale(0, RoundingMode.FLOOR);
    }

    /** 無條件進位至整數 */
    public static BigDecimal ceil(Object value) {
        return toBigDecimal(value).setScale(0, RoundingMode.CEILING);
    }

    /** 四捨五入至整數 */
    public static BigDecimal round(Object value) {
        return toBigDecimal(value).setScale(0, RoundingMode.HALF_UP);
    }

    /** 四捨五入至指定小數位數 */
    public static BigDecimal roundScale(Object value, Object scale) {
        return toBigDecimal(value).setScale(toBigDecimal(scale).intValue(), RoundingMode.HALF_UP);
    }

    // ---------- 次方 / 開方 ----------

    /** base 的 exponent 次方（exponent 須為非負整數） */
    public static BigDecimal pow(Object base, Object exponent) {
        return toBigDecimal(base).pow(toBigDecimal(exponent).intValue());
    }

    /** 平方根（精度 DECIMAL64） */
    public static BigDecimal sqrt(Object value) {
        return toBigDecimal(value).sqrt(MathContext.DECIMAL64);
    }

    // ---------- 聚合（支援 List） ----------

    /** List 加總；非 List 時直接轉 BigDecimal */
    public static BigDecimal sum(Object list) {
        if (list instanceof List<?> items) {
            return items.stream()
                    .map(SafeFunctions::toBigDecimal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return toBigDecimal(list);
    }

    /** List 平均值；非 List 或空 List 時直接轉 BigDecimal */
    public static BigDecimal avg(Object list) {
        if (list instanceof List<?> items && !items.isEmpty()) {
            BigDecimal total = items.stream()
                    .map(SafeFunctions::toBigDecimal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.divide(new BigDecimal(items.size()), MathContext.DECIMAL64);
        }
        return toBigDecimal(list);
    }

    /** List 元素個數；非 List 時回傳 1 */
    public static BigDecimal count(Object list) {
        if (list instanceof List<?> items) {
            return new BigDecimal(items.size());
        }
        return BigDecimal.ONE;
    }

    // ---------- 除法 / 餘數 / 百分比 ----------

    /** 精確除法（DECIMAL64 精度） */
    public static BigDecimal divide(Object a, Object b) {
        return toBigDecimal(a).divide(toBigDecimal(b), MathContext.DECIMAL64);
    }

    /** 取餘數 */
    public static BigDecimal modulo(Object a, Object b) {
        return toBigDecimal(a).remainder(toBigDecimal(b));
    }

    /** part 佔 total 的百分比；total 為 0 時回傳 0 */
    public static BigDecimal percent(Object part, Object total) {
        BigDecimal divisor = toBigDecimal(total);
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return toBigDecimal(part).multiply(new BigDecimal("100")).divide(divisor, MathContext.DECIMAL64);
    }

    // ---------- 比較 ----------

    /** value 是否在 [min, max] 區間內（閉區間） */
    public static boolean between(Object value, Object min, Object max) {
        BigDecimal v = toBigDecimal(value);
        return v.compareTo(toBigDecimal(min)) >= 0 && v.compareTo(toBigDecimal(max)) <= 0;
    }

    /** 數值相等（避免 SpEL 保留字 eq，改用 isEq） */
    public static boolean isEq(Object a, Object b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b)) == 0;
    }

    /** 數值大於（避免 SpEL 保留字 gt，改用 isGt） */
    public static boolean isGt(Object a, Object b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b)) > 0;
    }

    /** 數值大於等於 */
    public static boolean isGte(Object a, Object b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b)) >= 0;
    }

    /** 數值小於（避免 SpEL 保留字 lt，改用 isLt） */
    public static boolean isLt(Object a, Object b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b)) < 0;
    }

    /** 數值小於等於 */
    public static boolean isLte(Object a, Object b) {
        return toBigDecimal(a).compareTo(toBigDecimal(b)) <= 0;
    }

    // ---------- 符號 ----------

    /** 取反值 */
    public static BigDecimal negate(Object value) {
        return toBigDecimal(value).negate();
    }

    // ---------- Null 處理 ----------

    /** 回傳第一個非 null 的值；全為 null 時回傳 0 */
    public static BigDecimal coalesce(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return toBigDecimal(value);
            }
        }
        return BigDecimal.ZERO;
    }

    /** value 為 null 時回傳 defaultValue，否則回傳 value */
    public static BigDecimal ifNull(Object value, Object defaultValue) {
        return value == null ? toBigDecimal(defaultValue) : toBigDecimal(value);
    }

    public static boolean isNull(Object value) {
        return value == null;
    }

    public static boolean notNull(Object value) {
        return value != null;
    }

    // ---------- 內部工具 ----------

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }
}
