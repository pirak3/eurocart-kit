package nl.group9.eurocart.api;

import jakarta.servlet.http.HttpServletRequest;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.geo.GeoLocationService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

	private final GeoLocationService geo;

	public GeoController(GeoLocationService geo) {
		this.geo = geo;
	}

	/** The storefront for the visitor's IP address; {@code located} tells whether it was found or is the default. */
	@GetMapping("/country")
	public Located country(HttpServletRequest request) {
		return geo.locate(clientIp(request))
				.map(country -> new Located(country, true))
				.orElse(new Located(GeoLocationService.DEFAULT_COUNTRY, false));
	}

	/** Behind a proxy the real client is the first address of X-Forwarded-For. That header can be forged. */
	private static String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	public record Located(Country country, boolean located) {
	}

}
