package nl.group9.eurocart.finance;

import java.math.BigDecimal;

/** Totals of the paid orders in one currency. {@code grossEur} is null when one of the orders has no stored rate. */
public record CurrencyTotal(String currency, int orders, BigDecimal net, BigDecimal vat, BigDecimal gross, BigDecimal grossEur) {
}
