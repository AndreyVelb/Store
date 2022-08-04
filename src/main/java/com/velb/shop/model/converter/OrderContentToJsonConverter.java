package com.velb.shop.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.velb.shop.exception.JsonConvertedException;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderContentToJsonConverter extends StdConverter<List<OrderElement>, String> {
    private final ObjectMapper objectMapper;

    @Override
    public String convert(List<OrderElement> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonConvertedException("Не удалось получить информацию из базы данных о вашем заказе(ошибка конвертации json; ");
        }
    }
}
