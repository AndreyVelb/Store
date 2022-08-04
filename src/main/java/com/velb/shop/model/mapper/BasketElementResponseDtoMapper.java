package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.BasketElementResponseDto;
import com.velb.shop.model.entity.BasketElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = ProductFromBasketResponseDtoMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BasketElementResponseDtoMapper {

    BasketElementResponseDto map(BasketElement basketElement);

}
