package com.velb.shop.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.velb.shop.model.entity.auxiliary.OrderStatus;
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
public class OrderHistoryDto {

    @JsonProperty(value = "order")
    private OrderInfoForHistoryDto orderInfo;

    @JsonProperty(value = "content")
    private List<BasketElementForOrderHistoryDto> content;

}
