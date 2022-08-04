package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdatingDto {

    @NotNull(message = " Поле УНИКАЛЬНЫЙ ИДЕНТИФИКАТОР не может быть пустым; ")
    @Min(value = 1, message = " Поле УНИКАЛЬНЫЙ ИДЕНТИФИКАТОР не может быть меньше 1 ")
    private Long productId;

    @Size(max = 30, message = " НАЗВАНИЕ ТОВАРА не может быть больше 30 символов; ")
    private String title;

    @Size(max = 150, message = " ОПИСАНИЕ ТОВАРА не может быть больше 150 символов; ")
    private String description;

    @Min(value = 1, message = " ЦЕНА ТОВАРА не может быть меньше 1; ")
    @Max(value = Integer.MAX_VALUE, message = " ЦЕНА ТОВАРА не может быть такой большой; ")
    private Integer price;

    @NotNull(message = " Поле РАЗРЕШЕНИЕ НА ИЗМЕНЕНИЕ не может быть пустым; ")
    private boolean canBeUpdated;

    private List<String> hashtagsAsString;
}
