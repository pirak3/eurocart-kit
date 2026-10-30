package nl.group9.eurocart.geo;

import java.util.Optional;

/** Finds the country of a public IP address, as an ISO 3166 alpha-2 code such as {@code NL}. */
public interface IpCountryLookup {

	Optional<String> countryCode(String ip);

}
