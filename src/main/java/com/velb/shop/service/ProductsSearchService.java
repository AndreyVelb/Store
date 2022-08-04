package com.velb.shop.service;

import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductsSearchService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductForSearchDto> findProducts(String searchQuery, String hashtags, Pageable pageable) {
        boolean isSearchQueryPresent = isSearchQueryPresent(searchQuery);
        boolean isHashtagsPresent = isHashtagsPresent(hashtags);
        if (isSearchQueryPresent && isHashtagsPresent) {
            String searchQueryWithORBetweenWords = replaceAllSpacesWithOR(searchQuery);
            return productRepository.findAllThroughAdvancedSearch(searchQueryWithORBetweenWords, hashtags, pageable);
        } else if (isSearchQueryPresent) {
            String searchQueryWithORBetweenWords = replaceAllSpacesWithOR(searchQuery);
            return productRepository.findAllThroughQuickSearch(searchQueryWithORBetweenWords, pageable);
        } else if (isHashtagsPresent) {
            return productRepository.findAllThroughSearchByHashtags(hashtags, pageable);
        } else {
            return productRepository.findAllProducts(pageable);
        }
    }

    @Transactional(readOnly = true)
    public ProductForSearchDto findProductById(Long productId) {
        return productRepository.findProductForSearchDtoById(productId);
    }

    private boolean isSearchQueryPresent(String searchQuery) {
        return !Objects.equals(searchQuery, "");
    }

    private boolean isHashtagsPresent(String hashtags) {
        return !Objects.equals(hashtags, "");
    }

    private String replaceAllSpacesWithOR(String stringWithSpaces) {
        return stringWithSpaces.replaceAll("[ +]", " OR ");
    }

}
