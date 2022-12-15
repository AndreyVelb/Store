package com.velb.shop.unit.service;

import com.velb.shop.exception.ProductChangingException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.model.converter.HashtagsFromSetConverter;
import com.velb.shop.model.converter.HashtagsToSetConverter;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.service.EmailService;
import com.velb.shop.service.ProductService;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BasketElementRepository basketElementRepository;
    @Spy
    private HashtagsFromSetConverter hashtagsFromSetConverter;
    @Spy
    private HashtagsToSetConverter hashtagsToSetConverter;
    @InjectMocks
    private ProductService productService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createProduct() {
        final Product[] newProduct = new Product[1];
        ProductCreatingDto creatingDto = ProductCreatingDto.builder()
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags(List.of("красивый", "качественный", "хороший", "красивый"))
                .build();

        when(productRepository.save(argThat(product -> product.getTitle().equals("Название товара"))))
                .thenAnswer(invocationOnMock -> {
                    newProduct[0] = invocationOnMock.getArgument(0);
                    newProduct[0].setId(100L);
                    return newProduct[0];
                });

        productService.createProduct(creatingDto);

        assertEquals(creatingDto.getTitle(), newProduct[0].getTitle());
        assertEquals(creatingDto.getDescription(), newProduct[0].getDescription());
        assertEquals(creatingDto.getPrice(), newProduct[0].getPrice());
        assertEquals(creatingDto.getAmount(), newProduct[0].getAmount());
        Set<String> hashtagsAfterSaving = hashtagsToSetConverter.convert(newProduct[0].getHashtags());
        assertEquals(3, Objects.requireNonNull(hashtagsAfterSaving).size());
        assertTrue(hashtagsAfterSaving.contains("красивый"));
        assertTrue(hashtagsAfterSaving.contains("качественный"));
        assertTrue(hashtagsAfterSaving.contains("хороший"));
    }

    @Test
    public void deleteProductWhenIsNotAllowedAndNotInBaskets() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .canBeDeleted(false)
                .productId(product.getId())
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(deletingDto.getProductId()))
                .thenReturn(emptyList());

        productService.deleteProduct(deletingDto);

        assertEquals(0, product.getAmount());
        assertEquals(0, product.getPrice());
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(basketElementRepository, times(0)).deleteAll(anyCollection());
    }

    @Test
    public void deleteProductWhenIsAllowedAndNotInBaskets() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .canBeDeleted(true)
                .productId(product.getId())
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(deletingDto.getProductId()))
                .thenReturn(emptyList());

        productService.deleteProduct(deletingDto);

        assertEquals(0, product.getAmount());
        assertEquals(0, product.getPrice());
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(basketElementRepository, times(0)).deleteAll(anyCollection());
    }

    @Test
    public void deleteProductWhenIsAllowedAndInBaskets() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .canBeDeleted(true)
                .productId(product.getId())
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(deletingDto.getProductId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        productService.deleteProduct(deletingDto);

        assertEquals(0, product.getAmount());
        assertEquals(0, product.getPrice());
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(basketElementRepository, times(1)).deleteAll(anyCollection());
    }

    @Test
    public void deleteProductThrowProductChangingEx() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .canBeDeleted(false)
                .productId(product.getId())
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(deletingDto.getProductId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        assertThrows(ProductChangingException.class, () -> productService.deleteProduct(deletingDto));

        assertTrue(0 != product.getAmount());
        assertTrue(0 != product.getPrice());
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(basketElementRepository, times(0)).deleteAll(anyCollection());
    }

    @Test
    public void deleteProductThrowProductNotFoundEx() {
        Long nonExistentProductId = 1L;
        ProductDeletingDto deletingDto = ProductDeletingDto.builder()
                .canBeDeleted(false)
                .productId(nonExistentProductId)
                .build();

        when(productRepository.findByIdWithPessimisticLock(nonExistentProductId))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(deletingDto));

        verify(productRepository, times(1)).findByIdWithPessimisticLock(nonExistentProductId);
        verify(basketElementRepository, times(0)).findAllByProductIdNotOrdered(nonExistentProductId);
        verify(basketElementRepository, times(0)).deleteAll(anyCollection());
    }

    @Test
    public void updateProductAndSendEmails() {
        final Product[] updatedProduct = new Product[1];
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(true)
                .title(null)
                .description(null)
                .productId(product.getId())
                .price(120)
                .updatingProductAmount(-5)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(product.getId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocationOnMock -> {
                    updatedProduct[0] = invocationOnMock.getArgument(0);
                    return updatedProduct[0];
                });

        productService.updateProductAndSendEmails(updatingDto);

        verify(emailService, times(1)).sendEmailAboutProductUpdating(any(), anyLong());
    }

    @Test
    public void updateProductWhenIsAllowedAndInBaskets() {
        final Product[] updatedProduct = new Product[1];
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(true)
                .title(null)
                .description(null)
                .productId(product.getId())
                .price(120)
                .updatingProductAmount(-5)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(product.getId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocationOnMock -> {
                    updatedProduct[0] = invocationOnMock.getArgument(0);
                    return updatedProduct[0];
                });

        productService.updateProduct(updatingDto);

        Set<String> hashtagsAfterUpdating = hashtagsToSetConverter.convert(updatedProduct[0].getHashtags());

        assertEquals(updatedProduct[0].getTitle(), product.getTitle());
        assertEquals(updatedProduct[0].getDescription(), product.getDescription());
        assertEquals(updatedProduct[0].getPrice(), updatingDto.getPrice());
        assertEquals(updatedProduct[0].getAmount(), product.getAmount() + updatingDto.getUpdatingProductAmount());
        assertTrue(updatedProduct[0].getAmount() >= 0);
        assertEquals(4, Objects.requireNonNull(hashtagsAfterUpdating).size());
        assertTrue(hashtagsAfterUpdating.contains("красивый"));
        assertTrue(hashtagsAfterUpdating.contains("качественный"));
        assertTrue(hashtagsAfterUpdating.contains("хороший"));
        assertTrue(hashtagsAfterUpdating.contains("чудный"));
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(productRepository, times(1)).save(any());
    }

    @Test
    public void updateProductWhenIsNotAllowedAndNotInBaskets() {
        final Product[] updatedProduct = new Product[1];
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(false)
                .title(null)
                .description(null)
                .productId(product.getId())
                .price(120)
                .updatingProductAmount(-5)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(product.getId()))
                .thenReturn(emptyList());

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocationOnMock -> {
                    updatedProduct[0] = invocationOnMock.getArgument(0);
                    return updatedProduct[0];
                });

        productService.updateProduct(updatingDto);

        Set<String> hashtagsAfterUpdating = hashtagsToSetConverter.convert(updatedProduct[0].getHashtags());

        assertEquals(updatedProduct[0].getTitle(), product.getTitle());
        assertEquals(updatedProduct[0].getDescription(), product.getDescription());
        assertEquals(updatedProduct[0].getPrice(), updatingDto.getPrice());
        assertEquals(updatedProduct[0].getAmount(), product.getAmount() + updatingDto.getUpdatingProductAmount());
        assertTrue(updatedProduct[0].getAmount() >= 0);
        assertEquals(4, Objects.requireNonNull(hashtagsAfterUpdating).size());
        assertTrue(hashtagsAfterUpdating.contains("красивый"));
        assertTrue(hashtagsAfterUpdating.contains("качественный"));
        assertTrue(hashtagsAfterUpdating.contains("хороший"));
        assertTrue(hashtagsAfterUpdating.contains("чудный"));
        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(productRepository, times(1)).save(any());
    }

    @Test
    public void updateProductWhenNotAllowedAndIsInBasketThrowProductChangingEx() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(false)
                .title(null)
                .description(null)
                .productId(product.getId())
                .price(120)
                .updatingProductAmount(-5)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(product.getId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        assertThrows(ProductChangingException.class, () -> productService.updateProduct(updatingDto));

        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(productRepository, times(0)).save(any());
    }

    @Test
    public void updateProductWhenAmountIsLowerThenZeroThrowProductChangingEx() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(100)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(true)
                .title(null)
                .description(null)
                .productId(product.getId())
                .price(120)
                .updatingProductAmount(-101)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(product.getId()))
                .thenReturn(Optional.of(product));

        when(basketElementRepository.findAllByProductIdNotOrdered(product.getId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        assertThrows(ProductChangingException.class, () -> productService.updateProduct(updatingDto));

        verify(productRepository, times(1)).findByIdWithPessimisticLock(product.getId());
        verify(basketElementRepository, times(1)).findAllByProductIdNotOrdered(product.getId());
        verify(productRepository, times(0)).save(any());
    }

    @Test
    public void updateProductThrowProductNotFoundEx() {
       Long nonExistentProductId = 1L;
        ProductUpdatingDto updatingDto = ProductUpdatingDto.builder()
                .canBeUpdated(false)
                .title(null)
                .description(null)
                .productId(nonExistentProductId)
                .price(120)
                .updatingProductAmount(-5)
                .hashtagsAsString(List.of("чудный", "красивый"))
                .build();

        when(productRepository.findByIdWithPessimisticLock(nonExistentProductId))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(updatingDto));

        verify(productRepository, times(1)).findByIdWithPessimisticLock(nonExistentProductId);
        verify(basketElementRepository, times(0)).findAllByProductIdNotOrdered(anyLong());
        verify(productRepository, times(0)).save(any());
    }

    @Test
    public void getConsumersByProductId() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();
        List<BasketElement> basketElsWhichContainsThisProduct = createBasketElementDbWithProduct(product);

        when(basketElementRepository.findAllFetchConsumerByProductId(product.getId()))
                .thenReturn(basketElsWhichContainsThisProduct);

        Set<User> consumers = productService.getConsumersByProductId(product.getId());

        assertEquals(2, consumers.size());
        verify(basketElementRepository, times(1)).findAllFetchConsumerByProductId(product.getId());
    }

    @Test
    public void getConsumersByProductIdWhenTheyAreNot() {
        Product product = Product.builder()
                .id(10L)
                .title("Название товара")
                .description("Описание товара")
                .price(100)
                .amount(1000)
                .hashtags("красивый#качественный#хороший")
                .build();

        when(basketElementRepository.findAllFetchConsumerByProductId(product.getId()))
                .thenReturn(emptyList());

        Set<User> consumers = productService.getConsumersByProductId(product.getId());

        assertEquals(0, consumers.size());
        verify(basketElementRepository, times(1)).findAllFetchConsumerByProductId(product.getId());
    }

    private List<BasketElement> createBasketElementDbWithProduct(Product product) {
        User consumer1 = createConsumer(1L);
        User consumer2 = createConsumer(2L);

        return List.of(
                BasketElement.builder()
                        .consumer(consumer1)
                        .product(product)
                        .amount(10)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer2)
                        .product(product)
                        .amount(5)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer1)
                        .product(product)
                        .amount(1)
                        .build()
        );
    }

    private User createConsumer(Long consumerId) {
        return User.builder()
                .lastName("Иванов")
                .firstName("Иван")
                .middleName("Иванович")
                .role(Role.CONSUMER)
                .email("test" + consumerId + "@mail.com")
                .password("pass")
                .build();
    }
}

















