package com.velb.shop.integration.service;

import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderResponseDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.OrderAuditRecord;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.auxiliary.AdminOrderStatus;
import com.velb.shop.model.entity.auxiliary.ConsumerOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import com.velb.shop.model.mapper.ProductForOrderMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderAuditRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@RequiredArgsConstructor
public class OrderServiceIT extends IntegrationTestBase {
    private final OrderService orderService;
    private final BasketElementRepository basketElementRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderAuditRepository orderAuditRepository;
    private final ProductForOrderMapper productForOrderMapper;

    @Test
    void prepareOrderByConsumer() {
        long consumerId = 2L;
        List<BasketElement> consumersBasketBeforePreparing = basketElementRepository.findAllByUserIdFetchProduct(consumerId);
        Map<Long, Integer> productsAmountMap = new HashMap<>();
        Map<Long, Integer> amountInBasketBeforePreparing = new HashMap<>();
        int expectedTotalCoast = 0;
        String expectedUserMessage = "";
        for (BasketElement basketElement : consumersBasketBeforePreparing) {
            expectedTotalCoast += basketElement.getAmount() * basketElement.getProduct().getPrice();
            productsAmountMap.put(basketElement.getProduct().getId(), basketElement.getProduct().getAmount());
            amountInBasketBeforePreparing.put(basketElement.getId(), basketElement.getAmount());
        }

        PreparedOrderForShowUserDto preparedOrder = orderService.prepareOrderByConsumer(consumerId);

        List<BasketElement> consumersBasketAfterPreparing = basketElementRepository.findAllByUserIdFetchProduct(consumerId);

        assertEquals(consumersBasketBeforePreparing.size(), consumersBasketAfterPreparing.size());
        assertEquals(expectedTotalCoast, preparedOrder.getTotalCoast());
        assertEquals(expectedUserMessage, preparedOrder.getMessageForUser());
        for (BasketElement basketElement : consumersBasketAfterPreparing) {
            assertEquals(productsAmountMap.get(basketElement.getProduct().getId())
                            - amountInBasketBeforePreparing.get(basketElement.getId()),
                    basketElement.getProduct().getAmount());
            assertTrue(basketElement.getProductBookingTime().isBefore(LocalDateTime.now()));
        }
    }

    @Test
    void prepareOrderByConsumerWithMessageForUser() {
        long consumerId = 4L;
        List<BasketElement> consumersBasketBeforePreparing = basketElementRepository.findAllByUserIdFetchProduct(consumerId);
        Map<Long, Integer> productsAmountMap = new HashMap<>();
        Map<Long, Integer> amountInBasketBeforePreparingMap = new HashMap<>();
        int expectedTotalCoast = 0;
        StringBuilder expectedUserMessageBuilder = new StringBuilder();
        for (BasketElement basketElement : consumersBasketBeforePreparing) {
            if (basketElement.getAmount() <= basketElement.getProduct().getAmount()) {
                expectedTotalCoast += basketElement.getAmount() * basketElement.getProduct().getPrice();
            } else {
                expectedTotalCoast += basketElement.getProduct().getAmount() * basketElement.getProduct().getPrice();
                expectedUserMessageBuilder.append(
                        createResponseAboutNotEnoughAmountOfProductWithAdding(
                                basketElement.getProduct(),
                                basketElement.getAmount()));
            }
            productsAmountMap.put(basketElement.getProduct().getId(), basketElement.getProduct().getAmount());
            amountInBasketBeforePreparingMap.put(basketElement.getId(), basketElement.getAmount());
        }

        PreparedOrderForShowUserDto preparedOrder = orderService.prepareOrderByConsumer(consumerId);

        List<BasketElement> consumersBasketAfterPreparing = basketElementRepository.findAllByUserIdFetchProduct(consumerId);

        assertEquals(consumersBasketBeforePreparing.size(), consumersBasketAfterPreparing.size());
        assertEquals(expectedTotalCoast, preparedOrder.getTotalCoast());
        assertEquals(expectedUserMessageBuilder.toString(), preparedOrder.getMessageForUser());

        for (BasketElement basketElement : consumersBasketAfterPreparing) {
            long productId = basketElement.getProduct().getId();
            long basketElementId = basketElement.getId();
            if (productsAmountMap.get(productId) <= amountInBasketBeforePreparingMap.get(basketElementId)) {
                assertEquals(0, basketElement.getProduct().getAmount());
                assertEquals(productsAmountMap.get(productId), basketElement.getAmount());
            } else {
                assertEquals(
                        productsAmountMap.get(productId) - amountInBasketBeforePreparingMap.get(basketElementId),
                        basketElement.getProduct().getAmount());
            }
        }
    }

