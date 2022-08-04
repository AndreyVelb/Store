package com.velb.shop.model.mapper;

import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.auxiliary.ProductForOrder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductForOrderMapper {

    ProductForOrder map(Product product);

}
