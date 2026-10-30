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
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;

import org.springframework.stereotype.Service;

/**
 * Prices catalogue amounts (EUR, excl. VAT) for Poland: converts to PLN at a fixed rate and adds VAT.
 * Rounding is HALF_UP to 2 decimals, first the net amount, then the VAT over that net amount.
 */
@Service
public class PricingService {

	static final String CURRENCY = "PLN";

	/** ECB reference rate of 2026-10-29. */
	static final BigDecimal EUR_TO_PLN = new BigDecimal("4.3938");

	/** ASSUMPTION: to be confirmed by compliance / the product owner. */
	private static final Map<Category, BigDecimal> POLISH_VAT_PERCENT = Map.of(
			Category.LAPTOP, new BigDecimal("23"),
			Category.BOOK, new BigDecimal("5"),
			Category.FOOD, new BigDecimal("5"),
			Category.MEDICINE, new BigDecimal("8"),
			Category.EBOOK, new BigDecimal("5"));

	private final ProductCatalogue catalogue;

	public PricingService(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	/** Price per unit of every catalogue product, by product id. */
	public Map<String, Price> productPrices() {
		return catalogue.all().stream()
				.collect(Collectors.toMap(Product::id, product -> price(product.price(), product.category())));
	}

	public PricedCart price(CartView cart) {
		List<PricedLine> lines = cart.lines().stream().map(this::price).toList();
		return new PricedCart(CURRENCY, lines,
				sum(lines, Price::net), sum(lines, Price::vat), sum(lines, Price::gross));
	}

	/** Prices {@code euros} (EUR, excl. VAT) of a product in the given category. */
	Price price(BigDecimal euros, Category category) {
		BigDecimal vatPercent = POLISH_VAT_PERCENT.get(category);
		BigDecimal net = euros.multiply(EUR_TO_PLN).setScale(2, RoundingMode.HALF_UP);
		BigDecimal vat = net.multiply(vatPercent).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
		return new Price(vatPercent, net, vat, net.add(vat));
	}

	private PricedLine price(CartLine line) {
		return new PricedLine(line.product(), line.quantity(), price(line.lineTotal(), line.product().category()));
	}

	private static BigDecimal sum(List<PricedLine> lines, Function<Price, BigDecimal> amount) {
		return lines.stream()
				.map(line -> amount.apply(line.price()))
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
	}

}
