package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.ProductForOrderHistoryDto;
import com.velb.shop.model.entity.Product;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductForOrderHistoryDtoMapper {

    ProductForOrderHistoryDto map(Product product);

}
