package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.ProductFromBasketResponseDto;
import com.velb.shop.model.entity.Product;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductFromBasketResponseDtoMapper {

    ProductFromBasketResponseDto map(Product product);

}
