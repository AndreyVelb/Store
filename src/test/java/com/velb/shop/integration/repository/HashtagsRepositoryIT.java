package com.velb.shop.integration.repository;

import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.ProductForSearchImplDto;
import com.velb.shop.repository.HashtagsRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class HashtagsRepositoryIT extends IntegrationTestBase {
    private final HashtagsRepository hashtagsRepository;

    @Test
    void findProductListByHashtags() {
        Set<String> hashtags = new HashSet<>();
        hashtags.add("надежный");
        hashtags.add("кухонный");

        List<ProductForSearchImplDto> result = hashtagsRepository.searchProductsByHashtags(hashtags);

        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(4, result.get(0).getId());
        Assertions.assertEquals(10, result.get(1).getId());
    }

    @Test
    void findProductForSearchDtoPageByHashtags() {
        Set<String> hashtags = new HashSet<>();
        hashtags.add("надежный");
        hashtags.add("кухонный");
        Pageable pageable = PageRequest.of(0, 1);

        Page<ProductForSearchImplDto> result = hashtagsRepository.searchProductsByHashtagsAsPage(hashtags, pageable);

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(2, result.getTotalPages());
    }

}
