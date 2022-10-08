package com.velb.shop.repository;

import com.velb.shop.model.entity.BasketElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BasketElementRepository extends JpaRepository<BasketElement, Long> {

    @Query("select b from BasketElement b " +
            "where b.id = :basketElementId " +
            "and b.consumer.id = :consumerId")
    Optional<BasketElement> findByIdAndByConsumerId(Long basketElementId, Long consumerId);

    @Query("select b from BasketElement b " +
            "join fetch b.product " +
            "where b.order.id = :orderId")
    List<BasketElement> findAllByOrderIdFetchProduct(Long orderId);

    @Query("select b from BasketElement b " +
            "join fetch b.product " +
            "where b.consumer.id  = :consumerId " +
            "and b.order is null")
    List<BasketElement> findAllByConsumerIdFetchProductNotOrdered(Long consumerId);

    @Query("select b from BasketElement b " +
            "where b.consumer.id  = :consumerId " +
            "and b.order is null")
    List<BasketElement> findAllByConsumerIdNotOrdered(Long consumerId);

    @Query("select b from BasketElement b " +
            "where b.consumer.id  = :consumerId")
    List<BasketElement> findAllByConsumerId(Long consumerId);

    @Query("select b from BasketElement b " +
            "join fetch b.product " +
            "where b.consumer.id = :consumerId " +
            "and b.product.id = :productId " +
            "and b.order is null ")
    Optional<BasketElement> findByConsumerIdAndProductIdFetchProductNotOrdered(Long consumerId, Long productId);

    @Query(value = "select b from BasketElement b " +
            "join fetch b.product " +
            "where b.productBookingTime < :nowMinusTenMinutes ")
    List<BasketElement> findAllByDeferredTimeBiggerThanTenMinutes(LocalDateTime nowMinusTenMinutes);

    @Query("select b from BasketElement b " +
            "join fetch b.consumer " +
            "where b.product.id = :productId")
    List<BasketElement> findAllFetchConsumerByProductId(Long productId);

    @Query("select b from BasketElement b " +
            "where b.product.id = :productId")
    List<BasketElement> findAllByProductId(Long productId);

    @Query("select b from BasketElement b " +
            "where b.product.id = :productId " +
            "and b.order is null ")
    List<BasketElement> findAllByProductIdNotOrdered(Long productId);

    @Query("select b from BasketElement b " +
            "join fetch b.product " +
            "where b.consumer.id = :consumerId " +
            "and b.order is null ")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<BasketElement> findAllFetchProductByConsumerIdNotOrderedWithLock(Long consumerId);

    @Query("select b from BasketElement b " +
            "where b.order.id = :orderId")
    List<BasketElement> findAllByOrderId(Long orderId);

}
