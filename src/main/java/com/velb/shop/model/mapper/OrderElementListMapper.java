package com.velb.shop.model.mapper;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = OrderElementMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderElementListMapper {

    List<OrderElement> map(List<BasketElement> basketElementList);
}
