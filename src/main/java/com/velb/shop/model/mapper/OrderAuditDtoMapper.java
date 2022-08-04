package com.velb.shop.model.mapper;

import com.velb.shop.model.dto.OrderAuditDto;
import com.velb.shop.model.entity.OrderAuditRecord;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(uses = {AdminDtoMapper.class, ConsumerDtoMapper.class},
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderAuditDtoMapper {

    OrderAuditDto map(OrderAuditRecord orderAuditRecord);

}
