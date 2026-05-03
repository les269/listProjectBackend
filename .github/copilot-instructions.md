---
name: spring-backend-conventions
description: "Layered architecture and naming conventions for this Spring Boot repo. Use when: adding new table, new feature, new entity, new controller, new service, new repository, scaffold CRUD, generate Java files, understand project structure, naming rules, package layout, annotation patterns."
argument-hint: "Describe what you want to create (e.g. new table + CRUD for xxx)"
---

# Spring Boot 分層架構與命名慣例

## 資料庫 Schema（dynamic.sql）

- 所有 DDL 放在 `src/main/resources/dynamic.sql`，格式一律用 `CREATE TABLE IF NOT EXISTS`
- 欄位命名：snake_case（`cookie_id`, `ref_id`, `updated_time`）
- 時間欄位：`updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP`
- 複合主鍵直接在 `PRIMARY KEY(col1, col2)` 宣告
- 自增主鍵：`id INTEGER PRIMARY KEY AUTOINCREMENT`（非業務 key 用此）
- index 另外 `CREATE INDEX IF NOT EXISTS idx_<table>_<col> ON <table>(<col>)`

---

## 套件結構

```
com.lsb.listProjectBackend
├── aop/                  # @UseDynamic, @UseDynamicTx
├── config/               # DataSource, Web 設定
├── controller/           # REST controllers
├── converter/            # JPA JSON converters
├── domain/               # DTO / TO (傳輸物件)
├── entity/
│   ├── dynamic/          # 動態 DB 的 JPA Entity + PK
│   └── local/            # 本地 DB 的 JPA Entity
├── mapper/               # MapStruct interfaces
├── repository/
│   ├── dynamic/          # 動態 DB JPA Repositories
│   └── local/
├── service/              # Service interfaces
│   └── impl/             # ServiceImpl 實作
└── utils/                # Global enum, Utils
```

---

## 各層命名規則（以 `FooBar` 功能為例）

| 層次         | 檔名                     | 說明                       |
| ------------ | ------------------------ | -------------------------- |
| SQL          | `dynamic.sql` 追加       | snake_case table name      |
| Entity       | `FooBar.java`            | 對應單一 table             |
| 複合 PK      | `FooBarPK.java`          | 複合主鍵時才建             |
| Domain       | `FooBarTO.java`          | 傳輸用 DTO，TO 結尾        |
| Mapper       | `FooBarMapper.java`      | MapStruct interface        |
| Repository   | `FooBarRepository.java`  | 放在 `repository/dynamic/` |
| Service 介面 | `FooBarService.java`     | 放在 `service/`            |
| Service 實作 | `FooBarServiceImpl.java` | 放在 `service/impl/`       |
| Controller   | `FooBarController.java`  | 放在 `controller/`         |

---

## Entity 範本

```java
@Data
@Entity
@Table(name = "foo_bar")
@IdClass(FooBarPK.class)          // 複合 PK 才加
public class FooBar implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "foo_id", nullable = false)
    private String fooId;

    @Column(name = "json", nullable = false)
    @Convert(converter = XxxConverter.class)  // JSON 欄位才加
    private List<Xxx> json;

    @Enumerated(EnumType.STRING)              // Enum 欄位才加
    @Column(name = "type", nullable = false)
    private Global.XxxType type;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
}
```

## 複合 PK 範本

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FooBarPK implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String fooId;
    private String barId;
}
```

---

## Domain TO 範本

```java
@Data
public class FooBarTO {
    private String fooId;
    private String barId;
    private List<Cookie> list;   // 直接用 entity 內的 value object
    private LocalDateTime updatedTime;
}
```

---

## Converter（JSON 欄位）

- 繼承 `JsonAttributeConverter<T>`，只需實作 `getTypeClass()`
- 已有：`CookieListConverter`（`List<Cookie>`）
- 新增範本：

```java
public class FooListConverter extends JsonAttributeConverter<List<Foo>> {
    @Override
    protected TypeReference<List<Foo>> getTypeClass() {
        return new TypeReference<>() {};
    }
}
```

---

## MapStruct Mapper 範本

```java
@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface FooBarMapper {
    FooBar toEntity(FooBarTO to);
    List<FooBar> toEntityList(List<FooBarTO> list);
    FooBarTO toDomain(FooBar entity);
    List<FooBarTO> toDomainList(List<FooBar> list);
}
```

---

## Repository 範本

```java
public interface FooBarRepository extends JpaRepository<FooBar, String> {
    // 複合 PK：JpaRepository<FooBar, FooBarPK>

