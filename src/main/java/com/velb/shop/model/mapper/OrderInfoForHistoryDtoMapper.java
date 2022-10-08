package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.OrderInfoForHistoryDto;
import com.velb.shop.model.entity.Order;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = UserForOrderHistoryDtoMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderInfoForHistoryDtoMapper {

    OrderInfoForHistoryDto map(Order order);

}
