package com.example.coindesk.dto;

import com.example.coindesk.entity.Currency;
import lombok.Getter;

/** 回給前端的格式（Entity -> DTO，避免把資料庫欄位直接曝給外面） */
@Getter
public class CurrencyResponse {

    private final Long id;
    private final String code;
    private final String chineseName;

    public CurrencyResponse(Currency entity) {
        this.id = entity.getId();
        this.code = entity.getCode();
        this.chineseName = entity.getChineseName();
    }
}
