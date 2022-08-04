package com.velb.shop.model.mapper;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = ProductForOrderMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderElementMapper {

    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "product", target = "productForOrder")
    OrderElement map(BasketElement basketElement);

}
