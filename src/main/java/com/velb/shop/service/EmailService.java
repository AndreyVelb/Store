package com.velb.shop.service;

import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.model.entity.auxiliary.OrderElement;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender emailSender;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public void sendEmailAboutOrderCreating(Long orderId, String toAddress) {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject("О создании заказа в магазине");
        simpleMailMessage.setText(createEmailMessageAboutOrderCreating(orderId));
        emailSender.send(simpleMailMessage);
    }

    public void sendEmailAboutProductUpdating(Set<User> consumersWhoHaveUpdatingProduct, Long productId) {
        String subject = "Оповещение об изменении характеристик товара";
        String message = createEmailMessageAboutProductUpdating(productId);
        for (User consumer : consumersWhoHaveUpdatingProduct) {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(consumer.getEmail());
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            emailSender.send(simpleMailMessage);
        }
    }

    public void sendEmailAboutProductDeleting(Set<User> consumersWhoHaveDeletingProduct, ProductForMessageDto deletedProductDto) {
        String subject = "Оповещение о снятии товара с продажи";
        String message = createEmailMessageAboutProductDeleting(deletedProductDto);
        for (User consumer : consumersWhoHaveDeletingProduct) {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(consumer.getEmail());
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            emailSender.send(simpleMailMessage);
        }
    }

    private String createEmailMessageAboutProductDeleting(ProductForMessageDto deletedProductDto) {
        return "Здравствуйте. К сожалению мы были вынуждены удалить товар " +
                deletedProductDto.getTitle() + " \n " +
                deletedProductDto.getDescription() + " \n " +
                "который находился у вас в корзине. " +
                "Приносим свои огромные извинения...";
    }

    @Transactional
    public String createEmailMessageAboutProductUpdating(Long productId) {
        Product updatedProduct = productRepository.findById(productId).orElseThrow(()
                -> new ProductNotFoundException("Email покупателям отправлен не будет так как уникальный идентификатор" +
                " товара при создании сообщения об отправке указан неправильно; "));
        return "Здравствуйте. Приносим свои извинения но нам пришлось обновить товар " +
                "который находится у вас в корзине и теперь он представляет из себя: " +
                updatedProduct.getTitle() + " \n " + updatedProduct.getDescription() + " \n " +
                "Цена: " + updatedProduct.getPrice() + " \n ";
    }

    @Transactional
    public String createEmailMessageAboutOrderCreating(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(()
                -> new OrderNotFoundException("Приносим свои извинения, но email о создании заказа вам отправлен не будет " +
                "так как произошли проблемы на сервере; "));
        StringBuilder stringBuilder = new StringBuilder();
        for (OrderElement orderElement : order.getContent()) {
            stringBuilder.append(" - ")
                    .append(orderElement.getProductForOrder().getTitle())
                    .append(" - ")
                    .append(orderElement.getAmount())
                    .append(" - c общей стоимостью ")
                    .append(orderElement.getProductForOrder().getPrice() * orderElement.getAmount())
                    .append("рублей. \n");
        }
        return "Здравствуйте. Вы сделали в нашем магазине заказ на сумму " + order.getTotalCost() +
                "рублей. Вами были заказаны следующие товары: \n" + stringBuilder +
                "Спасибо за покупку!!!";
    }


}
