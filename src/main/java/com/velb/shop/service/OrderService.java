package com.velb.shop.service;

import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.model.dto.BasketElementForPrepareOrderDto;
import com.velb.shop.model.dto.OrderCreatingDto;
import com.velb.shop.model.dto.OrderHistoryDto;
import com.velb.shop.model.dto.OrderUpdatingDto;
import com.velb.shop.model.dto.PreparedOrderForShowUserDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.OrderStatus;
import com.velb.shop.model.mapper.OrderHistoryDtoMapper;
import com.velb.shop.model.mapper.ProductForOrderMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final BasketElementRepository basketElementRepository;
    private final ProductForOrderMapper productForOrderMapper;
    private final OrderHistoryDtoMapper orderHistoryDtoMapper;

    @Transactional
    public PreparedOrderForShowUserDto prepareOrderByConsumer(Long consumerId) {
        List<BasketElementForPrepareOrderDto> preparedOrderContent = new ArrayList<>();
        int totalCost = 0;
        StringBuilder messageBuilder = new StringBuilder();

        userRepository.findById(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> consumersBasket = basketElementRepository.findAllFetchProductByConsumerIdNotOrderedWithLock(consumerId);

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
            } else {
                basketElement.setProductBookingTime(LocalDateTime.now());
                basketElementRepository.save(basketElement);
                basketElement.getProduct().setAmount(amountInShop - orderedAmount);
            }
            basketElement.setPriceInOrder(basketElement.getProduct().getPrice());   //Позволяет гарантировать цену товара при заказе покупателем
            productRepository.saveAndFlush(basketElement.getProduct());

            totalCost += basketElement.getProduct().getPrice() * basketElement.getAmount();

            preparedOrderContent.add(
                    BasketElementForPrepareOrderDto.builder()
                            .product(productForOrderMapper.map(basketElement.getProduct()))
                            .amount(basketElement.getAmount())
                            .build());
        }

        return PreparedOrderForShowUserDto.builder()
                .content(preparedOrderContent)
                .totalCoast(totalCost)
                .messageForUser(messageBuilder.toString())
                .build();
    }

    @Transactional
    public Long makeOrderByConsumer(Long consumerId) {
        int totalCost = 0;

        User consumer = userRepository.findById(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> usersBasket = basketElementRepository.findAllByConsumerIdNotOrdered(consumerId);

        if (usersBasket.isEmpty()) {
            throw new BasketIsEmptyException("Ваша корзина пуста; ");
        }

        for (BasketElement basketElement : usersBasket) {
            totalCost += basketElement.getPriceInOrder() * basketElement.getAmount();
        }

        Order order = Order.builder()
                .date(LocalDateTime.now())
                .consumer(consumer)
                .totalCost(totalCost)
                .orderStatus(OrderStatus.IN_PROCESS)
                .lastUser(consumer)
                .build();

        for (BasketElement basketElement : usersBasket) {
            basketElement.setOrder(order);
            basketElement.setProductBookingTime(null);
        }
        return orderRepository.save(order).getId();
    }

    @Transactional
    public void cancelOrderCreationByConsumer(Long consumerId) {
        userRepository.findByIdFetchBasket(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        List<BasketElement> usersBasket = basketElementRepository.findAllByConsumerIdNotOrdered(consumerId);

        for (BasketElement basketElement : usersBasket) {
            int amountInShop = basketElement.getProduct().getAmount();
            int amountInUsersBasket = basketElement.getAmount();
            //При отмене создания заказа с зарезервированных товаров снимается резервация
            basketElement.getProduct().setAmount(amountInShop + amountInUsersBasket);
            productRepository.save(basketElement.getProduct());
            basketElement.setProductBookingTime(null);
            basketElement.setPriceInOrder(null);
            basketElementRepository.save(basketElement);
        }
    }

    @Transactional
    public Long createNewOrderByAdmin(Long adminId, OrderCreatingDto orderCreatingDto) {
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        User consumer = userRepository.findById(orderCreatingDto.getConsumerId()).orElseThrow(()
                -> new UserNotFoundException("Вы выбрали некорректного покупателя; "));

        List<BasketElement> orderContent = new ArrayList<>();
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
                BasketElement createdBasketElement = BasketElement.builder()
                        .consumer(consumer)
                        .product(product)
                        .amount(entry.getValue())
                        .priceInOrder(product.getPrice())
                        .build();
                product.setAmount(product.getAmount() - entry.getValue());
                productRepository.saveAndFlush(product);
                basketElementRepository.save(createdBasketElement);
                orderContent.add(createdBasketElement);
                totalCoast += entry.getValue() * product.getPrice();
            }
        } else throw new InsufficientProductQuantityException(messageBuilder.toString());

        Order createdOrder = orderRepository.save(
                Order.builder()
                        .consumer(consumer)
                        .date(LocalDateTime.now())
                        .totalCost(totalCoast)
                        .orderStatus(OrderStatus.IN_PROCESS)
                        .lastUser(admin)
                        .build());

        for (BasketElement basketElement : orderContent) {
            basketElement.setOrder(createdOrder);
        }
        return createdOrder.getId();
    }

    @Transactional
    public void updateOrderByAdmin(Long adminId, OrderUpdatingDto orderUpdatingDto) {
        Order orderForUpdate = orderRepository.findById(orderUpdatingDto.getOrderId()).orElseThrow(()
                -> new OrderNotFoundException("Заказа с id " + orderUpdatingDto.getOrderId() + " не существует; "));
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));

        int totalCoast = 0;
        StringBuilder messageBuilder = new StringBuilder();
        List<BasketElement> orderContent = basketElementRepository.findAllByOrderId(orderUpdatingDto.getOrderId());

        for (Map.Entry<Long, Integer> entry : orderUpdatingDto.getProductsAndAmount().entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey()).orElseThrow(()
                    -> new ProductNotFoundException("Товара с id " + entry.getKey() + " не существует; "));

            if (isPresentInBasket(entry.getKey(), orderContent)) {
                for (BasketElement basketElement : orderContent) {
                    if (Objects.equals(basketElement.getProduct().getId(), entry.getKey())) {
                        if (isEnoughAmountOfProducts(product, basketElement, entry.getValue())) {
                            product.setAmount(product.getAmount() + basketElement.getAmount() - entry.getValue());
                            productRepository.save(product);
                            basketElement.setAmount(entry.getValue());
                            totalCoast += entry.getValue() * product.getPrice();
                        } else {
                            messageBuilder.append(createResponseAboutNotEnoughAmountOfProduct(product, entry.getValue()));
                        }
                    }
                }
            } else {
                if (isEnoughAmountOfProducts(product, entry.getValue())) {
                    basketElementRepository.saveAndFlush(
                            BasketElement.builder()
                                    .product(product)
                                    .amount(entry.getValue())
                                    .consumer(orderForUpdate.getConsumer())
                                    .order(orderForUpdate)
                                    .priceInOrder(product.getPrice())
                                    .build());
                } else {
                    messageBuilder.append(createResponseAboutNotEnoughAmountOfProduct(product, entry.getValue()));
                }
            }
        }

        if (messageBuilder.isEmpty()) {
            OrderStatus consumerStatus = orderUpdatingDto.getConsumerStatus() == null
                    ? orderForUpdate.getOrderStatus()
                    : OrderStatus.valueOf(orderUpdatingDto.getConsumerStatus());
            orderForUpdate.setTotalCost(totalCoast);
            orderForUpdate.setOrderStatus(consumerStatus);
            orderForUpdate.setLastUser(admin);
            orderRepository.save(orderForUpdate);
        } else throw new InsufficientProductQuantityException(messageBuilder.toString());
    }

    @Transactional
    public void deleteOrderByAdmin(Long adminId, Long orderId) {
        User admin = userRepository.findById(adminId).orElseThrow(()
                -> new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        Order orderForDelete = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderNotFoundException("Заказа с id " + orderId + " не существует; "));
        orderForDelete.setOrderStatus(OrderStatus.DELETED);
        orderForDelete.setLastUser(admin);
        //TODO проверить не нужно ли save
    }

    @Transactional(readOnly = true)
    public Page<OrderHistoryDto> getOrderHistory(Long consumerId, Pageable pageable) {
        Page<Order> allNecessaryOrders;
        if (consumerId == null) {
            allNecessaryOrders = orderRepository.findAllFetchConsumerAndLastUser(pageable);
        } else {
            allNecessaryOrders = orderRepository.findAllByConsumerIdFetchConsumerAndLastUser(consumerId, pageable);
        }
        List<OrderHistoryDto> orderHistoryDtoList = new ArrayList<>();
        for (Order order : allNecessaryOrders) {
            List<BasketElement> basketElementList = basketElementRepository.findAllByOrderIdFetchProduct(order.getId());
            orderHistoryDtoList.add(orderHistoryDtoMapper.map(order, basketElementList));
        }
        return new PageImpl<>(orderHistoryDtoList, pageable, orderHistoryDtoList.size());
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

    private boolean isPresentInBasket(Long productId, List<BasketElement> basket) {
        boolean isPresent = false;
        for (BasketElement basketElement : basket) {
            if (basketElement.getProduct().getId().equals(productId)) {
                isPresent = true;
                break;
            }
        }
        return isPresent;
    }

    private boolean isEnoughAmountOfProducts(Product product, BasketElement basketElement, Integer amountInRequest) {
        return product.getAmount() + basketElement.getAmount() > amountInRequest;
    }

    private boolean isEnoughAmountOfProducts(Product product, Integer amountInRequest) {
        return product.getAmount() > amountInRequest;
    }

    private boolean isSufficientAmountOfProducts(Integer amountInShop, Integer amountInConsumersBasket) {
        return amountInShop >= amountInConsumersBasket;
    }
}