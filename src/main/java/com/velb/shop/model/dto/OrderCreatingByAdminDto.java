package com.velb.shop.model.dto;

import com.velb.shop.validation.ProductsAndAmount;
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
public class OrderCreatingByAdminDto {

    @NotNull(message = " Поле ПОКУПАТЕЛЬ обязательно должно быть заполнено; ")
    @Min(value = 1L, message = " Некорректное значение уникального идентификатора пользователя - оно не может быть меньше 1; ")
    @Max(value = Long.MAX_VALUE, message = " Некорректное значение уникального идентификатора пользователя - оно слишком велико; ")
    private Long consumerId;

    @ProductsAndAmount
    private Map<Long, Integer> productsAndAmount;

    @NotNull(message = " Некорректно передалось значение уникального идентификатора администратора. Оно не может быть пустым; ")
    @Min(value = 1L,
         message = " Некорректно передалось значение уникального идентификатора администратора." + "Оно не может быть меньше 1; ")
    @Max(value = Long.MAX_VALUE,
         message = " Некорректно передалось значение уникального идентификатора администратора. " + "Оно слишком велико; ")
    private Long adminId;
}
