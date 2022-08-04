package com.velb.shop.service;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//Этот сервис снимает бронирование с товаров которые покупатель забронировал подготовив заказ, но не завершив его.
//Он запускается каждые 3 часа, проверяет корзины пользователей и при наличии там товаров, которые были забронированы
//более 10 минут назад и заказ с ними не был завершен, увеличивает общее количество товара на ту величину, котора была забронирована
@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ProductRepository productRepository;
    private final BasketElementRepository basketElementRepository;

    @Transactional
    public void clearDeferredProducts() {
        List<BasketElement> basketElementsFromDeferredOrder = basketElementRepository.findAllByDeferredTimeBiggerThanTenMinutes(LocalDateTime.now().plusMinutes(10));
        basketElementsFromDeferredOrder.forEach(basketElement ->
        {
            basketElement.setProductBookingTime(null);
            basketElement.getProduct().setAmount(
                    basketElement.getProduct().getAmount()
                    + basketElement.getAmount());
            productRepository.save(basketElement.getProduct());
        });
    }
}
