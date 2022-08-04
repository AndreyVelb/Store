package com.velb.shop.controller.admin;

import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderResponseDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admins/{adminId}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public PageResponse<OrderResponseDto> getAllOrders(@PathVariable Long adminId, Pageable pageable) {
        Page<OrderResponseDto> allOrders = orderService.getAllOrdersByAdmin(pageable);
        return PageResponse.of(allOrders);
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #adminId")
    public ResponseEntity<String> createOrder(@RequestBody @Validated OrderCreatingDto orderCreatingDto,
                                              @PathVariable Long adminId) {
        Long newOrderId = orderService.createNewOrderByAdmin(adminId, orderCreatingDto);
        URI location = URI.create("/api/v1/products/" + newOrderId);
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void updateOrder(@RequestBody @Validated OrderUpdatingDto orderUpdatingDto,
                            @PathVariable Long adminId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        orderService.updateOrderByAdmin(userDetails.getId(), orderUpdatingDto);
    }

    @DeleteMapping(value = "/{orderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void deleteOrder(@PathVariable Long adminId,
                            @RequestBody Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        orderService.deleteOrderByAdmin(userDetails.getId(), orderId);
    }
}
