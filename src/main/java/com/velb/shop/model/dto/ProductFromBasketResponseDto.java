package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFromBasketResponseDto {

    private Long id;

    private String title;

    private Integer price;

    private Integer amount;
}
