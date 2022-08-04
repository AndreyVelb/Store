package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.ConsumerDto;
import com.velb.shop.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ConsumerDtoMapper {

    ConsumerDto map(User user);

}
