package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.BasketElementResponseDto;
import com.velb.shop.model.entity.BasketElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = BasketElementResponseDtoMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface BasketElementResponseDtoListMapper {

    List<BasketElementResponseDto> map(List<BasketElement> basketElementList);

}
