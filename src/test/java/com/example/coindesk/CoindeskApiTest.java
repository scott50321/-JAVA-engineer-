package com.example.coindesk;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 測試需求 3：測試呼叫 coindesk API，並顯示其內容。
 * 測試需求 4：測試呼叫資料轉換的 API，並顯示其內容。
 *
 * 這兩支會實際連到 https://kengp3.github.io/blog/coindesk.json，需要網路。
 * 該網址的內容是固定不變的測試資料，所以可以對內容做斷言。
 */
@SpringBootTest
@AutoConfigureMockMvc
class CoindeskApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("測試需求 3：呼叫 coindesk 原始 API 並顯示內容")
    void shouldCallCoindeskApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/coindesk/raw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chartName").value("Bitcoin"))
                .andExpect(jsonPath("$.bpi.USD.code").value("USD"))
                .andExpect(jsonPath("$.bpi.GBP.code").value("GBP"))
                .andExpect(jsonPath("$.bpi.EUR.code").value("EUR"))
                .andReturn();

        System.out.println("===== coindesk 原始內容 =====");
        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("測試需求 4：呼叫轉換後的 API 並顯示內容")
    void shouldCallTransformedApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/coindesk/transformed"))
                .andExpect(status().isOk())
                // 更新時間要是 yyyy/MM/dd HH:mm:ss
                .andExpect(jsonPath("$.updateTime").value("2024/09/02 07:07:20"))
                .andExpect(jsonPath("$.currencies.length()").value(3))
                // 依幣別代碼排序：EUR / GBP / USD
                .andExpect(jsonPath("$.currencies[0].code").value("EUR"))
                .andExpect(jsonPath("$.currencies[0].chineseName").value("歐元"))
                .andExpect(jsonPath("$.currencies[2].code").value("USD"))
                .andExpect(jsonPath("$.currencies[2].chineseName").value("美元"))
                .andExpect(jsonPath("$.currencies[2].rate").value(57756.298))
                .andReturn();

        System.out.println("===== 轉換後的內容 =====");
        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
