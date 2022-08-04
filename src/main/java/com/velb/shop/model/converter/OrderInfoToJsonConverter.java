package com.velb.shop.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.velb.shop.exception.JsonConvertedException;
import com.velb.shop.model.entity.auxiliary.OrderInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderInfoToJsonConverter extends StdConverter<OrderInfo, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String convert(OrderInfo value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonConvertedException("Не удалось получить информацию из базы данных о вашем заказе(ошибка конвертации json; ");
        }
    }
}
