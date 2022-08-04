package com.velb.shop.model.entity.auxiliary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderElement {

    @JsonProperty(value = "product")
    private ProductForOrder productForOrder;

    @JsonProperty(value = "amount")
    private Integer amount;

}
