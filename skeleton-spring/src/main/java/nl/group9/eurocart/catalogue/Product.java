package nl.group9.eurocart.catalogue;

import java.math.BigDecimal;
import java.util.Map;

/** A catalogue product; {@code vatPercent} holds the VAT percentage per country the product is sold to. */
public record Product(
		String id,
		String name,
		Category category,
		BigDecimal price,
		Country countryOfOrigin,
		Map<Country, BigDecimal> vatPercent) {

	public BigDecimal vatPercent(Country country) {
		BigDecimal percent = vatPercent.get(country);
		if (percent == null) {
			throw new IllegalStateException("No VAT percentage for " + country + " on product " + id);
		}
		return percent;
	}

}
