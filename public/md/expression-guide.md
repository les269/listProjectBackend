# 運算式（Expression）撰寫指南

本文件說明如何在欄位「運算式」中正確填寫計算公式。

---

## 基本規則

| 規則     | 說明                                                                                          |
| -------- | --------------------------------------------------------------------------------------------- |
| 變數引用 | 以 `#變數名稱` 引用，例如 `#price`                                                            |
| 函式呼叫 | 以 `#函式名稱(參數)` 呼叫，例如 `#abs(#value)`                                                |
| 最大長度 | 運算式不可超過 **500 字元**                                                                   |
| 允許字元 | 數字、運算子、括號、`#變數`、`#函式`、空白                                                    |
| 不允許   | 字串（`'hello'`）、陣列存取（`#v[0]`）、類別引用（`T(Math)`）、`new`、方法鏈（`#v.length()`） |

---

## 基本運算子

| 運算子 | 說明             | 範例                   | 結果             |
| ------ | ---------------- | ---------------------- | ---------------- |
| `+`    | 加法             | `#a + #b`              |                  |
| `-`    | 減法             | `#a - #b`              |                  |
| `*`    | 乘法             | `#price * #qty`        |                  |
| `/`    | 除法             | `#total / 2`           |                  |
| `%`    | 取餘數           | `#n % 3`               |                  |
| `==`   | 等於             | `#status == 1`         | `true` / `false` |
| `!=`   | 不等於           | `#type != 0`           | `true` / `false` |
| `>`    | 大於             | `#score > 60`          | `true` / `false` |
| `>=`   | 大於等於         | `#score >= 60`         | `true` / `false` |
| `<`    | 小於             | `#age < 18`            | `true` / `false` |
| `<=`   | 小於等於         | `#age <= 65`           | `true` / `false` |
| `&&`   | 且               | `#a > 0 && #b > 0`     | `true` / `false` |
| `\|\|` | 或               | `#a > 0 \|\| #b > 0`   | `true` / `false` |
| `!`    | 非               | `!#flag`               | `true` / `false` |
| `? :`  | 三元（條件選擇） | `#score >= 60 ? 1 : 0` |                  |

> **注意**：`div`、`mod`、`eq`、`gt`、`lt` 為系統保留字，**不可**作為變數名稱。請改用 `#divide()`、`#modulo()` 等函式，或直接使用 `/`、`%` 運算子。

---

## 內建函式一覽

### 基本數學

#### `#abs(value)` — 絕對值

```
#abs(#amount)         → 若 amount = -50，結果為 50
#abs(-3.14)           → 3.14
```

#### `#max(a, b)` — 取最大值

```
#max(#score, 0)       → 分數與 0 取最大（不低於 0）
#max(#a, #b)          → a 與 b 中較大者
```

#### `#min(a, b)` — 取最小值

```
#min(#score, 100)     → 分數與 100 取最小（不超過 100）
#min(#a, #b)          → a 與 b 中較小者
```

#### `#clamp(value, min, max)` — 限制在區間內

> 若 value < min 回傳 min；若 value > max 回傳 max；否則回傳 value。

```
#clamp(#score, 0, 100)   → 確保分數在 0～100 之間
#clamp(#qty, 1, 999)     → 數量限制 1～999
```

#### `#negate(value)` — 取反值

```
#negate(#profit)      → 若 profit = 100，結果為 -100
#negate(-5)           → 5
```

---

### 取整

#### `#floor(value)` — 無條件捨去（向下取整）

```
#floor(3.9)           → 3
#floor(-1.1)          → -2
```

#### `#ceil(value)` — 無條件進位（向上取整）

```
#ceil(3.1)            → 4
#ceil(-1.9)           → -1
```

#### `#round(value)` — 四捨五入至整數

```
#round(3.5)           → 4
#round(3.4)           → 3
```

#### `#roundScale(value, scale)` — 四捨五入至指定小數位

```
#roundScale(#price, 2)       → 保留 2 位小數
#roundScale(3.14159, 2)      → 3.14
#roundScale(3.14159, 4)      → 3.1416
```

---

### 次方 / 開方

#### `#pow(base, exponent)` — 次方

> exponent 須為非負整數。

```
#pow(2, 10)           → 1024
#pow(#base, 3)        → base 的三次方
```

#### `#sqrt(value)` — 平方根

```
#sqrt(9)              → 3
#sqrt(2)              → 1.4142135...
```

---

### 除法 / 餘數 / 百分比

#### `#divide(a, b)` — 精確除法

