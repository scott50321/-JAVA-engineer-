package com.example.coindesk.service;

import com.example.coindesk.dto.CurrencyRequest;
import com.example.coindesk.entity.Currency;
import com.example.coindesk.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 幣別維護。
 * 用建構子注入（@RequiredArgsConstructor 幫 final 欄位生建構子），欄位可以宣告 final，
 * 依賴不可變，物件建好就保證依賴齊全，不用容器也能 new 出來做單元測試。
 */
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Currency findById(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "幣別不存在: id=" + id));
    }

    @Transactional
    public Currency create(CurrencyRequest request) {
        if (currencyRepository.existsByCode(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "幣別代碼已存在: " + request.getCode());
        }
        return currencyRepository.save(new Currency(request.getCode(), request.getChineseName()));
    }

    @Transactional
    public Currency update(Long id, CurrencyRequest request) {
        Currency entity = findById(id);
        if (!entity.getCode().equals(request.getCode()) && currencyRepository.existsByCode(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "幣別代碼已存在: " + request.getCode());
        }
        entity.setCode(request.getCode());
        entity.setChineseName(request.getChineseName());
        return currencyRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        Currency entity = findById(id);
        currencyRepository.delete(entity);
    }
}
