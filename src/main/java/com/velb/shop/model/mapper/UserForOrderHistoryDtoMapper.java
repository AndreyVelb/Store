package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.UserForOrderHistoryDto;
import com.velb.shop.model.entity.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserForOrderHistoryDtoMapper {

    UserForOrderHistoryDto map(User user);

}
