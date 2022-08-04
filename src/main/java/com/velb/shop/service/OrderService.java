package com.velb.shop.service;

import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderResponseDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.OrderAuditRecord;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.AdminOrderStatus;
import com.velb.shop.model.entity.auxiliary.ConsumerOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import com.velb.shop.model.entity.auxiliary.OrderInfo;
import com.velb.shop.model.mapper.OrderResponseDtoMapper;
import com.velb.shop.model.mapper.ProductForOrderMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderAuditRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderAuditRepository orderAuditRepository;
    private final BasketElementRepository basketElementRepository;
    private final ProductForOrderMapper productForOrderMapper;
    private final OrderResponseDtoMapper orderResponseDtoMapper;

    @Transactional
    public PreparedOrderForShowUserDto prepareOrderByConsumer(Long consumerId) {
        List<OrderElement> orderContent = new ArrayList<>();
        int totalCost = 0;
        StringBuilder messageBuilder = new StringBuilder();

        userRepository.findById(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> consumersBasket = basketElementRepository.findAllFetchProductByConsumerIdWithPessimisticLock(consumerId);

        if (consumersBasket.isEmpty()) {
            throw new BasketIsEmptyException("Ваша корзина пуста; ");
        }

        for (BasketElement basketElement : consumersBasket) {
            int amountInShop = basketElement.getProduct().getAmount();
            int orderedAmount = basketElement.getAmount();

            if (!isSufficientAmountOfProducts(amountInShop, orderedAmount)) {
                //пользователь заказал товара больше чем есть в магазине
                basketElement.setAmount(amountInShop);
                basketElement.setProductBookingTime(LocalDateTime.now());
                basketElementRepository.save(basketElement);
                messageBuilder.append(createResponseAboutNotEnoughAmountOfProductWithAdding(basketElement.getProduct(), orderedAmount));
                basketElement.getProduct().setAmount(0);
                productRepository.saveAndFlush(basketElement.getProduct());
            } else {
                basketElement.setProductBookingTime(LocalDateTime.now());
                basketElementRepository.save(basketElement);
                basketElement.getProduct().setAmount(amountInShop - orderedAmount);
                productRepository.saveAndFlush(basketElement.getProduct());
            }

            totalCost += basketElement.getProduct().getPrice() * basketElement.getAmount();

            orderContent.add(
                    OrderElement.builder()
                            .productForOrder(productForOrderMapper.map(basketElement.getProduct()))
                            .amount(basketElement.getAmount())
                            .build());
        }

        return PreparedOrderForShowUserDto.builder()
                .content(orderContent)
                .totalCoast(totalCost)
                .messageForUser(messageBuilder.toString())
                .build();
    }

    @Transactional
    public Long makeOrderByConsumer(Long consumerId) {
        List<OrderElement> orderContent = new ArrayList<>();
        int totalCost = 0;

        User user = userRepository.findById(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> usersBasket = user.getBasket();

        if (usersBasket.isEmpty()) {
            throw new BasketIsEmptyException("Ваша корзина пуста; ");
        }

        for (BasketElement basketElement : usersBasket) {
            totalCost += basketElement.getProduct().getPrice() * basketElement.getAmount();

            orderContent.add(
                    OrderElement.builder()
                            .productForOrder(productForOrderMapper.map(basketElement.getProduct()))
                            .amount(basketElement.getAmount())
                            .build());

            basketElementRepository.delete(basketElement);
        }

        Order order = Order.builder()
                .date(LocalDateTime.now())
                .consumer(user)
                .content(orderContent)
                .totalCost(totalCost)
                .consumerOrderStatus(ConsumerOrderStatus.IN_PROCESS)
                .build();
        return orderRepository.save(order).getId();
    }

    @Transactional
    public void cancelOrderCreationByConsumer(Long consumerId) {
        User user = userRepository.findByIdFetchBasket(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> usersBasket = user.getBasket();

        for (BasketElement basketElement : usersBasket) {
            int amountInShop = basketElement.getProduct().getAmount();
            int amountInUsersBasket = basketElement.getAmount();
            //При отмене создания заказа с зарезервированных товаров снимается резервация
            basketElement.getProduct().setAmount(amountInShop + amountInUsersBasket);
            productRepository.save(basketElement.getProduct());
            basketElement.setProductBookingTime(null);
            basketElementRepository.save(basketElement);
        }
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrdersByAdmin(Pageable pageable) {
        Page<Order> orders = orderRepository.findAllFetchConsumers(pageable);
        return orders.map(orderResponseDtoMapper::map);
    }

    @Transactional
    public Long createNewOrderByAdmin(Long adminId, OrderCreatingDto orderCreatingDto) {
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        User consumer = userRepository.findById(orderCreatingDto.getConsumerId()).orElseThrow(()
                -> new UserNotFoundException("Вы выбрали некорректного покупателя; "));

        List<OrderElement> orderContent = new ArrayList<>();
        int totalCoast = 0;
        StringBuilder messageBuilder = new StringBuilder();
        boolean isEnoughAmountOfProducts = true;
        Map<Long, Product> productMap = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : orderCreatingDto.getProductsAndAmount().entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey()).orElseThrow(()
                    -> new ProductNotFoundException("Товара с id " + entry.getKey() + " не существует; "));
            productMap.put(product.getId(), product);
            if (product.getAmount() < entry.getValue()) {
                messageBuilder.append(createResponseAboutNotEnoughAmountOfProduct(product, entry.getValue()));
                isEnoughAmountOfProducts = false;
            }
        }
        if (isEnoughAmountOfProducts) {
            for (Map.Entry<Long, Integer> entry : orderCreatingDto.getProductsAndAmount().entrySet()) {
                Product product = productMap.get(entry.getKey());
                orderContent.add(
                        OrderElement.builder()
                                .productForOrder(productForOrderMapper.map(product))
                                .amount(entry.getValue())
                                .build());
                product.setAmount(product.getAmount() - entry.getValue());
                productRepository.saveAndFlush(product);

                totalCoast += entry.getValue() * product.getPrice();
            }
        } else throw new InsufficientProductQuantityException(messageBuilder.toString());

        orderAuditRepository.save(
                OrderAuditRecord.builder()
                        .admin(admin)
                        .consumer(consumer)
                        .date(LocalDateTime.now())
                        .orderInfo(OrderInfo.builder()
                                .content(orderContent)
                                .totalPrice(totalCoast)
                                .consumerStatus(ConsumerOrderStatus.IN_PROCESS)
                                .build())
                        .adminOrderStatus(AdminOrderStatus.CREATED)
                        .build()
        );
        return orderRepository.save(
                        Order.builder()
                                .consumer(consumer)
                                .date(LocalDateTime.now())
                                .content(orderContent)
                                .totalCost(totalCoast)
                                .consumerOrderStatus(ConsumerOrderStatus.IN_PROCESS)
                                .build())
                .getId();
    }

    @Transactional
    public void updateOrderByAdmin(Long adminId, OrderUpdatingDto orderUpdatingDto) {
        Order orderForUpdate = orderRepository.findById(orderUpdatingDto.getOrderId()).orElseThrow(()
                -> new OrderNotFoundException("Заказа с id " + orderUpdatingDto.getOrderId() + " не существует; "));
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));

        List<OrderElement> orderContent = new ArrayList<>();
        int totalCoast = 0;
        StringBuilder messageBuilder = new StringBuilder();
        boolean isEnoughAmountOfProducts = true;

        for (Map.Entry<Long, Integer> entry : orderUpdatingDto.getProductsAndAmount().entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey()).orElseThrow(()
                    -> new ProductNotFoundException("Товара с id " + entry.getKey() + " не существует; "));

            for (OrderElement orderElement : orderForUpdate.getContent()) {
                if (Objects.equals(orderElement.getProductForOrder().getId(), entry.getKey())) {
                    if (product.getAmount() + orderElement.getAmount() < entry.getValue()) {
                        messageBuilder.append(createResponseAboutNotEnoughAmountOfProduct(product, entry.getValue()));
                        isEnoughAmountOfProducts = false;
                    }

                    if (isEnoughAmountOfProducts) {
                        product.setAmount(product.getAmount() + orderElement.getAmount() - entry.getValue());
                        productRepository.save(product);
                        orderContent.add(
                                OrderElement.builder()
                                        .productForOrder(productForOrderMapper.map(product))
                                        .amount(entry.getValue())
                                        .build());
                        totalCoast += entry.getValue() * product.getPrice();
                    }
                }
            }
            orderForUpdate.getContent().forEach(orderElement -> {

            });
        }

        if (messageBuilder.isEmpty()) {
            ConsumerOrderStatus consumerStatus =
                    orderUpdatingDto.getConsumerStatus() == null ? orderForUpdate.getConsumerOrderStatus()
                            : ConsumerOrderStatus.valueOf(orderUpdatingDto.getConsumerStatus());
            orderForUpdate.setContent(orderContent);
            orderForUpdate.setTotalCost(totalCoast);
            orderForUpdate.setConsumerOrderStatus(consumerStatus);
            orderRepository.save(orderForUpdate);

            orderAuditRepository.save(
                    OrderAuditRecord.builder()
                            .consumer(orderForUpdate.getConsumer())
                            .date(LocalDateTime.now())
                            .orderInfo(OrderInfo.builder()
                                    .consumerStatus(consumerStatus)
                                    .content(orderForUpdate.getContent())
                                    .totalPrice(orderForUpdate.getTotalCost())
                                    .build())
                            .admin(admin)
                            .adminOrderStatus(AdminOrderStatus.CHANGED)
                            .build());
        } else throw new InsufficientProductQuantityException(messageBuilder.toString());
    }

    @Transactional
    public void deleteOrderByAdmin(Long adminId, Long orderId) {
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        Order orderForDelete = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderNotFoundException("Заказа с id " + orderId + " не существует; "));
        orderAuditRepository.save(
                OrderAuditRecord.builder()
                        .consumer(orderForDelete.getConsumer())
                        .date(LocalDateTime.now())
                        .orderInfo(OrderInfo.builder()
                                .content(orderForDelete.getContent())
                                .totalPrice(orderForDelete.getTotalCost())
                                .consumerStatus(orderForDelete.getConsumerOrderStatus())
                                .build())
                        .admin(admin)
                        .adminOrderStatus(AdminOrderStatus.DELETED)
                        .build());
        orderRepository.delete(orderForDelete);

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

    private boolean isSufficientAmountOfProducts(Integer amountInShop, Integer amountInConsumersBasket) {
        return amountInShop >= amountInConsumersBasket;
    }
}