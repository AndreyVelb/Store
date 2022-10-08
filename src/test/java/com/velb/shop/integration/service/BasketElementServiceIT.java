package com.velb.shop.integration.service;

import com.velb.shop.exception.BasketElementNotFoundException;
import com.velb.shop.exception.BasketIsEmptyException;
import com.velb.shop.exception.InsufficientProductQuantityException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.exception.TotalRuntimeException;
import com.velb.shop.exception.UserNotFoundException;
import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.BasketElementDto;
import com.velb.shop.model.dto.BasketElementUpdatingDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import com.velb.shop.service.BasketElementService;
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
public class BasketElementServiceIT extends IntegrationTestBase {
    private final BasketElementRepository basketElementRepository;
    private final ProductRepository productRepository;
    private final BasketElementService basketElementService;

    @Test
    void getAllBasketElementsFromBasket() {
        long consumerId = 5L;
        var basketDto = basketElementService.getAllBasketElementsFromBasket(consumerId);

        assertEquals(basketDto.getBasketElementResponseDtoList().size(), 5);
        assertEquals(basketDto.getTotalPrice(), 2502);
    }

    @Test
    void getAllBasketElementsFromBasketThrowUserNotFoundEx() {
        long nonExistentUserId = 1000000L;
        String expectedExceptionMessage = "Вы указали некорректные данные. Такого пользователя не существует";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> basketElementService.getAllBasketElementsFromBasket(nonExistentUserId));
        String actualExceptionMessage = exception.getMessage();