> 使用 DECIMAL64 高精度，避免整數除法截斷。

```
#divide(10, 4)        → 2.5（一般 10 / 4 在整數模式為 2）
#divide(#total, #count)
```

#### `#modulo(a, b)` — 取餘數

> 功能同 `%`，但透過函式呼叫語意更清晰。

```
#modulo(10, 3)        → 1
#modulo(#n, 2)        → 判斷奇偶（結果為 0 代表偶數）
```

#### `#percent(part, total)` — 百分比

> 計算 part 佔 total 的百分比（0～100）。若 total = 0，回傳 0。

```
#percent(25, 200)            → 12.5
#percent(#sold, #stock)      → 售出百分比
```

---

### 聚合（List 變數）

> 以下函式接受一個 List 變數作為輸入。

#### `#sum(list)` — 加總

```
#sum(#amounts)        → 清單內所有數值加總
```

#### `#avg(list)` — 平均值

```
#avg(#scores)         → 清單內所有數值的平均
```

#### `#count(list)` — 元素個數

```
#count(#items)        → 清單元素數量
```

---

### 比較（回傳 true / false）

> 這些函式使用 BigDecimal 精確比較，不受浮點誤差影響（`5.0` 與 `5` 視為相等）。

#### `#between(value, min, max)` — 閉區間包含

```
#between(#score, 60, 100)    → score 是否在 60～100（含邊界）
#between(5, 0, 10)           → true
#between(11, 0, 10)          → false
```

#### `#isEq(a, b)` — 數值相等

```
#isEq(#a, #b)         → a 與 b 數值是否相等
#isEq(5.0, 5)         → true（精確比較，不受小數點影響）
```

#### `#isGt(a, b)` — 大於

```
#isGt(#score, 60)     → score 是否大於 60（不含 60）
```

#### `#isGte(a, b)` — 大於等於

```
#isGte(#score, 60)    → score 是否大於等於 60（含 60）
```

#### `#isLt(a, b)` — 小於

```
#isLt(#age, 18)       → age 是否小於 18（不含 18）
```

#### `#isLte(a, b)` — 小於等於

```
#isLte(#age, 65)      → age 是否小於等於 65（含 65）
```

---

### Null 處理

#### `#ifNull(value, default)` — Null 時給預設值

```
#ifNull(#discount, 0)        → discount 為 null 時回傳 0
#ifNull(#price, 99)          → price 為 null 時回傳 99
```

#### `#coalesce(v1, v2, ...)` — 第一個非 null 值

```
#coalesce(#a, #b, #c)        → 依序回傳第一個不為 null 的值
#coalesce(#discount, 0)      → 同 ifNull
```

#### `#isNull(value)` — 是否為 null

```
#isNull(#value)       → true / false
```

#### `#notNull(value)` — 是否不為 null

```
#notNull(#value)      → true / false
```

---

## 完整範例

```
# 計算折扣後金額，最低 0
#clamp(#price * (1 - #discount), 0, #price)

# 若數量大於 10 打九折，否則原價
#qty > 10 ? #price * 0.9 : #price

# 計算 BMI（體重 / 身高平方）
#divide(#weight, #pow(#height, 2))

# 確保百分比在 0～100
#clamp(#percent(#sold, #total), 0, 100)

# 有折扣用折扣價，否則用原價
#ifNull(#salePrice, #price)

# 判斷成績等第（60 分以上及格）
#score >= 60 ? 1 : 0
```

---

## 常見錯誤

| 錯誤寫法           | 原因             | 正確寫法                      |
| ------------------ | ---------------- | ----------------------------- |
| `price * qty`      | 變數需加 `#`     | `#price * #qty`               |
| `'hello'`          | 不支援字串字面值 | 改用數值或布林                |
| `#list[0]`         | 不支援陣列索引   | 使用 `#sum(#list)` 等聚合函式 |
| `T(Math).random()` | 不允許類別引用   | 無對應替代，不支援            |
| `#v.toString()`    | 不允許方法鏈     | 無對應替代，不支援            |
| `#eq(#a, #b)`      | `eq` 是保留字    | `#isEq(#a, #b)`               |
| `#gt(#a, #b)`      | `gt` 是保留字    | `#isGt(#a, #b)`               |
| `#lt(#a, #b)`      | `lt` 是保留字    | `#isLt(#a, #b)`               |
| `#div(#a, #b)`     | `div` 是保留字   | `#divide(#a, #b)`             |
| `#mod(#a, #b)`     | `mod` 是保留字   | `#modulo(#a, #b)`             |
