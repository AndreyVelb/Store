package com.velb.shop.model.dto;


import com.velb.shop.model.entity.auxiliary.ConsumerOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;

    private LocalDateTime date;

    private ConsumerDto consumer;

    private List<OrderElement> content;

    private Integer totalCost;

    private ConsumerOrderStatus consumerOrderStatus;
}
