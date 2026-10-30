package nl.group9.eurocart.order;

import nl.group9.eurocart.catalogue.Country;

/**
 * What the customer fills in: an email address, plus shipping details when the cart has physical products.
 * {@code storefront} is the storefront the order is placed in and priced for; PL when missing.
 */
public record OrderRequest(String email, ShippingAddress shipping, Country storefront) {
}
