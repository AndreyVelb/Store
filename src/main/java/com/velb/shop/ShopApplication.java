package com.velb.shop;

import com.velb.shop.model.entity.BasketElement;
import com.velb.shop.model.entity.Product;
import com.velb.shop.model.entity.auxiliary.Role;
import com.velb.shop.model.entity.User;
import com.velb.shop.repository.UserRepository;
import com.velb.shop.service.BasketElementService;
import com.velb.shop.service.ProductService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
public class ShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
