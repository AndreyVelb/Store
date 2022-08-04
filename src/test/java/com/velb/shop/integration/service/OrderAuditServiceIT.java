package com.velb.shop.integration.service;

import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.OrderAuditDto;
import com.velb.shop.service.OrderAuditService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
public class OrderAuditServiceIT extends IntegrationTestBase {
    private final OrderAuditService orderAuditService;

    @Test
    void getAllOrderAudit() {
        Long consumerId = null;
        Pageable pageable = PageRequest.of(0, 2);

        Page<OrderAuditDto> pageOfOrderAuditDto = orderAuditService.getAllOrderAudit(consumerId, pageable);

        assertEquals(4, pageOfOrderAuditDto.getTotalElements());
        assertEquals(2, pageOfOrderAuditDto.get().count());
        assertEquals(0, pageOfOrderAuditDto.getPageable().getPageNumber());
        assertEquals(2, pageOfOrderAuditDto.getPageable().getPageSize());
    }

    @Test
    void getAllOrderAuditByConsumerId() {
        Long consumerId = 2L;
        Pageable pageable = PageRequest.of(0, 2);

        Page<OrderAuditDto> pageOfOrderAuditDto = orderAuditService.getAllOrderAudit(consumerId, pageable);

        assertEquals(1, pageOfOrderAuditDto.getTotalElements());
        assertEquals(1, pageOfOrderAuditDto.get().count());
        assertEquals(0, pageOfOrderAuditDto.getPageable().getPageNumber());
        assertEquals(2, pageOfOrderAuditDto.getPageable().getPageSize());
    }
}
