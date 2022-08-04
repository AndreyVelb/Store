package com.velb.shop.integration.service;

import com.velb.shop.exception.ProductChangingException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Hashtag;
import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.HashtagRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
    private final HashtagRepository hashtagRepository;

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
        Long newProductId = productService.createNewProduct(creatingDto);

        Optional<Product> newProduct = productRepository.findById(newProductId);
        assertTrue(newProduct.isPresent());
        assertEquals(creatingDto.getTitle(), newProduct.get().getTitle());
        assertEquals(creatingDto.getDescription(), newProduct.get().getDescription());
        assertEquals(creatingDto.getAmount(), newProduct.get().getAmount());
        assertEquals(creatingDto.getPrice(), newProduct.get().getPrice());
        List<String> newProductHashtagsAsString = new ArrayList<>();
        newProduct.get().getHashtags().forEach(hashtag ->
                newProductHashtagsAsString.add(hashtag.getHashtag()));
        List<Hashtag> hashtags = hashtagRepository.findAllByProductId(newProductId);
        hashtags.forEach(hashtag ->
                newProductHashtagsAsString.add(hashtag.getHashtag()));
        assertEquals(creatingDto.getHashtags(), newProductHashtagsAsString);
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
        List<String> expectedHashtagsAsString = new ArrayList<>();
        productBeforeUpdating.get().getHashtags().forEach(hashtag ->
                expectedHashtagsAsString.add(hashtag.getHashtag()));
        expectedHashtagsAsString.addAll(hashtags);

        productService.updateProduct(updatingDto);

        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(updatingDto.getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(updatingDto.getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
        List<Hashtag> hashtagsOfUpdatedProduct = hashtagRepository.findAllByProductId(productId);
        List<String> hashtagsAsStringAfterUpdating = new ArrayList<>();
        hashtagsOfUpdatedProduct.forEach(hashtag ->
                hashtagsAsStringAfterUpdating.add(hashtag.getHashtag()));
        assertEquals(expectedHashtagsAsString, hashtagsAsStringAfterUpdating);
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
        List<String> expectedHashtagsAsString = new ArrayList<>();
        productBeforeUpdating.get().getHashtags().forEach(hashtag ->
                expectedHashtagsAsString.add(hashtag.getHashtag()));
        expectedHashtagsAsString.addAll(hashtags);

        productService.updateProduct(updatingDto);

        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(updatingDto.getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(updatingDto.getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
        List<Hashtag> hashtagsOfUpdatedProduct = hashtagRepository.findAllByProductId(productId);
        List<String> hashtagsAsStringAfterUpdating = new ArrayList<>();
        hashtagsOfUpdatedProduct.forEach(hashtag ->
                hashtagsAsStringAfterUpdating.add(hashtag.getHashtag()));
        assertEquals(expectedHashtagsAsString, hashtagsAsStringAfterUpdating);
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
        List<String> expectedHashtagsAsString = new ArrayList<>();
        productBeforeUpdating.get().getHashtags().forEach(hashtag ->
                expectedHashtagsAsString.add(hashtag.getHashtag()));
        String expectedExceptionMessage = "Товар находится у кого-то в корзине а вы не указали что хотите изменить его в любом случае; ";

        Exception exception = assertThrows(ProductChangingException.class, ()
                -> productService.updateProduct(updatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        Optional<Product> productAfterUpdating = productRepository.findById(productId);
        assertTrue(productAfterUpdating.isPresent());
        assertEquals(productBeforeUpdating.get().getTitle(), productAfterUpdating.get().getTitle());
        assertEquals(productBeforeUpdating.get().getPrice(), productAfterUpdating.get().getPrice());
        assertEquals(productBeforeUpdating.get().getDescription(), productBeforeUpdating.get().getDescription());
        List<Hashtag> hashtagsOfUpdatedProduct = hashtagRepository.findAllByProductId(productId);
        List<String> hashtagsAsStringAfterUpdating = new ArrayList<>();
        hashtagsOfUpdatedProduct.forEach(hashtag ->
                hashtagsAsStringAfterUpdating.add(hashtag.getHashtag()));
        assertEquals(expectedHashtagsAsString, hashtagsAsStringAfterUpdating);
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
        List<BasketElement> basketElsThatContainsProduct = basketElementRepository.findAllByProductId(productId);
        assertTrue(basketElsThatContainsProduct.isEmpty());
        List<Hashtag> productHashtags = hashtagRepository.findAllByProductId(productId);
        assertEquals(2, productHashtags.size());

        productService.deleteProduct(deletingDto);

        Optional<Product> productAfterDeleting = productRepository.findById(productId);
        assertTrue(productAfterDeleting.isEmpty());
        List<Hashtag> productHashtagsAfterDeleting = hashtagRepository.findAllByProductId(productId);
        assertTrue(productHashtagsAfterDeleting.isEmpty());
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
        List<Hashtag> productHashtags = hashtagRepository.findAllByProductId(productId);
        assertEquals(3, productHashtags.size());

        productService.deleteProduct(deletingDto);

        Optional<Product> productAfterDeleting = productRepository.findById(productId);
        assertTrue(productAfterDeleting.isEmpty());
        List<BasketElement> basketElsThatContainsProductAfterDeleting = basketElementRepository
                .findAllByProductId(productId);
        assertTrue(basketElsThatContainsProductAfterDeleting.isEmpty());
        List<Hashtag> productHashtagsAfterDeleting = hashtagRepository.findAllByProductId(productId);
        assertTrue(productHashtagsAfterDeleting.isEmpty());
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
