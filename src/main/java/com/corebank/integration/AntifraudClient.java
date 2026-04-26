package com.corebank.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AntifraudClient {

    private static final Logger log = LoggerFactory.getLogger(AntifraudClient.class);

    @Value("${antifraud.enabled:true}")
    private boolean enabled;

    @Value("${antifraud.threshold:10000.00}")
    private double threshold;

    public AntifraudResult evaluate(Long accountId, double amount, String type) {
        if (!enabled) {
            log.info("Antifraude desabilitado — aprovando automaticamente");
            return new AntifraudResult(true, "disabled");
        }

        if (amount > threshold) {
            log.warn("Transacao suspeita: conta={}, valor={}, tipo={}", accountId, amount, type);
            return new AntifraudResult(false, "amount_exceeds_threshold");
        }

        log.info("Transacao aprovada: conta={}, valor={}, tipo={}", accountId, amount, type);
        return new AntifraudResult(true, "approved");
    }

    public record AntifraudResult(boolean approved, String reason) {}
}