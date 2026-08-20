package com.gaguraczi.paw.domain.billing.scheduler;

import com.gaguraczi.paw.domain.billing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingRenewalScheduler {

    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void tick() {
        try {
            int processed = subscriptionService.processDue();
            if (processed > 0) {
                log.info("Billing renewal processed {} subscriptions", processed);
            }
        } catch (Exception e) {
            log.error("Billing renewal failed", e);
        }
    }
}
