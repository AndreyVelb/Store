package com.velb.shop.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.velb.shop.model.converter.OrderContentFromJsonConverter;
import com.velb.shop.model.converter.OrderContentToJsonConverter;
import com.velb.shop.model.entity.auxiliary.ConsumerOrderStatus;
import com.velb.shop.model.entity.auxiliary.OrderElement;
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
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Order implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private User consumer;

    @Column(nullable = false)
    @Type(type = "jsonb")
    @JsonSerialize(converter = OrderContentToJsonConverter.class)
    @JsonDeserialize(converter = OrderContentFromJsonConverter.class)
    private List<OrderElement> content;

    @Column(nullable = false)
    private Integer totalCost;

    @Column(name = "consumer_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConsumerOrderStatus consumerOrderStatus;

}