    @Test
    void prepareOrderByConsumerThrowBasketIsEmptyEx() {
        long consumerId = 1L;
        String expectedExceptionMessage = "Ваша корзина пуста; ";

        Exception exception = assertThrows(BasketIsEmptyException.class, ()
                -> orderService.prepareOrderByConsumer(consumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void prepareOrderByConsumerThrowUserNotFoundEx() {
        long nonExistedConsumerId = 100000L;
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.prepareOrderByConsumer(nonExistedConsumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void makeOrderByConsumer() {
        long consumerId = 2L;
        List<BasketElement> consumersBasketBeforeOrdering = basketElementRepository.findAllByUserIdFetchProduct(consumerId);
        List<OrderElement> expectedOrderContent = new ArrayList<>();
        int expectedTotalCoast = 0;
        for (BasketElement basketElement : consumersBasketBeforeOrdering) {
            expectedTotalCoast += basketElement.getAmount() * basketElement.getProduct().getPrice();
            expectedOrderContent.add(
                    OrderElement.builder()
                            .productForOrder(productForOrderMapper.map(basketElement.getProduct()))
                            .amount(basketElement.getAmount())
                            .build());
        }

        Long orderId = orderService.makeOrderByConsumer(consumerId);

        Optional<Order> madeOrder = orderRepository.findById(orderId);
        List<BasketElement> consumersBasketAfterOrdering = basketElementRepository.findAllByUserId(consumerId);
        assertTrue(madeOrder.isPresent());
        assertEquals(consumerId, madeOrder.get().getConsumer().getId());
        assertEquals(expectedOrderContent, madeOrder.get().getContent());
        assertEquals(expectedTotalCoast, madeOrder.get().getTotalCost());
        assertEquals(ConsumerOrderStatus.IN_PROCESS, madeOrder.get().getConsumerOrderStatus());
        assertNotNull(madeOrder.get().getDate());
        assertTrue(consumersBasketAfterOrdering.isEmpty());
    }

    @Test
    void makeOrderByConsumerThrowBasketIsEmptyEx() {
        long consumerId = 1L;
        String expectedExceptionMessage = "Ваша корзина пуста; ";

        Exception exception = assertThrows(BasketIsEmptyException.class, ()
                -> orderService.makeOrderByConsumer(consumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void makeOrderByConsumerThrowUserNotFoundEx() {
        long nonExistedConsumerId = 100000L;
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.makeOrderByConsumer(nonExistedConsumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void cancelOrderCreationByConsumer() {
        long consumerId = 2L;
        List<BasketElement> basketBeforePreparingOrder = basketElementRepository.findAllByUserId(consumerId);
        Map<Long, Integer> productsAmountMap = new HashMap<>();
        for (BasketElement basketElement : basketBeforePreparingOrder) {
            productsAmountMap.put(basketElement.getProduct().getId(), basketElement.getProduct().getAmount());
        }

        orderService.prepareOrderByConsumer(consumerId);
        List<BasketElement> basketAfterPreparingOrder = basketElementRepository.findAllByUserId(consumerId);
        for (BasketElement basketElement : basketAfterPreparingOrder) {
            assertNotNull(basketElement.getProductBookingTime());
        }

        orderService.cancelOrderCreationByConsumer(consumerId);
        List<BasketElement> basketAfterCanceling = basketElementRepository.findAllByUserId(consumerId);

        for (BasketElement basketElement : basketAfterCanceling) {
            assertNull(basketElement.getProductBookingTime());
            assertEquals(
                    productsAmountMap.get(basketElement.getProduct().getId()),
                    basketElement.getProduct().getAmount());
        }
    }

    @Test
    void cancelCreationAnOrderByConsumerThrowUserNotFoundEx() {
        long nonExistedConsumerId = 100000L;
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.cancelOrderCreationByConsumer(nonExistedConsumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void getAllOrdersByAdmin() {
        Pageable pageable = PageRequest.of(0, 2);
        long expectedCountOfOrders = orderRepository.findAll().size();

        Page<OrderResponseDto> orderResponseDtoPage = orderService.getAllOrdersByAdmin(pageable);

        assertEquals(expectedCountOfOrders, orderResponseDtoPage.getTotalElements());
    }

    @Test
    void createNewOrderByAdmin() {
        long adminId = 1L;
        long consumerId = 2L;
        long productId2 = 2L;
        long productId3 = 3L;
        long productId8 = 8L;
        Optional<Product> productWithId2 = productRepository.findById(productId2);
        Optional<Product> productWithId3 = productRepository.findById(productId3);
        Optional<Product> productWithId8 = productRepository.findById(productId8);
        assertTrue(productWithId2.isPresent());
        assertTrue(productWithId3.isPresent());
        assertTrue(productWithId8.isPresent());
        int product2Amount = productWithId2.get().getAmount();
        int product3Amount = productWithId3.get().getAmount();
        int product8Amount = productWithId8.get().getAmount();
        Map<Long, Integer> productsAndAmount = Map.of(
                productWithId2.get().getId(), 10,
                productWithId3.get().getId(), 16,
                productWithId8.get().getId(), 10);
        int expectedTotalCoast = productWithId2.get().getPrice() * productsAndAmount.get(productId2)
                + productWithId3.get().getPrice() * productsAndAmount.get(productId3)
                + productWithId8.get().getPrice() * productsAndAmount.get(productId8);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        List<OrderAuditRecord> orderAuditBeforeCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);

        Long orderId = orderService.createNewOrderByAdmin(adminId, orderCreatingDto);

        Optional<Order> createdOrder = orderRepository.findById(orderId);
        assertTrue(createdOrder.isPresent());
        assertEquals(consumerId, createdOrder.get().getConsumer().getId());
        assertEquals(expectedTotalCoast, createdOrder.get().getTotalCost());
        assertEquals(ConsumerOrderStatus.IN_PROCESS, createdOrder.get().getConsumerOrderStatus());

        List<OrderAuditRecord> orderAuditAfterCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);
        List<OrderElement> orderContent = createdOrder.get().getContent();
        for (OrderElement orderElement : orderContent) {
            if (orderElement.getProductForOrder().getId() == productId2) {
                assertEquals(productsAndAmount.get(productId2), orderElement.getAmount());
            }
            if (orderElement.getProductForOrder().getId() == productId3) {
                assertEquals(productsAndAmount.get(productId3), orderElement.getAmount());
            }
            if (orderElement.getProductForOrder().getId() == productId8) {
                assertEquals(productsAndAmount.get(productId8), orderElement.getAmount());
            }
        }
        assertEquals(productsAndAmount.size(), orderContent.size());

        Optional<Product> productAfterCreating2 = productRepository.findById(productId2);
        Optional<Product> productAfterCreating3 = productRepository.findById(productId3);
        Optional<Product> productAfterCreating8 = productRepository.findById(productId8);
        assertTrue(productAfterCreating2.isPresent());
        assertTrue(productAfterCreating3.isPresent());
        assertTrue(productAfterCreating8.isPresent());
        assertEquals(product2Amount - productsAndAmount.get(productId2), productAfterCreating2.get().getAmount());
        assertEquals(product3Amount - productsAndAmount.get(productId3), productAfterCreating3.get().getAmount());
        assertEquals(product8Amount - productsAndAmount.get(productId8), productAfterCreating8.get().getAmount());
        assertEquals(orderAuditBeforeCreating.size() + 1, orderAuditAfterCreating.size());
    }

    @Test
    void createNewOrderByAdminThrowProductNotFoundEx() {
        long adminId = 1L;
        long consumerId = 2L;
        long productId1 = 1L;
        long productId2 = 2L;
        long nonExistedProductId = 10000000L;
        Optional<Product> product1 = productRepository.findById(productId1);
        Optional<Product> product2 = productRepository.findById(productId2);
        Optional<Product> nonExistedProduct2 = productRepository.findById(nonExistedProductId);
        assertTrue(product2.isPresent());
        assertTrue(product1.isPresent());
        assertTrue(nonExistedProduct2.isEmpty());
        int product1Amount = product1.get().getAmount();
        int product2Amount = product2.get().getAmount();
        List<OrderAuditRecord> orderAuditBeforeCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);

        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, 16,
                productId2, 10,
                nonExistedProductId, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        String expectedExceptionMessage = "Товара с id " + nonExistedProductId + " не существует; ";

        Exception exception = assertThrows(RuntimeException.class, ()
                -> orderService.createNewOrderByAdmin(adminId, orderCreatingDto));

        Optional<Product> product1AfterCreating = productRepository.findById(productId1);
        Optional<Product> product2AfterCreating = productRepository.findById(productId2);
        List<OrderAuditRecord> orderAuditAfterCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertTrue(product1AfterCreating.isPresent());
        assertTrue(product2AfterCreating.isPresent());
        assertEquals(product1Amount, product1AfterCreating.get().getAmount());
        assertEquals(product2Amount, product2AfterCreating.get().getAmount());
        assertEquals(orderAuditBeforeCreating.size(), orderAuditAfterCreating.size());
    }

    @Test
    void createNewOrderByAdminThrowInsufficientProductQuantityEx() {
        long adminId = 1L;
        long consumerId = 2L;
        long productId2 = 2L;
        long productId8 = 8L;
        long productId10 = 10L;
        Optional<Product> product2 = productRepository.findById(productId2);
        Optional<Product> product8 = productRepository.findById(productId8);
        Optional<Product> product10 = productRepository.findById(productId10);
        assertTrue(product2.isPresent());
        assertTrue(product8.isPresent());
        assertTrue(product10.isPresent());
        int product2Amount = product2.get().getAmount();
        int product8Amount = product8.get().getAmount();
        int product10Amount = product10.get().getAmount();

        Map<Long, Integer> productsAndAmount = Map.of(
                productId2, 10,
                productId8, 13,
                productId10, 600);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        String expectedExceptionMessage1 = createResponseAboutNotEnoughAmountOfProduct(
                product10.get(),
                productsAndAmount.get(productId10));
        String expectedExceptionMessage2 = createResponseAboutNotEnoughAmountOfProduct(
                product8.get(),
                productsAndAmount.get(productId8));
        List<OrderAuditRecord> orderAuditBeforeCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);

        Exception exception = assertThrows(InsufficientProductQuantityException.class, ()
                -> orderService.createNewOrderByAdmin(adminId, orderCreatingDto));

        Optional<Product> product2AfterCreating = productRepository.findById(productId2);
        Optional<Product> product8AfterCreating = productRepository.findById(productId8);
        Optional<Product> product10AfterCreating = productRepository.findById(productId10);
        List<OrderAuditRecord> orderAuditAfterCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                adminId,
                AdminOrderStatus.CREATED);

        assertTrue(exception.getMessage().contains(expectedExceptionMessage1));
        assertTrue(exception.getMessage().contains(expectedExceptionMessage2));
        assertEquals(expectedExceptionMessage1.length() + expectedExceptionMessage2.length(),
                exception.getMessage().length());
        assertTrue(product2AfterCreating.isPresent());
        assertTrue(product8AfterCreating.isPresent());
        assertTrue(product10AfterCreating.isPresent());
        assertEquals(product2Amount, product2AfterCreating.get().getAmount());
        assertEquals(product8Amount, product8AfterCreating.get().getAmount());
        assertEquals(product10Amount, product10AfterCreating.get().getAmount());
        assertEquals(orderAuditBeforeCreating.size(), orderAuditAfterCreating.size());
    }

    @Test
    void createNewOrderByAdminThrowUserNotFoundExWithNonExistedConsumer() {
        long adminId = 1L;
        long nonExistedConsumerId = 100000L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, 3L, 16, 8L, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(nonExistedConsumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        List<OrderAuditRecord> orderAuditBeforeCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                nonExistedConsumerId,
                adminId,
                AdminOrderStatus.CREATED);
        String expectedExceptionMessage = "Вы выбрали некорректного покупателя; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.createNewOrderByAdmin(adminId, orderCreatingDto));

        List<OrderAuditRecord> orderAuditAfterCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                nonExistedConsumerId,
                adminId,
                AdminOrderStatus.CREATED);
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(orderAuditBeforeCreating.size(), orderAuditAfterCreating.size());
    }

    @Test
    void createNewOrderByAdminThrowUserNotFoundExWithNonExistedAdmin() {
        long nonExistedAdminId = 100000L;
        long consumerId = 2L;
        Map<Long, Integer> productsAndAmount = Map.of(2L, 10, 3L, 16, 8L, 10);
        OrderCreatingDto orderCreatingDto = OrderCreatingDto.builder()
                .consumerId(consumerId)
                .productsAndAmount(productsAndAmount)
                .build();
        List<OrderAuditRecord> orderAuditBeforeCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                nonExistedAdminId,
                AdminOrderStatus.CREATED);
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.createNewOrderByAdmin(nonExistedAdminId, orderCreatingDto));

        List<OrderAuditRecord> orderAuditAfterCreating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                consumerId,
                nonExistedAdminId,
                AdminOrderStatus.CREATED);
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(orderAuditBeforeCreating.size(), orderAuditAfterCreating.size());
    }

    @Test
    void updateOrderByAdmin() {
        long adminId = 1;
        long orderId = 3L;
        long productId1 = 1L;
        long productId2 = 2L;
        int amountProduct1ForOrder = 3;
        int amountProduct2ForOrder = 5;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, amountProduct1ForOrder,
                productId2, amountProduct2ForOrder);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(orderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        Optional<Order> orderForUpdating = orderRepository.findById(orderId);
        assertTrue(orderForUpdating.isPresent());
        Map<Long, Integer> expectedAmountAfterUpdating = new HashMap<>();
        for (OrderElement orderElement : orderForUpdating.get().getContent()) {
            expectedAmountAfterUpdating.put(orderElement.getProductForOrder().getId(), orderElement.getAmount());
            for (Map.Entry<Long, Integer> entryUpdatingDto : productsAndAmount.entrySet()) {
                if (Objects.equals(orderElement.getProductForOrder().getId(), entryUpdatingDto.getKey())) {
                    expectedAmountAfterUpdating.put(entryUpdatingDto.getKey(), entryUpdatingDto.getValue());
                }
            }
        }
        List<OrderAuditRecord> orderAuditBeforeUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);

        orderService.updateOrderByAdmin(adminId, orderUpdatingDto);

        Optional<Order> orderAfterUpdating = orderRepository.findById(orderId);
        List<OrderAuditRecord> orderAuditAfterUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);
        assertTrue(orderAfterUpdating.isPresent());
        for (OrderElement orderElement : orderAfterUpdating.get().getContent()) {
            for (Map.Entry<Long, Integer> entry : expectedAmountAfterUpdating.entrySet()) {
                if (orderElement.getProductForOrder().getId().equals(entry.getKey())) {
                    assertEquals(entry.getValue(), orderElement.getAmount());
                }
            }
        }
        assertEquals(orderAfterUpdating.get().getConsumerOrderStatus(), ConsumerOrderStatus.SENT);
        assertEquals(orderAuditBeforeUpdating.size() + 1, orderAuditAfterUpdating.size());
    }

    @Test
    void updateOrderByAdminThrowInsufficientProductQuantityEx() {
        long adminId = 1;
        long orderId = 3L;
        long productId1 = 1L;
        long productId2 = 2L;
        int amountProduct1ForOrder = 3;
        int amountProduct2ForOrder = 1000000000;
        Map<Long, Integer> productsAndAmount = Map.of(
                productId1, amountProduct1ForOrder,
                productId2, amountProduct2ForOrder);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(orderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        Optional<Order> orderForUpdating = orderRepository.findById(orderId);
        assertTrue(orderForUpdating.isPresent());
        Optional<Product> product1 = productRepository.findById(productId1);
        Optional<Product> product2 = productRepository.findById(productId2);
        assertTrue(product1.isPresent());
        assertTrue(product2.isPresent());
        String expectedExceptionMessage = createResponseAboutNotEnoughAmountOfProduct(product2.get(), amountProduct2ForOrder);
        int product1Amount = product1.get().getAmount();
        int product2Amount = product2.get().getAmount();
        List<OrderAuditRecord> orderAuditBeforeUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);

        Exception exception = assertThrows(InsufficientProductQuantityException.class, ()
                -> orderService.updateOrderByAdmin(adminId, orderUpdatingDto));

        Optional<Product> product1AfterUpdating = productRepository.findById(productId1);
        Optional<Product> product2AfterUpdating = productRepository.findById(productId2);
        List<OrderAuditRecord> orderAuditAfterUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);

        assertTrue(product1AfterUpdating.isPresent());
        assertTrue(product2AfterUpdating.isPresent());
        assertEquals(expectedExceptionMessage, exception.getMessage());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(product1Amount, product1AfterUpdating.get().getAmount());
        assertEquals(product2Amount, product2AfterUpdating.get().getAmount());
        assertEquals(orderAuditBeforeUpdating.size(), orderAuditAfterUpdating.size());
    }

