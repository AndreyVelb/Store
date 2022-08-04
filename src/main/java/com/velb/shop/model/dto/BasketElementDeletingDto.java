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
public class BasketElementDeletingDto {

    @NotNull(message = " Выбранная вами позиция для удаления некорректна; ")
    @Min(value = 1L, message = " Вы некорректно выбрали позицию в корзине; ")
    @Max(value = Long.MAX_VALUE, message = " Вы некорректно выбрали позицию в корзине; ")
    private Long basketElementId;

}
