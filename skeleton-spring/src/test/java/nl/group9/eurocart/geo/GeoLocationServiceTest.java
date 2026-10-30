package nl.group9.eurocart.geo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nl.group9.eurocart.catalogue.Country;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class GeoLocationServiceTest {

	private final List<String> asked = new ArrayList<>();

	private final Map<String, String> countryByIp = Map.of(
			"93.184.216.34", "DE",
			"81.2.69.142", "gb",
			"85.17.0.1", "BE");

	private final GeoLocationService geo = new GeoLocationService(ip -> {
		asked.add(ip);
		return Optional.ofNullable(countryByIp.get(ip));
	});

	@Test
	void locatesAPublicIpInASupportedCountry() {
		assertThat(geo.locate("93.184.216.34")).contains(Country.DE);
		assertThat(geo.locate(" 81.2.69.142 ")).contains(Country.GB);
	}

	@Test
	void unknownIpOrCountryWithoutStorefrontGivesNothing() {
		assertThat(geo.locate("203.0.113.9")).isEmpty();
		assertThat(geo.locate("85.17.0.1")).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"127.0.0.1", "::1", "10.1.2.3", "192.168.1.20", "172.16.0.5", "169.254.1.1", "0.0.0.0"})
	void neverLooksUpAPrivateOrLocalAddress(String ip) {
		assertThat(geo.locate(ip)).isEmpty();
		assertThat(asked).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "  ", "example.com", "93.184.216.34/../x", "93.184.216.34;rm", "not an ip"})
	void neverLooksUpSomethingThatIsNotAnIpAddress(String ip) {
		assertThat(geo.locate(ip)).isEmpty();
		assertThat(asked).isEmpty();
	}

	@Test
	void missingIpGivesNothing() {
		assertThat(geo.locate(null)).isEmpty();
	}

}
