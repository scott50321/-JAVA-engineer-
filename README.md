# Coindesk API 轉換與幣別維護

國泰世華 JAVA engineer 線上作業。

## 環境

Maven、JDK 8、Spring Boot 2.7.18（Boot 3 需要 JDK 17，所以鎖 2.7.x）、H2 in-memory、Spring Data JPA。

## 怎麼跑

```bash
mvn spring-boot:run
```

啟動後 API base 是 http://localhost:8080，H2 Console 在 http://localhost:8080/h2-console
（JDBC URL `jdbc:h2:mem:coindesk`、使用者 `sa`、密碼空白）。

跑測試：

```bash
mvn test
```

## 資料表

建表 SQL 在 `src/main/resources/schema.sql`，測試資料初始化 SQL 在
`src/main/resources/data.sql`（預先灌了 USD／GBP／EUR 三筆）。

主鍵用自增 BIGINT，幣別代碼 `code` 用 unique 約束當業務唯一鍵。這是因為 Spring Data JPA
的 `save()` 是依 `@Id` 是否為 `null` 來決定要 `persist` 還是 `merge`，如果把 `code` 當主鍵，
新增時 id 不為 null 會走 merge（先 SELECT 再 INSERT）。

## API

幣別維護（實作內容 1）：

- `GET /api/currencies` 查詢全部
- `GET /api/currencies/{id}` 查詢單筆
- `POST /api/currencies` 新增
- `PUT /api/currencies/{id}` 修改
- `DELETE /api/currencies/{id}` 刪除

```bash
curl http://localhost:8080/api/currencies
curl -X POST http://localhost:8080/api/currencies \
     -H "Content-Type: application/json" \
     -d '{"code":"JPY","chineseName":"日圓"}'
```

coindesk（實作內容 2、3）：

- `GET /api/coindesk/raw` 呼叫 coindesk 原始 API
- `GET /api/coindesk/transformed` 轉換後的新 API

轉換後的輸出長這樣：

```json
{
  "updateTime": "2024/09/02 07:07:20",
  "currencies": [
    { "code": "EUR", "chineseName": "歐元", "rate": 52243.287 },
    { "code": "GBP", "chineseName": "英鎊", "rate": 43984.02 },
    { "code": "USD", "chineseName": "美元", "rate": 57756.298 }
  ]
}
```

## 實作上的幾個決定

`bpi` 用 `Map<String, Bpi>` 而不是三個固定欄位，這樣對方多回一個幣別時不用改程式碼。

匯率用 `rate` 字串解析成 `BigDecimal`，不用 `rate_float`。`rate_float` 是 `double`，本身
儲存就不準確，`new BigDecimal(double)` 只會把那個不準的值如實展開，沒有解決精度問題。
解析時先去掉千分位逗號：`new BigDecimal(rate.replace(",", ""))`，比較大小一律用
`compareTo`（`equals` 會連小數位數一起比）。

更新時間不做時區轉換，來源 `updatedISO` 帶 `+00:00` 就照它的時間輸出成
`yyyy/MM/dd HH:mm:ss`，題目沒有要求轉成本地時間。

DB 查不到對應中文名稱時，用幣別代碼當 fallback，避免外部資料讓自己的 API 失敗。

轉換邏輯抽成獨立的 `CoindeskTransformer`，不碰 DB、不碰網路，所以可以用純 JUnit 做單元測試。

外部呼叫設了 timeout（connect 3 秒 / read 5 秒），不設的話對方卡住，自己的執行緒也會跟著卡住。

## 測試

- 資料轉換邏輯單元測試：`CoindeskTransformerTest`（純 JUnit，不起 Spring）
- 幣別 CRUD API 測試並顯示內容：`CurrencyApiTest`
- 呼叫 coindesk API 測試並顯示內容：`CoindeskApiTest#shouldCallCoindeskApi`
- 轉換 API 測試並顯示內容：`CoindeskApiTest#shouldCallTransformedApi`

測試 3、4 會實際連到 `https://kengp3.github.io/blog/coindesk.json`，需要網路。
