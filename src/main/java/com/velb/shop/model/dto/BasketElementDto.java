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
public class BasketElementDto {

    @NotNull(message = " Некорректно выбран ТОВАР; ")
    @Min(value = 1L, message = " Уникальный идентификатор ТОВАР должен быть больше 1; ")
    @Max(value = Long.MAX_VALUE, message = " Уникальный идентификатор ТОВАР должен быть не таким большим; ")
    private Long productId;

    @NotNull(message = " Поле КОЛИЧЕСТВО ТОВАРА должно быть заполнено; ")
    @Min(value = 1, message = " КОЛИЧЕСТВО ТОВАРА должно быть больше 0")
    @Max(value = Integer.MAX_VALUE)
    private Integer amount;
}
