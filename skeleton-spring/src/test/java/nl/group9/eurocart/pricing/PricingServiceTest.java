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
			"LAP-001, 23, 439.38, 101.06, 540.44",
			"BOK-001,  5, 439.38,  21.97, 461.35",
			"EBK-001,  5, 439.38,  21.97, 461.35",
			"FOD-001,  5, 439.38,  21.97, 461.35",
			"MED-001,  8, 439.38,  35.15, 474.53"
	})
	void pricesOneHundredEuroWithThePolishVatOfTheProduct(String productId, String vatPercent, String net, String vat, String gross) {
		Price price = pricing.price(new BigDecimal("100.00"), catalogue.find(productId).orElseThrow());

		assertThat(price.vatPercent()).isEqualByComparingTo(vatPercent);
		assertThat(price.net()).isEqualTo(new BigDecimal(net));
		assertThat(price.vat()).isEqualTo(new BigDecimal(vat));
		assertThat(price.gross()).isEqualTo(new BigDecimal(gross));
	}

	@Test
	void roundsNetHalfUp() {
		// 25.00 EUR * 4.3938 = 109.8450 PLN
		Price price = pricing.price(new BigDecimal("25.00"), product("T-1", Category.BOOK, "12.50", "5"));

		assertThat(price.net()).isEqualTo(new BigDecimal("109.85"));
		assertThat(price.vat()).isEqualTo(new BigDecimal("5.49"));
		assertThat(price.gross()).isEqualTo(new BigDecimal("115.34"));
	}

	@Test
	void cartTotalsAreTheSumOfTheRoundedLines() {
		Product laptop = product("T-1", Category.LAPTOP, "899.00", "23");
		Product book = product("T-2", Category.BOOK, "12.50", "5");
		CartView cart = new CartView(List.of(CartLine.of(laptop, 1), CartLine.of(book, 2)), new BigDecimal("924.00"));

		PricedCart priced = pricing.price(cart);

		assertThat(priced.currency()).isEqualTo("PLN");
		assertThat(priced.net()).isEqualTo(new BigDecimal("4059.88"));
		assertThat(priced.vat()).isEqualTo(new BigDecimal("914.00"));
		assertThat(priced.gross()).isEqualTo(new BigDecimal("4973.88"));
	}

	@Test
	void emptyCartCostsNothing() {
		PricedCart priced = pricing.price(new CartView(List.of(), BigDecimal.ZERO));

		assertThat(priced.lines()).isEmpty();
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
