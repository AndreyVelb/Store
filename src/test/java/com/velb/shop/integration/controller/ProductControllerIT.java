package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails("admin@mail.ru")
@RequiredArgsConstructor
public class ProductControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Test
    void createProduct() throws Exception {
        ProductCreatingDto productCreatingDto = ProductCreatingDto.builder()
                .title("Тестовый товар")
                .description("Описание тестового товара")
                .amount(100)
                .price(10)
                .hashtags(List.of("тестовый", "качественный"))
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/admins/1/products")
                        .content(objectMapper.writeValueAsString(productCreatingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(result.getResponse().getHeader("Location")));
        String newProductIdAsString = "";
        if (matcher.find()) {
            newProductIdAsString = matcher.group();
        }
        Optional<Product> createdProduct = productRepository.findById(Long.valueOf(newProductIdAsString));
        assertTrue(createdProduct.isPresent());
        assertEquals(productCreatingDto.getTitle(), createdProduct.get().getTitle());
        assertEquals(productCreatingDto.getDescription(), createdProduct.get().getDescription());
        assertEquals(productCreatingDto.getPrice(), createdProduct.get().getPrice());
        assertEquals(productCreatingDto.getAmount(), createdProduct.get().getAmount());
    }

    //Установите свою посту и пароль для приложений в application.yml
    @Test
    void updateProductWithPositiveResolution() throws Exception {
        long productId = 3L;
        ProductUpdatingDto productUpdatingDto = ProductUpdatingDto.builder()
                .productId(productId)
                .title("Новое тестовое название товара")
                .description("Новое тестовое описание товара")
                .price(30)
                .hashtagsAsString(List.of("тестовый"))
                .canBeUpdated(true)
                .build();

        mockMvc.perform(put("/api/v1/admins/1/products/3")
                        .content(objectMapper.writeValueAsString(productUpdatingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());

        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(productUpdatingDto.getTitle(), updatedProduct.get().getTitle());
        assertEquals(productUpdatingDto.getDescription(), updatedProduct.get().getDescription());
        assertEquals(productUpdatingDto.getPrice(), updatedProduct.get().getPrice());
        assertEquals(productUpdatingDto.getPrice(), updatedProduct.get().getPrice());
    }

    @Test
    void updateProductWithNegativeResolution() throws Exception {
        long productId = 4L;
        ProductUpdatingDto productUpdatingDto = ProductUpdatingDto.builder()
                .productId(productId)
                .title("Новое тестовое название товара")
                .description(null)
                .price(30)
                .hashtagsAsString(List.of())
                .canBeUpdated(false)
                .build();

        mockMvc.perform(put("/api/v1/admins/1/products/4")
                        .content(objectMapper.writeValueAsString(productUpdatingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());

        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(productUpdatingDto.getTitle(), updatedProduct.get().getTitle());
        assertEquals(productUpdatingDto.getPrice(), updatedProduct.get().getPrice());
        assertEquals(productUpdatingDto.getPrice(), updatedProduct.get().getPrice());
    }

    @Test
    void updateProductAmount() throws Exception {
        long productId = 4L;
        ProductAmountUpdatingDto updatingDto = ProductAmountUpdatingDto.builder()
                .productId(productId)
                .updateAmount(-5)
                .build();
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        int expectedAmountAfterUpdating = productBeforeUpdating.get().getAmount() + updatingDto.getUpdateAmount();

        mockMvc.perform(patch("/api/v1/admins/1/products/4")
                        .content(objectMapper.writeValueAsString(updatingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());

        Optional<Product> updatedProduct = productRepository.findById(productId);
        assertTrue(updatedProduct.isPresent());
        assertEquals(expectedAmountAfterUpdating, updatedProduct.get().getAmount());
    }

    //Установите свою посту и пароль для приложений в application.yml
    @Test
    void deleteProductWithPositiveResolution() throws Exception {
        long productId = 3L;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(productId)
                .canBeDeleted(true)
                .build();
        Optional<Product> productBeforeDeleting = productRepository.findById(productId);
        assertTrue(productBeforeDeleting.isPresent());

        mockMvc.perform(delete("/api/v1/admins/1/products/3")
                        .content(objectMapper.writeValueAsString(deletingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertTrue(deletedProduct.isEmpty());
    }

    @Test
    void deleteProductWithNegativeResolution() throws Exception {
        long productId = 4L;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(productId)
                .canBeDeleted(false)
                .build();
        Optional<Product> productBeforeDeleting = productRepository.findById(productId);
        assertTrue(productBeforeDeleting.isPresent());

        mockMvc.perform(delete("/api/v1/admins/1/products/4")
                        .content(objectMapper.writeValueAsString(deletingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Optional<Product> deletedProduct = productRepository.findById(productId);
        assertTrue(deletedProduct.isEmpty());
    }

}
