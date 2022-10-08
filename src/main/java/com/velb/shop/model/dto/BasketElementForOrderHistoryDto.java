package com.velb.shop.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketElementForOrderHistoryDto {

    @JsonProperty(value = "product")
    private ProductForOrderHistoryDto product;

    @JsonProperty(value = "price")
    private Integer priceInOrder;
    
    @JsonProperty(value = "amount")
    private Integer amount;

}
