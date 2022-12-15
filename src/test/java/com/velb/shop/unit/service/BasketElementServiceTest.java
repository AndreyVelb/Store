package com.velb.shop.unit.service;

import com.velb.shop.exception.BasketElementNotFoundException;
import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.TotalRuntimeException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.model.dto.BasketDto;
import com.velb.shop.model.dto.BasketElementDto;
import com.velb.shop.model.dto.BasketElementUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.model.mapper.BasketElementResponseDtoListMapper;
import com.velb.shop.model.mapper.BasketElementResponseDtoMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.repository.UserRepository;
import com.velb.shop.service.BasketElementService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BasketElementServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BasketElementRepository basketElementRepository;
    @Spy
    private BasketElementResponseDtoMapper basketElementResponseDtoMapper;
    @Spy
    private BasketElementResponseDtoListMapper basketElementResponseDtoListMapper;
    @InjectMocks
    private BasketElementService basketElementService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getConsumersBasket() {
        User consumer = createConsumer(1L);

        List<BasketElement> expectedBasket = List.of(new BasketElement[]{
                createBasketElementWithoutOrder(1L, consumer, createProduct(1L, 10), 4),
                createBasketElementWithoutOrder(2L, consumer, createProduct(2L, 20), 5),
                createBasketElementWithoutOrder(3L, consumer, createProduct(3L, 30), 6)});

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());
        doReturn(expectedBasket)
                .when(basketElementRepository)
                .findAllByConsumerIdNotOrdered(consumer.getId());

        BasketDto expectedResult = new BasketDto(basketElementResponseDtoListMapper.map(expectedBasket));

        var actualResult = basketElementService.getConsumersBasket(consumer.getId());

        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getConsumersBasketThrowUserNotFoundEx() {
        User consumer = createConsumer(1L);

        List<BasketElement> expectedBasket = List.of(new BasketElement[]{
                createBasketElementWithoutOrder(1L, consumer, createProduct(1L, 10), 3),
                createBasketElementWithoutOrder(2L, consumer, createProduct(2L, 20), 4),
                createBasketElementWithoutOrder(3L, consumer, createProduct(3L, 30), 6)});

        doReturn(Optional.empty())
                .when(userRepository)
                .findById(consumer.getId());
        doReturn(expectedBasket)
                .when(basketElementRepository)
                .findAllByConsumerId(consumer.getId());

        assertThrows(UserNotFoundException.class, () -> basketElementService.getConsumersBasket(consumer.getId()));
    }

    @Test
    public void getConsumersBasketThrowBasketIsEmptyEx() {
        User consumer = createConsumer(1L);

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());
        doReturn(Collections.emptyList())
                .when(basketElementRepository)
                .findAllByConsumerId(consumer.getId());

        assertThrows(BasketIsEmptyException.class, () -> basketElementService.getConsumersBasket(consumer.getId()));
    }

    @Test
    public void addProductsToBasket() {
        User consumer = createConsumer(1L);
        Product product10 = createProduct(10L, 100);
        final BasketElement[] newAddedBasketEl = new BasketElement[1];
        List<BasketElement> basket = createBasket(consumer);
        List<BasketElementDto> elementsForAdding = List.of(
                BasketElementDto.builder().productId(2L).amount(5).build(),
                BasketElementDto.builder().productId(3L).amount(10).build(),
                BasketElementDto.builder().productId(product10.getId()).amount(10).build());

        int expectedAmountAfterAdding2 = basket.get(1).getAmount() + elementsForAdding.get(0).getAmount();
        int expectedAmountAfterAdding3 = basket.get(2).getAmount() + elementsForAdding.get(1).getAmount();
        int expectedAmountAfterAdding10 = elementsForAdding.get(2).getAmount();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(basket.get(1).getProduct()))
                .when(productRepository)
                .findById(basket.get(1).getProduct().getId());

        doReturn(Optional.of(basket.get(2).getProduct()))
                .when(productRepository)
                .findById(basket.get(2).getProduct().getId());

        doReturn(Optional.of(product10))
                .when(productRepository)
                .findById(product10.getId());

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 2L);

        doReturn(Optional.of(basket.get(2)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 3L);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 10L);

        doReturn(basket.get(1))
                .when(basketElementRepository)
                .save(basket.get(1));

        doReturn(basket.get(2))
                .when(basketElementRepository)
                .save(basket.get(2));

        when(basketElementRepository.save(argThat(basketElement -> basketElement.getProduct().getId() == 10L)))
                .thenAnswer(
                        invocationOnMock -> {
                            newAddedBasketEl[0] = invocationOnMock.getArgument(0);
                            newAddedBasketEl[0].setId(101L);
                            return newAddedBasketEl[0];
                        });

        basketElementService.addProductsToBasket(consumer.getId(), elementsForAdding);

        assertEquals(expectedAmountAfterAdding2, basket.get(1).getAmount());
        assertEquals(expectedAmountAfterAdding3, basket.get(2).getAmount());
        assertEquals(expectedAmountAfterAdding10, newAddedBasketEl[0].getAmount());

        verify(basketElementRepository, times(3))
                .findByConsumerIdAndProductIdFetchProductNotOrdered(any(Long.class), any(Long.class));
    }

    @Test
    public void addProductsToBasketThrowTotalRuntimeEx() {
        User consumer = createConsumer(1L);
        Product product10 = createProduct(10L, 100);
        List<BasketElement> basket = createBasket(consumer);
        List<BasketElementDto> elementsForAdding = List.of(
                BasketElementDto.builder().productId(2L).amount(5).build(),
                BasketElementDto.builder().productId(3L).amount(1000000).build(),
                BasketElementDto.builder().productId(product10.getId()).amount(10).build());

        int expectedAmountAfterAdding2 = basket.get(1).getAmount() + elementsForAdding.get(0).getAmount();
        int expectedAmountAfterAdding3 = basket.get(2).getAmount();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(basket.get(1).getProduct()))
                .when(productRepository)
                .findById(basket.get(1).getProduct().getId());

        doReturn(Optional.of(basket.get(2).getProduct()))
                .when(productRepository)
                .findById(basket.get(2).getProduct().getId());

        doReturn(Optional.empty())
                .when(productRepository)
                .findById(10L);

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 2L);

        doReturn(Optional.of(basket.get(2)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 3L);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 10L);

        doReturn(basket.get(1))
                .when(basketElementRepository)
                .save(basket.get(1));

        TotalRuntimeException exception = assertThrows(TotalRuntimeException.class,
                () -> basketElementService.addProductsToBasket(consumer.getId(), elementsForAdding));

        assertTrue(exception.getExceptionList().get(0) instanceof InsufficientProductQuantityException);
        assertTrue(exception.getExceptionList().get(1) instanceof ProductNotFoundException);
        assertEquals(expectedAmountAfterAdding2, basket.get(1).getAmount());
        assertEquals(expectedAmountAfterAdding3, basket.get(2).getAmount());

        verify(basketElementRepository, times(3))
                .findByConsumerIdAndProductIdFetchProductNotOrdered(any(Long.class), any(Long.class));
    }

    @Test
    public void changeBasketElements() {
        User consumer = createConsumer(1L);
        Product product10 = createProduct(10L, 100);
        final BasketElement[] newAddedBasketEl = new BasketElement[1];
        List<BasketElement> basket = createBasket(consumer);
        List<BasketElementUpdatingDto> updatingDtoList = List.of(
                BasketElementUpdatingDto.builder().productId(1L).amount(4).build(),
                BasketElementUpdatingDto.builder().productId(2L).amount(5).build(),
                BasketElementUpdatingDto.builder().productId(10L).amount(10).build());

        int expectedAmountAfterAdding1 = updatingDtoList.get(0).getAmount();
        int expectedAmountAfterAdding2 = updatingDtoList.get(1).getAmount();
        int expectedAmountAfterAdding10 = updatingDtoList.get(2).getAmount();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(basket.get(0).getProduct()))
                .when(productRepository)
                .findById(basket.get(0).getProduct().getId());

        doReturn(Optional.of(basket.get(1).getProduct()))
                .when(productRepository)
                .findById(basket.get(1).getProduct().getId());

        doReturn(Optional.of(product10))
                .when(productRepository)
                .findById(product10.getId());

        doReturn(Optional.of(basket.get(0)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 1L);

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 2L);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 10L);

        doReturn(basket.get(0))
                .when(basketElementRepository)
                .save(basket.get(0));

        doReturn(basket.get(1))
                .when(basketElementRepository)
                .save(basket.get(1));

        when(basketElementRepository.save(argThat(basketElement -> basketElement.getProduct().getId() == 10L)))
                .thenAnswer(
                        invocationOnMock -> {
                            newAddedBasketEl[0] = invocationOnMock.getArgument(0);
                            newAddedBasketEl[0].setId(101L);
                            return newAddedBasketEl[0];
                        });

        basketElementService.changeBasketElements(updatingDtoList, consumer.getId());

        assertEquals(expectedAmountAfterAdding1, basket.get(0).getAmount());
        assertEquals(expectedAmountAfterAdding2, basket.get(1).getAmount());
        assertEquals(expectedAmountAfterAdding10, newAddedBasketEl[0].getAmount());

        verify(basketElementRepository, times(3))
                .findByConsumerIdAndProductIdFetchProductNotOrdered(any(Long.class), any(Long.class));
        verify(basketElementRepository, times(3))
                .save(any(BasketElement.class));
    }

    @Test
    public void changeBasketElementsThrowTotalRuntimeEx() {
        User consumer = createConsumer(1L);
        Product product10 = createProduct(10L, 100);
        List<BasketElement> basket = createBasket(consumer);
        List<BasketElementUpdatingDto> updatingDtoList = List.of(
                BasketElementUpdatingDto.builder().productId(1L).amount(10000).build(),
                BasketElementUpdatingDto.builder().productId(2L).amount(5).build(),
                BasketElementUpdatingDto.builder().productId(10L).amount(10).build());

        int expectedAmountAfterAdding1 = basket.get(0).getAmount();
        int expectedAmountAfterAdding2 = updatingDtoList.get(1).getAmount();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(basket.get(0).getProduct()))
                .when(productRepository)
                .findById(basket.get(0).getProduct().getId());

        doReturn(Optional.of(basket.get(1).getProduct()))
                .when(productRepository)
                .findById(basket.get(1).getProduct().getId());

        doReturn(Optional.empty())
                .when(productRepository)
                .findById(product10.getId());

        doReturn(Optional.of(basket.get(0)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 1L);

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 2L);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), 10L);

        doReturn(basket.get(1))
                .when(basketElementRepository)
                .save(basket.get(1));

        TotalRuntimeException exception = assertThrows(TotalRuntimeException.class,
                () -> basketElementService.changeBasketElements(updatingDtoList, consumer.getId()));

        assertTrue(exception.getExceptionList().get(0) instanceof InsufficientProductQuantityException);
        assertTrue(exception.getExceptionList().get(1) instanceof ProductNotFoundException);
        assertEquals(expectedAmountAfterAdding1, basket.get(0).getAmount());
        assertEquals(expectedAmountAfterAdding2, basket.get(1).getAmount());

        verify(basketElementRepository, times(1))
                .findByConsumerIdAndProductIdFetchProductNotOrdered(any(Long.class), any(Long.class));
        verify(basketElementRepository, times(1))
                .save(any(BasketElement.class));
    }

    @Test
    public void deleteBasketElements() {
        User consumer = createConsumer(1L);

        List<BasketElement> basket = createBasket(consumer);
        List<Long> basketElementsIdList = List.of(1L, 2L, 4L);

        doReturn(Optional.of(basket.get(0)))
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(1L, consumer.getId());

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(2L, consumer.getId());

        doReturn(Optional.of(basket.get(3)))
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(4L, consumer.getId());

        basketElementService.deleteBasketElements(basketElementsIdList, consumer.getId());

        verify(basketElementRepository, times(3)).delete(any(BasketElement.class));
    }

    @Test
    public void deleteBasketElementsThrowTotalRuntimeEx() {
        User consumer = createConsumer(1L);

        List<BasketElement> basket = createBasket(consumer);
        List<Long> basketElementsIdList = List.of(1L, 2L, 4L);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(1L, consumer.getId());

        doReturn(Optional.of(basket.get(1)))
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(2L, consumer.getId());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(4L, consumer.getId());

        TotalRuntimeException exception = assertThrows(TotalRuntimeException.class,
                () -> basketElementService.deleteBasketElements(basketElementsIdList, consumer.getId()));


        assertTrue(exception.getExceptionList().get(0) instanceof BasketElementNotFoundException);
        assertTrue(exception.getExceptionList().get(1) instanceof BasketElementNotFoundException);
        verify(basketElementRepository, times(1)).delete(any(BasketElement.class));
    }

    @Test
    public void addProductToBasketWhenItIsThere() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        BasketElement basketElementInBasket = createBasketElementWithoutOrder(1L, consumer, product, 5);

        int expectedTotalAmount = basketElementDto.getAmount() + basketElementInBasket.getAmount();

        doReturn(Optional.of(basketElementInBasket))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(basketElementInBasket)
                .when(basketElementRepository)
                .save(basketElementInBasket);

        Long actualBasketElId = basketElementService.addProductToBasket(consumer.getId(), basketElementDto);

        assertEquals(basketElementInBasket.getId(), actualBasketElId);
        assertEquals(expectedTotalAmount, basketElementInBasket.getAmount());
    }

    @Test
    public void addProductToBasketWhenItIsThereAndWeAddAllProducts() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(4)
                .build();

        BasketElement basketElementInBasket = createBasketElementWithoutOrder(1L, consumer, product, 6);

        int expectedTotalAmount = basketElementDto.getAmount() + basketElementInBasket.getAmount();

        doReturn(Optional.of(basketElementInBasket))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(basketElementInBasket)
                .when(basketElementRepository)
                .save(basketElementInBasket);

        Long actualBasketElId = basketElementService.addProductToBasket(consumer.getId(), basketElementDto);

        assertEquals(basketElementInBasket.getId(), actualBasketElId);
        assertEquals(expectedTotalAmount, basketElementInBasket.getAmount());
    }

    @Test
    public void addProductToBasketWhenItIsThereThrowInsufficientProductQuantityEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 6);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(2)
                .build();

        BasketElement basketElementInBasket = createBasketElementWithoutOrder(1L, consumer, product, 5);

        doReturn(Optional.of(basketElementInBasket))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        assertThrows(InsufficientProductQuantityException.class,
                () -> basketElementService.addProductToBasket(consumer.getId(), basketElementDto));
    }

    @Test
    public void addProductToBasketItIsNotThere() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        BasketElement basketElement = createBasketElementWithoutOrder(2L, consumer, product, basketElementDto.getAmount());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(basketElement)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElId = basketElementService.addProductToBasket(consumer.getId(), basketElementDto);

        assertEquals(basketElement.getId(), actualBasketElId);
        assertEquals(basketElementDto.getAmount(), basketElement.getAmount());
    }

    @Test
    public void addProductToBasketWhenItIsNotThereAndWeAddAllProducts() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        BasketElement basketElement = createBasketElementWithoutOrder(2L, consumer, product, basketElementDto.getAmount());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(basketElement)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElId = basketElementService.addProductToBasket(consumer.getId(), basketElementDto);

        assertEquals(basketElement.getId(), actualBasketElId);
        assertEquals(basketElementDto.getAmount(), basketElement.getAmount());
    }

    @Test
    public void addProductToBasketWhenItIsNotThereThrowUserNotFoundEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        BasketElement basketElement = createBasketElementWithoutOrder(2L, consumer, product, basketElementDto.getAmount());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.empty())
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElement)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        assertThrows(UserNotFoundException.class,
                () -> basketElementService.addProductToBasket(consumer.getId(), basketElementDto));
    }

    @Test
    public void addProductToBasketWhenItIsNotThereThrowProductNotFoundEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        BasketElement basketElement = createBasketElementWithoutOrder(2L, consumer, product, basketElementDto.getAmount());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.empty())
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElement)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        assertThrows(ProductNotFoundException.class,
                () -> basketElementService.addProductToBasket(consumer.getId(), basketElementDto));
    }

    @Test
    public void addProductToBasketWhenItIsNotThereThrowInsufficientProductQuantityEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(product.getId())
                .amount(11)
                .build();

        BasketElement basketElement = createBasketElementWithoutOrder(2L, consumer, product, basketElementDto.getAmount());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(basketElement)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        assertThrows(InsufficientProductQuantityException.class,
                () -> basketElementService.addProductToBasket(consumer.getId(), basketElementDto));
    }

    @Test
    public void changeBasketElementWhenItIsThere() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElement basketElementForUpdate = createBasketElementWithoutOrder(1L, consumer, product, 30);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        doReturn(Optional.of(basketElementForUpdate))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElementForUpdate)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElementId = basketElementService.changeBasketElement(updatingDto, consumer.getId());

        assertEquals(basketElementForUpdate.getId(), actualBasketElementId);
        assertEquals(basketElementForUpdate.getAmount(), basketElementForUpdate.getAmount());
    }

    @Test
    public void changeBasketElementWhenItIsThereAndWeAddAllProducts() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);
        BasketElement basketElementForUpdate = createBasketElementWithoutOrder(1L, consumer, product, 10);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        doReturn(Optional.of(basketElementForUpdate))
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElementForUpdate)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElementId = basketElementService.changeBasketElement(updatingDto, consumer.getId());

        assertEquals(basketElementForUpdate.getId(), actualBasketElementId);
        assertEquals(basketElementForUpdate.getAmount(), basketElementForUpdate.getAmount());
    }

    @Test
    public void changeBasketElementWhenItIsNotThereAndWeAddAllProducts() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);
        BasketElement basketElementForUpdate = createBasketElementWithoutOrder(1L, consumer, product, 10);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(basketElementForUpdate.getAmount())
                .build();

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElementForUpdate)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElementId = basketElementService.changeBasketElement(updatingDto, consumer.getId());

        assertEquals(basketElementForUpdate.getId(), actualBasketElementId);
        assertEquals(basketElementForUpdate.getAmount(), basketElementForUpdate.getAmount());
    }

    @Test
    public void changeBasketElementWhenItIsNotThere() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 100);
        BasketElement basketElementForUpdate = createBasketElementWithoutOrder(1L, consumer, product, 30);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(basketElementForUpdate.getAmount())
                .build();

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByConsumerIdAndProductIdFetchProductNotOrdered(consumer.getId(), product.getId());

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        doReturn(basketElementForUpdate)
                .when(basketElementRepository)
                .save(any(BasketElement.class));

        Long actualBasketElementId = basketElementService.changeBasketElement(updatingDto, consumer.getId());

        assertEquals(basketElementForUpdate.getId(), actualBasketElementId);
        assertEquals(basketElementForUpdate.getAmount(), basketElementForUpdate.getAmount());
    }

    @Test
    public void changeBasketElementThrowUserNotFoundEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        doReturn(Optional.empty())
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        assertThrows(UserNotFoundException.class,
                () -> basketElementService.changeBasketElement(updatingDto, consumer.getId()));
    }

    @Test
    public void changeBasketElementThrowProductNotFoundEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(10)
                .build();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.empty())
                .when(productRepository)
                .findById(product.getId());

        assertThrows(ProductNotFoundException.class,
                () -> basketElementService.changeBasketElement(updatingDto, consumer.getId()));
    }

    @Test
    public void changeBasketElementThrowInsufficientProductQuantityEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(1L, 10);

        BasketElementUpdatingDto updatingDto = BasketElementUpdatingDto.builder()
                .productId(product.getId())
                .amount(11)
                .build();

        doReturn(Optional.of(consumer))
                .when(userRepository)
                .findById(consumer.getId());

        doReturn(Optional.of(product))
                .when(productRepository)
                .findById(product.getId());

        assertThrows(InsufficientProductQuantityException.class,
                () -> basketElementService.changeBasketElement(updatingDto, consumer.getId()));
    }

    @Test
    public void deleteBasketElement() {
        User consumer = createConsumer(1L);
        Product product = createProduct(5L, 100);
        BasketElement basketElementForDeletion = createBasketElementWithoutOrder(1L, consumer, product, 10);

        doReturn(Optional.of(basketElementForDeletion))
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(basketElementForDeletion.getId(), consumer.getId());

        doNothing()
                .when(basketElementRepository)
                .delete(any(BasketElement.class));

        basketElementService.deleteBasketElement(basketElementForDeletion.getId(), consumer.getId());

        verify(basketElementRepository, times(1))
                .findByIdAndByConsumerIdNotOrdered(basketElementForDeletion.getId(), consumer.getId());
        verify(basketElementRepository, times(1)).delete(any(BasketElement.class));
    }

    @Test
    public void deleteBasketElementThrowBasketElementNotFoundEx() {
        User consumer = createConsumer(1L);
        Product product = createProduct(5L, 100);
        BasketElement basketElementForDeletion = createBasketElementWithoutOrder(1L, consumer, product, 10);

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findByIdAndByConsumerIdNotOrdered(basketElementForDeletion.getId(), consumer.getId());

        assertThrows(BasketElementNotFoundException.class,
                () -> basketElementService.deleteBasketElement(basketElementForDeletion.getId(), consumer.getId()));

        verify(basketElementRepository, times(1))
                .findByIdAndByConsumerIdNotOrdered(basketElementForDeletion.getId(), consumer.getId());
        verify(basketElementRepository, times(0)).delete(any(BasketElement.class));
    }

    @Test
    public void getBasketElement() {
        User consumer = createConsumer(2L);
        Long desiredBasketElId = 1L;
        BasketElement basketElement = createBasketElementWithoutOrder(desiredBasketElId, consumer, createProduct(1L, 30), 5);

        doReturn(Optional.of(basketElement))
                .when(basketElementRepository)
                .findById(1L);

        var expectedBasketElResponseDto = basketElementResponseDtoMapper.map(basketElement);

        var foundBasketEl = basketElementService.getBasketElement(desiredBasketElId);

        assertEquals(expectedBasketElResponseDto, foundBasketEl);
    }

    @Test
    public void getBasketElementThrowBasketElNotFoundEx() {
        Long nonExistentBasketElId = 1L;

        doReturn(Optional.empty())
                .when(basketElementRepository)
                .findById(1L);

        assertThrows(BasketElementNotFoundException.class, () -> basketElementService.getBasketElement(nonExistentBasketElId));
    }

    private List<BasketElement> createBasket(User consumer) {
        Product product1 = createProduct(1L, 10);
        Product product2 = createProduct(2L, 20);
        Product product3 = createProduct(3L, 30);
        Product product4 = createProduct(4L, 40);

        return List.of(
                createBasketElementWithoutOrder(1L, consumer, product1, 1),
                createBasketElementWithoutOrder(2L, consumer, product2, 2),
                createBasketElementWithoutOrder(3L, consumer, product3, 3),
                createBasketElementWithoutOrder(4L, consumer, product4, 4)
        );
    }

    private BasketElement createBasketElementWithoutOrder(Long basketElementId, User consumer, Product product, int amount) {
        return BasketElement.builder()
                .id(basketElementId)
                .consumer(consumer)
                .product(product)
                .priceInOrder(null)
                .amount(amount)
                .order(null)
                .productBookingTime(null)
                .build();
    }

    private User createConsumer(Long consumerId) {
        return User.builder()
                .id(consumerId)
                .role(Role.CONSUMER)
                .email("test@mail.ru")
                .password("testpassword")
                .lastName("Иванов")
                .firstName("Иван")
                .middleName("Иванович")
                .build();
    }

    private Product createProduct(Long productId, int amount) {
        return Product.builder()
                .id(productId)
                .title("ТестовоеНазвание")
                .description("ТестовоеОписание")
                .amount(amount)
                .price(100)
                .build();
    }

}