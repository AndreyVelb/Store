package com.velb.shop.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velb.shop.model.entity.auxiliary.AdminOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAuditDto {

    private Long id;

    private LocalDateTime date;

    private ConsumerDto consumer;

    private OrderInfo orderInfo;

    private AdminDto admin;

    @JsonProperty(value = "adminStatus")
    private AdminOrderStatus adminOrderStatus;
}
