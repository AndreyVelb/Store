package com.velb.shop.integration.mapper;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.mapper.OrderElementMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@RequiredArgsConstructor
public class OrderElementMapperIT extends IntegrationTestBase {
    private final BasketElementRepository basketElementRepository;
    private final OrderElementMapper orderElementMapper;

    @Test
    void mapFromBasketElementToOrderElement() {
        Optional<BasketElement> mayBeBasketElement = basketElementRepository.findById(1L);
        Assertions.assertTrue(mayBeBasketElement.isPresent());
        BasketElement basketElement = mayBeBasketElement.get();

        OrderElement orderElement = orderElementMapper.map(basketElement);

        Assertions.assertEquals(basketElement.getProduct().getId(), orderElement.getProductForOrder().getId());
        Assertions.assertEquals(basketElement.getProduct().getPrice(), orderElement.getProductForOrder().getPrice());
        Assertions.assertEquals(basketElement.getProduct().getTitle(), orderElement.getProductForOrder().getTitle());
        Assertions.assertEquals(basketElement.getProduct().getDescription(), orderElement.getProductForOrder().getDescription());
        Assertions.assertEquals(basketElement.getAmount(), orderElement.getAmount());
    }
}
