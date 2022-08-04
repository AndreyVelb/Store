package com.velb.shop.integration.mapper;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.mapper.OrderElementListMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OrderElementListMapperIT extends IntegrationTestBase {
    private final BasketElementRepository basketElementRepository;
    private final OrderElementListMapper orderElementListMapper;

    @Test
    void mapFromBasketElementListToOrderElementList() {
        List<BasketElement> basketElementList = new ArrayList<>();
        basketElementList.add(basketElementRepository.findById(1L).orElseThrow());
        basketElementList.add(basketElementRepository.findById(2L).orElseThrow());
        basketElementList.add(basketElementRepository.findById(3L).orElseThrow());
        basketElementList.add(basketElementRepository.findById(4L).orElseThrow());
        basketElementList.add(basketElementRepository.findById(5L).orElseThrow());

        List<OrderElement> orderElementList = orderElementListMapper.map(basketElementList);

        Assertions.assertEquals(basketElementList.size(), orderElementList.size());
        Assertions.assertEquals(basketElementList.get(0).getProduct().getId(), orderElementList.get(0).getProductForOrder().getId());
        Assertions.assertEquals(basketElementList.get(0).getProduct().getPrice(), orderElementList.get(0).getProductForOrder().getPrice());
        Assertions.assertEquals(basketElementList.get(0).getProduct().getTitle(), orderElementList.get(0).getProductForOrder().getTitle());
        Assertions.assertEquals(basketElementList.get(0).getProduct().getDescription(), orderElementList.get(0).getProductForOrder().getDescription());
        Assertions.assertEquals(basketElementList.get(0).getAmount(), orderElementList.get(0).getAmount());

        Assertions.assertEquals(basketElementList.size(), orderElementList.size());
        Assertions.assertEquals(basketElementList.get(1).getProduct().getId(), orderElementList.get(1).getProductForOrder().getId());
        Assertions.assertEquals(basketElementList.get(1).getProduct().getPrice(), orderElementList.get(1).getProductForOrder().getPrice());
        Assertions.assertEquals(basketElementList.get(1).getProduct().getTitle(), orderElementList.get(1).getProductForOrder().getTitle());
        Assertions.assertEquals(basketElementList.get(1).getProduct().getDescription(), orderElementList.get(1).getProductForOrder().getDescription());
        Assertions.assertEquals(basketElementList.get(1).getAmount(), orderElementList.get(1).getAmount());

        Assertions.assertEquals(basketElementList.size(), orderElementList.size());
        Assertions.assertEquals(basketElementList.get(2).getProduct().getId(), orderElementList.get(2).getProductForOrder().getId());
        Assertions.assertEquals(basketElementList.get(2).getProduct().getPrice(), orderElementList.get(2).getProductForOrder().getPrice());
        Assertions.assertEquals(basketElementList.get(2).getProduct().getTitle(), orderElementList.get(2).getProductForOrder().getTitle());
        Assertions.assertEquals(basketElementList.get(2).getProduct().getDescription(), orderElementList.get(2).getProductForOrder().getDescription());
        Assertions.assertEquals(basketElementList.get(2).getAmount(), orderElementList.get(2).getAmount());

        Assertions.assertEquals(basketElementList.size(), orderElementList.size());
        Assertions.assertEquals(basketElementList.get(3).getProduct().getId(), orderElementList.get(3).getProductForOrder().getId());
        Assertions.assertEquals(basketElementList.get(3).getProduct().getPrice(), orderElementList.get(3).getProductForOrder().getPrice());
        Assertions.assertEquals(basketElementList.get(3).getProduct().getTitle(), orderElementList.get(3).getProductForOrder().getTitle());
        Assertions.assertEquals(basketElementList.get(3).getProduct().getDescription(), orderElementList.get(3).getProductForOrder().getDescription());
        Assertions.assertEquals(basketElementList.get(3).getAmount(), orderElementList.get(3).getAmount());

        Assertions.assertEquals(basketElementList.size(), orderElementList.size());
        Assertions.assertEquals(basketElementList.get(4).getProduct().getId(), orderElementList.get(4).getProductForOrder().getId());
        Assertions.assertEquals(basketElementList.get(4).getProduct().getPrice(), orderElementList.get(4).getProductForOrder().getPrice());
        Assertions.assertEquals(basketElementList.get(4).getProduct().getTitle(), orderElementList.get(4).getProductForOrder().getTitle());
        Assertions.assertEquals(basketElementList.get(4).getProduct().getDescription(), orderElementList.get(4).getProductForOrder().getDescription());
        Assertions.assertEquals(basketElementList.get(4).getAmount(), orderElementList.get(4).getAmount());
    }
}
