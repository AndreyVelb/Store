package com.velb.shop.controller;

import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderHistoryDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.service.EmailService;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final EmailService emailService;

    @GetMapping(value = "/admins/{adminId}/order-history", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public Page<OrderHistoryDto> getOrderHistory(@Nullable @RequestParam Long consumerId,
                                                 @PathVariable Long adminId,
                                                 Pageable pageable) {
        return orderService.getOrderHistory(consumerId, pageable);
    }

    @PostMapping(value = "/admins/{adminId}/orders", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #adminId")
    public ResponseEntity<String> create(@RequestBody @Validated OrderCreatingDto orderCreatingDto,
                                         @PathVariable Long adminId) {
        Long newOrderId = orderService.createNewOrderByAdmin(adminId, orderCreatingDto);
        URI location = URI.create("/api/v1/products/" + newOrderId);
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/admins/{adminId}/orders/{orderId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void update(@RequestBody @Validated OrderUpdatingDto orderUpdatingDto,
                       @PathVariable Long adminId) {
        orderService.updateOrderByAdmin(adminId, orderUpdatingDto);
    }

    @DeleteMapping(value = "/admins/{adminId}/orders/{orderId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void delete(@PathVariable Long adminId,
                       @PathVariable Long orderId) {
        orderService.deleteOrderByAdmin(adminId, orderId);
    }

    //Бронирует то количество товара которое хочет купить пользователь
    @PatchMapping(value = "/consumers/{consumerId}/order-layout", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public PreparedOrderForShowUserDto prepareOrder(@PathVariable Long consumerId) {
        return orderService.prepareOrderByConsumer(consumerId);
    }

    //Создает новый заказ на основе уже забронированных товаров после их бронирования при этом удаляя все из корзины
    @PostMapping(value = "/consumers/{consumerId}/order-layout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public ResponseEntity<String> makeOrder(@PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long orderId = orderService.makeOrderByConsumer(consumerId);
        URI location = URI.create("/api/v1/users/" + consumerId + "/orders/" + orderId);
        emailService.sendEmailAboutOrderCreating(orderId, userDetails.getUsername());
        return ResponseEntity.created(location).build();
    }

    //Отменяет бронирование товара, а в случае если пользователь не отменил его каждые 3 часа scheduler снимает бронирование автоматически
    @PutMapping(value = "/consumers/{consumerId}/order-layout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void cancelCreationAnOrder(@PathVariable Long consumerId) {
        orderService.cancelOrderCreationByConsumer(consumerId);
    }
}
