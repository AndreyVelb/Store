package com.velb.shop.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.OrderAuditDto;
import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.entity.OrderAuditRecord;
import com.velb.shop.model.mapper.OrderAuditDtoMapper;
import com.velb.shop.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithUserDetails("admin@mail.ru")
@RequiredArgsConstructor
public class OrderHistoryControllerIT extends IntegrationTestBase {
    private final MockMvc mockMvc;
    private final OrderAuditRepository orderAuditRepository;
    private final ObjectMapper objectMapper;
    private final OrderAuditDtoMapper orderAuditDtoMapper;

    @Test
    void getAllOrderHistory() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        Page<OrderAuditRecord> orderAuditRecordPage = orderAuditRepository.findAllFetchAdminAndConsumer(pageable);
        Page<OrderAuditDto> orderAuditDtoPage = orderAuditRecordPage.map(orderAuditDtoMapper::map);
        PageResponse<OrderAuditDto> expectedPageResponse = PageResponse.of(orderAuditDtoPage);

        MvcResult result = mockMvc.perform(get("/api/v1/admins/1/order-history")
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
    void getAllOrderHistoryByConsumerId() throws Exception {
        Pageable pageable = PageRequest.of(0, 2);
        Page<OrderAuditRecord> orderAuditRecordPage = orderAuditRepository.findAllByConsumerIdFetchAdminAndConsumer(2L, pageable);
        Page<OrderAuditDto> orderAuditDtoPage = orderAuditRecordPage.map(orderAuditDtoMapper::map);
        PageResponse<OrderAuditDto> expectedPageResponse = PageResponse.of(orderAuditDtoPage);

        MvcResult result = mockMvc.perform(get("/api/v1/admins/1/order-history")
                        .param("consumerId", "2")
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


}
