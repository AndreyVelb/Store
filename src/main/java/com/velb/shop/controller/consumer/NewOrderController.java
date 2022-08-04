package com.velb.shop.controller.consumer;

import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.service.EmailService;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/consumers/{consumerId}")
@RequiredArgsConstructor
public class NewOrderController {
    private final OrderService orderService;
    private final EmailService emailService;

    //Бронирует то количество товара которое хочет купить пользователь
    @PatchMapping(value = "/new-order", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public PreparedOrderForShowUserDto prepareOrder(@PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return orderService.prepareOrderByConsumer(userDetails.getId());
    }

    //Создает новый заказ на основе уже забронированных товаров после их бронирования при этом удаляя все из корзины
    @PostMapping(value = "/new-order", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public ResponseEntity<String> makeOrder(@PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long orderId = orderService.makeOrderByConsumer(userDetails.getId());
        URI location = URI.create("/api/v1/users/" + consumerId + "/orders/" + orderId);
        emailService.sendEmailAboutOrderCreating(orderId, userDetails.getUsername());
        return ResponseEntity.created(location).build();
    }

    //Отменяет бронирование товара, а в случае если пользователь не отменил его каждые 3 часа scheduler снимает бронирование автоматически
    @PutMapping(value = "/new-order", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void cancelCreationAnOrder(@PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        orderService.cancelOrderCreationByConsumer(userDetails.getId());
    }

}
