package com.velb.shop.service;

import com.velb.shop.model.dto.OrderAuditDto;
import com.velb.shop.model.mapper.OrderAuditDtoMapper;
import com.velb.shop.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderAuditService {
    private final OrderAuditRepository orderAuditRepository;
    private final OrderAuditDtoMapper orderAuditDtoMapper;

    @Transactional(readOnly = true)
    public Page<OrderAuditDto> getAllOrderAudit(Long consumerId, Pageable pageable) {
        if (consumerId == null) {
            return orderAuditRepository.findAllFetchAdminAndConsumer(pageable).map(orderAuditDtoMapper::map);
        } else {
            return orderAuditRepository.findAllByConsumerIdFetchAdminAndConsumer(consumerId, pageable)
                    .map(orderAuditDtoMapper::map);
        }
    }
}
