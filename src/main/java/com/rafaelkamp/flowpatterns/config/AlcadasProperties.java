package com.rafaelkamp.flowpatterns.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "flowpatterns.alcadas")
public record AlcadasProperties(
        BigDecimal despesaLimiteDiretor,
        BigDecimal compraLimiteDiretor,
        Integer feriasDiasLimiteDiretor
) {

    public AlcadasProperties {
        if (despesaLimiteDiretor == null) {
            despesaLimiteDiretor = new BigDecimal("5000.00");
        }
        if (compraLimiteDiretor == null) {
            compraLimiteDiretor = new BigDecimal("20000.00");
        }
        if (feriasDiasLimiteDiretor == null) {
            feriasDiasLimiteDiretor = 20;
        }
    }
}
