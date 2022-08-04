package com.velb.shop.model.mapper;

import com.velb.shop.model.converter.OrderContentToJsonConverter;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.FIELD)
public abstract class OrderContentToJsonMapper {
    @Autowired
    OrderContentToJsonConverter orderContentToJsonConverter;

    public String map(List<OrderElement> orderElements) {
        return orderContentToJsonConverter.convert(orderElements);
    }
}
