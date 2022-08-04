package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.Product;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductMapper {

    Product map(ProductUpdatingDto dto);

}
