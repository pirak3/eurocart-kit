package nl.group9.eurocart.finance;

import java.math.BigDecimal;

public record FinanceLine(
		String productId,
		String name,
		String category,
		int quantity,
		BigDecimal vatPercent,
		BigDecimal net,
		BigDecimal vat,
		BigDecimal gross) {
}
