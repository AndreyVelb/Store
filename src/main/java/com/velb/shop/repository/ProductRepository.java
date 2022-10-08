package com.velb.shop.repository;

import com.velb.shop.model.dto.ProductForMessageDto;
import com.velb.shop.model.dto.ProductForSearchDto;
import com.velb.shop.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
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