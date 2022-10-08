package com.velb.shop.integration.service;

import com.velb.shop.exception.ProductChangingException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
public class ProductServiceIT extends IntegrationTestBase {
    private final ProductService productService;
    private final BasketElementRepository basketElementRepository;
    private final ProductRepository productRepository;

    @Test
    void createNewProduct() {
        List<String> hashtagsAsString = List.of("чудесный", "новый", "товар");
        ProductCreatingDto creatingDto = ProductCreatingDto.builder()
                .title("Новый товар")
                .description("Прекрасный новый товар для нашего магазина")
                .price(100)
                .amount(1000)
                .hashtags(hashtagsAsString)
                .build();

        Long newProductId = productService.createProduct(creatingDto);

        Optional<Product> newProduct = productRepository.findById(newProductId);
        assertTrue(newProduct.isPresent());
        assertEquals(creatingDto.getTitle(), newProduct.get().getTitle());
        assertEquals(creatingDto.getDescription(), newProduct.get().getDescription());
        assertEquals(creatingDto.getAmount(), newProduct.get().getAmount());
        assertEquals(creatingDto.getPrice(), newProduct.get().getPrice());
    }

    @Test
    void updateProductWhichIsNotPresentInBasket() {
        long productId = 4L;
        List<String> hashtags = List.of("новый");
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .productId(productId)
                .title("Новое название")
                .canBeUpdated(false)
                .description(null)
                .hashtagsAsString(hashtags)
                .price(20)
                .build();

        productService.updateProduct(updatingDto);

        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(updatingDto.getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(updatingDto.getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
    }

    @Test
    void updateProductWhichIsPresentInBasket() {
        long productId = 10L;
        List<String> hashtags = List.of("новый");
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .productId(productId)
                .title("Новое название")
                .canBeUpdated(true)
                .description(null)
                .price(20)
                .hashtagsAsString(hashtags)
                .build();

        productService.updateProduct(updatingDto);

        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(updatingDto.getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(updatingDto.getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
    }

    @Test
    void updateProductThrowProductNotFoundEx() {
        long nonExistedProductId = 100000L;
        List<String> hashtags = List.of("новый");
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .productId(nonExistedProductId)
                .title("Новое название")
                .canBeUpdated(true)
                .description(null)
                .price(20)
                .hashtagsAsString(hashtags)
                .build();
        String expectedExceptionMessage = "Товара с id " + nonExistedProductId + " не существует; ";

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> productService.updateProduct(updatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void updateProductThrowProductChangingEx() {
        long productId = 10L;
        List<String> hashtags = List.of("новый");
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .productId(productId)
                .title("Новое название")
                .canBeUpdated(false)
                .description(null)
                .price(20)
                .hashtagsAsString(hashtags)
                .build();

        String expectedExceptionMessage = "Товар находится у кого-то в корзине а вы не указали что хотите изменить его в любом случае; ";

        Exception exception = assertThrows(ProductChangingException.class, ()
                -> productService.updateProduct(updatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(productBeforeUpdating.get().getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(productBeforeUpdating.get().getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
    }

    @Test
    void updateProductAmount() {
        Long productId = 2L;
        int updatedProductAmount = -7;
        ProductAmountUpdatingDto updatingDto = ProductAmountUpdatingDto.builder()
                .productId(productId)
                .updateAmount(updatedProductAmount)
                .build();
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        int amountBeforeUpdate = productBeforeUpdating.get().getAmount();

        productService.updateProductAmount(updatingDto);

        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(amountBeforeUpdate + updatedProductAmount, productAfterUpdating.get().getAmount());
    }

    @Test
    void updateProductAmountThrowProductNotFoundEx() {
        long nonExistedProductId = 100000L;
        int updatedProductAmount = -7;
        ProductAmountUpdatingDto updatingDto = ProductAmountUpdatingDto.builder()
                .productId(nonExistedProductId)
                .updateAmount(updatedProductAmount)
                .build();
        String expectedExceptionMessage = "Товара с id " + nonExistedProductId + " не существует; ";

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> productService.updateProductAmount(updatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void updateProductAmountThrowProductChangingEx() {
        Long productId = 2L;
        int nonAllowedUpdatedProductAmount = -1000;
        ProductAmountUpdatingDto updatingDto = ProductAmountUpdatingDto.builder()
                .productId(productId)
                .updateAmount(nonAllowedUpdatedProductAmount)
                .build();
        Optional<Product> productBeforeUpdating = productRepository.findById(productId);
        assertTrue(productBeforeUpdating.isPresent());
        int amountBeforeUpdate = productBeforeUpdating.get().getAmount();
        String expectedExceptionMessage = "Вы пытаетесь изменить количество товара на недопустимое значение " +
                "- оно станет меньше 0; ";

        Exception exception = assertThrows(ProductChangingException.class, ()
                -> productService.updateProductAmount(updatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(amountBeforeUpdate, productAfterUpdating.get().getAmount());
    }

    @Test
    void deleteProductWhichIsNotPresentInBasket() {
        Long productId = 4L;
        boolean canBeUpdated = false;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(productId)
                .canBeDeleted(canBeUpdated)
                .build();
        Optional<Product> productBeforeDeleting = productRepository.findById(productId);
        assertTrue(productBeforeDeleting.isPresent());
        List<BasketElement> basketElsThatContainsProduct = basketElementRepository.findAllByProductIdNotOrdered(productId);
        assertTrue(basketElsThatContainsProduct.isEmpty());

        productService.deleteProduct(deletingDto);

        Optional<Product> productAfterDeleting = productRepository.findById(productId);
        assertTrue(productAfterDeleting.isPresent());
        assertEquals(0, (int) productAfterDeleting.get().getAmount());
    }

    @Test
    void deleteProductWhichIsPresentInBasket() {
        Long productId = 1L;
        boolean canBeUpdated = true;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(productId)
                .canBeDeleted(canBeUpdated)
                .build();
        List<BasketElement> basketElsThatContainsProductBeforeDeleting = basketElementRepository
                .findAllByProductId(productId);
        assertFalse(basketElsThatContainsProductBeforeDeleting.isEmpty());

        productService.deleteProduct(deletingDto);

        Optional<Product> productAfterDeleting = productRepository.findById(productId);
        assertTrue(productAfterDeleting.isPresent());
        assertEquals(0, (int) productAfterDeleting.get().getAmount());
        List<BasketElement> basketElsThatContainsProductAfterDeleting = basketElementRepository
                .findAllByProductIdNotOrdered(productId);
        assertTrue(basketElsThatContainsProductAfterDeleting.isEmpty());
    }

    @Test
    void deleteProductThrowProductNotFoundEx() {
        long nonExistedProductId = 100000L;
        boolean canBeUpdated = true;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(nonExistedProductId)
                .canBeDeleted(canBeUpdated)
                .build();
        String expectedExceptionMessage = "Товара с id " + nonExistedProductId + " не существует; ";

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> productService.deleteProduct(deletingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void deleteProductThrowProductChangingEx() {
        long productId = 10L;
        boolean canBeUpdated = false;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .productId(productId)
                .canBeDeleted(canBeUpdated)
                .build();
        Optional<Product> productBeforeDeleting = productRepository.findById(productId);
        assertTrue(productBeforeDeleting.isPresent());
        String expectedExceptionMessage = "Товар находится у кого-то в корзине а вы не указали что хотите удалить его в любом случае; ";

        Exception exception = assertThrows(ProductChangingException.class, ()
                -> productService.deleteProduct(deletingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        Optional<Product> productAfterDeleting = productRepository.findById(productId);
        assertTrue(productAfterDeleting.isPresent());
    }
}
