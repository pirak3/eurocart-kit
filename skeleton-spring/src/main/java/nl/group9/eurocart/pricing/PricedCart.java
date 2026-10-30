package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * The cart priced in {@code currency}; one PLN costs {@code eurPerPln} EUR. {@code shipping} is null when the cart
 * has only digital products. The totals are the sums of the already rounded lines and the shipping.
 */
public record PricedCart(String currency, BigDecimal eurPerPln, List<PricedLine> lines, Price shipping, BigDecimal net, BigDecimal vat, BigDecimal gross) {
}
