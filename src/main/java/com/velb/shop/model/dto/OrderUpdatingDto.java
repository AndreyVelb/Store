package com.velb.shop.model.dto;

import com.velb.shop.model.entity.auxiliary.OrderStatus;
import com.velb.shop.validation.ProductsAndAmount;
import com.velb.shop.validation.ValueOfEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdatingDto {

    @ProductsAndAmount
    private Map<Long, Integer> productsAndAmount;

    @ValueOfEnum(enumClass = OrderStatus.class,
            message = "Не правильно установлен СТАТУС ЗАКАЗА для покупателя; ")
    private String consumerStatus;

    @NotNull(message = " Некорректно передалось значение уникального идентификатора администратора. Оно не может быть пустым; ")
    @Min(value = 1L,
         message = " Некорректно передалось значение уникального идентификатора администратора.Оно не может быть меньше 1; ")
    @Max(value = Long.MAX_VALUE,
         message = " Некорректно передалось значение уникального идентификатора администратора. Оно слишком велико; ")
    private Long adminId;

}
