package com.velb.shop.integration.mapper;

import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.model.entity.auxiliary.ProductForOrder;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.mapper.ProductForOrderMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

@RequiredArgsConstructor
public class ProductForOrderMapperIT extends IntegrationTestBase {
    private final ProductRepository productRepository;
    private final ProductForOrderMapper productForOrderMapper;

    @Test
    void mapFromProductToProductForOrder() {
        Optional<Product> mayBeProduct = productRepository.findById(1L);
        Assertions.assertTrue(mayBeProduct.isPresent());
        Product product = mayBeProduct.get();

        ProductForOrder productForOrder = productForOrderMapper.map(product);

        Assertions.assertEquals(product.getId(), productForOrder.getId());
        Assertions.assertEquals(product.getTitle(), productForOrder.getTitle());
        Assertions.assertEquals(product.getDescription(), productForOrder.getDescription());
        Assertions.assertEquals(product.getPrice(), productForOrder.getPrice());
    }
}
