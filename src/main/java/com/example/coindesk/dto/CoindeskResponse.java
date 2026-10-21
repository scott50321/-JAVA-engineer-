package com.example.coindesk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * coindesk 原始回應的結構。
 * bpi 用 Map（key = USD / GBP / EUR）而不是三個固定欄位，
 * 這樣對方多回一個幣別時不用改程式碼。
 * @JsonIgnoreProperties 讓對方多回欄位時不會讓反序列化失敗。
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoindeskResponse {

    private Time time;
    private String disclaimer;
    private String chartName;
    private Map<String, Bpi> bpi;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Time {
        private String updated;
        private String updatedISO;
        private String updateduk;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bpi {
        private String code;
        private String symbol;
        /** 帶逗號的字串，例如 "57,756.298" —— 匯率要用它，不要用 rateFloat */
        private String rate;
        private String description;
        @JsonProperty("rate_float")
        private Double rateFloat;
    }
}
