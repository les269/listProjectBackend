# Dynamic DataSource 機制說明

## 背景與目的

此應用程式需要支援使用者在執行期間切換不同的目標資料庫（例如從 UI 選擇要操作哪一個 DB）。  
標準 Spring JPA 設定只能固定一個資料庫，因此這裡實作了一套「執行期動態路由」機制，讓不同 HTTP Request 可以各自使用不同的 DataSource，互不干擾。

---

## 整體架構

```
HTTP Request
    │
    ▼
Controller → Service（標有 @UseDynamic）
    │
    ▼ AOP 攔截
DynamicDatabaseAspect
    │  1. 查詢 local.sqlite 的 setting 表
    │     讀取 name = "current_dynamic_database" 的 value
    │     (即使用者在 UI 選擇的 config_name)
    │  2. 將 key 寫入 DynamicDataSourceContextHolder（ThreadLocal）
    │  3. depth + 1（巢狀保護）
    │
    ▼ 執行原始方法
JPA Repository 操作
    │
    ▼
DynamicRoutingDataSource.determineCurrentLookupKey()
    │  從 ThreadLocal 讀取 key
    │  回傳對應已註冊的 DataSource
    │
    ▼
SQL 送往正確的資料庫執行
    │
    ▼ 方法結束（finally）
DynamicDataSourceContextHolder
    depth - 1；降至 0（最外層）才清除 ThreadLocal
```

---

## 各類別職責

| 類別                             | 所在位置             | 職責                                                                                                                           |
| -------------------------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `DynamicDataSourceConfig`        | `config/datasource/` | Spring Bean 組裝：建立 defaultDynamicDataSource、DynamicRoutingDataSource、EntityManagerFactory、TransactionManager            |
| `DynamicRoutingDataSource`       | `config/datasource/` | 繼承 `AbstractRoutingDataSource`，覆寫 `determineCurrentLookupKey()` 從 ThreadLocal 取 key，決定此次 JPA 操作用哪個 DataSource |
| `DynamicDataSourceContextHolder` | `config/datasource/` | 以 ThreadLocal 儲存「當前 thread 要用的 DB key」及「巢狀深度」，是 AOP 層與 JPA 層的資料橋樑                                   |
| `DynamicDatabaseAspect`          | `config/datasource/` | AOP Around Advice：攔截 `@UseDynamic` / `@UseDynamicTx` 方法，設定 key 並在結束後清理                                          |
| `DynamicDataSourceRefresher`     | `config/datasource/` | 將 `database_config` 表的設定轉成 DataSource 物件並批次更新到 DynamicRoutingDataSource                                         |
| `DynamicDataSourceRegistrar`     | `config/datasource/` | `ApplicationReadyEvent` 監聽器，應用啟動就緒後觸發第一次 refresh                                                               |

---

## 關鍵設計說明

### 為什麼用 ThreadLocal？

Web 應用每個 HTTP Request 由獨立 thread 處理。ThreadLocal 讓每個 thread 擁有各自的 DB key，互不影響——即使 100 個 Request 同時進來，各自設定不同的 key，彼此讀到的值完全獨立。

```
Thread-1 (Request A) → ThreadLocal key = "prod_db"
Thread-2 (Request B) → ThreadLocal key = "test_db"
Thread-3 (Request C) → ThreadLocal key = "prod_db"
// 三個 thread 各自持有獨立副本，不會互相覆蓋
```

### 為什麼需要 depth（巢狀深度）？

當 Service A（`@UseDynamic`）呼叫 Service B（`@UseDynamic`）時，AOP 會攔截兩次：

```
進入 A → depth: 0→1，設定 key
  進入 B → depth: 1→2，設定 key（相同，無影響）
  離開 B → depth: 2→1，depth > 0，不清除 key  ← 關鍵！
離開 A → depth: 1→0，depth = 0，清除 key
```

若 B 離開時就直接清除，A 後續的 JPA 操作就找不到 key，會 fallback 到預設 DB，導致查錯資料庫。depth 計數正是為了解決這個問題。

### 為什麼在 ApplicationReadyEvent 而非 @PostConstruct？

`@PostConstruct` 在 Spring Context 建立期間執行，此時 JPA / Repository bean 尚未完全就緒，呼叫 `databaseConfigRepository` 可能失敗。  
`ApplicationReadyEvent` 確保所有 bean 初始化完畢，是讀取資料庫設定的安全時機。

### 熱更新（Hot Reload）

使用者透過 API 新增/修改/刪除 `database_config` 後，Controller 只需呼叫 `DynamicDataSourceRefresher.refresh()`，不需重啟應用即可生效。  
`refresh()` 會一次性替換所有 targets（`setAllDataSources`），避免部分更新導致狀態不一致。

---

## 使用方式

在 Service 方法上標注 `@UseDynamic`，AOP 自動處理切換，無需任何手動操作：

```java
@UseDynamic
public List<Theme> findAll() {
    return themeRepository.findAll(); // 自動路由到使用者當前選擇的 DB
}

// 整個 class 標注：所有 public 方法都會自動切換
@UseDynamic
public class ThemeServiceImpl implements ThemeService {
    ...
}
```

需要事務支援時改用 `@UseDynamicTx`。

---

## 資料流總結

```
local.sqlite (setting 表)
    current_dynamic_database = "my_project_db"
              │
              │ DynamicDatabaseAspect 讀取
              ▼
    ThreadLocal key = "my_project_db"
              │
              │ DynamicRoutingDataSource 查表
              ▼
    targets["my_project_db"] = DataSource(jdbc:mysql://...)
              │
              │ JPA 使用此 DataSource
              ▼
    SQL 送往 mysql://... 執行
```

---

## 相關檔案索引

- `config/datasource/DynamicDataSourceConfig.java` — Bean 組裝
- `config/datasource/DynamicRoutingDataSource.java` — JPA 路由核心
- `config/datasource/DynamicDataSourceContextHolder.java` — ThreadLocal 橋樑
- `config/datasource/DynamicDatabaseAspect.java` — AOP 攔截切換
- `config/datasource/DynamicDataSourceRefresher.java` — 重建 DataSource targets
- `config/datasource/DynamicDataSourceRegistrar.java` — 啟動觸發器
- `aop/UseDynamic.java` — 觸發切換的 Annotation
- `entity/local/Setting.java` — 儲存當前選擇 DB 的設定
- `entity/local/DatabaseConfig.java` — 儲存各 DB 連線設定
