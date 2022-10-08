package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.OrderHistoryDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {OrderInfoForHistoryDtoMapper.class, OrderInfoForHistoryDtoMapper.class},
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderHistoryDtoMapper {

    @Mapping(target = "orderInfo", source = "order")
    @Mapping(target = "content", source = "content")
    OrderHistoryDto map(Order order, List<BasketElement> content);

}
