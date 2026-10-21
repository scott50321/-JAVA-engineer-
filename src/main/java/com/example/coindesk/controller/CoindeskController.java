package com.example.coindesk.controller;

import com.example.coindesk.dto.CoindeskResponse;
import com.example.coindesk.dto.TransformedResponse;
import com.example.coindesk.service.CoindeskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coindesk")
@RequiredArgsConstructor
public class CoindeskController {

    private final CoindeskService coindeskService;

    /**
     * 實作內容 2：呼叫 coindesk 的 API（原始內容）
     */
    @GetMapping("/raw")
    public CoindeskResponse raw() {
        return coindeskService.getRaw();
    }

    /**
     * 實作內容 3：轉換後的新 API
     */
    @GetMapping("/transformed")
    public TransformedResponse transformed() {
        return coindeskService.getTransformed();
    }
}
