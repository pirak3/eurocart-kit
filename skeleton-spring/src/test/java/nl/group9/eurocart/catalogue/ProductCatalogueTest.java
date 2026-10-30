package nl.group9.eurocart.catalogue;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogueTest {

	private final ProductCatalogue catalogue = new ProductCatalogue(new ObjectMapper());

	@Test
	void containsFiftyOneProductsWithUniqueIds() {
		List<Product> products = catalogue.all();

		assertThat(products).hasSize(51);
		assertThat(products).extracting(Product::id).doesNotHaveDuplicates();
	}

	@Test
	void hasTenProductsPerCategoryPlusTheFluteSheetMusic() {
		for (Category category : Category.values()) {
			assertThat(catalogue.all()).filteredOn(p -> p.category() == category)
					.as("products in category %s", category)
					.hasSize(category == Category.BOOK ? 11 : 10);
		}
	}

	@Test
	void coversAllSixCountries() {
		Set<Country> countries = catalogue.all().stream()
				.map(Product::countryOfOrigin)
				.collect(Collectors.toSet());

		assertThat(countries).containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Country.class));
	}

	@Test
	void everyProductHasNameAndPositivePrice() {
		assertThat(catalogue.all()).allSatisfy(product -> {
			assertThat(product.name()).isNotBlank();
			assertThat(product.price()).isGreaterThan(BigDecimal.ZERO);
		});
	}

	@Test
	void everyProductHasAVatPercentageForEverySupportedCountry() {
		assertThat(catalogue.all()).allSatisfy(product -> {
			assertThat(product.vatPercent()).containsOnlyKeys(EnumSet.allOf(Country.class));
			assertThat(product.vatPercent().values()).allSatisfy(percent ->
					assertThat(percent).isBetween(BigDecimal.ZERO, new BigDecimal("100")));
		});
	}

	@Test
	void looksUpTheVatPercentageOfAProductPerCountry() {
		Product laptop = catalogue.find("LAP-001").orElseThrow();

		assertThat(laptop.vatPercent(Country.NL)).isEqualByComparingTo("21");
		assertThat(laptop.vatPercent(Country.PL)).isEqualByComparingTo("23");
		assertThat(laptop.vatPercent(Country.CH)).isEqualByComparingTo("8.1");
	}

	@Test
	void findsProductById() {
		assertThat(catalogue.find("LAP-001")).isPresent()
				.get().extracting(Product::category).isEqualTo(Category.LAPTOP);
		assertThat(catalogue.find("NOPE-999")).isEmpty();
	}

}
