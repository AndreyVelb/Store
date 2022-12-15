package com.velb.shop.service;

import com.velb.shop.exception.OrderNotFoundException;
import com.velb.shop.exception.ProductNotFoundException;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Order;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.User;
import com.velb.shop.repository.BasketElementRepository;
import com.velb.shop.repository.OrderRepository;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final MessageCreatorService messageCreatorService;
    private final JavaMailSender emailSender;

    public void sendEmailAboutOrderCreating(Long orderId, String toAddress) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(toAddress);
        simpleMailMessage.setSubject("О создании заказа в магазине");
        simpleMailMessage.setText(messageCreatorService.createEmailMessageAboutOrderCreating(orderId));
        emailSender.send(simpleMailMessage);
    }

    public void sendEmailAboutProductUpdating(Set<User> consumersWhoHaveUpdatingProduct, Long productId) {
        String subject = "Оповещение об изменении характеристик товара";
        String message = messageCreatorService.createEmailMessageAboutProductUpdating(productId);
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
        String message = messageCreatorService.createEmailMessageAboutProductDeleting(deletedProductDto);
        for (User consumer : consumersWhoHaveDeletingProduct) {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(consumer.getEmail());
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);
            emailSender.send(simpleMailMessage);
        }
    }

}