    @Test
    void updateOrderByAdminThrowOrderNotFoundEx() {
        long adminId = 1L;
        long nonExistedOrderId = 10000L;
        Map<Long, Integer> productsAndAmount = Map.of(1L, 2, 2L, 3);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(nonExistedOrderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        String expectedExceptionMessage = "Заказа с id " + nonExistedOrderId + " не существует; ";

        Exception exception = assertThrows(OrderNotFoundException.class, ()
                -> orderService.updateOrderByAdmin(adminId, orderUpdatingDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void updateOrderByAdminThrowUserNotFoundEx() {
        long nonExistedAdminId = 100000L;
        long orderId = 3L;
        Map<Long, Integer> productsAndAmount = Map.of(1L, 2, 2L, 3);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(orderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        Optional<Order> orderForUpdating = orderRepository.findById(orderId);
        assertTrue(orderForUpdating.isPresent());
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";
        List<OrderAuditRecord> orderAuditBeforeUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                nonExistedAdminId,
                AdminOrderStatus.CHANGED);

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.updateOrderByAdmin(nonExistedAdminId, orderUpdatingDto));

        List<OrderAuditRecord> orderAuditAfterUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                nonExistedAdminId,
                AdminOrderStatus.CHANGED);
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(orderAuditBeforeUpdating.size(), orderAuditAfterUpdating.size());
    }

    @Test
    void updateOrderByAdminThrowProductNotFoundEx() {
        long adminId = 1;
        long orderId = 3L;
        long productId1 = 1L;
        long nonExistingProductId = 100000L;
        Map<Long, Integer> productsAndAmount = Map.of(productId1, 2, nonExistingProductId, 3);
        OrderUpdatingDto orderUpdatingDto = OrderUpdatingDto.builder()
                .orderId(orderId)
                .productsAndAmount(productsAndAmount)
                .consumerStatus("SENT")
                .build();
        Optional<Order> orderForUpdating = orderRepository.findById(orderId);
        assertTrue(orderForUpdating.isPresent());
        String expectedExceptionMessage = "Товара с id " + nonExistingProductId + " не существует; ";
        Optional<Product> product1 = productRepository.findById(productId1);
        Optional<Product> nonExistingProduct = productRepository.findById(nonExistingProductId);
        assertTrue(product1.isPresent());
        assertTrue(nonExistingProduct.isEmpty());
        int product1Amount = product1.get().getAmount();
        List<OrderAuditRecord> orderAuditBeforeUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> orderService.updateOrderByAdmin(adminId, orderUpdatingDto));

        Optional<Product> product1AfterUpdating = productRepository.findById(productId1);
        List<OrderAuditRecord> orderAuditAfterUpdating = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForUpdating.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.CHANGED);
        assertTrue(product1AfterUpdating.isPresent());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(product1Amount, product1AfterUpdating.get().getAmount());
        assertEquals(orderAuditBeforeUpdating.size(), orderAuditAfterUpdating.size());
    }

