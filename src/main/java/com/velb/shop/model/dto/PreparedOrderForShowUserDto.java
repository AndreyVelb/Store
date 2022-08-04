package com.velb.shop.model.dto;

import com.velb.shop.model.entity.auxiliary.OrderElement;
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

    private List<OrderElement> content;

    private Integer totalCoast;

    private String messageForUser;
}
