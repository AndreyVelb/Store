package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class SearchingProductControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Test
    void searchProducts() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        String searchQuery = "Шкаф";
        String hashtags = "надежный,пианино";
        Page<ProductForSearchDto> products = productRepository.findAllThroughAdvancedSearch(searchQuery, hashtags, pageable);

        MvcResult result = mockMvc.perform(get("/api/v1/products")
                        .param("searchQuery", "Шкаф")
                        .param("hashtags", "надежный")
                        .param("hashtags", "пианино")
                        .param("page", "0")
                        .param("size", "2")
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(PageResponse.of(products)), result.getResponse().getContentAsString());
    }

    @Test
    void getProduct() throws Exception {
        long productId = 3L;
        ProductForSearchDto productForSearchDto = productRepository.findProductForSearchDtoById(productId);

        MvcResult result = mockMvc.perform(get("/api/v1/products/" + productId)
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(productForSearchDto), result.getResponse().getContentAsString());
    }
}
