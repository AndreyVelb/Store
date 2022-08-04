package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.AdminDto;
import com.velb.shop.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AdminDtoMapper {

    AdminDto map(User user);

}
