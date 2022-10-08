package com.velb.shop.repository;

import com.velb.shop.model.dto.ProductForSearchImplDto;
import com.velb.shop.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class HashtagsRepository {
    private final EntityManager entityManager;

    public List<ProductForSearchImplDto> searchProductsByHashtags(Set<String> hashtags) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductForSearchImplDto> criteria = builder.createQuery(ProductForSearchImplDto.class);
        Root<Product> product = criteria.from(Product.class);

        criteria.select(
                builder.construct(ProductForSearchImplDto.class,
                        product.get("id"),
                        product.get("title"),
                        product.get("description"),
                        product.get("amount"),
                        product.get("price")));

        List<Predicate> predicates = new ArrayList<>();

        for (String hashtag : hashtags) {
            predicates.add(builder.like(product.get("hashtags"), "%" + hashtag + "%"));
        }
        predicates.add(builder.greaterThan(product.get("amount").as(Integer.class), 0));

        criteria.where(builder.and(predicates.toArray(new Predicate[]{})));

        return entityManager.createQuery(criteria).getResultList();
    }

    public Page<ProductForSearchImplDto> searchProductsByHashtagsAsPage(Set<String> hashtags, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductForSearchImplDto> criteria = builder.createQuery(ProductForSearchImplDto.class);
        Root<Product> product = criteria.from(Product.class);

        criteria.select(
                builder.construct(ProductForSearchImplDto.class,
                        product.get("id"),
                        product.get("title"),
                        product.get("description"),
                        product.get("amount"),
                        product.get("price")));

        List<Predicate> predicates = new ArrayList<>();

        for (String hashtag : hashtags) {
            predicates.add(builder.like(product.get("hashtags"), "%" + hashtag + "%"));
        }
        predicates.add(builder.greaterThan(product.get("amount").as(Integer.class), 0));

        criteria.where(builder.and(predicates.toArray(new Predicate[]{})));

        List<ProductForSearchImplDto> result = entityManager.createQuery(criteria).setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Product> productsRootCount = countQuery.from(Product.class);
        countQuery.select(builder.count(productsRootCount)).where(builder.and(predicates.toArray(new Predicate[0])));

        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, count);
    }
}
