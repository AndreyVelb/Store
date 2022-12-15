package com.velb.shop.service;

import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.Product;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageCreatorService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BasketElementRepository basketElementRepository;

    public String createEmailMessageAboutProductDeleting(ProductForMessageDto deletedProductDto) {
        return "Здравствуйте. К сожалению мы были вынуждены удалить товар " +
                deletedProductDto.getTitle() + " \n " +
                deletedProductDto.getDescription() + " \n " +
                "который находился у вас в корзине. " +
                "Приносим свои огромные извинения...";
    }

    @Transactional(readOnly = true)
    public String createEmailMessageAboutProductUpdating(Long productId) {
        Product updatedProduct = productRepository.findById(productId).orElseThrow(()
                -> new ProductNotFoundException("Email покупателям отправлен не будет так как уникальный идентификатор" +
                " товара при создании сообщения об отправке указан неправильно; "));
        return "Здравствуйте. Приносим свои извинения но нам пришлось обновить товар " +
                "который находится у вас в корзине и теперь он представляет из себя: " +
                updatedProduct.getTitle() + " \n " + updatedProduct.getDescription() + " \n " +
                "Цена: " + updatedProduct.getPrice() + " \n ";
    }

    @Transactional(readOnly = true)
    public String createEmailMessageAboutOrderCreating(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderNotFoundException("Приносим свои извинения, но email о создании заказа вам отправлен не будет " +
                "так как произошли проблемы на сервере; "));
        List<BasketElement> orderContent = basketElementRepository.findAllByOrderId(orderId);
        StringBuilder stringBuilder = new StringBuilder();
        for (BasketElement basketElement : orderContent) {
            stringBuilder.append(" - ")
                    .append(basketElement.getProduct().getTitle())
                    .append(" - ")
                    .append(basketElement.getAmount())
                    .append(" - c общей стоимостью ")
                    .append(basketElement.getProduct().getPrice() * basketElement.getAmount())
                    .append("рублей. \n");
        }
        return "Здравствуйте. Вы сделали в нашем магазине заказ на сумму " + order.getTotalCost() +
                "рублей. Вами были заказаны следующие товары: \n" + stringBuilder +
                "Спасибо за покупку!!!";
    }

    public String createResponseAboutNotEnoughAmountOfProduct(Product product, Integer amountInUsersBasket) {
        return " - На данный момент такого количества товара " +
                product.getTitle() + " на складе нет. " +
                "Осталось " + product.getAmount() + " экземпляров, а вы хотели заказать -  " + amountInUsersBasket;
    }

    public String createResponseAboutNotEnoughAmountOfProductWithAdding(Product product, Integer amountInUsersBasket) {
        return " - Приносим свои извинения, но к сожалению на данный момент такого количества товара " +
                product.getTitle() + " на складе нет. " +
                "Мы добавили в ваш заказ " + product.getAmount() + " из " + amountInUsersBasket + " экземпляров. ";
    }

}
