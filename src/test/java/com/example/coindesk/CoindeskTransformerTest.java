package com.example.coindesk;

import com.example.coindesk.dto.CoindeskResponse;
import com.example.coindesk.dto.TransformedResponse;
import com.example.coindesk.service.CoindeskTransformer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 測試需求 1：針對資料轉換相關邏輯作單元測試。
 * 純 JUnit，不起 Spring、不連網路、不碰 DB，所以執行快而且結果穩定。
 */
class CoindeskTransformerTest {

    private final CoindeskTransformer transformer = new CoindeskTransformer();

    /** 組一份跟 coindesk 真實回應一樣結構的假資料 */
    private CoindeskResponse sampleResponse() {
        CoindeskResponse.Time time = new CoindeskResponse.Time();
        time.setUpdated("Sep 2, 2024 07:07:20 UTC");
        time.setUpdatedISO("2024-09-02T07:07:20+00:00");
        time.setUpdateduk("Sep 2, 2024 at 08:07 BST");

        Map<String, CoindeskResponse.Bpi> bpi = new LinkedHashMap<>();
        bpi.put("USD", bpi("USD", "57,756.298", 57756.2984));
        bpi.put("GBP", bpi("GBP", "43,984.02", 43984.0203));
        bpi.put("EUR", bpi("EUR", "52,243.287", 52243.2865));

        CoindeskResponse response = new CoindeskResponse();
        response.setTime(time);
        response.setChartName("Bitcoin");
        response.setBpi(bpi);
        return response;
    }

    private CoindeskResponse.Bpi bpi(String code, String rate, double rateFloat) {
        CoindeskResponse.Bpi b = new CoindeskResponse.Bpi();
        b.setCode(code);
        b.setRate(rate);
        b.setRateFloat(rateFloat);
        return b;
    }

    private Map<String, String> currencyNames() {
        Map<String, String> map = new HashMap<>();
        map.put("USD", "美元");
        map.put("GBP", "英鎊");
        map.put("EUR", "歐元");
        return map;
    }

    @Test
    @DisplayName("更新時間要轉成 yyyy/MM/dd HH:mm:ss")
    void shouldFormatUpdateTime() {
        TransformedResponse result = transformer.transform(sampleResponse(), currencyNames());

        System.out.println("更新時間 = " + result.getUpdateTime());
        assertEquals("2024/09/02 07:07:20", result.getUpdateTime());
    }

    @Test
    @DisplayName("幣別要帶出中文名稱，並依幣別代碼排序")
    void shouldMapChineseName() {
        TransformedResponse result = transformer.transform(sampleResponse(), currencyNames());

        result.getCurrencies().forEach(c ->
                System.out.println(c.getCode() + " / " + c.getChineseName() + " / " + c.getRate()));

        assertEquals(3, result.getCurrencies().size());
        assertEquals("EUR", result.getCurrencies().get(0).getCode());
        assertEquals("歐元", result.getCurrencies().get(0).getChineseName());
        assertEquals("美元", result.getCurrencies().get(2).getChineseName());
    }

    @Test
    @DisplayName("匯率要用 rate 字串解析成精確的 BigDecimal（不是用 rate_float）")
    void shouldParseRateExactly() {
        TransformedResponse result = transformer.transform(sampleResponse(), currencyNames());

        BigDecimal usdRate = result.getCurrencies().stream()
                .filter(c -> "USD".equals(c.getCode()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("找不到 USD"))
                .getRate();

        System.out.println("USD 匯率 = " + usdRate);

        // 用 compareTo 不用 equals：BigDecimal 的 equals 連 scale 一起比（1.0 不等於 1.00）
        assertEquals(0, new BigDecimal("57756.298").compareTo(usdRate));
        // 千分位逗號要被去掉
        assertFalse(usdRate.toPlainString().contains(","));
    }

    @Test
    @DisplayName("DB 查不到對應中文名稱時，用幣別代碼當 fallback，不讓整支 API 失敗")
    void shouldFallbackWhenChineseNameNotFound() {
        TransformedResponse result = transformer.transform(sampleResponse(), new HashMap<>());

        result.getCurrencies().forEach(c ->
                System.out.println(c.getCode() + " -> " + c.getChineseName()));

        assertEquals("USD", result.getCurrencies().get(2).getChineseName());
    }

    @Test
    @DisplayName("來源是 null 或缺欄位時不要拋例外")
    void shouldHandleNullSafely() {
        TransformedResponse result = transformer.transform(null, currencyNames());

        assertNull(result.getUpdateTime());
        assertTrue(result.getCurrencies().isEmpty());
    }
}
