package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.OrderResponseDto;
import com.velb.shop.model.entity.Order;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = ConsumerDtoMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderResponseDtoMapper {

    OrderResponseDto map(Order Order);

}
