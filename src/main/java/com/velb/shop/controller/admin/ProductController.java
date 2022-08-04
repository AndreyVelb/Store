package com.velb.shop.controller.admin;

import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.service.EmailService;
import com.velb.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admins/{adminId}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final EmailService emailService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #adminId")
    public ResponseEntity<String> createProduct(@RequestBody @Validated ProductCreatingDto productCreatingDto,
                                                @PathVariable Long adminId) {
        Long newProductId = productService.createNewProduct(productCreatingDto);
        URI location = URI.create("/api/v1/products/" + newProductId);
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void updateProduct(@RequestBody @Validated ProductUpdatingDto productUpdatingDto,
                              @PathVariable Long adminId) {
        Set<User> consumersWhoHaveUpdatingProduct = productService.getConsumersWhoHaveThisProductInBasket(productUpdatingDto.getProductId());
        productService.updateProduct(productUpdatingDto);
        emailService.sendEmailAboutProductUpdating(consumersWhoHaveUpdatingProduct, productUpdatingDto.getProductId());
    }

    @PatchMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void updateProductAmount(@RequestBody ProductAmountUpdatingDto updatingDto,
                                    @PathVariable Long adminId) {
        productService.updateProductAmount(updatingDto);
    }

    @DeleteMapping(value = "/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void deleteProduct(@RequestBody ProductDeletingDto deletingDto,
                              @PathVariable Long adminId) {
        Set<User> consumersWhoHaveDeletingProduct = productService.getConsumersWhoHaveThisProductInBasket(deletingDto.getProductId());
        ProductForMessageDto beingDeletedProductDto = productService.getProductDto(deletingDto.getProductId());
        productService.deleteProduct(deletingDto);
        emailService.sendEmailAboutProductDeleting(consumersWhoHaveDeletingProduct, beingDeletedProductDto);
    }
}
