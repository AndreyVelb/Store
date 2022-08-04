package com.velb.shop.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode
@ToString
public class BasketDto {

    public BasketDto(List<BasketElementResponseDto> basketElementResponseDtoList) {
        setBasketElementResponseDtoList(basketElementResponseDtoList);
    }

    @Getter
    @JsonProperty(value = "basket")
    private List<BasketElementResponseDto> basketElementResponseDtoList;

    @Getter
    private Integer totalPrice;

    public void setBasketElementResponseDtoList(List<BasketElementResponseDto> basketElementResponseDtoList) {
        this.basketElementResponseDtoList = basketElementResponseDtoList;
        this.totalPrice = basketElementResponseDtoList.stream().mapToInt(BasketElementResponseDto::getTotalPriceOfProductPosition).sum();
    }
}
