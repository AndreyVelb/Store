package com.velb.shop.model.dto;

import com.velb.shop.model.entity.auxiliary.OrderElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderForCreateDto {

    private List<OrderElement> content;

    private Integer totalCoast;

}
