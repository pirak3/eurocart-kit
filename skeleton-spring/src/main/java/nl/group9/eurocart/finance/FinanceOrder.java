package nl.group9.eurocart.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * One order as finance sees it, in the currency the customer paid in. {@code shippingNet} is zero for digital-only
 * orders. {@code grossEur} is the gross amount converted back to EUR with the exchange rate stored on the order;
 * null when the order has no stored rate.
 */
public record FinanceOrder(
		String id,
		Instant createdAt,
		Instant paidAt,
		String status,
		String email,
		String currency,
		BigDecimal eurPerUnit,
		BigDecimal net,
		BigDecimal shippingNet,
		BigDecimal vat,
		BigDecimal gross,
		BigDecimal grossEur,
		List<FinanceLine> lines) {
}
