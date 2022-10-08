package com.velb.shop.model.dto;

import com.velb.shop.model.entity.auxiliary.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfoForHistoryDto {

    private Long id;

    private LocalDateTime date;

    private UserForOrderHistoryDto consumer;

    private Integer totalCost;

    private OrderStatus orderStatus;

    private UserForOrderHistoryDto lastUser;

}
