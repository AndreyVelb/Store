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
public class OrderDeletingDto {

    @NotNull(message = " Некорректно передалось значение уникального идентификатора администратора. Оно не может быть пустым; ")
    @Min(value = 1L, message = " Некорректно передалось значение уникального идентификатора администратора." +
                                 "Оно не может быть меньше 1; ")
    @Max(value = Long.MAX_VALUE, message = " Некорректно передалось значение уникального идентификатора администратора. " +
                                            "Оно слишком велико; ")
    private Long adminId;

}
