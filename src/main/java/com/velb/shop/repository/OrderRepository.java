package com.velb.shop.repository;

import com.velb.shop.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "select o from Order o " +
            "join fetch o.consumer ",
            countQuery = "select count(o) from Order o ")
    Page<Order> findAllFetchConsumers(Pageable pageable);

}
