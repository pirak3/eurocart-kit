package nl.group9.eurocart.pricing;

import java.math.BigDecimal;
import java.util.List;

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

	private final PricingService pricing = new PricingService(new ProductCatalogue(new ObjectMapper()));

	@ParameterizedTest
	@CsvSource({
			"LAPTOP,   23, 439.38, 101.06, 540.44",
			"BOOK,      5, 439.38,  21.97, 461.35",
			"EBOOK,     5, 439.38,  21.97, 461.35",
			"FOOD,      5, 439.38,  21.97, 461.35",
			"MEDICINE,  8, 439.38,  35.15, 474.53"
	})
	void pricesOneHundredEuroPerCategory(Category category, String vatPercent, String net, String vat, String gross) {
		Price price = pricing.price(new BigDecimal("100.00"), category);

		assertThat(price.vatPercent()).isEqualByComparingTo(vatPercent);
		assertThat(price.net()).isEqualTo(new BigDecimal(net));
		assertThat(price.vat()).isEqualTo(new BigDecimal(vat));
		assertThat(price.gross()).isEqualTo(new BigDecimal(gross));
	}

	@Test
	void roundsNetHalfUp() {
		// 25.00 EUR * 4.3938 = 109.8450 PLN
		Price price = pricing.price(new BigDecimal("25.00"), Category.BOOK);

		assertThat(price.net()).isEqualTo(new BigDecimal("109.85"));
		assertThat(price.vat()).isEqualTo(new BigDecimal("5.49"));
		assertThat(price.gross()).isEqualTo(new BigDecimal("115.34"));
	}

	@Test
	void cartTotalsAreTheSumOfTheRoundedLines() {
		Product laptop = new Product("T-1", "Laptop", Category.LAPTOP, new BigDecimal("899.00"), Country.NL);
		Product book = new Product("T-2", "Book", Category.BOOK, new BigDecimal("12.50"), Country.NL);
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

}
