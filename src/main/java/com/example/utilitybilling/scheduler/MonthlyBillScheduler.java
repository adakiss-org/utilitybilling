package com.example.utilitybilling.scheduler;

import com.example.utilitybilling.service.UtilityBillingService;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MonthlyBillScheduler {

    private final UtilityBillingService billingService;

    // Cron expression explanation:
    // second minute hour dayOfMonth month dayOfWeek
    // 0 0 4 1 * * => 4:00:00 AM on day 1 of every month
    @Scheduled(cron = "0 0 4 1 * *", zone = "UTC")
    public void generateMonthlyBills() {
        billingService.createBillsForCurrentMonth();
    }
}