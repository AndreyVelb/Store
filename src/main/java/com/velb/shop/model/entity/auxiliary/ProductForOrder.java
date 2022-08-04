package com.velb.shop.model.entity.auxiliary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
@Builder
public class ProductForOrder {

    private Long id;

    private String title;

    private String description;

    private Integer price;
}
