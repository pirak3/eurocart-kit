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
 * Prices catalogue amounts (EUR, excl. VAT) for Poland: converts to PLN at a fixed rate and adds VAT.
 * Rounding is HALF_UP to 2 decimals, first the net amount, then the VAT over that net amount.
 * Physical products add a flat shipping fee per order.
 */
@Service
public class PricingService {

	static final String CURRENCY = "PLN";

	/** Fixed exchange rates supplied by finance: how many EUR one unit of the currency is worth. */
	static final Map<String, BigDecimal> EUR_PER_UNIT = Map.of(
			"GBP", new BigDecimal("1.20"),
			"CHF", new BigDecimal("1.09"),
			"PLN", new BigDecimal("0.23"));

	static final BigDecimal EUR_PER_PLN = EUR_PER_UNIT.get(CURRENCY);

	/** Flat shipping fee per order with physical products, in EUR excl. VAT. */
	static final BigDecimal SHIPPING_EUR = new BigDecimal("5.00");

	/** ASSUMPTION: shipping is taxed at the standard Polish rate; to be confirmed by compliance. */
	static final BigDecimal SHIPPING_VAT_PERCENT = new BigDecimal("23");

	static final Country COUNTRY = Country.PL;

	private final ProductCatalogue catalogue;

	public PricingService(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	/** Price per unit of every catalogue product, by product id. */
	public Map<String, Price> productPrices() {
		return catalogue.all().stream()
				.collect(Collectors.toMap(Product::id, product -> price(product.price(), product)));
	}

	public PricedCart price(CartView cart) {
		List<PricedLine> lines = cart.lines().stream().map(this::price).toList();
		boolean physical = cart.lines().stream().anyMatch(line -> line.product().category() != Category.EBOOK);
		Price shipping = physical ? price(SHIPPING_EUR, SHIPPING_VAT_PERCENT) : null;
		return new PricedCart(CURRENCY, EUR_PER_PLN, lines, shipping,
				sum(lines, shipping, Price::net), sum(lines, shipping, Price::vat), sum(lines, shipping, Price::gross));
	}

	/** Prices {@code euros} (EUR, excl. VAT) of {@code product}, using its VAT percentage for {@link #COUNTRY}. */
	Price price(BigDecimal euros, Product product) {
		return price(euros, product.vatPercent(COUNTRY));
	}

	/** Prices {@code euros} (EUR, excl. VAT) with the given VAT percentage. */
	private Price price(BigDecimal euros, BigDecimal vatPercent) {
		BigDecimal net = euros.divide(EUR_PER_PLN, 2, RoundingMode.HALF_UP);
		BigDecimal vat = net.multiply(vatPercent).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
		return new Price(vatPercent, net, vat, net.add(vat));
	}

	private PricedLine price(CartLine line) {
		return new PricedLine(line.product(), line.quantity(), price(line.lineTotal(), line.product()));
	}

	private static BigDecimal sum(List<PricedLine> lines, Price shipping, Function<Price, BigDecimal> amount) {
		BigDecimal goods = lines.stream()
				.map(line -> amount.apply(line.price()))
				.reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
		return shipping == null ? goods : goods.add(amount.apply(shipping));
	}

}
