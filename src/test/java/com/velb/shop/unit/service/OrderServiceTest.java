package com.velb.shop.unit.service;

import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.OrderStatus;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.model.mapper.OrderHistoryDtoMapper;
import com.velb.shop.model.mapper.ProductForOrderMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.repository.UserRepository;
import com.velb.shop.service.MessageCreatorService;
import com.velb.shop.service.OrderService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @Mock
    private MessageCreatorService messageCreatorService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private BasketElementRepository basketElementRepository;
    @Spy
    private ProductForOrderMapper productForOrderMapper;
    @Spy
    private OrderHistoryDtoMapper orderHistoryDtoMapper;
    @InjectMocks
    private OrderService orderService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void prepareOrderByConsumerWhenAllProductsAreEnough() {
        User consumer = createConsumer(1L);
        List<BasketElement> basket = createBasket(consumer);
        List<BasketElement> basketForCompare = createBasket(consumer);

        int expectedTotalCoast = 0;
        for (BasketElement basketElement : basket) {
            expectedTotalCoast += basketElement.getProduct().getPrice() * basketElement.getAmount();
        }

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllFetchProductByConsumerIdNotOrderedWithLock(consumer.getId()))
                .thenReturn(basket);

        PreparedOrderForShowUserDto preparedOrderForShowUserDto = orderService.prepareOrderByConsumer(consumer.getId());

        assertEquals(basketForCompare.get(0).getProduct().getPrice(), basket.get(0).getPriceInOrder());
        assertEquals(basketForCompare.get(1).getProduct().getPrice(), basket.get(1).getPriceInOrder());
        assertEquals(basketForCompare.get(2).getProduct().getPrice(), basket.get(2).getPriceInOrder());
        assertEquals(basketForCompare.get(3).getProduct().getPrice(), basket.get(3).getPriceInOrder());

        assertEquals(basketForCompare.get(0).getAmount(), basket.get(0).getAmount());
        assertEquals(basketForCompare.get(1).getAmount(), basket.get(1).getAmount());
        assertEquals(basketForCompare.get(2).getAmount(), basket.get(2).getAmount());
        assertEquals(basketForCompare.get(3).getAmount(), basket.get(3).getAmount());
        assertEquals(
                basketForCompare.get(0).getProduct().getAmount(),
                basket.get(0).getProduct().getAmount() + basketForCompare.get(0).getAmount());
        assertEquals(
                basketForCompare.get(1).getProduct().getAmount(),
                basket.get(1).getProduct().getAmount() + basketForCompare.get(1).getAmount());
        assertEquals(
                basketForCompare.get(2).getProduct().getAmount(),
                basket.get(2).getProduct().getAmount() + basketForCompare.get(2).getAmount());
        assertEquals(
                basketForCompare.get(3).getProduct().getAmount(),
                basket.get(3).getProduct().getAmount() + basketForCompare.get(3).getAmount());
        assertEquals(expectedTotalCoast, preparedOrderForShowUserDto.getTotalCoast());
        verify(userRepository, times(1)).findById(any());
        verify(basketElementRepository, times(1)).findAllFetchProductByConsumerIdNotOrderedWithLock(any());
        verify(basketElementRepository, times(4)).save(any());
        verify(productRepository, times(4)).saveAndFlush(any());
    }

    @Test
    public void prepareOrderByConsumerWhenNotAllProductsAreEnough() {
        User consumer = createConsumer(1L);
        List<BasketElement> basket = createBasketWhereNotAllProductAreEnough(consumer);
        List<BasketElement> basketForCompare = createBasketWhereNotAllProductAreEnough(consumer);

        int expectedTotalCoast = 0;
        for (BasketElement basketElement : basketForCompare) {
            if (basketElement.getAmount() > basketElement.getProduct().getAmount()) {
                expectedTotalCoast += basketElement.getProduct().getPrice() * basketElement.getProduct().getAmount();
                continue;
            }
            expectedTotalCoast += basketElement.getProduct().getPrice() * basketElement.getAmount();
        }

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllFetchProductByConsumerIdNotOrderedWithLock(consumer.getId()))
                .thenReturn(basket);

        PreparedOrderForShowUserDto preparedOrderForShowUserDto = orderService.prepareOrderByConsumer(consumer.getId());

        assertEquals(basketForCompare.get(0).getProduct().getPrice(), basket.get(0).getPriceInOrder());
        assertEquals(basketForCompare.get(1).getProduct().getPrice(), basket.get(1).getPriceInOrder());
        assertEquals(basketForCompare.get(2).getProduct().getPrice(), basket.get(2).getPriceInOrder());
        assertEquals(basketForCompare.get(3).getProduct().getPrice(), basket.get(3).getPriceInOrder());

        assertEquals(basketForCompare.get(0).getAmount(), basket.get(0).getAmount());
        assertEquals(basketForCompare.get(1).getProduct().getAmount(), basket.get(1).getAmount());
        assertEquals(basketForCompare.get(2).getAmount(), basket.get(2).getAmount());
        assertEquals(basketForCompare.get(3).getProduct().getAmount(), basket.get(3).getAmount());
        assertEquals(
                basketForCompare.get(0).getProduct().getAmount(),
                basket.get(0).getProduct().getAmount() + basketForCompare.get(0).getAmount());
        assertEquals(0, basket.get(1).getProduct().getAmount());
        assertEquals(
                basketForCompare.get(2).getProduct().getAmount(),
                basket.get(2).getProduct().getAmount() + basketForCompare.get(2).getAmount());
        assertEquals(0, basket.get(3).getProduct().getAmount());
        assertEquals(expectedTotalCoast, preparedOrderForShowUserDto.getTotalCoast());
        verify(userRepository, times(1)).findById(any());
        verify(basketElementRepository, times(1)).findAllFetchProductByConsumerIdNotOrderedWithLock(any());
        verify(basketElementRepository, times(4)).save(any());
        verify(productRepository, times(4)).saveAndFlush(any());
        verify(messageCreatorService, times(2))
                .createResponseAboutNotEnoughAmountOfProductWithAdding(any(), anyInt());
    }

    @Test
    public void prepareOrderByConsumerThrowUserNotFoundEx() {
        User consumer = createConsumer(1L);

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderService.prepareOrderByConsumer(consumer.getId()));

        verify(userRepository, times(1)).findById(any());
        verify(basketElementRepository, times(0)).findAllFetchProductByConsumerIdNotOrderedWithLock(any());
    }

    @Test
    public void prepareOrderByConsumerThrowBasketIsEmptyEx() {
        User consumer = createConsumer(1L);

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllFetchProductByConsumerIdNotOrderedWithLock(consumer.getId()))
                .thenReturn(emptyList());

        assertThrows(BasketIsEmptyException.class, () -> orderService.prepareOrderByConsumer(consumer.getId()));

        verify(userRepository, times(1)).findById(any());
        verify(basketElementRepository, times(1)).findAllFetchProductByConsumerIdNotOrderedWithLock(any());
    }

    @Test
    public void createOrderByConsumer() {
        Order[] createdOrder = new Order[1];
        User consumer = createConsumer(2L);
        List<BasketElement> basket = createPreparedForOrderBasket(consumer);

        int expectedTotalCoast = 0;
        for (BasketElement basketElement : basket) {
            expectedTotalCoast += basketElement.getProduct().getPrice() * basketElement.getAmount();
        }

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllByConsumerIdNotOrdered(consumer.getId()))
                .thenReturn(basket);

        when(orderRepository.save(argThat(order -> order.getConsumer().equals(consumer))))
                .thenAnswer(invocationOnMock -> {
                    createdOrder[0] = invocationOnMock.getArgument(0);
                    createdOrder[0].setId(101L);
                    return createdOrder[0];
                });

        orderService.createOrderByConsumer(consumer.getId());


        assertEquals(101L, createdOrder[0].getId());
        assertEquals(expectedTotalCoast, createdOrder[0].getTotalCost());
        assertEquals(consumer, createdOrder[0].getConsumer());
        assertEquals(consumer, createdOrder[0].getLastUser());
        assertEquals(OrderStatus.IN_PROCESS, createdOrder[0].getOrderStatus());
        basket.forEach(basketElement -> assertEquals(basketElement.getOrder(), createdOrder[0]));

        verify(userRepository, times(1)).findById(consumer.getId());
        verify(basketElementRepository, times(1)).findAllByConsumerIdNotOrdered(consumer.getId());
        verify(orderRepository, times(1)).save(argThat(order -> order.getConsumer().equals(consumer)));
    }

    @Test
    public void createOrderByConsumerThrowUserNotFoundEx() {
        User consumer = createConsumer(2L);

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderService.createOrderByConsumer(consumer.getId()));

        verify(userRepository, times(1)).findById(consumer.getId());
        verify(basketElementRepository, times(0)).findAllByConsumerIdNotOrdered(any());
        verify(orderRepository, times(0)).save(any());
    }

    @Test
    public void createOrderByConsumerThrowBasketIsEmptyEx() {
        User consumer = createConsumer(2L);

        when(userRepository.findById(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllByConsumerIdNotOrdered(consumer.getId()))
                .thenReturn(emptyList());

        assertThrows(BasketIsEmptyException.class, () -> orderService.createOrderByConsumer(consumer.getId()));

        verify(userRepository, times(1)).findById(consumer.getId());
        verify(basketElementRepository, times(1)).findAllByConsumerIdNotOrdered(consumer.getId());
        verify(orderRepository, times(0)).save(any());
    }

    @Test
    public void cancelOrderByConsumer() {
        User consumer = createConsumer(1L);
        List<BasketElement> basket = createPreparedForOrderBasket(consumer);
        List<BasketElement> basketForCompare = createPreparedForOrderBasket(consumer);

        when(userRepository.findByIdFetchBasket(consumer.getId()))
                .thenReturn(Optional.of(consumer));

        when(basketElementRepository.findAllByConsumerIdNotOrdered(consumer.getId()))
                .thenReturn(basket);

        orderService.cancelOrderByConsumer(consumer.getId());

        assertEquals(
                basketForCompare.get(0).getProduct().getAmount() + basketForCompare.get(0).getAmount(),
                basket.get(0).getProduct().getAmount());
        assertEquals(
                basketForCompare.get(1).getProduct().getAmount() + basketForCompare.get(1).getAmount(),
                basket.get(1).getProduct().getAmount());
        assertEquals(
                basketForCompare.get(2).getProduct().getAmount() + basketForCompare.get(2).getAmount(),
                basket.get(2).getProduct().getAmount());
        assertEquals(
                basketForCompare.get(3).getProduct().getAmount() + basketForCompare.get(3).getAmount(),
                basket.get(3).getProduct().getAmount());
        basket.forEach(basketElement -> {
            assertNull(basketElement.getProductBookingTime());
            assertNull(basketElement.getPriceInOrder());
        });
        verify(userRepository, times(1)).findByIdFetchBasket(consumer.getId());
        verify(basketElementRepository, times(1)).findAllByConsumerIdNotOrdered(consumer.getId());
        verify(basketElementRepository, times(4)).save(any());
        verify(productRepository, times(4)).save(any());
    }

    @Test
    public void cancelOrderByConsumerThrowUserNotFoundEx() {
        User consumer = createConsumer(1L);

        when(userRepository.findByIdFetchBasket(consumer.getId()))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> orderService.cancelOrderByConsumer(consumer.getId()));

        verify(userRepository, times(1)).findByIdFetchBasket(consumer.getId());
        verify(basketElementRepository, times(0)).findAllByConsumerIdNotOrdered(consumer.getId());
        verify(basketElementRepository, times(0)).save(any());
        verify(productRepository, times(0)).save(any());
    }

    private List<BasketElement> createPreparedForOrderBasket(User consumer) {
        Product product1 = createProduct(1L, 10, 100);
        Product product2 = createProduct(2L, 20, 200);
        Product product3 = createProduct(3L, 30, 300);
        Product product4 = createProduct(4L, 40, 400);

        return List.of(
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product1)
                        .amount(9)
                        .priceInOrder(product1.getPrice())
                        .productBookingTime(LocalDateTime.now())
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product2)
                        .amount(5)
                        .priceInOrder(product2.getPrice())
                        .productBookingTime(LocalDateTime.now())
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product3)
                        .amount(80)
                        .priceInOrder(product3.getPrice())
                        .productBookingTime(LocalDateTime.now())
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product4)
                        .amount(140)
                        .priceInOrder(product4.getPrice())
                        .productBookingTime(LocalDateTime.now())
                        .build()
        );
    }

    private List<BasketElement> createBasketWhereNotAllProductAreEnough(User consumer) {
        Product product1 = createProduct(1L, 10, 9);
        Product product2 = createProduct(2L, 20, 4);
        Product product3 = createProduct(3L, 30, 100);
        Product product4 = createProduct(4L, 40, 100);

        return List.of(
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product1)
                        .amount(9)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product2)
                        .amount(5)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product3)
                        .amount(80)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product4)
                        .amount(140)
                        .build()
        );
    }

    private List<BasketElement> createBasket(User consumer) {
        Product product1 = createProduct(1L, 10, 100);
        Product product2 = createProduct(2L, 20, 110);
        Product product3 = createProduct(3L, 30, 125);
        Product product4 = createProduct(4L, 40, 140);

        return List.of(
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product1)
                        .amount(9)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product2)
                        .amount(5)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product3)
                        .amount(1)
                        .build(),
                BasketElement.builder()
                        .consumer(consumer)
                        .product(product4)
                        .amount(1)
                        .build()
        );
    }

    private Product createProduct(Long productId, int price, int amount) {
        return Product.builder()
                .id(productId)
                .title("Товар")
                .description("Тестовый")
                .price(price)
                .amount(amount)
                .hashtags("хороший#качественный")
                .build();
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
