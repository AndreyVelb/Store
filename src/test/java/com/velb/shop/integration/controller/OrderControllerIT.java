package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.handler.ExceptionResponse;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderResponseDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.mapper.OrderResponseDtoMapper;
import com.velb.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails("admin@mail.ru")
@RequiredArgsConstructor
public class OrderControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final OrderRepository orderRepository;
    private final OrderResponseDtoMapper orderResponseDtoMapper;
    private final ObjectMapper objectMapper;

    @Test
    void getAllOrders() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        Page<Order> orderPage = orderRepository.findAllFetchConsumers(pageable);
        Page<OrderResponseDto> orderResponseDtoPage = orderPage.map(orderResponseDtoMapper::map);
        PageResponse<OrderResponseDto> expectedPageResponse = PageResponse.of(orderResponseDtoPage);

        MvcResult result = mockMvc.perform(get("/api/v1/admins/1/orders")
                        .param("page", "0")
                        .param("size", "2")
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8")
                )
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(expectedPageResponse), result.getResponse().getContentAsString());
    }

    @Test
    void createOrder() throws Exception {
        long productId2 = 2L;
        long productId3 = 3L;
        long productId8 = 8L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId2, 10,
                productId3, 16,
                productId8, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(3L)
                .productsAndAmount(productsAndAmount)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);

        MvcResult result = mockMvc.perform(post("/api/v1/admins/1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();

        Pattern pattern = Pattern.compile("\\d+$");
        Matcher matcher = pattern.matcher(Objects.requireNonNull(result.getResponse().getHeader("Location")));
        String newOrderIdAsString = "";
        if (matcher.find()) {
            newOrderIdAsString = matcher.group();
        }
        Optional<Order> createdOrder = orderRepository.findById(Long.valueOf(newOrderIdAsString));
        assertTrue(createdOrder.isPresent());
    }

    @Test
    void createNewOrderByAdminThrowProductNotFoundEx() throws Exception {
        long productId1 = 1L;
        long productId2 = 2L;
        long nonExistedProductId = 10000000L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, 10,
                productId2, 16,
                nonExistedProductId, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(3L)
                .productsAndAmount(productsAndAmount)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        String expectedExceptionMessageAsJson = objectMapper.writeValueAsString(
                new ExceptionResponse("Товара с id " + nonExistedProductId + " не существует; "));

        MvcResult result = mockMvc.perform(post("/api/v1/admins/1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(expectedExceptionMessageAsJson, result.getResponse().getContentAsString());
    }

    @Test
    void createNewOrderByAdminThrowUserNotFoundExWithNonExistedConsumer() throws Exception {
        long nonExistedConsumerId = 100000L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, 3L, 16, 8L, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(nonExistedConsumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        String expectedExceptionMessageAsJson = objectMapper.writeValueAsString(
                new ExceptionResponse("Вы выбрали некорректного покупателя; "));

        MvcResult result = mockMvc.perform(post("/api/v1/admins/1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andReturn();

        assertEquals(expectedExceptionMessageAsJson, result.getResponse().getContentAsString());
    }

    @Test
    void createNewOrderByAdminThrowMethodArgumentNotValidEx() throws Exception {
        long consumerId = -3L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, -3L, 16, 8L, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        List<String> expectedExceptionMessages = List.of(
                " Вы ввели некорректные данные - либо значение уникального идентификатора товара либо его количество меньше 0; ",
                " Некорректное значение уникального идентификатора пользователя - оно не может быть меньше 1; ");

        MvcResult result = mockMvc.perform(post("/api/v1/admins/1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();

        for (String message : expectedExceptionMessages) {
            assertTrue(result.getResponse().getContentAsString().contains(message));
        }
    }

    @Test
    void updateOrder() throws Exception {
        long orderId = 3L;
        long productId1 = 1L;
        long productId2 = 2L;
        long productId3 = 3L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, 3,
                productId2, 4,
                productId3, 7);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(orderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        String orderUpdatingDtoAsJson = objectMapper.writeValueAsString(orderUpdatingDto);

        mockMvc.perform(put("/api/v1/admins/1/orders/3")
                        .content(orderUpdatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        Optional<Order> updatedOrder = orderRepository.findById(orderId);
        assertTrue(updatedOrder.isPresent());
        assertEquals(orderUpdatingDto.getConsumerStatus(), updatedOrder.get().getConsumerOrderStatus().name());
    }

    @Test
    void deleteOrder() throws Exception {
        long orderId = 3L;
        String orderIdAsJson = objectMapper.writeValueAsString(orderId);

        mockMvc.perform(delete("/api/v1/admins/1/orders/3")
                        .content(orderIdAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Order> updatedOrder = orderRepository.findById(orderId);
        assertTrue(updatedOrder.isEmpty());
    }

}
