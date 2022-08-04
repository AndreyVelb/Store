package com.velb.shop.model.dto;

import com.velb.shop.validation.HashtagsList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreatingDto {

    @NotBlank(message = " НАЗВАНИЕ ТОВАРА не может быть пустым; ")
    @Size(max = 30, message = " НАЗВАНИЕ ТОВАРА не может быть больше 30 символов; ")
    private String title;

    @NotBlank(message = " ОПИСАНИЕ ТОВАРА не может быть пустым; ")
    @Size(max = 150, message = " ОПИСАНИЕ ТОВАРА не может быть больше 150 символов; ")
    private String description;

    @NotNull(message = " ЦЕНА ТОВАРА не может быть пустым; ")
    @Min(value = 0, message = " ЦЕНА ТОВАРА не может быть меньше 0; ")
    @Max(value = Integer.MAX_VALUE, message = " ЦЕНА ТОВАРА не может быть таким большим; ")
    private Integer price;

    @NotNull(message = " КОЛЛИЧЕСТВО ТОВАРА не может быть пустым; ")
    @Min(value = 0, message = " КОЛЛИЧЕСТВО ТОВАРА не может быть меньше 0; ")
    @Max(value = Integer.MAX_VALUE, message = " КОЛЛИЧЕСТВО ТОВАРА не может быть таким большим; ")
    private Integer amount;

    @HashtagsList
    private List<String> hashtags;
}
