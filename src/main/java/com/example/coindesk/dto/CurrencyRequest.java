package com.example.coindesk.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 新增／修改幣別用的入參，不直接用 Entity 當 API 入參，避免把資料庫欄位結構曝露給外部 */
@Getter
@Setter
public class CurrencyRequest {

    @NotBlank(message = "code 不可為空")
    @Size(max = 10, message = "code 長度不可超過 10")
    private String code;

    @NotBlank(message = "chineseName 不可為空")
    @Size(max = 50, message = "chineseName 長度不可超過 50")
    private String chineseName;
}
