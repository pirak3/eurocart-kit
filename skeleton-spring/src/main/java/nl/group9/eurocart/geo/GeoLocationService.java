package nl.group9.eurocart.geo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import nl.group9.eurocart.catalogue.Country;

import org.springframework.stereotype.Service;

/** Decides which storefront a visitor gets, from the country of their IP address. */
@Service
public class GeoLocationService {

	/** The storefront for visitors we cannot locate, or who are in a country we do not sell to. */
	public static final Country DEFAULT_COUNTRY = Country.PL;

	/** Only literal IPv4 / IPv6 addresses: anything else is never looked up, nor resolved through DNS. */
	private static final Pattern IP_LITERAL = Pattern.compile("[0-9a-fA-F:.]{2,45}");

	private final IpCountryLookup lookup;

	public GeoLocationService(IpCountryLookup lookup) {
		this.lookup = lookup;
	}

	/** The storefront country for {@code ip}, or empty when unknown (private address, failed lookup, no storefront). */
	public Optional<Country> locate(String ip) {
		if (ip == null || !IP_LITERAL.matcher(ip.trim()).matches() || !isPublic(ip.trim())) {
			return Optional.empty();
		}
		return lookup.countryCode(ip.trim()).flatMap(GeoLocationService::storefront);
	}

	private static Optional<Country> storefront(String code) {
		return Arrays.stream(Country.values()).filter(country -> country.name().equalsIgnoreCase(code)).findFirst();
	}

	private static boolean isPublic(String ip) {
		try {
			InetAddress address = InetAddress.getByName(ip);
			return !(address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
					|| address.isSiteLocalAddress() || address.isMulticastAddress());
		}
		catch (UnknownHostException ex) {
			return false;
		}
	}

}
