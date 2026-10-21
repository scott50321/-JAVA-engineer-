package com.example.coindesk.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 幣別與中文名稱的對應表。
 *
 * 主鍵用自增 BIGINT，不用 code（例如 "USD"）當主鍵，是因為 Spring Data JPA 的 save()
 * 是看 @Id 是否為 null 來決定要 persist 還是 merge；如果主鍵是手動指定的字串，新增時
 * id 不為 null，會走 merge，變成先 SELECT 再 INSERT，多一次不必要的查詢。
 * code 是業務上的唯一鍵，用 unique 約束（schema.sql 的 uk_currency_code）保證不重複。
 */
@Entity
@Table(name = "currency")
@Getter
@Setter
@NoArgsConstructor
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 10, unique = true)
    private String code;

    @Column(name = "chinese_name", nullable = false, length = 50)
    private String chineseName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Currency(String code, String chineseName) {
        this.code = code;
        this.chineseName = chineseName;
    }

    @PrePersist
    void onInsert() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
