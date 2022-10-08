package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.BasketElementForOrderHistoryDto;
import com.velb.shop.model.entity.BasketElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = ProductForOrderHistoryDtoMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BasketElementForOrderHistoryDtoMapper {

    BasketElementForOrderHistoryDto map(BasketElement basketElement);

}
