package com.example.coindesk.service;

import com.example.coindesk.dto.CoindeskResponse;
import com.example.coindesk.dto.TransformedResponse;
import com.example.coindesk.entity.Currency;
import com.example.coindesk.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoindeskService {

    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private final CoindeskTransformer transformer;

    @Value("${coindesk.api-url}")
    private String coindeskApiUrl;

    /** 實作內容 2：呼叫 coindesk 原始 API */
    public CoindeskResponse getRaw() {
        return restTemplate.getForObject(coindeskApiUrl, CoindeskResponse.class);
    }

    /** 實作內容 3：呼叫 coindesk 並轉換成新 API */
    @Transactional(readOnly = true)
    public TransformedResponse getTransformed() {
        CoindeskResponse raw = getRaw();

        Set<String> codes = (raw == null || raw.getBpi() == null)
                ? Collections.emptySet()
                : raw.getBpi().keySet();

        Map<String, String> codeToChineseName = new HashMap<>();
        for (Currency currency : currencyRepository.findByCodeIn(codes)) {
            codeToChineseName.put(currency.getCode(), currency.getChineseName());
        }

        return transformer.transform(raw, codeToChineseName);
    }
}
