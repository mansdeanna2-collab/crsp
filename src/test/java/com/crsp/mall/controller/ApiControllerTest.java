package com.crsp.mall.controller;

import com.crsp.mall.entity.ProductEntity;
import com.crsp.mall.service.ProductDbService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductDbService productDbService;

    @Test
    void getProductReturnsNotFoundWhenMissing() throws Exception {
        given(productDbService.getProductById(42L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("商品不存在"));
    }

    @Test
    void getProductReturnsProductWhenPresent() throws Exception {
        ProductEntity product = new ProductEntity();
        product.setId(1L);
        product.setTitle("测试商品");
        product.setPrice(128.0);
        given(productDbService.getProductById(1L)).willReturn(Optional.of(product));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("测试商品"));
    }
}
