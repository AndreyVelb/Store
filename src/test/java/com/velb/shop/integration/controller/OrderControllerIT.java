package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.handler.ExceptionResponse;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.BasketElementForOrderHistoryDto;
import com.velb.shop.model.dto.OrderCreatingByAdminDto;
import com.velb.shop.model.dto.OrderHistoryDto;
import com.velb.shop.model.dto.OrderInfoForHistoryDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.ProductForOrderHistoryDto;
import com.velb.shop.model.dto.UserForOrderHistoryDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.auxiliary.OrderStatus;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails("admin@mail.ru")
@RequiredArgsConstructor
public class OrderControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final BasketElementRepository basketElementRepository;
    private final ObjectMapper objectMapper;

    @Test
    void getOrdersByConsumer() throws Exception {
        Long consumerId = 2L;
        Pageable pageable = PageRequest.of(0, 2);
        Page<Order> ordersPage = orderRepository.findAllByConsumerIdFetchConsumerAndLastUser(consumerId, pageable);
        List<OrderHistoryDto> expectedResultAsList = new ArrayList<>();

        for (Order order : ordersPage) {
            List<BasketElement> basketElementList = basketElementRepository.findAllByOrderIdFetchProduct(order.getId());
            expectedResultAsList.add(mapToOrderHistoryDto(order, basketElementList));
        }

        Page<OrderHistoryDto> expectedResultAsPage = new PageImpl<>(expectedResultAsList, pageable, expectedResultAsList.size());

        MvcResult result = mockMvc.perform(get("/api/v1/orders")
                        .param("consumerId", "2")
                        .param("page", "0")
                        .param("size", "2")
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8")
                )
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(expectedResultAsPage), result.getResponse().getContentAsString());
        System.out.println(result.getResponse().getContentAsString());
    }

    @Test
    void createOrder() throws Exception {
        long adminId = 1L;
        long productId2 = 2L;
        long productId3 = 3L;
        long productId8 = 8L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId2, 10,
                productId3, 16,
                productId8, 10);
        OrderCreatingByAdminDto orderCreatingDto = OrderCreatingByAdminDto.builder()
                .consumerId(3L)
                .productsAndAmount(productsAndAmount)
                .adminId(adminId)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
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
        long adminId = 1L;
        long productId1 = 1L;
        long productId2 = 2L;
        long nonExistedProductId = 10000000L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, 10,
                productId2, 16,
                nonExistedProductId, 10);
        OrderCreatingByAdminDto orderCreatingDto = OrderCreatingByAdminDto.builder()
                .consumerId(3L)
                .productsAndAmount(productsAndAmount)
                .adminId(adminId)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        String expectedExceptionMessageAsJson = objectMapper.writeValueAsString(
                new ExceptionResponse("Товара с id " + nonExistedProductId + " не существует; "));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(expectedExceptionMessageAsJson, result.getResponse().getContentAsString());
    }

    @Test
    void createNewOrderByAdminThrowUserNotFoundExWithNonExistedConsumer() throws Exception {
        long adminId = 1L;
        long nonExistedConsumerId = 100000L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, 3L, 16, 8L, 10);
        OrderCreatingByAdminDto orderCreatingDto = OrderCreatingByAdminDto.builder()
                .consumerId(nonExistedConsumerId)
                .productsAndAmount(productsAndAmount)
                .adminId(adminId)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        String expectedExceptionMessageAsJson = objectMapper.writeValueAsString(
                new ExceptionResponse("Вы выбрали некорректного покупателя; "));

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .content(orderCreatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andReturn();

        assertEquals(expectedExceptionMessageAsJson, result.getResponse().getContentAsString());
    }

    @Test
    void createNewOrderByAdminThrowMethodArgumentNotValidEx() throws Exception {
        long adminId = 1L;
        long consumerId = -3L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, -3L, 16, 8L, 10);
        OrderCreatingByAdminDto orderCreatingDto = OrderCreatingByAdminDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .adminId(adminId)
                .build();
        String orderCreatingDtoAsJson = objectMapper.writeValueAsString(orderCreatingDto);
        List<String> expectedExceptionMessages = List.of(
                " Вы ввели некорректные данные - либо значение уникального идентификатора товара либо его количество меньше 0; ",
                " Некорректное значение уникального идентификатора пользователя - оно не может быть меньше 1; ");

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
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
        long adminId = 1L;
        long orderId = 3L;
        long productId1 = 1L;
        long productId2 = 2L;
        long productId3 = 3L;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, 3,
                productId2, 4,
                productId3, 7);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .adminId(adminId)
                .build();
        String orderUpdatingDtoAsJson = objectMapper.writeValueAsString(orderUpdatingDto);

        mockMvc.perform(put("/api/v1/orders/3")
                        .content(orderUpdatingDtoAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        Optional<Order> updatedOrder = orderRepository.findById(orderId);
        assertTrue(updatedOrder.isPresent());
        assertEquals(orderUpdatingDto.getConsumerStatus(), updatedOrder.get().getOrderStatus().name());
    }

    @Test
    void deleteOrder() throws Exception {
        long orderId = 3L;
        String orderIdAsJson = objectMapper.writeValueAsString(orderId);

        mockMvc.perform(delete("/api/v1/orders/3")
                        .content(orderIdAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Order> updatedOrder = orderRepository.findById(orderId);
        assertTrue(updatedOrder.isPresent());
        assertEquals(OrderStatus.DELETED, updatedOrder.get().getOrderStatus());
    }

    @Test
    @WithUserDetails("petrov@gmail.com")
    void prepareOrder() throws Exception {
        long consumerId = 2L;

        mockMvc.perform(patch("/api/v1/consumers/" + consumerId + "/order-layout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"));
    }

    //Установите свою почту и пароль для приложений в application.yml
    @Test
    @WithUserDetails("petrov@gmail.com")
    void makeOrder() throws Exception {
        long consumerId = 2L;

        List<BasketElement> preparedBasketElement = basketElementRepository.findAllByConsumerIdNotOrdered(consumerId);
        for (BasketElement basketElement : preparedBasketElement) {
            basketElement.setPriceInOrder(basketElement.getProduct().getPrice());
        }

        MvcResult result = mockMvc.perform(post("/api/v1/consumers/" + consumerId + "/order-layout")
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
    @WithUserDetails("petrov@gmail.com")
    void cancelCreationAnOrder() throws Exception {
        long consumerId = 2L;
        orderService.prepareOrderByConsumer(consumerId);

        mockMvc.perform(put("/api/v1/consumers/" + consumerId + "/order-layout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    OrderHistoryDto mapToOrderHistoryDto(Order order, List<BasketElement> basketElementList) {
        List<BasketElementForOrderHistoryDto> basketElementForOrderHistoryDtoList = new ArrayList<>();
        for (BasketElement basketElement : basketElementList) {
            basketElementForOrderHistoryDtoList.add(
                    BasketElementForOrderHistoryDto.builder()
                            .amount(basketElement.getAmount())
                            .priceInOrder(basketElement.getPriceInOrder())
                            .product(
                                    ProductForOrderHistoryDto.builder()
                                            .id(basketElement.getProduct().getId())
                                            .title(basketElement.getProduct().getTitle())
                                            .build())
                            .build());
        }

        return OrderHistoryDto.builder()
                .orderInfo(OrderInfoForHistoryDto.builder()
                        .id(order.getId())
                        .consumer(
                                UserForOrderHistoryDto.builder()
                                        .id(order.getConsumer().getId())
                                        .lastName(order.getConsumer().getLastName())
                                        .firstName(order.getConsumer().getFirstName())
                                        .middleName(order.getConsumer().getMiddleName())
                                        .email(order.getConsumer().getEmail())
                                        .build())
                        .date(order.getDate())
                        .orderStatus(order.getOrderStatus())
                        .totalCost(order.getTotalCost())
                        .lastUser(
                                UserForOrderHistoryDto.builder()
                                        .id(order.getLastUser().getId())
                                        .lastName(order.getLastUser().getLastName())
                                        .firstName(order.getLastUser().getFirstName())
                                        .middleName(order.getLastUser().getMiddleName())
                                        .email(order.getLastUser().getEmail())
                                        .build())
                        .build())
                .content(basketElementForOrderHistoryDtoList)
                .build();
    }
}

