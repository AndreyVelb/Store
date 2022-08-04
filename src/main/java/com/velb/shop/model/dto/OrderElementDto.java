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
public class OrderElementDto {

    @JsonProperty(value = "product")
    private ProductForOrderDto productForOrderDto;

    @JsonProperty(value = "amount")
    private Integer amount;
}
