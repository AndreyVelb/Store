package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductForSearchImplDto implements ProductForSearchDto {

    private Long id;

    private String title;

    private String description;

    private Integer amount;

    private Integer price;

}