    @Test
    void deleteOrderByAdmin() {
        long adminId = 1L;
        long orderId = 3L;
        Optional<Order> orderForDeleting = orderRepository.findById(orderId);
        assertTrue(orderForDeleting.isPresent());
        List<OrderAuditRecord> orderAuditBeforeDeleting = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForDeleting.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.DELETED);

        orderService.deleteOrderByAdmin(adminId, orderId);

        Optional<Order> orderAfterDeleting = orderRepository.findById(orderId);
        List<OrderAuditRecord> orderAuditAfterDeleting = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForDeleting.get().getConsumer().getId(),
                adminId,
                AdminOrderStatus.DELETED);
        assertTrue(orderAfterDeleting.isEmpty());
        assertEquals(orderAuditBeforeDeleting.size() + 1, orderAuditAfterDeleting.size());
    }

    @Test
    void deleteOrderByAdminThrowOrderNotFoundEx() {
        long adminId = 1L;
        long nonExistedOrderId = 10000L;
        String expectedExceptionMessage = "Заказа с id " + nonExistedOrderId + " не существует; ";

        Exception exception = assertThrows(OrderNotFoundException.class, ()
                -> orderService.deleteOrderByAdmin(adminId, nonExistedOrderId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void deleteOrderByAdminThrowUserNotFoundEx() {
        long nonExistedAdminId = 100000L;
        long orderId = 3L;
        Optional<Order> orderForDeleting = orderRepository.findById(orderId);
        assertTrue(orderForDeleting.isPresent());
        List<OrderAuditRecord> orderAuditBeforeDeleting = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForDeleting.get().getConsumer().getId(),
                nonExistedAdminId,
                AdminOrderStatus.DELETED);

        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> orderService.deleteOrderByAdmin(nonExistedAdminId, orderId));

        Optional<Order> orderAfterDeleting = orderRepository.findById(orderId);
        List<OrderAuditRecord> orderAuditAfterDeleting = orderAuditRepository.findAllByConsumerIdAndAdminIdAndAdminStatus(
                orderForDeleting.get().getConsumer().getId(),
                nonExistedAdminId,
                AdminOrderStatus.DELETED);
        assertTrue(orderAfterDeleting.isPresent());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
        assertEquals(orderAuditBeforeDeleting.size(), orderAuditAfterDeleting.size());
    }

    private String createResponseAboutNotEnoughAmountOfProduct(Product product, Integer amountInUsersBasket) {
        return " - На данный момент такого количества товара " +
                product.getTitle() + " на складе нет. " +
                "Осталось " + product.getAmount() + " экземпляров, а вы хотели заказать -  " + amountInUsersBasket;
    }

    private String createResponseAboutNotEnoughAmountOfProductWithAdding(Product product, Integer amountInUsersBasket) {
        return " - Приносим свои извинения, но к сожалению на данный момент такого количества товара " +
                product.getTitle() + " на складе нет. " +
                "Мы добавили в ваш заказ " + product.getAmount() + " из " + amountInUsersBasket + " экземпляров. ";
    }
}
