package com.example.utilitybilling.service;

import com.example.utilitybilling.model.*;
import com.example.utilitybilling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityBillingService {

    private final UtilityProviderRepository providerRepository;
    private final BillRepository billRepository;

    // Create new Provider
    @Transactional
    public UtilityProvider createProvider(UtilityProvider provider) {
        provider.setId(UUID.randomUUID());
        provider.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        provider = providerRepository.save(provider);

        // Create bill for current month
        createBillsForMonthByProvider(provider, ZonedDateTime.now(ZoneOffset.UTC).toLocalDate());

        return provider;
    }

    // Update existing provider with frequency changes handling
    @Transactional
    public UtilityProvider updateProvider(UUID id, UtilityProvider updatedProvider) {
        UtilityProvider existing = providerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Provider not found: " + id.toString()));

        boolean frequencyChanged = !existing.getFrequency().equals(updatedProvider.getFrequency());
        boolean dueDayChanged = existing.getDueDay() != updatedProvider.getDueDay();

        existing.setName(updatedProvider.getName());
        existing.setFrequency(updatedProvider.getFrequency());
        existing.setComment(updatedProvider.getComment());
        existing.setDueDay(updatedProvider.getDueDay());
        // We do not update createdAt after creation

        existing = providerRepository.save(existing);

        if (frequencyChanged || dueDayChanged) {
            // Sync bills for current month and forward accordingly

            // Delete bills starting from current month for this provider
            LocalDate now = LocalDate.now(ZoneOffset.UTC);
            LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
            List<Bill> billsToDelete = billRepository.findByProviderIdAndDueDateBetween(
                    id, monthStart, monthStart.plusMonths(120)); // long future window
            billRepository.deleteAll(billsToDelete);

            // Recreate bills for current month and future if needed
            createBillsForMonthByProvider(existing, now);
        }

        return existing;
    }

    // Fetch all providers
    public List<UtilityProvider> findAllProviders() {
        return providerRepository.findAll();
    }

    // Update Bill: amount & status
    @Transactional
    public Bill updateBill(UUID billId, BigDecimal amount, BillStatus status) {
        Optional<Bill> billOpt = billRepository.findById(billId);
        if (billOpt.isPresent()) {
            Bill bill = billOpt.get();
            if (amount != null) {
                bill.setAmount(amount);
            }
            bill.setStatus(status);
            bill.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            return billRepository.save(bill);
        } else {
            // Create a new bill with the provided content and new ID
            Bill newBill = Bill.builder()
                    .id(UUID.randomUUID())
                    .providerId(null) // Should be set by the caller if needed
                    .amount(amount)
                    .status(status)
                    .dueDate(null) // Should be set by the caller if needed
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();
            return billRepository.save(newBill);
        }
    }

    // Fetch bills by yyyy-MM parameter
    @Transactional(readOnly = true)
    public List<Bill> findBillsByYearMonth(String yearMonth) {
        YearMonth ym;
        try {
            ym = YearMonth.parse(yearMonth);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid year-month format. Expected yyyy-MM");
        }

        LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59);

        LocalDate nowDate = LocalDate.now(ZoneOffset.UTC);
        YearMonth currentYM = YearMonth.from(nowDate);

        if (ym.isAfter(currentYM)) {
            // Future month: calculate bills on the fly based on providers and frequency rules
            List<UtilityProvider> providers = providerRepository.findAll();
            List<Bill> bills = generateBillsForMonth(providers, startOfMonth.toLocalDate());
            // Set status to DRAFT for all future bills
            bills.forEach(b -> b.setStatus(BillStatus.DRAFT));
            return bills;
        } else {
            // Actual or past month: fetch from DB
            return billRepository.findByDueDateBetween(startOfMonth, endOfMonth);
        }
    }

    // Scheduled job: create bills for current month for all providers
    @Transactional
    public void createBillsForCurrentMonth() {
        LocalDate now = LocalDate.now(ZoneOffset.UTC);
        List<UtilityProvider> providers = providerRepository.findAll();

        for (UtilityProvider provider : providers) {
            createBillsForMonthByProvider(provider, now);
        }
    }

    // Internal: generate bills for a month based on providers and the provider's frequency / createdAt rules
    private List<Bill> generateBillsForMonth(List<UtilityProvider> providers, LocalDate targetDate) {
        List<Bill> bills = new ArrayList<>();
        int targetYear = targetDate.getYear();
        int targetMonth = targetDate.getMonthValue();

        for (UtilityProvider provider : providers) {
            if (shouldGenerateBillForMonth(provider, targetDate)) {
                // Due date with provider's due day (limit max 28)
                int dueDay = Math.min(provider.getDueDay(), targetDate.lengthOfMonth());
                LocalDateTime dueDate = LocalDateTime.of(targetYear, targetMonth, dueDay, 0, 0);

                // Generate bill instance WITHOUT id (as no persistence needed)
                Bill b = Bill.builder()
                        .id(UUID.nameUUIDFromBytes((
                                provider.getId().toString() + "-" + targetYear + "-" + targetMonth).getBytes()))
                        .providerId(provider.getId())
                        .amount(provider.getDefaultAmount())
                        .status(BillStatus.NOT_ARRIVED)
                        .dueDate(dueDate)
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                        .build();

                bills.add(b);
            }
        }
        bills.sort(Comparator.comparing(Bill::getDueDate));
        return bills;
    }

    // Helper: decide if a bill should be generated for a provider in target month
    private boolean shouldGenerateBillForMonth(UtilityProvider provider, LocalDate targetDate) {
        YearMonth targetYM = YearMonth.from(targetDate);
        YearMonth createdYM = YearMonth.from(provider.getCreatedAt());

        if (targetYM.isBefore(createdYM)) return false;

        switch (provider.getFrequency()) {
            case MONTHLY:
                return true;
            case BI_MONTHLY:
                int monthsDiff = (targetYM.getYear() - createdYM.getYear()) * 12 + (targetYM.getMonthValue() - createdYM.getMonthValue());
                return monthsDiff % 2 == 0;
            case YEARLY:
                return targetYM.getMonthValue() == createdYM.getMonthValue();
            default:
                return false;
        }
    }

    // When a new provider is created or frequency changed: create bills for current month forward
    private void createBillsForMonthByProvider(UtilityProvider provider, LocalDate fromDate) {
        // Only current month bill creation is required for new provider or after frequency change per spec,
        // but follow scheduler logic: generate only the bill for the 'fromDate' month (usually current month)
        List<Bill> existingBills = billRepository.findByProviderIdAndDueDateBetween(
                provider.getId(),
                fromDate.withDayOfMonth(1).atStartOfDay(),
                fromDate.withDayOfMonth(fromDate.lengthOfMonth()).atTime(23, 59, 59)
        );

        LocalDateTime dueDateForMonthDeadline = fromDate.withDayOfMonth(Math.min(provider.getDueDay(), fromDate.lengthOfMonth())).atStartOfDay();

        Optional<Bill> existingBillOpt = existingBills.stream()
                .filter(bill -> bill.getDueDate().toLocalDate().equals(fromDate.withDayOfMonth(1)))
                .findFirst();

        if (!existingBillOpt.isPresent() && shouldGenerateBillForMonth(provider, fromDate)) {
            Bill bill = Bill.builder()
                    .id(UUID.randomUUID())
                    .providerId(provider.getId())
                    .amount(null) // blank initial amount
                    .status(BillStatus.NOT_ARRIVED)
                    .dueDate(dueDateForMonthDeadline)
                    .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                    .updatedAt(LocalDateTime.now(ZoneOffset.UTC))
                    .build();

            billRepository.save(bill);
        }
    }
}