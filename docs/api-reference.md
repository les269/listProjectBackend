# API 參考文件

本文件依目前工作目錄的 Controller 原始碼整理，包含尚未提交的變更。共 **179 個 API、29 個 Controller**；不包含已移除的舊路由。此為靜態盤點，未啟動服務驗證執行結果。

## 基本資訊

- 開發環境主機：`http://localhost:8081`。
- Context path：`/list-project-backend`。
- 下表路徑已包含 `/api`；完整網址為主機 + context path + 表中路徑。
- 例如：`GET http://localhost:8081/list-project-backend/api/api-config/exist?apiId=example`。
- `Query` 是 URL 查詢參數；`Path` 是路徑變數；`Body` 是請求本文；`Part` 是 multipart 欄位。Body 後方名稱為 Java 參數名稱，不代表 JSON 需要同名外層物件。
- 除另有標註，Query、Path、Body 與 Part 參數均依 Spring 註解預設為必填。物件內部欄位的必填與驗證規則需參照 DTO、Service 與 OpenAPI。
- JSON Body 使用 `Content-Type: application/json`；`List<T>` 對應 JSON 陣列，`Map<K,V>` 對應物件。
- 回傳欄列出 Java 宣告型別：`void` 表示無回應本文，`ResponseEntity<T>` 的本文為 `T`。`String` 可能是文字或已序列化 JSON，應以實際端點為準，並非統一回應封裝。

## 模組索引

