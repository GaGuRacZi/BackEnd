package com.gaguraczi.paw.domain.billing.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SubscriptionRenewalTxService {

    private final SubscriptionService subscriptionService;

    public SubscriptionRenewalTxService(@Lazy SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDueOne(UUID uid) {
        subscriptionService.processDueOne(uid);
    }
}
