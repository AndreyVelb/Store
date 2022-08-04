package com.velb.shop.controller;

import com.velb.shop.model.dto.PageResponse;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.service.ProductsSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class SearchingProductsController {
    private final ProductsSearchService searchService;

    @GetMapping(value = "", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ProductForSearchDto> searchProducts(@RequestParam(name = "searchQuery") String searchQuery,
                                                            @RequestParam(name = "hashtags") String hashtags,
                                                            Pageable pageable) {
        Page<ProductForSearchDto> products = searchService.findProducts(searchQuery, hashtags, pageable);
        return PageResponse.of(products);
    }

    @GetMapping(value = "/{productId}", produces = "application/json;charset=UTF-8")
    @ResponseStatus(HttpStatus.OK)
    public ProductForSearchDto getProduct(@PathVariable Long productId) {
        return searchService.findProductById(productId);
    }

}
