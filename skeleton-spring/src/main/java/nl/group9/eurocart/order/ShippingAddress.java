package nl.group9.eurocart.order;

/** Where physical products are delivered. {@code phone} is optional, everything else is required. */
public record ShippingAddress(
		String fullName,
		String street,
		String houseNumber,
		String postalCode,
		String city,
		String country,
		String phone) {
}
