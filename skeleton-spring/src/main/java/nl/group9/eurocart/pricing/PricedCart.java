package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.util.List;

/** The cart priced in {@code currency}; the totals are the sums of the already rounded lines. */
public record PricedCart(String currency, List<PricedLine> lines, BigDecimal net, BigDecimal vat, BigDecimal gross) {
}
