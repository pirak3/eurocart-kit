package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.group9.eurocart.cart.CartLine;
import nl.group9.eurocart.cart.CartView;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;

import org.springframework.stereotype.Service;

/**
 * Prices catalogue amounts (EUR, excl. VAT) for the storefront of a country: converts to the currency of that
 * country at a fixed rate and adds the VAT the product has in that country.
 * Rounding is HALF_UP to 2 decimals, first the net amount, then the VAT over that net amount.
 */
@Service
public class PricingService {

	/**
	 * Units of each currency per EUR. PLN is the ECB reference rate of 2026-10-29.
	 * ASSUMPTION: CHF and GBP are placeholders, to be replaced by the ECB reference rates.
	 */
	static final Map<String, BigDecimal> EUR_RATES = Map.of(
			"EUR", BigDecimal.ONE,
			"PLN", new BigDecimal("4.3938"),
			"CHF", new BigDecimal("0.9300"),
			"GBP", new BigDecimal("0.8700"));

	private final ProductCatalogue catalogue;

	public PricingService(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	/** Price per unit of every catalogue product in the storefront of {@code country}, by product id. */
	public Map<String, Price> productPrices(Country country) {
		return catalogue.all().stream()
				.collect(Collectors.toMap(Product::id, product -> price(product.price(), product, country)));
	}

	public PricedCart price(CartView cart, Country country) {
		List<PricedLine> lines = cart.lines().stream().map(line -> price(line, country)).toList();
		return new PricedCart(country.currency(), lines,
				sum(lines, Price::net), sum(lines, Price::vat), sum(lines, Price::gross));
	}

	/** Prices {@code euros} (EUR, excl. VAT) of {@code product} in the storefront of {@code country}. */
	Price price(BigDecimal euros, Product product, Country country) {
		BigDecimal vatPercent = product.vatPercent(country);
		BigDecimal net = euros.multiply(EUR_RATES.get(country.currency())).setScale(2, RoundingMode.HALF_UP);
		BigDecimal vat = net.multiply(vatPercent).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
		return new Price(vatPercent, net, vat, net.add(vat));
	}

	private PricedLine price(CartLine line, Country country) {
		return new PricedLine(line.product(), line.quantity(), price(line.lineTotal(), line.product(), country));
	}

	private static BigDecimal sum(List<PricedLine> lines, Function<Price, BigDecimal> amount) {
		return lines.stream()
				.map(line -> amount.apply(line.price()))
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
	}

}
