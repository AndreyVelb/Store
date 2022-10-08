package com.velb.shop.repository;

import com.velb.shop.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "select o from Order o " +
            "join fetch o.consumer " +
            "join fetch o.lastUser ",
            countQuery = "select count(o) from Order o")
    Page<Order> findAllFetchConsumerAndLastUser(Pageable pageable);

    @Query(value = "select o from Order o " +
            "join fetch o.consumer " +
            "join fetch o.lastUser " +
            "where o.consumer.id = :consumerId",
            countQuery = "select count(o) from Order o")
    Page<Order> findAllByConsumerIdFetchConsumerAndLastUser(Long consumerId, Pageable pageable);

}