        assertTrue(actualExceptionMessage.contains(expectedExceptionMessage));
    }

    @Test
    void getAllBasketElementsFromBasketThrowBasketIsEmptyEx() {
        long consumerId = 1L;
        String expectedExceptionMessage = "Ваша корзина пуста; ";

        Exception exception = assertThrows(BasketIsEmptyException.class, ()
                -> basketElementService.getAllBasketElementsFromBasket(consumerId));
        String actualExceptionMessage = exception.getMessage();

        assertTrue(actualExceptionMessage.contains(expectedExceptionMessage));
    }

    @Test
    void addProductsToBasket() {
        Long consumerId = 3L;
        List<BasketElementDto> basketElementDtoList = createBasketElementDtoList();
        var basketEl1FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(0).getProductId());
        var basketEl2FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(1).getProductId());
        var basketEl3FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(2).getProductId());
        int amountBeforeAdding1 = getBasketElementAmount(basketEl1FromDB);
        int amountBeforeAdding2 = getBasketElementAmount(basketEl2FromDB);
        int amountBeforeAdding3 = getBasketElementAmount(basketEl3FromDB);

        basketElementService.addProductsToBasket(consumerId, basketElementDtoList);

        var basketEl1AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(0).getProductId());
        var basketEl2AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(1).getProductId());
        var basketEl3AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(2).getProductId());

        assertTrue(basketEl1AfterAdding.isPresent());
        assertTrue(basketEl2AfterAdding.isPresent());
        assertTrue(basketEl3AfterAdding.isPresent());
        assertEquals(basketEl1AfterAdding.get().getAmount(), basketElementDtoList.get(0).getAmount() + amountBeforeAdding1);
        assertEquals(basketEl2AfterAdding.get().getAmount(), basketElementDtoList.get(1).getAmount() + amountBeforeAdding2);
        assertEquals(basketEl3AfterAdding.get().getAmount(), basketElementDtoList.get(2).getAmount() + amountBeforeAdding3);
    }

    @Test
    void addProductsToBasketThrowTotalRuntimeEx() {
        Long consumerId = 3L;
        BasketElementDto unNormalBasketElementDto1 = BasketElementDto.builder()
                .productId(10000L)
                .amount(3)
                .build();
        BasketElementDto unNormalBasketElementDto2 = BasketElementDto.builder()
                .productId(5L)
                .amount(100000000)
                .build();
        List<BasketElementDto> basketElementDtoList = new ArrayList<>();
        List<BasketElementDto> listWithNormalBasketEls = createBasketElementDtoList();
        basketElementDtoList.add(listWithNormalBasketEls.get(0));
        basketElementDtoList.add(unNormalBasketElementDto1);
        basketElementDtoList.add(listWithNormalBasketEls.get(1));
        basketElementDtoList.add(unNormalBasketElementDto2);
        basketElementDtoList.add(listWithNormalBasketEls.get(2));
        var basketEl1FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(0).getProductId());
        var basketEl3FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(2).getProductId());
        var basketEl4FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(3).getProductId());
        var basketEl5FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementDtoList.get(4).getProductId());
        int amountBeforeAdding1 = getBasketElementAmount(basketEl1FromDB);
        int amountBeforeAdding3 = getBasketElementAmount(basketEl3FromDB);
        int amountBeforeAdding4 = getBasketElementAmount(basketEl4FromDB);
        int amountBeforeAdding5 = getBasketElementAmount(basketEl5FromDB);
        Optional<Product> product = productRepository.findById(basketElementDtoList.get(3).getProductId());
        assertTrue(product.isPresent());
        String expectedExceptionMessage = "Вы выбрали некорректный товар, возможно он уже был удален; " +
                "К сожалению товара: " +
                product.get().getTitle() +
                " недостаточно в магазине. Мы можем предложить вам " +
                product.get().getAmount() +
                " единиц; ";

        Exception exception = assertThrows(TotalRuntimeException.class, ()
                -> basketElementService.addProductsToBasket(consumerId, basketElementDtoList));

        var basketEl1AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(0).getProductId());
        var basketEl3AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(2).getProductId());
        var basketEl4AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(3).getProductId());
        var basketEl5AfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElementDtoList.get(4).getProductId());

        assertTrue(basketEl1AfterAdding.isPresent());
        assertTrue(basketEl3AfterAdding.isPresent());
        assertTrue(basketEl5AfterAdding.isPresent());
        assertEquals(basketElementDtoList.get(0).getAmount() + amountBeforeAdding1, basketEl1AfterAdding.get().getAmount());
        assertEquals(basketElementDtoList.get(2).getAmount() + amountBeforeAdding3, basketEl3AfterAdding.get().getAmount());
        assertEquals(amountBeforeAdding4, getBasketElementAmount(basketEl4AfterAdding));
        assertEquals(basketElementDtoList.get(4).getAmount() + amountBeforeAdding5, basketEl5AfterAdding.get().getAmount());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void changeBasketElements() {
        Long consumerId = 2L;
        List<BasketElementUpdatingDto> basketElementUpdatingDtoList = createBasketElementUpdatingDtoList();

        basketElementService.changeBasketElements(basketElementUpdatingDtoList, consumerId);

        var basketEl1AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementUpdatingDtoList.get(0).getProductId());
        var basketEl2AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementUpdatingDtoList.get(1).getProductId());
        var basketEl3AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElementUpdatingDtoList.get(2).getProductId());

        assertTrue(basketEl1AfterChanging.isPresent());
        assertTrue(basketEl2AfterChanging.isPresent());
        assertTrue(basketEl3AfterChanging.isPresent());
        assertEquals(basketElementUpdatingDtoList.get(0).getAmount(), basketEl1AfterChanging.get().getAmount());
        assertEquals(basketElementUpdatingDtoList.get(1).getAmount(), basketEl2AfterChanging.get().getAmount());
        assertEquals(basketElementUpdatingDtoList.get(2).getAmount(), basketEl3AfterChanging.get().getAmount());
    }

    @Test
    void changeBasketElementsThrowTotalRuntimeEx() {
        Long consumerId = 2L;
        BasketElementUpdatingDto unNormalBasketElUpdatingDto1 = BasketElementUpdatingDto.builder()
                .productId(10000L)
                .amount(3)
                .build();
        BasketElementUpdatingDto unNormalBasketElUpdatingDto2 = BasketElementUpdatingDto.builder()
                .productId(5L)
                .amount(100000000)
                .build();
        List<BasketElementUpdatingDto> basketElUpdatingDtoList = new ArrayList<>();
        List<BasketElementUpdatingDto> normalBasketElUpdatingDtoList = createBasketElementUpdatingDtoList();
        basketElUpdatingDtoList.add(normalBasketElUpdatingDtoList.get(0));
        basketElUpdatingDtoList.add(unNormalBasketElUpdatingDto1);
        basketElUpdatingDtoList.add(normalBasketElUpdatingDtoList.get(1));
        basketElUpdatingDtoList.add(unNormalBasketElUpdatingDto2);
        basketElUpdatingDtoList.add(normalBasketElUpdatingDtoList.get(2));

        var basketEl4FromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(
                consumerId,
                basketElUpdatingDtoList.get(3).getProductId());
        int amountBeforeChanging4 = getBasketElementAmount(basketEl4FromDB);

        Optional<Product> product = productRepository.findById(basketElUpdatingDtoList.get(3).getProductId());
        assertTrue(product.isPresent());
        String expectedExceptionMessage = "Выбранный товар не существует; " +
                "К сожалению товара: " +
                product.get().getTitle() +
                " недостаточно в магазине. Мы можем предложить вам " +
                product.get().getAmount() +
                " единиц; ";

        Exception exception = assertThrows(TotalRuntimeException.class, ()
                -> basketElementService.changeBasketElements(basketElUpdatingDtoList, consumerId));

        var basketEl1AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElUpdatingDtoList.get(0).getProductId());
        var basketEl3AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElUpdatingDtoList.get(2).getProductId());
        var basketEl4AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElUpdatingDtoList.get(3).getProductId());
        var basketEl5AfterChanging = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, basketElUpdatingDtoList.get(4).getProductId());

        assertTrue(basketEl1AfterChanging.isPresent());
        assertTrue(basketEl3AfterChanging.isPresent());
        assertTrue(basketEl5AfterChanging.isPresent());
        assertEquals(basketElUpdatingDtoList.get(0).getAmount(), basketEl1AfterChanging.get().getAmount());
        assertEquals(basketElUpdatingDtoList.get(2).getAmount(), basketEl3AfterChanging.get().getAmount());
        assertEquals(amountBeforeChanging4, getBasketElementAmount(basketEl4AfterChanging));
        assertEquals(basketElUpdatingDtoList.get(4).getAmount(), basketEl5AfterChanging.get().getAmount());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void deleteBasketElements() {
        Long consumerId = 2L;
        List<Long> basketElementIdList = List.of(1L, 2L, 3L);
        Optional<BasketElement> basketElBeforeDeleting1 = basketElementRepository.findById(basketElementIdList.get(0));
        Optional<BasketElement> basketElBeforeDeleting2 = basketElementRepository.findById(basketElementIdList.get(1));
        Optional<BasketElement> basketElBeforeDeleting3 = basketElementRepository.findById(basketElementIdList.get(2));
        assertTrue(basketElBeforeDeleting1.isPresent());
        assertTrue(basketElBeforeDeleting2.isPresent());
        assertTrue(basketElBeforeDeleting3.isPresent());

        basketElementService.deleteBasketElements(basketElementIdList, consumerId);

        Optional<BasketElement> basketElAfterDeleting1 = basketElementRepository.findById(basketElementIdList.get(0));
        Optional<BasketElement> basketElAfterDeleting2 = basketElementRepository.findById(basketElementIdList.get(1));
        Optional<BasketElement> basketElAfterDeleting3 = basketElementRepository.findById(basketElementIdList.get(2));
        assertFalse(basketElAfterDeleting1.isPresent());
        assertFalse(basketElAfterDeleting2.isPresent());
        assertFalse(basketElAfterDeleting3.isPresent());
    }

    @Test
    void deleteBasketElementsThrowTotalRuntimeEx() {
        Long consumerId = 2L;
        Long nonExistedBasketElementId1 = 15L;
        Long nonExistedBasketElementId2 = 5L;
        List<Long> basketElementIdList = List.of(1L, nonExistedBasketElementId1, 2L, nonExistedBasketElementId2, 3L);
        Optional<BasketElement> basketElBeforeDeleting0 = basketElementRepository.findById(basketElementIdList.get(0));
        Optional<BasketElement> basketElBeforeDeleting2 = basketElementRepository.findById(basketElementIdList.get(2));
        Optional<BasketElement> basketElBeforeDeleting4 = basketElementRepository.findById(basketElementIdList.get(4));
        assertTrue(basketElBeforeDeleting0.isPresent());
        assertTrue(basketElBeforeDeleting2.isPresent());
        assertTrue(basketElBeforeDeleting4.isPresent());

        String expectedExceptionMessage = "Вами указана неверно одна из позиций для удаления. Ее не существует; " +
                "Вами указана неверно одна из позиций для удаления. Ее не существует; ";

        Exception exception = assertThrows(TotalRuntimeException.class, ()
                -> basketElementService.deleteBasketElements(basketElementIdList, consumerId));

        Optional<BasketElement> basketElAfterDeleting0 = basketElementRepository.findById(basketElementIdList.get(0));
        Optional<BasketElement> basketElAfterDeleting2 = basketElementRepository.findById(basketElementIdList.get(2));
        Optional<BasketElement> basketElAfterDeleting4 = basketElementRepository.findById(basketElementIdList.get(4));
        assertFalse(basketElAfterDeleting0.isPresent());
        assertFalse(basketElAfterDeleting2.isPresent());
        assertFalse(basketElAfterDeleting4.isPresent());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void getBasketElementFromBasket() {
        long basketElId = 5L;
        Optional<BasketElement> basketElementFromDb = basketElementRepository.findById(basketElId);
        assertTrue(basketElementFromDb.isPresent());
        int expectedTotalPriceOfProductPosition = basketElementFromDb.get().getAmount()
                * basketElementFromDb.get().getProduct().getPrice();

        var basketEl = basketElementService.getBasketElementFromBasket(basketElId);

        assertEquals(basketEl.getId(), basketElId);
        assertEquals(expectedTotalPriceOfProductPosition, basketEl.getTotalPriceOfProductPosition());
        assertEquals(basketElementFromDb.get().getAmount(), basketEl.getAmount());
    }

    @Test
    void getBasketElementFromBasketThrowBasketElNotFoundEx() {
        long nonExistedBasketElementId = 100000L;
        String expectedExceptionMessage = "Такой позиции в вашей корзине нет; ";

        Exception exception = assertThrows(BasketElementNotFoundException.class, ()
                -> basketElementService.getBasketElementFromBasket(nonExistedBasketElementId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void addNewProductToBasketIfItNotPresent() {
        long consumerId = 2L;
        long productId = 4L;
        int amount = 15;
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(productId)
                .amount(amount)
                .build();

        long basketElId = basketElementService.addProductToBasket(consumerId, basketElementDto);

        Optional<BasketElement> mayBeAddedBasketEl = basketElementRepository.findById(basketElId);

        assertTrue(mayBeAddedBasketEl.isPresent());
        assertEquals(mayBeAddedBasketEl.get().getProduct().getId(), basketElementDto.getProductId());
        assertEquals(mayBeAddedBasketEl.get().getAmount(), basketElementDto.getAmount());
    }

    @Test
    void addNewProductToBasketIfItNotPresentThrowUserNotFoundEx() {
        long nonExistentUserId = 10000000L;
        long productId = 4L;
        int amount = 15;
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(productId)
                .amount(amount)
                .build();
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> basketElementService.addProductToBasket(nonExistentUserId, basketElementDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void addNewProductToBasketIfItNotPresentThrowProductNotFoundEx() {
        long consumerId = 2L;
        long nonExistentProductId = 100000L;
        int amount = 15;
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(nonExistentProductId)
                .amount(amount)
                .build();
        String expectedExceptionMessage = "Вы выбрали некорректный товар, возможно он уже был удален; ";

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> basketElementService.addProductToBasket(consumerId, basketElementDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void addNewProductToBasketIfItNotPresentThrowInsufficientProductQuantityEx() {
        long consumerId = 2L;
        long productId = 4L;
        int nonExistedBasketAmount = 100000;
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(productId)
                .amount(nonExistedBasketAmount)
                .build();
        Optional<Product> product = productRepository.findById(productId);
        assertTrue(product.isPresent());
        String expectedExceptionMessage = "К сожалению товара: " +
                product.get().getTitle() +
                " недостаточно в магазине. Мы можем предложить вам " +
                product.get().getAmount() +
                " единиц; ";

        Exception exception = assertThrows(InsufficientProductQuantityException.class, ()
                -> basketElementService.addProductToBasket(consumerId, basketElementDto));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void addProductToBasketIfItIsPreset() {
        long consumerId = 3L;
        long productId = 1L;
        int amount = 5;
        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(productId)
                .amount(amount)
                .build();
        Optional<BasketElement> basketElFromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, productId);
        assertTrue(basketElFromDB.isPresent());
        assertTrue(basketElFromDB.get().getAmount() > 0);
        int amountProductsInBasket = basketElFromDB.get().getAmount();

        long basketElId = basketElementService.addProductToBasket(consumerId, basketElementDto);
        Optional<BasketElement> mayBeAddedBasketEl = basketElementRepository.findById(basketElId);

        assertTrue(mayBeAddedBasketEl.isPresent());
        assertEquals(mayBeAddedBasketEl.get().getProduct().getId(), basketElementDto.getProductId());
        System.out.println(basketElementDto.getAmount());
        System.out.println(basketElFromDB.get().getAmount());
        assertEquals(mayBeAddedBasketEl.get().getAmount(), basketElementDto.getAmount() + amountProductsInBasket);
    }

    @Test
    void addNewProductToBasketIfItIsPresentThrowInsufficientProductQuantityEx() {
        long consumerId = 3L;
        long productId = 1L;
        int nonExistedBasketAmount = 1500000000;

        BasketElementDto basketElementDto = BasketElementDto.builder()
                .productId(productId)
                .amount(nonExistedBasketAmount)
                .build();
        Optional<BasketElement> basketElFromDB = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, productId);
        assertTrue(basketElFromDB.isPresent());
        assertTrue(basketElFromDB.get().getAmount() > 0);
        String expectedExceptionMessage = "К сожалению товара: " +
                basketElFromDB.get().getProduct().getTitle() +
                " недостаточно в магазине. Мы можем предложить вам " +
                basketElFromDB.get().getProduct().getAmount() +
                " единиц; ";

        Exception exception = assertThrows(InsufficientProductQuantityException.class, ()
                -> basketElementService.addProductToBasket(consumerId, basketElementDto));
        Optional<BasketElement> basketElAfterAdding = basketElementRepository.findByConsumerIdAndProductIdFetchProductNotOrdered(consumerId, productId);

        assertTrue(basketElAfterAdding.isPresent());
        assertEquals(basketElAfterAdding.get().getProduct().getId(), basketElementDto.getProductId());
        assertEquals(basketElAfterAdding.get().getAmount(), basketElFromDB.get().getAmount());
        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void deleteBasketElement() {
        long consumerId = 2L;
        long basketElementId = 10L;
        Optional<BasketElement> basketElementForDeletion = basketElementRepository.findById(basketElementId);
        assertTrue(basketElementForDeletion.isPresent());

        basketElementService.deleteBasketElement(basketElementId, consumerId);

        Optional<BasketElement> deletedBasketElement = basketElementRepository.findById(basketElementId);
        assertFalse(deletedBasketElement.isPresent());
    }

    @Test
    void deleteBasketElementThrowBasketElementNotFoundException() {
        long consumerId = 3L;
        long nonExistedBasketElementId = 10000L;
        String expectedExceptionMessage = "Вами указана неверно одна из позиций для удаления. Ее не существует; ";

        Exception exception = assertThrows(BasketElementNotFoundException.class, ()
                -> basketElementService.deleteBasketElement(nonExistedBasketElementId, consumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void changeBasketElement() {
        long consumerId = 3L;
        long productId = 1L;
        int amount = 4;
        BasketElementUpdatingDto updatingInfo = BasketElementUpdatingDto.builder()
                .productId(productId)
                .amount(amount)
                .build();

        long changingBasketElId = basketElementService.changeBasketElement(updatingInfo, consumerId);
        Optional<BasketElement> changingBasketEl = basketElementRepository.findById(changingBasketElId);

        assertTrue(changingBasketEl.isPresent());
        assertEquals(changingBasketEl.get().getAmount(), updatingInfo.getAmount());
    }

    @Test
    void changeBasketElementThrowUserNotFoundEx() {
        long nonExistedConsumerId = 100000L;
        long productId = 1L;
        int amount = 10;
        BasketElementUpdatingDto updatingInfo = BasketElementUpdatingDto.builder()
                .productId(productId)
                .amount(amount)
                .build();
        String expectedExceptionMessage = "Вы вошли в систему как некорректный пользователь; ";

        Exception exception = assertThrows(UserNotFoundException.class, ()
                -> basketElementService.changeBasketElement(updatingInfo, nonExistedConsumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    @Test
    void changeBasketElementThrowProductNotFoundEx() {
        long consumerId = 3L;
        long nonExistedProductId = 1000000L;
        int amount = 10;
        BasketElementUpdatingDto updatingInfo = BasketElementUpdatingDto.builder()
                .productId(nonExistedProductId)
                .amount(amount)
                .build();
        String expectedExceptionMessage = "Выбранный товар не существует; ";

        Exception exception = assertThrows(ProductNotFoundException.class, ()
                -> basketElementService.changeBasketElement(updatingInfo, consumerId));

        assertTrue(exception.getMessage().contains(expectedExceptionMessage));
    }

    private List<BasketElementUpdatingDto> createBasketElementUpdatingDtoList() {
        BasketElementUpdatingDto basketElementDto1 = BasketElementUpdatingDto.builder()
                .productId(1L)
                .amount(3)
                .build();
        BasketElementUpdatingDto basketElementDto2 = BasketElementUpdatingDto.builder()
                .productId(2L)
                .amount(10)
                .build();
        BasketElementUpdatingDto basketElementDto3 = BasketElementUpdatingDto.builder()
                .productId(3L)
                .amount(5)
                .build();
        return List.of(basketElementDto1, basketElementDto2, basketElementDto3);
    }

    private List<BasketElementDto> createBasketElementDtoList() {
        BasketElementDto basketElementDto1 = BasketElementDto.builder()
                .productId(1L)
                .amount(3)
                .build();
        BasketElementDto basketElementDto2 = BasketElementDto.builder()
                .productId(2L)
                .amount(10)
                .build();
        BasketElementDto basketElementDto3 = BasketElementDto.builder()
                .productId(3L)
                .amount(5)
                .build();
        return List.of(basketElementDto1, basketElementDto2, basketElementDto3);
    }

    private int getBasketElementAmount(Optional<BasketElement> optionalBasketElement) {
        if (optionalBasketElement.isPresent()) {
            return optionalBasketElement.get().getAmount();
        } else return 0;
    }
}