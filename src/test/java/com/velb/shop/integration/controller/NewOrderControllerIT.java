package com.velb.shop.integration.controller;

import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.entity.Order;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RequiredArgsConstructor
public class NewOrderControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Test
    @WithUserDetails("petrov@gmail.com")
    void prepareOrder() throws Exception {
        long consumerId = 2L;

        mockMvc.perform(patch("/api/v1/consumers/" + consumerId + "/new-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpectAll(
                        status().isOk(),
                        content().contentType("application/json;charset=UTF-8"));
    }

    //Установите свою посту и пароль для приложений в application.yml
    @Test
    @WithUserDetails("petrov@gmail.com")
    void makeOrder() throws Exception {
        long consumerId = 2L;

        MvcResult result = mockMvc.perform(post("/api/v1/consumers/" + consumerId + "/new-order")
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

        mockMvc.perform(put("/api/v1/consumers/" + consumerId + "/new-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
