package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.BasketDto;
import com.velb.shop.model.dto.BasketElementDeletingDto;
import com.velb.shop.model.dto.BasketElementDto;
import com.velb.shop.model.dto.BasketElementResponseDto;
import com.velb.shop.model.dto.BasketElementUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.mapper.BasketElementResponseDtoListMapper;
import com.velb.shop.model.mapper.BasketElementResponseDtoMapper;
import com.velb.shop.repository.BasketElementRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class BasketControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final BasketElementRepository basketElementRepository;
    private final BasketElementResponseDtoMapper basketElementResponseDtoMapper;
    private final BasketElementResponseDtoListMapper basketElementResponseDtoListMapper;
    private final ObjectMapper objectMapper;

    @Test
    @WithUserDetails("ivanov@yandex.com")
    void addProductsToBasket() throws Exception {
        long consumerId = 3L;
        List<BasketElementDto> basketElementDtoList = List.of(
                BasketElementDto.builder()
                        .productId(2L)
                        .amount(10)
                        .build(),
                BasketElementDto.builder()
                        .productId(3L)
                        .amount(10)
                        .build());

        mockMvc.perform(post("/api/v1/consumers/" + consumerId + "/basket")
                        .content(objectMapper.writeValueAsString(basketElementDtoList))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isCreated(),
                        header().string("Location", "/api/v1/consumers/" + consumerId + "/basket"));
    }

    @Test
    @WithUserDetails("ivanov@yandex.com")
    void getBasket() throws Exception {
        long consumerId = 3L;
        List<BasketElement> consumersBasket = basketElementRepository.findAllByConsumerIdNotOrdered(consumerId);
        String expectedResponse = objectMapper.writeValueAsString(
                new BasketDto(
                        basketElementResponseDtoListMapper
                                .map(consumersBasket)));

        MvcResult result = mockMvc.perform(get("/api/v1/consumers/" + consumerId + "/basket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        assertEquals(expectedResponse, result.getResponse().getContentAsString());
    }

    @Test
    @WithUserDetails("ivanov@yandex.com")
    void getBasketElement() throws Exception {
        long consumerId = 3L;
        long basketElementId = 4L;
        Optional<BasketElement> basketElement = basketElementRepository.findById(basketElementId);
        assertTrue(basketElement.isPresent());
        BasketElementResponseDto expectedResponseDto = basketElementResponseDtoMapper.map(basketElement.get());

        MvcResult result = mockMvc.perform(get("/api/v1/consumers/" + consumerId + "/basket/" + basketElementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"))
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(expectedResponseDto), result.getResponse().getContentAsString());
    }

    @Test
    @WithUserDetails("ivanov@yandex.com")
    void changeBasketElements() throws Exception {
        long consumerId = 3L;
        List<BasketElementUpdatingDto> basketElementUpdatingDtoList = List.of(
                BasketElementUpdatingDto.builder()
                        .productId(1L)
                        .amount(2)
                        .build(),
                BasketElementUpdatingDto.builder()
                        .productId(2L)
                        .amount(3)
                        .build(),
                BasketElementUpdatingDto.builder()
                        .productId(4L)
                        .amount(5)
                        .build()
        );

        mockMvc.perform(patch("/api/v1/consumers/" + consumerId + "/basket")
                        .content(objectMapper.writeValueAsString(basketElementUpdatingDtoList))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("petrov@gmail.com")
    void deleteBasketElements() throws Exception {
        long consumerId = 2L;
        List<Long> basketElementIdList = List.of(10L, 11L, 12L);

        mockMvc.perform(delete("/api/v1/consumers/" + consumerId + "/basket")
                        .content(objectMapper.writeValueAsString(basketElementIdList))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(basketElementRepository.findById(basketElementIdList.get(0)).isEmpty());
        assertTrue(basketElementRepository.findById(basketElementIdList.get(1)).isEmpty());
        assertTrue(basketElementRepository.findById(basketElementIdList.get(2)).isEmpty());
    }

    @Test
    @WithUserDetails("ivanov@yandex.com")
    void changeBasketElement() throws Exception {
        long consumerId = 3L;
        long basketElementId = 4L;
        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(1L)
                .amount(2)
                .build();

        mockMvc.perform(patch("/api/v1/consumers/" + consumerId + "/basket/" + basketElementId)
                        .content(objectMapper.writeValueAsString(updatingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("petrov@gmail.com")
    void deleteBasketElement() throws Exception {
        long consumerId = 2L;
        long basketElementId = 11L;
        BasketElementDeletingDto deletingDto = BasketElementDeletingDto.builder()
                .basketElementId(basketElementId)
                .build();

        mockMvc.perform(delete("/api/v1/consumers/" + consumerId + "/basket/" + basketElementId)
                        .content(objectMapper.writeValueAsString(deletingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertTrue(basketElementRepository.findById(basketElementId).isEmpty());
    }
}
