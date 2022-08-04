package com.velb.shop.integration.repository;

import com.velb.shop.integration.IntegrationTestBase;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@RequiredArgsConstructor
public class ProductRepositoryIT extends IntegrationTestBase {
    private final ProductRepository productRepository;

    @Test
    void findAllThroughQuickSearchTest() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<ProductForSearchDto> foundProducts1 = productRepository.findAllThroughQuickSearch("красивый OR дубовый OR шкаф", pageable);
        Page<ProductForSearchDto> foundProducts2 = productRepository.findAllThroughQuickSearch("ШКАФ OR ЛЕГКО OR СОБИРАЕТСЯ", pageable);
        Page<ProductForSearchDto> foundProducts3 = productRepository.findAllThroughQuickSearch("сТУл", pageable);
        Page<ProductForSearchDto> foundProducts4 = productRepository.findAllThroughQuickSearch("береза ", pageable);
        Page<ProductForSearchDto> foundProducts5 = productRepository.findAllThroughQuickSearch("березовый ", pageable);

        Assertions.assertEquals(5, foundProducts1.getTotalElements());
        Assertions.assertEquals(5, foundProducts2.getTotalElements());
        Assertions.assertEquals(4, foundProducts3.getTotalElements());
        Assertions.assertEquals(2, foundProducts4.getTotalElements());
        Assertions.assertEquals(1, foundProducts5.getTotalElements());

    }

    @Test
    void findAllThroughSearchByHashtagsTest() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<ProductForSearchDto> foundProducts1 = productRepository.findAllThroughSearchByHashtags("купить березовый надежный стол", pageable);
        Page<ProductForSearchDto> foundProducts2 = productRepository.findAllThroughSearchByHashtags("приобрести хороший стул", pageable);
        Page<ProductForSearchDto> foundProducts3 = productRepository.findAllThroughSearchByHashtags("купить журнальный столик", pageable);

        Assertions.assertEquals(7, foundProducts1.getTotalElements());
        Assertions.assertEquals(1, foundProducts2.getTotalElements());
        Assertions.assertEquals(2, foundProducts3.getTotalElements());

    }

    @Test
    void findAllThroughAdvancedSearchTest() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<ProductForSearchDto> foundProducts1 = productRepository.findAllThroughAdvancedSearch("купить OR березовый OR надежный OR компьютерный OR стол", "#красивый", pageable);
        Page<ProductForSearchDto> foundProducts2 = productRepository.findAllThroughAdvancedSearch("приобрести OR хороший OR стул OR для OR пианино", "#новый", pageable);
        Page<ProductForSearchDto> foundProducts3 = productRepository.findAllThroughAdvancedSearch("купить OR журнальный OR столик OR шпон", "#журнальный", pageable);
        Page<ProductForSearchDto> foundProducts4 = productRepository.findAllThroughAdvancedSearch("купить OR приличный OR стеллаж OR по OR приемлемой OR цене ", "#приличный", pageable);

        Assertions.assertEquals(5, foundProducts1.getTotalElements());
        Assertions.assertEquals(4, foundProducts2.getTotalElements());
        Assertions.assertEquals(3, foundProducts3.getTotalElements());
        Assertions.assertEquals(2, foundProducts4.getTotalElements());
    }

}
