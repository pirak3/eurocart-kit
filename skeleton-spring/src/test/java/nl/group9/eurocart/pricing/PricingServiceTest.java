package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import nl.group9.eurocart.cart.CartLine;
import nl.group9.eurocart.cart.CartView;
import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

	private final ProductCatalogue catalogue = new ProductCatalogue(new ObjectMapper());

	private final PricingService pricing = new PricingService(catalogue);

	@ParameterizedTest
	@CsvSource({
			"LAP-001, 23, 434.78, 100.00, 534.78",
			"BOK-001,  5, 434.78,  21.74, 456.52",
			"EBK-001,  5, 434.78,  21.74, 456.52",
			"FOD-001,  5, 434.78,  21.74, 456.52",
			"MED-001,  8, 434.78,  34.78, 469.56"
	})
	void pricesOneHundredEuroWithThePolishVatOfTheProduct(String productId, String vatPercent, String net, String vat, String gross) {
		Price price = pricing.price(new BigDecimal("100.00"), catalogue.find(productId).orElseThrow());

		assertThat(price.vatPercent()).isEqualByComparingTo(vatPercent);
		assertThat(price.net()).isEqualTo(new BigDecimal(net));
		assertThat(price.vat()).isEqualTo(new BigDecimal(vat));
		assertThat(price.gross()).isEqualTo(new BigDecimal(gross));
	}

	@Test
	void roundsNetAndVatHalfUp() {
		// 25.00 EUR / 0.23 = 108.6956... PLN, and 5% of 108.70 is exactly 5.435
		Price price = pricing.price(new BigDecimal("25.00"), product("T-1", Category.BOOK, "12.50", "5"));

		assertThat(price.net()).isEqualTo(new BigDecimal("108.70"));
		assertThat(price.vat()).isEqualTo(new BigDecimal("5.44"));
		assertThat(price.gross()).isEqualTo(new BigDecimal("114.14"));
	}

	@Test
	void cartTotalsAreTheSumOfTheRoundedLinesAndTheShipping() {
		Product laptop = product("T-1", Category.LAPTOP, "899.00", "23");
		Product book = product("T-2", Category.BOOK, "12.50", "5");
		CartView cart = new CartView(List.of(CartLine.of(laptop, 1), CartLine.of(book, 2)), new BigDecimal("924.00"));

		PricedCart priced = pricing.price(cart);

		assertThat(priced.currency()).isEqualTo("PLN");
		assertThat(priced.eurPerPln()).isEqualByComparingTo("0.23");
		// shipping: 5.00 EUR / 0.23 = 21.74 PLN, plus 23% VAT of 5.00
		assertThat(priced.shipping().net()).isEqualTo(new BigDecimal("21.74"));
		assertThat(priced.shipping().vat()).isEqualTo(new BigDecimal("5.00"));
		assertThat(priced.shipping().gross()).isEqualTo(new BigDecimal("26.74"));
		assertThat(priced.net()).isEqualTo(new BigDecimal("4039.14"));
		assertThat(priced.vat()).isEqualTo(new BigDecimal("909.44"));
		assertThat(priced.gross()).isEqualTo(new BigDecimal("4948.58"));
	}

	@Test
	void shippingIsChargedOncePerOrderNotPerProduct() {
		Product book = product("T-2", Category.BOOK, "12.50", "5");

		PricedCart one = pricing.price(new CartView(List.of(CartLine.of(book, 1)), new BigDecimal("12.50")));
		PricedCart three = pricing.price(new CartView(List.of(CartLine.of(book, 3)), new BigDecimal("37.50")));

		assertThat(three.shipping()).isEqualTo(one.shipping());
	}

	@Test
	void digitalOnlyCartHasNoShipping() {
		Product ebook = product("T-3", Category.EBOOK, "10.00", "5");

		PricedCart priced = pricing.price(new CartView(List.of(CartLine.of(ebook, 1)), new BigDecimal("10.00")));

		assertThat(priced.shipping()).isNull();
		assertThat(priced.gross()).isEqualTo(priced.lines().get(0).price().gross());
	}

	@Test
	void emptyCartCostsNothing() {
		PricedCart priced = pricing.price(new CartView(List.of(), BigDecimal.ZERO));

		assertThat(priced.lines()).isEmpty();
		assertThat(priced.shipping()).isNull();
		assertThat(priced.gross()).isEqualByComparingTo("0");
	}

	@Test
	void usesTheVatOfTheProductNotOfItsCategory() {
		Product zeroRatedLaptop = product("T-1", Category.LAPTOP, "100.00", "0");

		Price price = pricing.price(new BigDecimal("100.00"), zeroRatedLaptop);

		assertThat(price.vatPercent()).isEqualByComparingTo("0");
		assertThat(price.vat()).isEqualByComparingTo("0");
	}

	private static Product product(String id, Category category, String price, String polishVatPercent) {
		return new Product(id, id, category, new BigDecimal(price), Country.NL,
				Map.of(Country.PL, new BigDecimal(polishVatPercent)));
	}

}
