package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductForUpdateDto {

    private Long id;

    private String title;

    private String description;

    private List<String> hashtags;

    private Integer amount;

    private Integer price;
}
