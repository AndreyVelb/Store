package com.velb.shop.controller.consumer;

import com.velb.shop.model.dto.BasketDto;
import com.velb.shop.model.dto.BasketElementDeletingDto;
import com.velb.shop.model.dto.BasketElementDto;
import com.velb.shop.model.dto.BasketElementResponseDto;
import com.velb.shop.model.dto.BasketElementUpdatingDto;
import com.velb.shop.model.security.CustomUserDetails;
import com.velb.shop.service.BasketElementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/consumers/{consumerId}/basket-elements")
@RequiredArgsConstructor
public class BasketController {
    private final BasketElementService basketElementService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public ResponseEntity<String> addProductsToBasket(@RequestBody @Validated List<BasketElementDto> basketElementDtoList,
                                                      @PathVariable Long consumerId) {
        basketElementService.addProductsToBasket(consumerId, basketElementDtoList);
        URI location = URI.create("/api/v1/consumers/" + consumerId + "/basket");
        return ResponseEntity.created(location).build();
    }

    @GetMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public BasketDto getBasket(@PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return basketElementService.getAllBasketElementsFromBasket(userDetails.getId());
    }

    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void changeBasketElements(@RequestBody List<@Valid BasketElementUpdatingDto> basketElementUpdatingInfoList,
                                     @PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        basketElementService.changeBasketElements(basketElementUpdatingInfoList, userDetails.getId());
    }

    @DeleteMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void deleteBasketElements(@RequestBody List<Long> basketElementIdList,
                                     @PathVariable Long consumerId) {
        basketElementService.deleteBasketElements(basketElementIdList, consumerId);
    }

    @GetMapping(value = "/{basketElementId}", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public BasketElementResponseDto getBasketElement(@PathVariable Long consumerId,
                                                     @PathVariable Long basketElementId) {
        return basketElementService.getBasketElementFromBasket(basketElementId);
    }

    @PatchMapping(value = "/{basketElementId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void changeBasketElement(@RequestBody @Validated BasketElementUpdatingDto basketElementUpdatingInfo,
                                    @PathVariable Long consumerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        basketElementService.changeBasketElement(basketElementUpdatingInfo, userDetails.getId());
    }

    @DeleteMapping(value = "/{basketElementId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #consumerId")
    public void deleteBasketElement(@RequestBody BasketElementDeletingDto deletingDto,
                                    @PathVariable Long consumerId) {
        basketElementService.deleteBasketElement(deletingDto.getBasketElementId(), consumerId);
    }
}
