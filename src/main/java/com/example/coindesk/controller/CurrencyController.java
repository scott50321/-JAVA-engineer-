package com.example.coindesk.controller;

import com.example.coindesk.dto.CurrencyRequest;
import com.example.coindesk.dto.CurrencyResponse;
import com.example.coindesk.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 幣別對應表的 CRUD（題目實作內容 1）
 */
@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Validated
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public List<CurrencyResponse> list() {
        return currencyService.findAll().stream()
                .map(CurrencyResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public CurrencyResponse get(@PathVariable Long id) {
        return new CurrencyResponse(currencyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CurrencyResponse> create(@Valid @RequestBody CurrencyRequest request) {
        CurrencyResponse body = new CurrencyResponse(currencyService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{id}")
    public CurrencyResponse update(@PathVariable Long id, @Valid @RequestBody CurrencyRequest request) {
        return new CurrencyResponse(currencyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        currencyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
