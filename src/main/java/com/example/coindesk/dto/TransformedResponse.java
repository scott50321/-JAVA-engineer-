package com.example.coindesk.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** 題目要求的新 API 格式：更新時間 + 幣別資訊（幣別／中文名稱／匯率） */
@Getter
public class TransformedResponse {

    /** 格式：1990/01/01 00:00:00 */
    private final String updateTime;
    private final List<CurrencyRate> currencies;

    public TransformedResponse(String updateTime, List<CurrencyRate> currencies) {
        this.updateTime = updateTime;
        this.currencies = currencies;
    }

    @Getter
    public static class CurrencyRate {
        private final String code;
        private final String chineseName;
        private final BigDecimal rate;

        public CurrencyRate(String code, String chineseName, BigDecimal rate) {
            this.code = code;
            this.chineseName = chineseName;
            this.rate = rate;
        }
    }
}
