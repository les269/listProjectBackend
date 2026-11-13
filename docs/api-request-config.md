# API request configuration

`ApiRequestConfigTO` and `ApiRequestConfig` now use the frontend `ApiRequestConfig` shape:

```json
{
  "apiId": "example",
  "description": "Example request",
  "config": {
    "httpMethod": "post",
    "endpointUrl": "https://example.com/items",
    "requestBody": "{}",
    "httpParams": [{"seq": 1, "name": "flag", "value": "", "enabled": true}],
    "bodyType": "json",
    "bodyFormFields": [],
    "timeoutMs": 5000,
    "enabled": true,
    "successMessage": "Done"
  },
  "createdTime": "2026-11-09T10:00:00",
  "updatedTime": "2026-11-10T12:30:00"
}
```

`createdTime` and `updatedTime` are optional `LocalDateTime` fields serialized as ISO date-time strings,
consistent with the other backend configurations. Hibernate generates the creation time on insert
and the update time on insert/update; creation time is not updated. The old `date` field is removed.
Optional settings remain optional, with no editor defaults imposed by the backend.
Both `httpParams` and `bodyFormFields` use `ApiRequestQueryParam` entries with
`seq: number` (Java `int`), `name: string`, `value: string`, and `enabled: boolean`.
`seq` is stored and returned; requests retain the supplied array order rather than
sorting by `seq`. Older entries without `seq` deserialize with a value of `0`.
There is no `hasEquals`
field or separate form-entry type. The backend models the declared settings fields;
unknown extension properties are not preserved.
`bodyType` accepts `none`, `form-data`, `x-www-form-urlencoded`, `text`,
`javascript`, `json`, `html`, and `xml`.

| Method | Path | Input |
| --- | --- | --- |
| GET | `/api/api-config/exist?apiId=example` | API ID; returns `true` if the configuration exists, otherwise `false` |
| GET | `/api/api-config/all` | None |
| GET | `/api/api-config/id?apiId=example` | API ID |
| POST | `/api/api-config/all/id` | Array of API IDs |
| POST | `/api/api-config/update` | Configuration object |
| POST | `/api/api-config/singalCallApi` | `{ "apiId": "example", "json": { "id": "123" } }`; loads stored settings and resolves `{{$.id}}` placeholders using `json` |
| POST | `/api/api-config/delete` | Configuration object; identifies the row using `apiId` |

The previous `/name` and `/all/name` routes have been replaced with the ID routes.
Java classes use the `ApiRequestConfig` name. The `/api/api-config` prefix is retained.

`singalCallApi` uses the saved URL, method, query parameters, timeout, and enabled
flag. `json` is external template data, not the request body. The existing
`JsonUtils.replaceValueByJsonPath` mechanism resolves placeholders in the URL,
request body, query names/values, form names/values, and success message. Body type
is retained, and templates without placeholders remain unchanged. Embedded URL
replacements are percent encoded; query/form values use their normal encoding
after substitution. Missing/null paths retain their placeholders, consistent with
the shared utility. Stored settings are not modified. Unknown API IDs return HTTP 404.

For example, saved URL `https://example.com/items/{{$.id}}` and body
`{"name":"{{$.name}}"}` use `json: {"id":"123","name":"example"}` to produce
URL `https://example.com/items/123` and body `{"name":"example"}`. Body substitution
is textual, matching the shared utility; JSON templates must produce valid JSON.

SQLite and PostgreSQL initialization scripts create `api_request_config` with
`api_id`, `description`, `config`, `created_time`, and `updated_time`. JSON columns use TEXT in
SQLite and JSONB in PostgreSQL. Existing `api_config` tables are left untouched;
old records are not migrated because the old configuration is currently unused.

For an existing `api_request_config` table, add the two timestamp columns before using
the updated application (`CREATE TABLE IF NOT EXISTS` does not alter existing tables).
For PostgreSQL:

```sql
ALTER TABLE api_request_config ADD COLUMN created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE api_request_config ADD COLUMN updated_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
```

For SQLite, add nullable columns and backfill existing rows in one transaction;
Hibernate supplies timestamps for subsequent application writes:

```sql
BEGIN;
ALTER TABLE api_request_config ADD COLUMN created_time TIMESTAMP;
ALTER TABLE api_request_config ADD COLUMN updated_time TIMESTAMP;
UPDATE api_request_config SET created_time = CURRENT_TIMESTAMP, updated_time = CURRENT_TIMESTAMP;
COMMIT;
```

These migration statements are for tables that do not yet have the new columns.
The unused `request_date` column may remain in existing tables.

`callApi` executes the nested `config` settings:

- `enabled: false` skips the request and returns JSON null; omitted means enabled.
- `httpParams` replaces existing URL parameters with the same decoded name using
  enabled entries. Unrelated URL parameters keep their original encoding; disabled
  entries do not replace URL parameters. Explicit duplicates within `httpParams`
  retain their order. Names and values are UTF-8 percent encoded and always sent as
  `name=value`, including `name=` when the value is empty.
- `bodyType` supports no body, multipart text fields, URL-encoded fields, and raw
  text/JavaScript/JSON/HTML/XML with the matching content type. Form bodies use enabled
  `bodyFormFields` and preserve duplicates. Raw bodies are sent unchanged as UTF-8.
  When `bodyType` is omitted, a nonblank `requestBody` uses JSON for compatibility;
  otherwise no body is sent. Multipart fields represent text values, not file uploads.
  GET requests use query parameters and do not send a body.
- `timeoutMs` sets both connection and read timeouts for that request. Omitted uses
  the HTTP client's defaults; zero means no timeout. Values must be between zero and
  `Integer.MAX_VALUE`. This is not a total end-to-end execution deadline.
- GET, POST, PUT, and DELETE preserve the response JSON type: object, array,
  string, boolean, number, or null. Empty responses (including 204) return JSON null.
  Explicit non-JSON content types such as `text/plain` return a string value.
  The single-call endpoint returns that JSON value directly. The dataset caller
  separately accepts objects (as one row) or arrays of objects; scalar and mixed
  array responses cannot be stored as dataset rows. Null produces no dataset rows.
  HTTP errors, timeouts, and incompatible JSON responses propagate to the caller.
- `successMessage` is logged after a successful request and response parsing. It does
  not replace the returned dataset data.
