package com.velb.shop.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class BasketElementResponseDto {

    public BasketElementResponseDto(Long id, ProductFromBasketResponseDto product, Integer amount) {
        setId(id);
        setProduct(product);
        setAmount(amount);
    }

    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    @JsonProperty(value = "product")
    private ProductFromBasketResponseDto product;

    @Getter
    private Integer amount;

    @Getter
    private Integer totalPriceOfProductPosition;

    public void setAmount(Integer amount) {
        this.amount = amount;
        this.totalPriceOfProductPosition = this.amount * product.getPrice();
    }
}
