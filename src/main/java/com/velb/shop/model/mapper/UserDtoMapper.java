package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.UserDto;
import com.velb.shop.model.dto.UserRegistrationDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = PasswordEncoderMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserDtoMapper {

    @Mapping(source = "rawPassword", target = "encryptedPassword", qualifiedBy = EncodedMapping.class)
    UserDto map(UserRegistrationDto userRegistrationDto);
}
