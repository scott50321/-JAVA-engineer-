package com.example.coindesk.repository;

import com.example.coindesk.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    /** 依幣別代碼查（coindesk 轉換 API 要用它把 USD 換成「美元」） */
    Optional<Currency> findByCode(String code);

    /** 只撈 coindesk 這次回應裡出現的代碼，不用整張表都查出來 */
    List<Currency> findByCodeIn(Collection<String> codes);

    boolean existsByCode(String code);
}