    @Query(value = "SELECT * FROM foo_bar WHERE foo_id = :fooId", nativeQuery = true)
    List<FooBar> findAllByFooId(@Param("fooId") String fooId);

    @Query(value = "SELECT COUNT(*) FROM foo_bar WHERE bar_id = :barId", nativeQuery = true)
    int countByBarId(@Param("barId") String barId);
}
```

---

## Service 介面 + 實作範本

```java
// Service 介面
public interface FooBarService {
    List<FooBarTO> getAll();
    FooBarTO getById(String fooId);
    void update(FooBarTO req);
    void delete(String fooId);
}

// ServiceImpl
@Slf4j
@UseDynamic          // 使用動態 DB 時必須加
@Service
@RequiredArgsConstructor
public class FooBarServiceImpl implements FooBarService {
    private final FooBarRepository fooBarRepository;

    private final FooBarMapper fooBarMapper;

    @Override
    public List<FooBarTO> getAll() {
        return fooBarMapper.toDomainList(fooBarRepository.findAll());
    }

    @Override
    public FooBarTO getById(String fooId) {
        return fooBarMapper.toDomain(fooBarRepository.findById(fooId).orElse(null));
    }

    @Override
    public void update(FooBarTO req) {
        fooBarRepository.save(fooBarMapper.toEntity(req));
    }

    @Override
    public void delete(String fooId) {
        fooBarRepository.deleteById(fooId);
    }
}
```

---

## Controller 範本

```java
@CrossOrigin("*")
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class FooBarController {
    private final FooBarService fooBarService;

    @GetMapping("/foo-bar/all")
    public List<FooBarTO> getAll() {
        return fooBarService.getAll();
    }

    @GetMapping("/foo-bar")
    public FooBarTO getById(@RequestParam("fooId") String fooId) {
        return fooBarService.getById(fooId);
    }

    @PostMapping("/foo-bar/update")
    public void update(@RequestBody FooBarTO req) {
        fooBarService.update(req);
    }

    @DeleteMapping("/foo-bar/delete")
    public void delete(@RequestParam("fooId") String fooId) {
        fooBarService.delete(fooId);
    }
}
```

---

## URL 命名規則

| 動作       | Method | Path pattern                    |
| ---------- | ------ | ------------------------------- |
| 查全部     | GET    | `/api/foo-bar/all`              |
| 查單筆     | GET    | `/api/foo-bar?fooId=xxx`        |
| 查by條件   | GET    | `/api/foo-bar/by-xxx?xxx=`      |
| 新增/更新  | POST   | `/api/foo-bar/update`           |
| 刪除       | DELETE | `/api/foo-bar/delete?fooId=xxx` |
| 判斷使用中 | GET    | `/api/foo-bar/in-use?fooId=xxx` |

---

## 關鍵 AOP / 注意事項

- 所有存取動態 DB 的 ServiceImpl **必須** 加 `@UseDynamic`（class level）
- `@UpdateTimestamp` 自動更新時間，entity 不需手動 set
- MapStruct 一律使用 `@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)`，由 Spring 管理 bean
- Controller / ServiceImpl 一律使用 `@RequiredArgsConstructor` + `private final` 注入相依，不使用欄位 `@Autowired`
- Repository 命名：`findAll*`、`count*`，查詢一律用 `nativeQuery = true`
- Enum 存資料庫用 `@Enumerated(EnumType.STRING)`，Enum 定義在 `utils/Global.java`
