package com.velb.shop.service;

import com.velb.shop.exception.BasketElementNotFoundException;
import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.TotalRuntimeException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.model.dto.BasketDto;
import com.velb.shop.model.dto.BasketElementDto;
import com.velb.shop.model.dto.BasketElementResponseDto;
import com.velb.shop.model.dto.BasketElementUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.mapper.BasketElementResponseDtoListMapper;
import com.velb.shop.model.mapper.BasketElementResponseDtoMapper;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BasketElementService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BasketElementRepository basketElementRepository;
    private final BasketElementResponseDtoMapper basketElementResponseDtoMapper;
    private final BasketElementResponseDtoListMapper basketElementResponseDtoListMapper;

    @Transactional(readOnly = true)
    public BasketDto getAllBasketElementsFromBasket(Long consumerId) {
        userRepository.findById(consumerId).orElseThrow(()
                -> new UserNotFoundException("Вы указали некорректные данные. Такого пользователя не существует"));
        List<BasketElement> consumersBasket = basketElementRepository.findAllByUserId(consumerId);

        if (consumersBasket.isEmpty()) throw new BasketIsEmptyException("Ваша корзина пуста; ");

        var usersBasketAsResponseDto = basketElementResponseDtoListMapper
                .map(consumersBasket);
        return new BasketDto(usersBasketAsResponseDto);
    }

    public void addProductsToBasket(Long consumerId, List<BasketElementDto> basketElementDtoList) {
        TotalRuntimeException totalException = new TotalRuntimeException(new ArrayList<>());

        for (BasketElementDto basketElementDto : basketElementDtoList) {
            try {
                addProductToBasket(consumerId, basketElementDto);
            } catch (RuntimeException ex) {
                totalException.addExceptionToList(ex);
            }
        }

        if (!totalException.getExceptionList().isEmpty()) {
            throw totalException;
        }
    }

    public void changeBasketElements(List<BasketElementUpdatingDto> basketElementUpdatingInfoList, Long consumerId) {
        TotalRuntimeException totalException = new TotalRuntimeException(new ArrayList<>());

        for (BasketElementUpdatingDto basketElementUpdatingDto : basketElementUpdatingInfoList) {
            try {
                changeBasketElement(basketElementUpdatingDto, consumerId);
            } catch (RuntimeException ex) {
                totalException.addExceptionToList(ex);
            }
        }

        if (!totalException.getExceptionList().isEmpty()) {
            throw totalException;
        }
    }

    public void deleteBasketElements(List<Long> basketElementIdList, Long consumerId) {
        TotalRuntimeException totalException = new TotalRuntimeException(new ArrayList<>());

        for (Long basketElementId : basketElementIdList) {
            try {
                deleteBasketElement(basketElementId, consumerId);
            } catch (RuntimeException ex) {
                totalException.addExceptionToList(ex);
            }
        }

        if (!totalException.getExceptionList().isEmpty()) {
            throw totalException;
        }
    }

    @Transactional(readOnly = true)
    public BasketElementResponseDto getBasketElementFromBasket(Long basketElementId) {
        return basketElementResponseDtoMapper.map(basketElementRepository.findById(basketElementId).orElseThrow(()
                -> new BasketElementNotFoundException("Такой позиции в вашей корзине нет; ")));
    }

    @Transactional
    public Long addProductToBasket(Long consumerId, BasketElementDto basketElementDto) {
        Optional<BasketElement> optionalBasketElement = basketElementRepository.findByConsumerIdAndProductIdFetchProduct(
                consumerId,
                basketElementDto.getProductId());

        if (optionalBasketElement.isPresent()) {
            BasketElement basketElementFromDB = optionalBasketElement.get();
            if (basketElementFromDB.getAmount() + basketElementDto.getAmount() <= basketElementFromDB.getProduct().getAmount()) {
                basketElementFromDB.setAmount(basketElementFromDB.getAmount() + basketElementDto.getAmount());
                return basketElementRepository.save(basketElementFromDB).getId();
            } else {
                throw new InsufficientProductQuantityException("К сожалению товара: " +
                        basketElementFromDB.getProduct().getTitle() +
                        " недостаточно в магазине. Мы можем предложить вам " +
                        basketElementFromDB.getProduct().getAmount() +
                        " единиц; ");
            }
        } else {
            User consumer = userRepository.findById(consumerId).orElseThrow(() ->
                    new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
            Product product = productRepository.findById(basketElementDto.getProductId()).orElseThrow(() ->
                    new ProductNotFoundException("Вы выбрали некорректный товар, возможно он уже был удален; "));
            if (basketElementDto.getAmount() <= product.getAmount()) {
                BasketElement basketElement = BasketElement.builder()
                        .consumer(consumer)
                        .product(product)
                        .amount(basketElementDto.getAmount())
                        .build();
                return basketElementRepository.save(basketElement).getId();
            } else {
                throw new InsufficientProductQuantityException("К сожалению товара: " +
                        product.getTitle() +
                        " недостаточно в магазине. Мы можем предложить вам " +
                        product.getAmount() +
                        " единиц; ");
            }
        }
    }

    @Transactional
    public Long changeBasketElement(BasketElementUpdatingDto updatingInfo, Long consumerId) {
        User consumer = userRepository.findById(consumerId).orElseThrow(() ->
                new UserNotFoundException("Вы вошли в систему как некорректный пользователь; "));
        Product product = productRepository.findById(updatingInfo.getProductId()).orElseThrow(() ->
                new ProductNotFoundException("Выбранный товар не существует; "));

        if (product.getAmount() >= updatingInfo.getAmount()) {
            Optional<BasketElement> basketElementForUpdate = basketElementRepository.findByConsumerIdAndProductIdFetchProduct(
                    consumerId,
                    updatingInfo.getProductId());
            if (basketElementForUpdate.isPresent()) {
                basketElementForUpdate.get().setAmount(updatingInfo.getAmount());
                return basketElementRepository.save(basketElementForUpdate.get()).getId();
            } else return basketElementRepository.save(
                            BasketElement.builder()
                                    .consumer(consumer)
                                    .product(product)
                                    .amount(updatingInfo.getAmount())
                                    .productBookingTime(null)
                                    .build())
                    .getId();
        } else {
            throw new InsufficientProductQuantityException("К сожалению товара: " +
                    product.getTitle() +
                    " недостаточно в магазине. Мы можем предложить вам " +
                    product.getAmount() +
                    " единиц; ");
        }
    }

    @Transactional
    public void deleteBasketElement(Long basketElementId, Long consumerId) {
        BasketElement basketElementForDeletion = basketElementRepository.findByIdAndByConsumerId(basketElementId, consumerId)
                .orElseThrow(() -> new BasketElementNotFoundException("Вами указана неверно одна из позиций для удаления. Ее не существует; "));
        basketElementRepository.delete(basketElementForDeletion);
    }
}
