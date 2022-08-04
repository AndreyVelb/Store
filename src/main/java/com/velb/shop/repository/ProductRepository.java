package com.velb.shop.repository;

import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p")
    Page<ProductForSearchDto> findAllProducts(Pageable pageable);

    // Выводит все хоть сколь бы то ни было похожие товары похожие на запрос даже одним словом
    @Query(value = "SELECT id, title, description, price, amount FROM products " +
            "WHERE fulltext @@ websearch_to_tsquery('pg_catalog.russian', :searchQuery)" +
            "AND amount > 0",
            countQuery = "SELECT COUNT(*) FROM " +
                    "(SELECT id, title, description, price, amount FROM products " +
                    "WHERE fulltext @@ websearch_to_tsquery('pg_catalog.russian', :searchQuery) " +
                    "AND amount > 0) as all_found_products",
            nativeQuery = true)
    Page<ProductForSearchDto> findAllThroughQuickSearch(String searchQuery, Pageable pageable);

    // Выводит все товары которые содержат хотя бы один из упомянутых хештегов
    @Query("select p from Product p " +
            "where p in " +
            "(select h.product from Hashtag h " +
            "where :searchQuery like concat( '%', h.hashtag, '%')) " +
            "and p.amount > 0")
    Page<ProductForSearchDto> findAllThroughSearchByHashtags(String searchQuery, Pageable pageable);

    // Выводит все товары у которых в названии, описании либо хештегах содержится какое-либо слово из запроса
    @Query(value = "(SELECT id, title, description, price, amount FROM products " +
            "WHERE fulltext @@ websearch_to_tsquery('pg_catalog.russian', :searchQuery) " +
            "AND amount > 0)" +
            "UNION " +
            "(SELECT id, title, description, price, amount FROM products " +
            "WHERE id IN " +
            "(SELECT product_id FROM hashtags " +
            "WHERE :hashtags LIKE CONCAT('%', hashtag, '%'))" +
            "AND amount > 0)",
            countQuery = "SELECT COUNT(*) FROM " +
                    "((SELECT id, title, description, price FROM products " +
                    "WHERE fulltext @@ websearch_to_tsquery('pg_catalog.russian', :searchQuery) " +
                    "AND amount > 0) " +
                    "UNION " +
                    "(SELECT id, title, description, price FROM products " +
                    "WHERE id IN " +
                    "(SELECT product_id FROM hashtags " +
                    "WHERE :hashtags LIKE CONCAT('%', hashtag, '%'))" +
                    "AND amount > 0)) as all_found_products",
            nativeQuery = true)
    Page<ProductForSearchDto> findAllThroughAdvancedSearch(String searchQuery, String hashtags, Pageable pageable);


    @Query(value = "select p from Product p " +
            "where p.id = :productId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findByIdWithPessimisticLock(Long productId);

    @Query(value = "select p from Product p " +
            "where p.id = :productId")
    ProductForSearchDto findProductForSearchDtoById(Long productId);

    @Query(value = "select new com.velb.shop.model.dto.ProductForMessageDto(p.title, p.description, p.amount, p.price) " +
            "from Product p " +
            "where p.id = :productId")
    Optional<ProductForMessageDto> findProductDtoById(Long productId);

}