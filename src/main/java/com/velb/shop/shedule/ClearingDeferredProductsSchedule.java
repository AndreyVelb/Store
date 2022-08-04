package com.velb.shop.shedule;

import com.velb.shop.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ClearingDeferredProductsSchedule {
    private final ScheduleService schedulesService;

    @Scheduled(cron = "${cron_interval}")
    public void clearDeferredProducts() {
        schedulesService.clearDeferredProducts();
    }

}
