package com.example.coindesk;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 測試需求 2：測試呼叫幣別對應表資料 CRUD API，並顯示其內容。
 * class 上加 @Transactional，Spring Test 預設每支測試結束後自動回滾，
 * 不用手動 delete 清資料，測試之間也不會因為殘留資料互相干擾。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CurrencyApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String json(String code, String chineseName) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("code", code);
        body.put("chineseName", chineseName);
        return objectMapper.writeValueAsString(body);
    }

    private void print(String title, MvcResult result) throws Exception {
        System.out.println("===== " + title + " =====");
        System.out.println(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("查詢全部幣別")
    void shouldListAll() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("USD"))
                .andExpect(jsonPath("$[0].chineseName").value("美元"))
                .andReturn();

        print("查詢全部", result);
    }

    @Test
    @DisplayName("新增 -> 查詢 -> 修改 -> 刪除 一整輪")
    void shouldCreateUpdateAndDelete() throws Exception {
        // 新增
        MvcResult created = mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("JPY", "日圓")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("JPY"))
                .andReturn();
        print("新增", created);

        Long id = objectMapper.readTree(created.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        // 查詢單筆
        MvcResult found = mockMvc.perform(get("/api/currencies/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chineseName").value("日圓"))
                .andReturn();
        print("查詢單筆", found);

        // 修改
        MvcResult updated = mockMvc.perform(put("/api/currencies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("JPY", "日幣")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chineseName").value("日幣"))
                .andReturn();
        print("修改", updated);

        // 刪除
        mockMvc.perform(delete("/api/currencies/{id}", id))
                .andExpect(status().isNoContent());
        System.out.println("===== 刪除 ===== id=" + id + " 已刪除");

        // 刪除後再查應該 404
        mockMvc.perform(get("/api/currencies/{id}", id))
                .andExpect(status().isNotFound());
        System.out.println("===== 刪除後再查 ===== 回 404，符合預期");
    }

    @Test
    @DisplayName("新增重複的幣別代碼要被擋下來（code 有 unique 約束）")
    void shouldRejectDuplicateCode() throws Exception {
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("USD", "美金")))
                .andExpect(status().isConflict());

        System.out.println("===== 重複代碼 ===== 回 409 Conflict，符合預期");
    }

    @Test
    @DisplayName("修改成別人已經在用的幣別代碼要被擋下來（跟新增一樣要查 unique）")
    void shouldRejectUpdateToDuplicateCode() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("AUD", "澳幣")))
                .andExpect(status().isCreated())
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("id").asLong();

        mockMvc.perform(put("/api/currencies/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("USD", "澳幣")))
                .andExpect(status().isConflict());

        System.out.println("===== 修改成重複代碼 ===== 回 409 Conflict，符合預期");
    }

    @Test
    @DisplayName("code 為空要被 @Valid 擋下來")
    void shouldRejectBlankCode() throws Exception {
        mockMvc.perform(post("/api/currencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "空的")))
                .andExpect(status().isBadRequest());

        System.out.println("===== 空白代碼 ===== 回 400 Bad Request，符合預期");
    }
}
