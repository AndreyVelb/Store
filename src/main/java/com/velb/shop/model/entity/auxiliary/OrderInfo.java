package com.velb.shop.model.entity.auxiliary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfo {

    private List<OrderElement> content;

    private Integer totalPrice;

    @JsonProperty(value = "status")
    private ConsumerOrderStatus consumerStatus;

}
