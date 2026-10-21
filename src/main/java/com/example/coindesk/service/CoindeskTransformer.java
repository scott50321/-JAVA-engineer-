package com.example.coindesk.service;

import com.example.coindesk.dto.CoindeskResponse;
import com.example.coindesk.dto.TransformedResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 資料轉換邏輯，獨立成一個不碰 DB、不碰網路的 class，
 * 這樣題目要求的「針對資料轉換相關邏輯作單元測試」可以純 JUnit 測，不用起 Spring。
 */
@Component
public class CoindeskTransformer {

    private static final DateTimeFormatter OUTPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    /**
     * @param source             coindesk 的原始回應
     * @param codeToChineseName  幣別代碼 -> 中文名稱（從 DB 查出來後傳進來）
     */
    public TransformedResponse transform(CoindeskResponse source, Map<String, String> codeToChineseName) {
        return new TransformedResponse(
                formatUpdateTime(source),
                toCurrencyRates(source, codeToChineseName));
    }

    /**
     * "2024-09-02T07:07:20+00:00" -> "2024/09/02 07:07:20"
     * 不做時區轉換，原始資料帶的是 +00:00 就照它的時間輸出；題目沒有要求轉成台北時間。
     */
    private String formatUpdateTime(CoindeskResponse source) {
        if (source == null || source.getTime() == null || source.getTime().getUpdatedISO() == null) {
            return null;
        }
        return OffsetDateTime.parse(source.getTime().getUpdatedISO()).format(OUTPUT_FORMAT);
    }

    private List<TransformedResponse.CurrencyRate> toCurrencyRates(
            CoindeskResponse source, Map<String, String> codeToChineseName) {

        List<TransformedResponse.CurrencyRate> result = new ArrayList<>();
        if (source == null || source.getBpi() == null) {
            return result;
        }

        for (Map.Entry<String, CoindeskResponse.Bpi> entry : source.getBpi().entrySet()) {
            String code = entry.getKey();
            CoindeskResponse.Bpi bpi = entry.getValue();

            // DB 查不到對應中文名稱時，fallback 用代碼本身，
            // 避免外部多回一個幣別就讓整支 API 失敗
            String chineseName = codeToChineseName.getOrDefault(code, code);

            result.add(new TransformedResponse.CurrencyRate(code, chineseName, parseRate(bpi)));
        }

        result.sort(Comparator.comparing(TransformedResponse.CurrencyRate::getCode));
        return result;
    }

    /**
     * 匯率解析：用 rate 字串去掉千分位逗號再建 BigDecimal，不用 rate_float。
     * rate_float 是 double，本身儲存就不準確；new BigDecimal(double) 只是把那個不準的值
     * 如實展開（例如 0.1 會變成 0.1000000000000000055...），沒有解決精度問題。
     */
    private BigDecimal parseRate(CoindeskResponse.Bpi bpi) {
        if (bpi == null || bpi.getRate() == null) {
            return null;
        }
        return new BigDecimal(bpi.getRate().replace(",", "").trim());
    }
}
