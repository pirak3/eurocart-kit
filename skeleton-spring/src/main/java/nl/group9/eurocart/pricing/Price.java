package nl.group9.eurocart.pricing;

import java.math.BigDecimal;

/** A price in PLN: net amount, VAT amount and gross amount, all rounded to 2 decimals. */
public record Price(BigDecimal vatPercent, BigDecimal net, BigDecimal vat, BigDecimal gross) {
}
