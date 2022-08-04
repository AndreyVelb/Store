package com.velb.shop.controller.admin;

import com.velb.shop.model.dto.OrderAuditDto;
import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.service.OrderAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins/{adminId}/order-history")
@RequiredArgsConstructor
public class OrdersHistoryController {
    private final OrderAuditService orderAuditService;

    @GetMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public PageResponse<OrderAuditDto> getaAllOrderHistory(@Nullable @RequestParam Long consumerId,
                                                           @PathVariable Long adminId,
                                                           Pageable pageable) {
        Page<OrderAuditDto> allOrderAudit = orderAuditService.getAllOrderAudit(consumerId, pageable);
        return PageResponse.of(allOrderAudit);
    }
}
