package com.lsb.listProjectBackend.utils;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ExperessionUtils {

    private static final int MAX_EXPRESSION_LENGTH = 500;

    /**
     * Whitelist：允許數字、布林、null、算術/比較/邏輯/三元運算子、
     * 括號、逗號（函式引數分隔）、空白，以及 #varName / #funcName 引用。
     */
    private static final Pattern SAFE_EXPRESSION = Pattern.compile(
            "^(\\d+(\\.\\d+)?"
                    + "|\\btrue\\b"
                    + "|\\bfalse\\b"
                    + "|\\bnull\\b"
                    + "|[+\\-*/%(),]"
                    + "|==|!=|<=|>=|<|>"
                    + "|&&|\\|\\|"
                    + "|[!?:]"
                    + "|#[a-zA-Z_][a-zA-Z0-9_]*"
                    + "|\\s)+$");

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /** 預先反射取得所有 SafeFunctions 方法，供 context 註冊用 */
    private static final Map<String, Method> SAFE_FUNCTION_REGISTRY = new LinkedHashMap<>();

    static {
        try {
            registerFn("abs", Object.class);
            registerFn("max", Object.class, Object.class);
            registerFn("min", Object.class, Object.class);
            registerFn("clamp", Object.class, Object.class, Object.class);
            registerFn("floor", Object.class);
            registerFn("ceil", Object.class);
            registerFn("round", Object.class);
            registerFn("roundScale", Object.class, Object.class);
            registerFn("pow", Object.class, Object.class);
            registerFn("sqrt", Object.class);
            registerFn("sum", Object.class);
            registerFn("avg", Object.class);
            registerFn("count", Object.class);
            registerFn("divide", Object.class, Object.class);
            registerFn("modulo", Object.class, Object.class);
            registerFn("percent", Object.class, Object.class);
            registerFn("between", Object.class, Object.class, Object.class);
            registerFn("isEq",    Object.class, Object.class);
            registerFn("isGt",    Object.class, Object.class);
            registerFn("isGte",   Object.class, Object.class);
            registerFn("isLt",    Object.class, Object.class);
            registerFn("isLte",   Object.class, Object.class);
            registerFn("negate", Object.class);
            registerFn("coalesce", Object[].class);
            registerFn("ifNull", Object.class, Object.class);
            registerFn("isNull", Object.class);
            registerFn("notNull", Object.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to register SafeFunctions", e);
        }
    }

    private static void registerFn(String name, Class<?>... params) throws NoSuchMethodException {
        SAFE_FUNCTION_REGISTRY.put(name, SafeFunctions.class.getMethod(name, params));
    }

    /**
     * 以 SpEL SimpleEvaluationContext 計算 expression。
     * <ul>
     * <li>expression 長度上限 500</li>
     * <li>expression 通過 whitelist 驗證才執行</li>
     * <li>使用 SimpleEvaluationContext（不允許 type reference、反射）</li>
     * <li>內建 SafeFunctions 可直接以 #funcName(args) 呼叫</li>
     * </ul>
     *
     * @param expression SpEL 運算式，用 #varName 引用變數、#funcName(args) 呼叫函式
     * @param variables  變數 map，key 對應 expression 內的 #key
     * @return 計算結果
     * @throws IllegalArgumentException expression 為空、超長或包含不安全語法
     */
    public static Object calculateExpression(String expression, Map<String, Object> variables) {
        if (Utils.isBlank(expression)) {
            throw new IllegalArgumentException("Expression is blank");
        }
        if (expression.length() > MAX_EXPRESSION_LENGTH) {
            throw new IllegalArgumentException(
                    "Expression exceeds maximum length of " + MAX_EXPRESSION_LENGTH);
        }
        if (!SAFE_EXPRESSION.matcher(expression).matches()) {
            throw new IllegalArgumentException(
                    "Expression contains unsafe syntax: " + expression);
        }
        SimpleEvaluationContext context = SimpleEvaluationContext
                .forReadOnlyDataBinding()
                .build();

        // 註冊 SafeFunctions
        SAFE_FUNCTION_REGISTRY.forEach(context::setVariable);

        // 註冊使用者變數（不可覆蓋函式名稱）
        if (variables != null) {
            variables.forEach((k, v) -> {
                if (!SAFE_FUNCTION_REGISTRY.containsKey(k)) {
                    context.setVariable(k, v);
                }
            });
        }

        Expression expr = PARSER.parseExpression(expression);
        return expr.getValue(context);
    }
}
