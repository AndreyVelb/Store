package com.velb.shop.model.dto;

import com.velb.shop.model.entity.auxiliary.ConsumerOrderStatus;
import com.velb.shop.validation.ProductsAndAmount;
import com.velb.shop.validation.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdatingDto {

    @NotNull(message = " Поле УНИКАЛЬНЫЙ ИДЕНТИФИКАТОР  не может быть пустым; ")
    @Min(value = 1, message = " Поле УНИКАЛЬНЫЙ ИДЕНТИФИКАТОР не может быть меньше 1; ")
    private Long orderId;

    @ProductsAndAmount
    private Map<Long, Integer> productsAndAmount;

    @ValueOfEnum(enumClass = ConsumerOrderStatus.class,
            message = "Не правильно установлен СТАТУС ЗАКАЗА для покупателя; ")
    private String consumerStatus;

}