- [共用設定與系統](#common)：36 個 API
- [連線設定](#connection)：12 個 API
- [資料集](#dataset)：24 個 API
- [檔案與磁碟](#file)：9 個 API
- [Scrapy 擷取](#scrapy)：19 個 API
- [共用標籤](#share)：9 個 API
- [Spider 爬蟲與瀏覽器驗證](#spider)：42 個 API
- [主題](#theme)：28 個 API

<a id="common"></a>

## 共用設定與系統

### CookieListController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/CookieListController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/cookie-list/by-ref-id-and-type` | 查詢資料 (`getByRefIdAndType`) | `Query: refId: String`<br>`Query: type: Global.CookieListMapType` | `CookieListTO` |
| GET | `/api/cookie-list/all` | 查詢清單 (`getAll`) | 無 | `List<CookieListTO>` |
| GET | `/api/cookie-list` | 查詢資料 (`getByCookieId`) | `Query: cookieId: String` | `CookieListTO` |
| POST | `/api/cookie-list/update` | 儲存或更新 (`update`) | `Body: req: CookieListTO` | `void` |
| DELETE | `/api/cookie-list/delete` | 刪除資料 (`delete`) | `Query: cookieId: String` | `void` |

### CookieListMapController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/CookieListMapController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/cookie-list/map/by-id-and-type` | 查詢資料 (`getMapByIdAndType`) | `Query: refId: String`<br>`Query: type: Global.CookieListMapType` | `CookieListMapTO` |
| GET | `/api/cookie-list/map/by-ref-id` | 查詢資料 (`getMapByRefId`) | `Query: refId: String` | `List<CookieListMapTO>` |
| GET | `/api/cookie-list/map/by-type` | 查詢資料 (`getMapByType`) | `Query: type: Global.CookieListMapType` | `List<CookieListMapTO>` |
| POST | `/api/cookie-list/map/update` | 儲存或更新 (`updateMap`) | `Body: req: CookieListMapTO` | `void` |
| DELETE | `/api/cookie-list/map/delete` | 刪除資料 (`deleteMap`) | `Query: refId: String`<br>`Query: type: Global.CookieListMapType` | `void` |
| GET | `/api/cookie-list/map/in-use` | 檢查使用狀態 (`isInUseMap`) | `Query: cookieId: String`<br>`Query: type: Global.CookieListMapType` | `boolean` |
| GET | `/api/cookie-list/map/cookie-in-use` | 檢查使用狀態 (`cookieIsInUse`) | `Query: cookieId: String` | `boolean` |

### HeadersController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/HeadersController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/headers/by-ref-id-and-type` | 查詢資料 (`getByRefIdAndType`) | `Query: refId: String`<br>`Query: type: Global.HeadersMapType` | `HeadersTO` |
| GET | `/api/headers/all` | 查詢清單 (`getAll`) | 無 | `List<HeadersTO>` |
| GET | `/api/headers` | 查詢資料 (`getByHeadersId`) | `Query: headersId: String` | `HeadersTO` |
| POST | `/api/headers/update` | 儲存或更新 (`update`) | `Body: req: HeadersTO` | `void` |
| DELETE | `/api/headers/delete` | 刪除資料 (`delete`) | `Query: headersId: String` | `void` |

### HeadersMapController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/HeadersMapController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/headers/map/by-id-and-type` | 查詢資料 (`getMapByIdAndType`) | `Query: refId: String`<br>`Query: type: Global.HeadersMapType` | `HeadersMapTO` |
| GET | `/api/headers/map/by-ref-id` | 查詢資料 (`getMapByRefId`) | `Query: refId: String` | `List<HeadersMapTO>` |
| GET | `/api/headers/map/by-type` | 查詢資料 (`getMapByType`) | `Query: type: Global.HeadersMapType` | `List<HeadersMapTO>` |
| POST | `/api/headers/map/update` | 儲存或更新 (`updateMap`) | `Body: req: HeadersMapTO` | `void` |
| DELETE | `/api/headers/map/delete` | 刪除資料 (`deleteMap`) | `Query: refId: String`<br>`Query: type: Global.HeadersMapType` | `void` |
| GET | `/api/headers/map/in-use` | 檢查使用狀態 (`isInUseMap`) | `Query: headersId: String`<br>`Query: type: Global.HeadersMapType` | `boolean` |
| GET | `/api/headers/map/headers-in-use` | 檢查使用狀態 (`headersIsInUse`) | `Query: headersId: String` | `boolean` |

### ReplaceValueMapController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/ReplaceValueMapController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/replace-value-map/name-list` | 查詢清單 (`getNameList`) | 無 | `List<ReplaceValueMapTO>` |
| GET | `/api/replace-value-map/exist` | 檢查是否存在 (`existMap`) | `Query: name: String` | `boolean` |
| GET | `/api/replace-value-map/get` | 查詢資料 (`getByName`) | `Query: name: String` | `ReplaceValueMapTO` |
| POST | `/api/replace-value-map/update` | 儲存或更新 (`update`) | `Body: to: ReplaceValueMapTO` | `void` |
| DELETE | `/api/replace-value-map/delete` | 刪除資料 (`deleteByName`) | `Query: name: String` | `void` |

### SettingController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/SettingController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/setting/all` | 查詢清單 (`getAll`) | 無 | `List<SettingTO>` |
| POST | `/api/setting/update` | 儲存或更新 (`update`) | `Body: req: SettingTO` | `void` |
| POST | `/api/setting/update/all` | 儲存或更新 (`update`) | `Body: req: List<SettingTO>` | `void` |
| GET | `/api/setting/get-by-name` | 查詢資料 (`getByName`) | `Query: name: String` | `SettingTO` |
| POST | `/api/setting/changeDatabase` | 切換資料庫 (`changeDatabase`) | `Body: req: SettingTO` | `void` |

### SysController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/common/SysController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/sys/is-alive` | 檢查本機資料庫連線 (`isAlive`) | 無 | `ResponseEntity<String>` |
| POST | `/api/sys/refresh-dynamic-datasource` | 重新整理資料 (`refreshDynamicDatasource`) | 無 | `ResponseEntity<String>` |

<a id="connection"></a>

## 連線設定

### ApiRequestConfigController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/connection/ApiRequestConfigController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/api-config/exist` | 檢查是否存在 (`isExist`) | `Query: apiId: String` | `boolean` |
| GET | `/api/api-config/all` | 查詢清單 (`getAll`) | 無 | `List<ApiRequestConfigTO>` |
| POST | `/api/api-config/all/id` | 查詢資料 (`getListById`) | `Body: apiIds: List<String>` | `List<ApiRequestConfigTO>` |
| GET | `/api/api-config/id` | 查詢資料 (`getById`) | `Query: apiId: String` | `ApiRequestConfigTO` |
| POST | `/api/api-config/update` | 儲存或更新 (`update`) | `Body: apiRequestConfigTO: ApiRequestConfigTO` | `void` |
| POST | `/api/api-config/singalCallApi` | singalCallApi (`singalCallApi`) | `Body: request: SingalCallApiReqTO` | `JsonNode` |
| POST | `/api/api-config/delete` | 刪除資料 (`delete`) | `Body: apiRequestConfigTO: ApiRequestConfigTO` | `void` |

### DatabaseConfigController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/connection/DatabaseConfigController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/database-config/all` | 查詢清單 (`getAll`) | 無 | `List<DatabaseConfigTO>` |
| POST | `/api/database-config/test-connection` | 測試設定 (`testConnection`) | `Body: to: ConnectionTestReqTO` | `ConnectionTestResultTO` |
| POST | `/api/database-config/save` | 儲存或更新 (`save`) | `Body: to: DatabaseConfigTO` | `void` |
| DELETE | `/api/database-config/delete` | 刪除資料 (`delete`) | `Query: configId: String` | `void` |
| GET | `/api/database-config/open-local-folder` | 開啟本機目錄 (`openLocalFolder`) | `Query: configId: String` | `boolean` |

<a id="dataset"></a>

## 資料集

### DatasetController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/dataset/DatasetController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/dataset/get` | 查詢資料 (`getDataset`) | `Query: name: String` | `DatasetTO` |
| GET | `/api/dataset/get-data` | 查詢資料 (`getDatasetDataByName`) | `Query: name: String` | `DatasetDataTO` |
| GET | `/api/dataset/exist` | 檢查是否存在 (`existDataset`) | `Query: name: String` | `boolean` |
| GET | `/api/dataset/all` | 查詢清單 (`getAllDataset`) | 無 | `List<DatasetTO>` |
| POST | `/api/dataset/update` | 儲存或更新 (`updateDataset`) | `Body: req: DatasetTO` | `void` |
| DELETE | `/api/dataset/delete` | 刪除資料 (`deleteDataset`) | `Query: name: String` | `void` |
| GET | `/api/dataset/refresh` | 重新整理資料 (`refreshData`) | `Query: name: String` | `void` |
| POST | `/api/dataset/name-list/refresh` | 重新整理資料 (`refreshDataByNameList`) | `Body: nameList: List<String>` | `void` |
| POST | `/api/dataset/name-list/get-data` | 查詢資料 (`getDatasetDataByNameList`) | `Body: nameList: List<String>` | `List<DatasetDataTO>` |
| POST | `/api/dataset/quick-refresh` | 快速更新 (`quickRefresh`) | `Body: to: DatasetQuickRefreshTO` | `Map<String,Object>` |

### GroupDatasetController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/dataset/GroupDatasetController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/group-dataset/get` | 查詢資料 (`getGroupDataset`) | `Query: groupName: String` | `GroupDatasetTO` |
| GET | `/api/group-dataset/exist` | 檢查是否存在 (`existGroupDataset`) | `Query: groupName: String` | `boolean` |
| GET | `/api/group-dataset/all` | 查詢清單 (`getAllGroupDataset`) | 無 | `List<GroupDatasetTO>` |
| POST | `/api/group-dataset/update` | 儲存或更新 (`updateGroupDataset`) | `Body: req: GroupDatasetTO` | `void` |
| DELETE | `/api/group-dataset/delete` | 刪除資料 (`deleteGroupDataset`) | `Query: groupName: String` | `void` |
| GET | `/api/group-dataset/refresh` | 重新整理資料 (`refreshGroupDataset`) | `Query: groupName: String` | `void` |

### GroupDatasetDataController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/dataset/GroupDatasetDataController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/group-dataset-data/get` | 查詢資料 (`getGroupDatasetData`) | `Query: groupName: String`<br>`Query: primeValue: String` | `GroupDatasetDataTO` |
| GET | `/api/group-dataset-data/exist` | 檢查是否存在 (`existGroupDatasetData`) | `Query: groupName: String`<br>`Query: primeValue: String` | `boolean` |
| GET | `/api/group-dataset-data/all` | 查詢清單 (`getAllGroupDatasetData`) | `Query: groupName: String` | `List<GroupDatasetDataTO>` |
| GET | `/api/group-dataset-data/all-only-prime-value` | 查詢清單 (`getAllGroupDatasetDataNoJson`) | `Query: groupName: String` | `List<GroupDatasetDataTO>` |
| POST | `/api/group-dataset-data/update` | 儲存或更新 (`updateGroupDatasetData`) | `Body: req: GroupDatasetDataTO` | `void` |
| DELETE | `/api/group-dataset-data/delete` | 刪除資料 (`deleteGroupDatasetData`) | `Query: groupName: String`<br>`Query: primeValue: String` | `void` |
| DELETE | `/api/group-dataset-data/delete-image` | 刪除資料 (`deleteGroupDatasetDataForImage`) | `Query: groupName: String`<br>`Query: primeValue: String` | `String` |
| POST | `/api/group-dataset-data/update-list` | 儲存或更新 (`updateGroupDatasetDataList`) | `Body: req: List<GroupDatasetDataTO>` | `void` |

<a id="file"></a>

## 檔案與磁碟

### DiskController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/file/DiskController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/disk/all` | 查詢清單 (`getAll`) | 無 | `List<DiskTO>` |
| POST | `/api/disk/add` | 新增資料 (`add`) | `Body: disk: String` | `void` |
| POST | `/api/disk/delete` | 刪除資料 (`delete`) | `Body: disk: String` | `void` |
| GET | `/api/disk/refresh` | 重新整理資料 (`refresh`) | 無 | `void` |

### FileController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/file/FileController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| POST | `/api/file/delete` | 刪除資料 (`delete`) | `Body: fileRequest: FileReqTO` | `boolean` |
| POST | `/api/file/move-to` | 移動檔案 (`moveTo`) | `Body: fileRequest: FileReqTO` | `boolean` |
| POST | `/api/file/file-exist` | 批次檢查檔案存在 (`fileExist`) | `Body: fileExistRequestList: List<FileExistReqTO>` | `Map<String, Boolean>` |
| POST | `/api/file/open-file` | 開啟本機目錄 (`openHGame`) | `Body: request: FileReqTO` | `boolean` |

### ImageProxyController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/file/ImageProxyController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/proxy-image` | 查詢資料 (`getImage`) | `Query: url: String` | `ResponseEntity<byte[]>` |

<a id="scrapy"></a>

## Scrapy 擷取

### ScrapyController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/scrapy/ScrapyController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/scrapy/get` | 查詢資料 (`getConfig`) | `Query: name: String` | `ScrapyConfigTO` |
| GET | `/api/scrapy/exist` | 檢查是否存在 (`existConfig`) | `Query: name: String` | `boolean` |
| GET | `/api/scrapy/all` | 查詢清單 (`getAllConfig`) | 無 | `List<ScrapyConfigTO>` |
| GET | `/api/scrapy/allName` | 查詢清單 (`getAllName`) | 無 | `List<String>` |
| POST | `/api/scrapy/by-name-list` | 查詢資料 (`getByNameList`) | `Body: nameList: List<String>` | `List<ScrapyConfigTO>` |
| POST | `/api/scrapy/update` | 儲存或更新 (`updateConfig`) | `Body: req: ScrapyConfigTO` | `void` |
| DELETE | `/api/scrapy/delete` | 刪除資料 (`deleteConfig`) | `Query: name: String` | `void` |
| POST | `/api/scrapy/test/html` | 測試設定 (`testHtml`) | `Body: req: ScrapyTestTO` | `Map<String, Object>` |
| POST | `/api/scrapy/test/json` | 測試設定 (`testJson`) | `Body: req: ScrapyTestTO` | `Map<String, Object>` |
| POST | `/api/scrapy/test/url` | 測試設定 (`testUrl`) | `Body: req: ScrapyTestTO` | `Map<String, Object>` |
| POST | `/api/scrapy/use-json` | scrapyByJson (`scrapyByJson`) | `Body: req: ScrapyReqTO` | `Map<String, Object>` |
| POST | `/api/scrapy/use-url` | scrapyByUrl (`scrapyByUrl`) | `Body: req: ScrapyReqTO` | `Map<String, Object>` |

### ScrapyPaginationController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/scrapy/ScrapyPaginationController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/scrapy-pagination/get` | 查詢資料 (`get`) | `Query: name: String` | `ScrapyPaginationTO` |
| GET | `/api/scrapy-pagination/exist` | 檢查是否存在 (`exist`) | `Query: name: String` | `boolean` |
| GET | `/api/scrapy-pagination/all` | 查詢清單 (`getAll`) | 無 | `List<ScrapyPaginationTO>` |
| POST | `/api/scrapy-pagination/update` | 儲存或更新 (`update`) | `Body: req: ScrapyPaginationTO` | `void` |
| DELETE | `/api/scrapy-pagination/delete` | 刪除資料 (`delete`) | `Query: name: String` | `void` |
| POST | `/api/scrapy-pagination/test/html` | 測試設定 (`testHtml`) | `Body: req: ScrapyPaginationTestTO` | `Map<String, Object>` |
| GET | `/api/scrapy-pagination/update-redirect-data` | 儲存或更新 (`updateRedirectData`) | `Query: name: String` | `ScrapyPaginationTO` |

<a id="share"></a>

## 共用標籤

### ShareTagController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/share/ShareTagController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/share-tag/all` | 查詢清單 (`getAllTag`) | 無 | `List<ShareTagTO>` |
| POST | `/api/share-tag/add` | 新增資料 (`addTag`) | `Body: req: ShareTagTO` | `void` |
| DELETE | `/api/share-tag/delete` | 刪除資料 (`deleteTag`) | `Query: shareTagId: String` | `void` |
| GET | `/api/share-tag/in-use` | 查詢資料 (`getThemeHeaderIdsByShareTagId`) | `Query: shareTagId: String` | `List<String>` |
| POST | `/api/share-tag/value/by-ids` | 查詢資料 (`getShareTagValues`) | `Body: shareTagIds: List<String>` | `List<ShareTagValueTO>` |
| GET | `/api/share-tag/value/id` | 查詢資料 (`getShareTagValueById`) | `Query: shareTagId: String` | `List<ShareTagValueTO>` |
| POST | `/api/share-tag/value/add` | 新增資料 (`addShareTagValue`) | `Body: req: ShareTagValueTO` | `void` |
| DELETE | `/api/share-tag/value/delete` | 刪除資料 (`deleteShareTagValue`) | `Query: shareTagId: String`<br>`Query: value: String` | `void` |
| DELETE | `/api/share-tag/value/delete-list` | 刪除資料 (`deleteShareTagValueList`) | `Body: req: ShareTagValueDeleteListTO` | `void` |

<a id="spider"></a>

## Spider 爬蟲與瀏覽器驗證

### BrowserVerificationController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/BrowserVerificationController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| POST | `/api/spider/browser-verification` | 1. 開啟驗證瀏覽器 (`start`) | `Body: request: StartRequest` | `Session` |
| GET | `/api/spider/browser-verification/{sessionId}/status` | 2. 查詢瀏覽器驗證狀態 (`status`) | `Path: sessionId: String` | `Status` |
| POST | `/api/spider/browser-verification/{sessionId}/test-jsoup` | 3. 獨立測試 Jsoup (`testJsoup`) | `Path: sessionId: String` | `Status` |
| POST | `/api/spider/browser-verification/{sessionId}/complete` | 4. 確認驗證並儲存 (`complete`) | `Path: sessionId: String`<br>`Body: request: CompleteRequest` | `Saved` |
| DELETE | `/api/spider/browser-verification/{sessionId}` | 5. 取消驗證 (`cancel`) | `Path: sessionId: String` | `void` |

### SpiderConfigController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderConfigController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/spider-config/all` | 查詢清單 (`getAllConfig`) | 無 | `List<SpiderConfigTO>` |
| GET | `/api/spider-config` | 查詢資料 (`getConfig`) | `Query: spiderId: String` | `SpiderConfigTO` |
| POST | `/api/spider-config/update` | 儲存或更新 (`updateConfig`) | `Body: req: SpiderConfigTO` | `void` |
| DELETE | `/api/spider-config/delete` | 刪除資料 (`deleteConfig`) | `Query: spiderId: String` | `void` |
| GET | `/api/spider-config/in-use` | 檢查使用狀態 (`isConfigInUse`) | `Query: spiderId: String` | `boolean` |

### SpiderController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| POST | `/api/spider/execute-pagination` | 執行爬取 (`executePagination`) | `Query: spiderPaginationId: String`<br>`Query: force: boolean，預設 false` | `List<SpiderPaginationDataTO>` |
| POST | `/api/spider/use-url` | 執行爬取 (`executeByUrl`) | `Body: req: SpiderReqTO` | `String` |
| POST | `/api/spider/use-prime-key` | 執行爬取 (`executeByPrimeKeyList`) | `Body: req: SpiderReqTO` | `String` |
| POST | `/api/spider/preview-extraction` | 預覽擷取結果 (`previewExtraction`) | `Body: req: SpiderItemTO` | `String` |
| POST | `/api/spider/preview-extraction-with-file` | 預覽擷取結果 (`previewExtractionWithFile`) | `Part: file: MultipartFile`<br>`Part: setting: SpiderItemTO` | `String` |

### SpiderItemController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderItemController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/spider-item/all` | 查詢清單 (`getAllItem`) | 無 | `List<SpiderItemTO>` |
| GET | `/api/spider-item` | 查詢資料 (`getItem`) | `Query: spiderItemId: String` | `SpiderItemTO` |
| GET | `/api/spider-item/by-spider-id` | 查詢資料 (`getItemBySpiderId`) | `Query: spiderId: String` | `List<SpiderItemTO>` |
| POST | `/api/spider-item/update` | 儲存或更新 (`updateItem`) | `Body: req: SpiderItemTO` | `void` |
| DELETE | `/api/spider-item/delete` | 刪除資料 (`deleteItem`) | `Query: spiderItemId: String` | `void` |
| GET | `/api/spider-item/in-use` | 檢查使用狀態 (`isItemInUse`) | `Query: spiderItemId: String` | `boolean` |
| POST | `/api/spider-item/by-id-list` | 查詢資料 (`getItemByIdList`) | `Body: spiderItemIdList: List<String>` | `List<SpiderItemTO>` |

### SpiderMappingController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderMappingController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/spider-mapping/all` | 查詢清單 (`getAllMapping`) | 無 | `List<SpiderMappingTO>` |
| GET | `/api/spider-mapping/by-spider-id` | 查詢資料 (`getMappingBySpiderId`) | `Query: spiderId: String` | `List<SpiderMappingTO>` |
| GET | `/api/spider-mapping` | 查詢資料 (`getMapping`) | `Query: spiderId: String`<br>`Query: executionOrder: Integer` | `SpiderMappingTO` |
| POST | `/api/spider-mapping/update` | 儲存或更新 (`updateMapping`) | `Body: req: SpiderMappingTO` | `void` |
| POST | `/api/spider-mapping/update-list` | 儲存或更新 (`updateMappingList`) | `Body: req: List<SpiderMappingTO>` | `void` |
| DELETE | `/api/spider-mapping/delete` | 刪除資料 (`deleteMapping`) | `Query: spiderId: String`<br>`Query: executionOrder: Integer`<br>`Query: spiderItemId: String` | `void` |
| DELETE | `/api/spider-mapping/delete-by-spider-id` | 刪除資料 (`deleteMappingBySpiderId`) | `Query: spiderId: String` | `void` |
| GET | `/api/spider-mapping/in-use-by-spider-item-id` | 檢查使用狀態 (`inUseBySpiderItemId`) | `Query: spiderItemId: String` | `boolean` |

### SpiderPaginationController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderPaginationController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/spider-pagination/all` | 查詢清單 (`getAll`) | 無 | `List<SpiderPaginationTO>` |
| GET | `/api/spider-pagination` | 查詢資料 (`getById`) | `Query: spiderPaginationId: String` | `SpiderPaginationTO` |
| POST | `/api/spider-pagination/update` | 儲存或更新 (`update`) | `Body: req: SpiderPaginationTO` | `void` |
| DELETE | `/api/spider-pagination/delete` | 刪除資料 (`delete`) | `Query: spiderPaginationId: String` | `void` |
| GET | `/api/spider-pagination/in-use` | 檢查使用狀態 (`inUse`) | `Query: spiderPaginationId: String` | `boolean` |

### SpiderPaginationDataController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/spider/SpiderPaginationDataController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/spider-pagination-data/all` | 查詢清單 (`getAll`) | 無 | `List<SpiderPaginationDataTO>` |
| GET | `/api/spider-pagination-data/by-spider-pagination-id` | 查詢資料 (`getBySpiderPaginationId`) | `Query: spiderPaginationId: String` | `List<SpiderPaginationDataTO>` |
| GET | `/api/spider-pagination-data` | 查詢資料 (`getById`) | `Query: spiderPaginationId: String`<br>`Query: groupId: String`<br>`Query: dataId: String` | `SpiderPaginationDataTO` |
| POST | `/api/spider-pagination-data/update` | 儲存或更新 (`update`) | `Body: req: SpiderPaginationDataTO` | `void` |
| DELETE | `/api/spider-pagination-data/delete` | 刪除資料 (`delete`) | `Query: spiderPaginationId: String`<br>`Query: groupId: String`<br>`Query: dataId: String` | `void` |
| DELETE | `/api/spider-pagination-data/delete-by-spider-pagination-id` | 刪除資料 (`deleteBySpiderPaginationId`) | `Query: spiderPaginationId: String` | `void` |
| GET | `/api/spider-pagination-data/in-use` | 檢查使用狀態 (`inUse`) | `Query: spiderPaginationId: String`<br>`Query: groupId: String`<br>`Query: dataId: String` | `boolean` |

<a id="theme"></a>

## 主題

### ThemeController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/theme/ThemeController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/theme/all` | 查詢清單 (`getAllTheme`) | 無 | `List<ThemeHeaderTO>` |
| GET | `/api/theme/id` | 查詢資料 (`getByHeaderId`) | `Query: headerId: String` | `ThemeHeaderTO` |
| POST | `/api/theme/one` | 查詢資料 (`findTheme`) | `Body: headerTO: ThemeHeaderTO` | `ThemeHeaderTO` |
| POST | `/api/theme/exist` | 檢查是否存在 (`existTheme`) | `Body: headerTO: ThemeHeaderTO` | `boolean` |
| POST | `/api/theme/update` | 儲存或更新 (`updateTheme`) | `Body: theme: ThemeHeaderTO` | `void` |
| POST | `/api/theme/delete` | 刪除資料 (`deleteTheme`) | `Body: headerTO: ThemeHeaderTO` | `void` |
| POST | `/api/theme/copy` | 複製資料 (`copyTheme`) | `Body: copyThemeRequest: CopyThemeReqTO` | `void` |
| POST | `/api/theme/custom/value` | 查詢資料 (`findCustomValue`) | `Body: request: ThemeCustomValueReqTO` | `Map<String, Map<String, String>>` |
| POST | `/api/theme/custom/update` | 儲存或更新 (`updateCustomValue`) | `Body: customValueTO: ThemeCustomValueTO` | `void` |
| POST | `/api/theme/tag/update` | 儲存或更新 (`updateTagValue`) | `Body: list: List<ThemeTagValueTO>` | `void` |
| POST | `/api/theme/tag/update/single` | 儲存或更新 (`updateSingleTagValue`) | `Body: to: ThemeTagValueTO` | `void` |
| GET | `/api/theme/tag/value` | 查詢資料 (`getTagValueList`) | `Query: headerId: String` | `List<ThemeTagValueTO>` |
| GET | `/api/theme/top-custom/value` | 查詢資料 (`findTopCustomValue`) | `Query: headerId: String` | `Map<String, String>` |
| POST | `/api/theme/top-custom/update` | 儲存或更新 (`updateTopCustomValue`) | `Body: topCustomValueTO: ThemeTopCustomValueTO` | `void` |

### ThemeHiddenController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/theme/ThemeHiddenController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/theme-hidden/all` | 查詢清單 (`getAll`) | 無 | `List<ThemeHiddenTO>` |
| POST | `/api/theme-hidden/save` | 儲存或更新 (`save`) | `Body: req: ThemeHiddenTO` | `void` |
| DELETE | `/api/theme-hidden/delete` | 刪除資料 (`delete`) | `Query: headerId: String` | `void` |

### ThemeItemController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/theme/ThemeItemController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/theme/item` | 查詢資料 (`getThemeItemById`) | `Query: type: Global.ThemeItemType`<br>`Query: itemId: String` | `ThemeItemTO` |
| GET | `/api/theme/item/all` | 查詢清單 (`getAllThemeItem`) | `Query: type: Global.ThemeItemType` | `List<ThemeItemTO>` |
| GET | `/api/theme/item/by-type` | 查詢資料 (`getThemeItemByType`) | `Query: type: Global.ThemeItemType` | `List<ThemeItemTO>` |
| GET | `/api/theme/item/header-id` | 查詢資料 (`getItemsByHeaderId`) | `Query: headerId: String` | `List<ThemeItemTO>` |
| POST | `/api/theme/item/update` | 儲存或更新 (`updateThemeItem`) | `Body: req: ThemeItemTO` | `void` |
| POST | `/api/theme/item/copy` | 複製資料 (`copyThemeItem`) | `Body: req: CopyThemeItemReqTO` | `void` |
| DELETE | `/api/theme/item/delete` | 刪除資料 (`deleteThemeItem`) | `Query: type: Global.ThemeItemType`<br>`Query: itemId: String` | `void` |

### ThemeItemMapController

[Controller 原始碼](../src/main/java/com/lsb/listProjectBackend/controller/theme/ThemeItemMapController.java)

| 方法 | 路徑 | 用途／處理方法 | 輸入參數 | 回傳型別 |
| --- | --- | --- | --- | --- |
| GET | `/api/theme/item/map/by-type` | 查詢資料 (`getThemeItemMapByType`) | `Query: type: Global.ThemeItemType` | `List<ThemeItemMapTO>` |
| POST | `/api/theme/item/map/update` | 儲存或更新 (`updateThemeItemMap`) | `Body: req: ThemeItemMapTO` | `void` |
| DELETE | `/api/theme/item/map/delete` | 刪除資料 (`deleteThemeItemMap`) | `Query: type: Global.ThemeItemType`<br>`Query: headerId: String` | `void` |
| GET | `/api/theme/item/map/in-use` | 檢查使用狀態 (`themeItemMapInUse`) | `Query: type: Global.ThemeItemType`<br>`Query: itemId: String` | `boolean` |

## 特殊端點與回應注意事項

- `POST /api/spider/execute-pagination` 的 `spiderPaginationId` 與 `force` 均放在 Query，`force` 預設為 `false`。
- `POST /api/spider/preview-extraction-with-file` 使用 `multipart/form-data`，包含二進位 `file` 與 JSON `setting`（`SpiderItemTO`，此 part 使用 `application/json`）。
- `POST /api/api-config/singalCallApi` 保留現有拼字；本文為 `{"apiId":"example","json":{"id":"123"}}`。`json` 提供儲存設定的模板代入資料；詳見 [API request 設定](api-request-config.md)。
- `POST /api/api-config/delete` 仍以 JSON Body 傳入設定物件；並非 DELETE 或 Query 參數。
- `GET /api/dataset/refresh`、`GET /api/group-dataset/refresh`、`GET /api/disk/refresh` 與 `GET /api/scrapy-pagination/update-redirect-data` 具有更新行為；請依實際用途呼叫。
- 檔案移動、開啟目錄與瀏覽器視窗操作發生在後端電腦。`/api/file/delete` 嘗試移至垃圾桶；非本地 UNC 路徑會被拒絕。
- `GET /api/proxy-image` 成功回傳圖片位元組，Content-Type 為 `image/jpeg`，快取一天；失敗回傳 HTTP 400。
- `GET /api/sys/is-alive` 成功為 HTTP 200；資料庫不可用為 503。`POST /api/sys/refresh-dynamic-datasource` 失敗為 500。
- `LsbException` 由全域處理器依例外中的狀態碼回傳文字訊息；其他錯誤未在此宣告為固定格式。
- 瀏覽器驗證須啟用 `SPIDER_BROWSER_VERIFICATION_ENABLED=true`。依序開啟 session、查詢 status、測試 Jsoup、complete 儲存，或 DELETE 取消。`test-jsoup` 回傳 HTTP 200 不代表驗證成功，需讀取結果狀態；詳見 [瀏覽器驗證](browser-verification.md)。

## 呼叫範例

```powershell
# 查詢設定是否存在
Invoke-RestMethod -Method Get -Uri 'http://localhost:8081/list-project-backend/api/api-config/exist?apiId=example'

# 依 ID 清單查詢，Body 為 JSON 陣列
Invoke-RestMethod -Method Post -Uri 'http://localhost:8081/list-project-backend/api/api-config/all/id' -ContentType 'application/json' -Body '["example","another"]'

# 分頁爬蟲，參數放在 Query
Invoke-RestMethod -Method Post -Uri 'http://localhost:8081/list-project-backend/api/spider/execute-pagination?spiderPaginationId=example&force=false'
```

## DTO 與列舉型別索引

下列連結提供請求／回應物件欄位與列舉定義。巢狀型別 `StartRequest`、`CompleteRequest`、`Session`、`Status`、`Saved` 定義於 `BrowserVerificationTO.java`。Java 型別不等同完整 JSON 契約；序列化註解與 Service 行為仍需一併參照。

- [ApiRequestConfigTO](../src/main/java/com/lsb/listProjectBackend/domain/connection/ApiRequestConfigTO.java)
- [BrowserVerificationTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/BrowserVerificationTO.java)
- [ConnectionTestReqTO](../src/main/java/com/lsb/listProjectBackend/domain/connection/ConnectionTestReqTO.java)
- [ConnectionTestResultTO](../src/main/java/com/lsb/listProjectBackend/domain/connection/ConnectionTestResultTO.java)
- [CookieListMapTO](../src/main/java/com/lsb/listProjectBackend/domain/common/CookieListMapTO.java)
- [CookieListTO](../src/main/java/com/lsb/listProjectBackend/domain/common/CookieListTO.java)
- [CopyThemeItemReqTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/CopyThemeItemReqTO.java)
- [CopyThemeReqTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/CopyThemeReqTO.java)
- [DatabaseConfigTO](../src/main/java/com/lsb/listProjectBackend/domain/connection/DatabaseConfigTO.java)
- [DatasetDataTO](../src/main/java/com/lsb/listProjectBackend/domain/dataset/DatasetDataTO.java)
- [DatasetQuickRefreshTO](../src/main/java/com/lsb/listProjectBackend/domain/dataset/DatasetQuickRefreshTO.java)
- [DatasetTO](../src/main/java/com/lsb/listProjectBackend/domain/dataset/DatasetTO.java)
- [DiskTO](../src/main/java/com/lsb/listProjectBackend/domain/file/DiskTO.java)
- [FileExistReqTO](../src/main/java/com/lsb/listProjectBackend/domain/file/FileExistReqTO.java)
- [FileReqTO](../src/main/java/com/lsb/listProjectBackend/domain/file/FileReqTO.java)
- [GroupDatasetDataTO](../src/main/java/com/lsb/listProjectBackend/domain/dataset/GroupDatasetDataTO.java)
- [GroupDatasetTO](../src/main/java/com/lsb/listProjectBackend/domain/dataset/GroupDatasetTO.java)
- [HeadersMapTO](../src/main/java/com/lsb/listProjectBackend/domain/common/HeadersMapTO.java)
- [HeadersTO](../src/main/java/com/lsb/listProjectBackend/domain/common/HeadersTO.java)
- [ReplaceValueMapTO](../src/main/java/com/lsb/listProjectBackend/domain/common/ReplaceValueMapTO.java)
- [ScrapyConfigTO](../src/main/java/com/lsb/listProjectBackend/domain/scrapy/ScrapyConfigTO.java)
- [ScrapyPaginationTestTO](../src/main/java/com/lsb/listProjectBackend/domain/scrapy/ScrapyPaginationTestTO.java)
- [ScrapyPaginationTO](../src/main/java/com/lsb/listProjectBackend/domain/scrapy/ScrapyPaginationTO.java)
- [ScrapyReqTO](../src/main/java/com/lsb/listProjectBackend/domain/scrapy/ScrapyReqTO.java)
- [ScrapyTestTO](../src/main/java/com/lsb/listProjectBackend/domain/scrapy/ScrapyTestTO.java)
- [SettingTO](../src/main/java/com/lsb/listProjectBackend/domain/common/SettingTO.java)
- [ShareTagTO](../src/main/java/com/lsb/listProjectBackend/domain/share/ShareTagTO.java)
- [ShareTagValueDeleteListTO](../src/main/java/com/lsb/listProjectBackend/domain/share/ShareTagValueDeleteListTO.java)
- [ShareTagValueTO](../src/main/java/com/lsb/listProjectBackend/domain/share/ShareTagValueTO.java)
- [SingalCallApiReqTO](../src/main/java/com/lsb/listProjectBackend/domain/connection/SingalCallApiReqTO.java)
- [SpiderConfigTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderConfigTO.java)
- [SpiderItemTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderItemTO.java)
- [SpiderMappingTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderMappingTO.java)
- [SpiderPaginationDataTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderPaginationDataTO.java)
- [SpiderPaginationTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderPaginationTO.java)
- [SpiderReqTO](../src/main/java/com/lsb/listProjectBackend/domain/spider/SpiderReqTO.java)
- [ThemeCustomValueReqTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeCustomValueReqTO.java)
- [ThemeCustomValueTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeCustomValueTO.java)
- [ThemeHeaderTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeHeaderTO.java)
- [ThemeHiddenTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeHiddenTO.java)
- [ThemeItemMapTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeItemMapTO.java)
- [ThemeItemTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeItemTO.java)
- [ThemeTagValueTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeTagValueTO.java)
- [ThemeTopCustomValueTO](../src/main/java/com/lsb/listProjectBackend/domain/theme/ThemeTopCustomValueTO.java)
- [Global 列舉定義](../src/main/java/com/lsb/listProjectBackend/utils/Global.java)

## OpenAPI 與維護方式

服務啟動後，可透過 `/list-project-backend/swagger-ui.html` 查看互動文件，或取得 `/list-project-backend/v3/api-docs`、`/list-project-backend/v3/api-docs.yaml`。使用方式見 [OpenAPI 說明](openapi.md)。新增或修改 Controller 路由、參數及回傳型別時，請同步更新本文件；本清單以目前原始碼為準，未實際執行每個 API。
