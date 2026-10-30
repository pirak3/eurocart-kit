package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import nl.group9.eurocart.cart.CartLine;
import nl.group9.eurocart.cart.CartView;
import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;

import org.springframework.stereotype.Service;

/**
 * Prices catalogue amounts (EUR, excl. VAT) for the storefront of a country: converts to the currency of that
 * country at a fixed rate and adds the VAT the product has in that country.
 * Rounding is HALF_UP to 2 decimals, first the net amount, then the VAT over that net amount.
 * Physical products add a flat shipping fee per order.
 */
@Service
public class PricingService {

	/** Fixed exchange rates supplied by finance: how many EUR one unit of the currency is worth. */
	static final Map<String, BigDecimal> EUR_PER_UNIT = Map.of(
			"EUR", BigDecimal.ONE,
			"GBP", new BigDecimal("1.20"),
			"CHF", new BigDecimal("1.09"),
			"PLN", new BigDecimal("0.23"));

	/** Flat shipping fee per order with physical products, in EUR excl. VAT. */
	static final BigDecimal SHIPPING_EUR = new BigDecimal("5.00");

	/** ASSUMPTION: shipping is taxed at the standard rate of the country; to be confirmed by compliance. */
	static final Map<Country, BigDecimal> SHIPPING_VAT_PERCENT = Map.of(
			Country.NL, new BigDecimal("21"),
			Country.DE, new BigDecimal("19"),
			Country.FR, new BigDecimal("20"),
			Country.PL, new BigDecimal("23"),
			Country.CH, new BigDecimal("8.1"),
			Country.GB, new BigDecimal("20"));

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
		boolean physical = cart.lines().stream().anyMatch(line -> line.product().category() != Category.EBOOK);
		Price shipping = physical ? shipping(country) : null;
		return new PricedCart(country.currency(), eurPerUnit(country), lines, shipping,
				sum(lines, shipping, Price::net), sum(lines, shipping, Price::vat), sum(lines, shipping, Price::gross));
	}

	/** The flat shipping fee of an order with physical products in the storefront of {@code country}. */
	public Price shipping(Country country) {
		return price(SHIPPING_EUR, SHIPPING_VAT_PERCENT.get(country), country);
	}

	/** Prices {@code euros} (EUR, excl. VAT) of {@code product} in the storefront of {@code country}. */
	Price price(BigDecimal euros, Product product, Country country) {
		return price(euros, product.vatPercent(country), country);
	}

	/** Prices {@code euros} (EUR, excl. VAT) in the currency of {@code country} with the given VAT percentage. */
	private static Price price(BigDecimal euros, BigDecimal vatPercent, Country country) {
		BigDecimal net = euros.divide(eurPerUnit(country), 2, RoundingMode.HALF_UP);
		BigDecimal vat = net.multiply(vatPercent).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
		return new Price(vatPercent, net, vat, net.add(vat));
	}

	private static BigDecimal eurPerUnit(Country country) {
		return EUR_PER_UNIT.get(country.currency());
	}

	private PricedLine price(CartLine line, Country country) {
		return new PricedLine(line.product(), line.quantity(), price(line.lineTotal(), line.product(), country));
	}

	private static BigDecimal sum(List<PricedLine> lines, Price shipping, Function<Price, BigDecimal> amount) {
		BigDecimal goods = lines.stream()
				.map(line -> amount.apply(line.price()))
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
		return shipping == null ? goods : goods.add(amount.apply(shipping));
	}

}
