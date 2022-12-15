package com.velb.shop.controller;

import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.dto.ProductCreatingDto;
import com.velb.shop.model.dto.ProductDeletingDto;
import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.dto.ProductAmountUpdatingDto;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.model.dto.ProductUpdatingDto;
import com.velb.shop.model.entity.User;
import com.velb.shop.service.EmailService;
import com.velb.shop.service.ProductService;
import com.velb.shop.service.ProductsSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final EmailService emailService;
    private final ProductsSearchService searchService;

    @PostMapping(value = "/admins/{adminId}/products", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("authentication.principal.id == #adminId")
    public ResponseEntity<String> create(@RequestBody @Validated ProductCreatingDto productCreatingDto,
                                         @PathVariable Long adminId) {
        Long newProductId = productService.createProduct(productCreatingDto);
        URI location = URI.create("/api/v1/products/" + newProductId);
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "/admins/{adminId}/products/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void update(@RequestBody @Validated ProductUpdatingDto productUpdatingDto,
                       @PathVariable Long adminId) {
        productService.updateProductAndSendEmails(productUpdatingDto);
    }

    @DeleteMapping(value = "/admins/{adminId}/products/{productId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("authentication.principal.id == #adminId")
    public void delete(@RequestBody ProductDeletingDto deletingDto, @PathVariable Long adminId) {
        Set<User> consumersWhoHaveDeletingProduct = productService.getConsumersByProductId(deletingDto.getProductId());
        ProductForMessageDto beingDeletedProductDto = productService.getProductDto(deletingDto.getProductId());
        productService.deleteProduct(deletingDto);
        emailService.sendEmailAboutProductDeleting(consumersWhoHaveDeletingProduct, beingDeletedProductDto);
    }

    @GetMapping(value = "/products")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<? extends ProductForSearchDto> searchProducts(@RequestParam(name = "searchQuery") String searchQuery,
                                                                      @RequestParam(name = "hashtags") String hashtags,
                                                                      Pageable pageable) {
        Page<? extends ProductForSearchDto> products = searchService.findProducts(searchQuery, hashtags, pageable);
        return PageResponse.of(products);
    }

    @GetMapping(value = "/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ProductForSearchDto getProduct(@PathVariable Long productId) {
        return searchService.findProductById(productId);
    }
}
