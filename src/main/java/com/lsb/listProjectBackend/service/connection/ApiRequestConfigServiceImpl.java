package com.lsb.listProjectBackend.service.connection;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.ObjectMapper;
import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.connection.ApiRequestConfigTO;
import com.lsb.listProjectBackend.domain.connection.ApiRequestSettings;
import com.lsb.listProjectBackend.domain.connection.SingalCallApiReqTO;
import com.lsb.listProjectBackend.utils.Global.ApiRequestBodyType;
import com.lsb.listProjectBackend.utils.Global.HttpMethodType;
import com.lsb.listProjectBackend.utils.JsonUtils;
import com.lsb.listProjectBackend.entity.dynamic.common.ApiRequestConfig;
import com.lsb.listProjectBackend.mapper.connection.ApiRequestConfigMapper;
import com.lsb.listProjectBackend.repository.dynamic.connection.ApiRequestConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class ApiRequestConfigServiceImpl implements ApiRequestConfigService {
    private final ApiRequestConfigRepository apiRequestConfigRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ApiRequestConfigMapper apiRequestConfigMapper;

    @Override
    public boolean isExist(String apiId) {
        return apiRequestConfigRepository.existsById(apiId);
    }

    public List<ApiRequestConfigTO> getAll() {
        return apiRequestConfigMapper.toDomainList(apiRequestConfigRepository.findAll());
    }

    public List<ApiRequestConfigTO> getListById(List<String> apiIds) {
        return apiRequestConfigMapper.toDomainList(apiRequestConfigRepository.findAllById(apiIds));
    }

    public ApiRequestConfigTO getById(String apiId) {
        return apiRequestConfigMapper.toDomain(
                apiRequestConfigRepository.findById(apiId).orElse(null));
    }

    public void update(ApiRequestConfigTO apiRequestConfigTO) {
        ApiRequestConfig apiRequestConfig = apiRequestConfigMapper.toEntity(apiRequestConfigTO);
        this.apiRequestConfigRepository.save(apiRequestConfig);
    }

    public void delete(ApiRequestConfigTO apiRequestConfigTO) {
        this.apiRequestConfigRepository.deleteById(apiRequestConfigTO.apiId());
    }

    @Override
    public JsonNode singalCallApi(SingalCallApiReqTO request) throws Exception {
        // 依 API ID 取得已儲存的設定；找不到時回傳 HTTP 404。
        var to = getById(request.apiId());
        if (to == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "API configuration not found: " + request.apiId());
        }
        // 直接取得已具備 ApiRequestSettings 型別的設定，供後續替換欄位使用。
        var settings = to.config();
        // 將傳入的 JSON 轉成一般 Java 物件，作為 JSONPath 樣板取值的資料來源。
        Object params = objectMapper.convertValue(request.json(), Object.class);
        // HttpParams 已與 URL 同步，直接替換 URL 中的 JSONPath 樣板。
        settings.setEndpointUrl(JsonUtils.replaceValueByJsonPath(settings.getEndpointUrl(), params));
        // 將請求本文與成功訊息中的 JSONPath 樣板替換成來源資料的值。
        settings.setRequestBody(JsonUtils.replaceValueByJsonPath(settings.getRequestBody(), params));
        settings.setSuccessMessage(JsonUtils.replaceValueByJsonPath(settings.getSuccessMessage(), params));
        // 逐一替換表單欄位的名稱與值，略過空項目；是否啟用由組裝請求本文時判斷。
        if (settings.getBodyFormFields() != null) {
            for (var formField : settings.getBodyFormFields()) {
                if (formField != null) {
                    formField.setName(JsonUtils.replaceValueByJsonPath(formField.getName(), params));
                    formField.setValue(JsonUtils.replaceValueByJsonPath(formField.getValue(), params));
                }
            }
        }
        // 保留原設定的識別資訊與時間，搭配替換後的設定呼叫 API，並回傳解析後的 JSON 節點。
        return callApi(to);
    }

    /** 依設定發送 HTTP 請求，將回應轉成 JSON 節點並記錄執行結果。 */
    private JsonNode callApi(ApiRequestConfigTO apiRequestConfig) throws Exception {
        try {
            // 檢查 API 設定物件及其請求設定不可為 null。
            Objects.requireNonNull(apiRequestConfig, "API configuration is required");
            var settings = Objects.requireNonNull(apiRequestConfig.config(), "API settings are required");
            // 明確停用時略過呼叫，回傳 JSON null；未指定 enabled 時仍會執行。
            if (Boolean.FALSE.equals(settings.getEnabled())) {
                return NullNode.getInstance();
            }
            // 確認 HTTP 方法存在，並建立可設定連線與讀取逾時的請求工廠。
            Objects.requireNonNull(settings.getHttpMethod(), "HTTP method is required");
            var factory = new SimpleClientHttpRequestFactory();
            // 有指定逾時毫秒數時，先驗證可轉成 int，再同時套用至連線與讀取。
            if (settings.getTimeoutMs() != null) {
                long timeout = settings.getTimeoutMs();
                if (timeout < 0 || timeout > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("timeoutMs must be between 0 and " + Integer.MAX_VALUE);
                }
                factory.setConnectTimeout((int) timeout);
                factory.setReadTimeout((int) timeout);
            }
            // 直接使用已同步參數的 URL 發送請求，並以字串接收回應。
            var client = new RestTemplate(factory);
            var response = client.exchange(
                    settings.getEndpointUrl(),
                    HttpMethod.valueOf(settings.getHttpMethod().name().toUpperCase(java.util.Locale.ROOT)),
                    requestEntity(settings), String.class);
            // 依回應的 Content-Type 解析本文，統一轉為 JsonNode。
            JsonNode result = parseResponse(response.getBody(), response.getHeaders().getContentType());
            // 若有非空白的成功訊息，連同 API ID 寫入日誌，再回傳解析結果。
            if (settings.getSuccessMessage() != null && !settings.getSuccessMessage().isBlank()) {
                log.info("API {}: {}", apiRequestConfig.apiId(), settings.getSuccessMessage());
            }
            return result;
        } catch (HttpStatusCodeException e) {
            // HTTP 錯誤狀態：記錄原始例外並向上拋出，保留狀態碼與回應內容。
            log.error("HTTP error", e);
            throw e;
        } catch (Exception e) {
            // 其他錯誤（例如設定、連線或解析失敗）：記錄後向上拋出，由呼叫端處理。
            log.error("Failed to call API", e);
            throw e;
        }
    }

    /** 依回應內容與媒體類型，回傳 JSON null、文字節點或解析後的 JSON。 */
    private JsonNode parseResponse(String response, MediaType contentType) {
        // 無本文或只有空白時，統一回傳 JSON null。
        if (response == null || response.isBlank()) {
            return NullNode.getInstance();
        }
        // 明確指定為非 JSON 類型時，保留整段文字；+json 類型仍視為 JSON。
        if (contentType != null && !MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                && !contentType.getSubtype().endsWith("+json")) {
            return objectMapper.valueToTree(response);
        }
        // JSON 類型或未提供 Content-Type 時嘗試解析，格式錯誤則由解析器拋出例外。
        return objectMapper.readTree(response);
    }

    /** 依 HTTP 方法及本文類型，建立包含標頭與本文的請求實體。 */
    private HttpEntity<?> requestEntity(ApiRequestSettings settings) {
        // 建立標頭；GET 請求直接回傳不含本文的實體。
        var headers = new HttpHeaders();
        if (settings.getHttpMethod() == HttpMethodType.get) {
            return new HttpEntity<>(headers);
        }
        // 取得本文類型及原始本文。
        var type = settings.getBodyType();
        String raw = settings.getRequestBody();
        // 相容未指定 bodyType 的舊設定：本文為空白時使用 none，否則視為 JSON。
        if (type == null) {
            type = raw == null || raw.isBlank() ? ApiRequestBodyType.none : ApiRequestBodyType.json;
        }
        // none 類型不附加本文，也不主動設定 Content-Type。
        if (type == ApiRequestBodyType.none) {
            return new HttpEntity<>(headers);
        }
        // 表單類型使用多值映射，讓相同欄位名稱可對應多筆值。
        if (type == ApiRequestBodyType.formData || type == ApiRequestBodyType.formUrlEncoded) {
            var fields = new LinkedMultiValueMap<String, String>();
            if (settings.getBodyFormFields() != null) {
                for (var field : settings.getBodyFormFields()) {
                    // 僅加入啟用且非 null 的欄位；名稱必填，null 值改為空字串。
                    if (field != null && field.isEnabled()) {
                        fields.add(Objects.requireNonNull(field.getName(), "Form field name is required"),
                                field.getValue() == null ? "" : field.getValue());
                    }
                }
            }
            // 依表單類型設定 Content-Type，由 RestTemplate 的訊息轉換器序列化欄位。
            headers.setContentType(type == ApiRequestBodyType.formData
                    ? MediaType.MULTIPART_FORM_DATA
                    : MediaType.APPLICATION_FORM_URLENCODED);
            return new HttpEntity<>(fields, headers);
        }
        // 其餘類型直接使用原始本文，並對應至適當的媒體類型。
        String mediaType = switch (type) {
            case json -> "application/json";
            case javascript -> "application/javascript";
            case html -> "text/html";
            case xml -> "application/xml";
            default -> "text/plain";
        };
        // 指定 UTF-8 字元集；未提供本文時以空字串送出。
        headers.setContentType(MediaType.parseMediaType(mediaType + ";charset=UTF-8"));
        return new HttpEntity<>(raw == null ? "" : raw, headers);
    }
}
