package com.velb.shop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreparedOrderForShowUserDto {

    private List<BasketElementForPrepareOrderDto> content;

    private Integer totalCoast;

    private String messageForUser;

}
