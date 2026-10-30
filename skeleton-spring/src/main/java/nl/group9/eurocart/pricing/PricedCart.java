package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * The cart priced in {@code currency}; one unit of that currency costs {@code eurPerUnit} EUR. {@code shipping} is
 * null when the cart has only digital products. The totals are the sums of the already rounded lines and the shipping.
 */
public record PricedCart(String currency, BigDecimal eurPerUnit, List<PricedLine> lines, Price shipping, BigDecimal net, BigDecimal vat, BigDecimal gross) {
}
