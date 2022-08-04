package com.velb.shop.repository;

import com.velb.shop.model.entity.OrderAuditRecord;
import com.velb.shop.model.entity.auxiliary.AdminOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderAuditRepository extends JpaRepository<OrderAuditRecord, Long> {

    @Query("select r from OrderAuditRecord r " +
            "where r.consumer.id = :consumerId " +
            "and r.admin.id = :adminId " +
            "and r.adminOrderStatus = :adminOrderStatus")
    List<OrderAuditRecord> findAllByConsumerIdAndAdminIdAndAdminStatus(Long consumerId,
                                                                       Long adminId,
                                                                       AdminOrderStatus adminOrderStatus);

    @Query(value = "select r from OrderAuditRecord r " +
            "join fetch r.admin " +
            "join fetch r.consumer",
            countQuery = "select count (r) from OrderAuditRecord r ")
    Page<OrderAuditRecord> findAllFetchAdminAndConsumer(Pageable pageable);

    @Query(value = "select r from OrderAuditRecord r " +
            "join fetch r.admin " +
            "join fetch r.consumer " +
            "where r.consumer.id = :consumerId",
            countQuery = "select count (r) from OrderAuditRecord r " +
                    "where r.consumer.id = :consumerId")
    Page<OrderAuditRecord> findAllByConsumerIdFetchAdminAndConsumer(Long consumerId, Pageable pageable);

}
