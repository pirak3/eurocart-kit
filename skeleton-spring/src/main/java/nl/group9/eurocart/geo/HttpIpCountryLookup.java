package nl.group9.eurocart.geo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

/**
 * Asks an external IP geolocation service, by default country.is (free, no key), which answers
 * {@code {"ip":"...","country":"NL"}}. Note that this sends the visitor's IP address to that service.
 */
@Component
public class HttpIpCountryLookup implements IpCountryLookup {

	private final String urlTemplate;

	private final ObjectMapper objectMapper;

	private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	public HttpIpCountryLookup(@Value("${eurocart.geo.url:https://api.country.is/{ip}}") String urlTemplate,
			ObjectMapper objectMapper) {
		this.urlTemplate = urlTemplate;
		this.objectMapper = objectMapper;
	}

	/** Never throws: any failure just means the country is unknown. */
	@Override
	public Optional<String> countryCode(String ip) {
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(urlTemplate.replace("{ip}", ip)))
					.timeout(Duration.ofSeconds(2))
					.build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				return Optional.empty();
			}
			Object country = objectMapper.readValue(response.body(), Map.class).get("country");
			return country instanceof String code ? Optional.of(code) : Optional.empty();
		}
		catch (IOException | RuntimeException ex) {
			return Optional.empty();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

}
