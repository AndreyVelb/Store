package com.velb.shop.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.velb.shop.model.converter.OrderInfoFromJsonConverter;
import com.velb.shop.model.converter.OrderInfoToJsonConverter;
import com.velb.shop.model.entity.auxiliary.AdminOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderInfo;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class OrderAuditRecord implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id")
    private User consumer;

    @JoinColumn(name = "order_info")
    @Type(type = "jsonb")
    @JsonSerialize(converter = OrderInfoToJsonConverter.class)
    @JsonDeserialize(converter = OrderInfoFromJsonConverter.class)
    private OrderInfo orderInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "admin_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminOrderStatus adminOrderStatus;
}
