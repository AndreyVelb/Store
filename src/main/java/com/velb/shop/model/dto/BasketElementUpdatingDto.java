package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketElementUpdatingDto {

    @NotNull(message = " Вы выбрали некорректный ТОВАР; ")
    @Min(value = 1L, message = " Уникальный идентификатор ТОВАРА не может быть меньше 1; ")
    @Max(value = Long.MAX_VALUE, message = " Слишком большой уникальный идентификатор ТОВАРА; ")
    private Long productId;

    @NotNull(message = " КОЛИЧЕСТВО ТОВАРА не может быть пустым; ")
    @Min(value = 1, message = " КОЛИЧЕСТВО ТОВАРА не может быть меньше 1; ")
    @Max(value = Integer.MAX_VALUE, message = " Вы выбрали слишком большое значение количества товара; ")
    private Integer amount;

}
