package nl.group9.eurocart.order;

/** What the customer fills in: an email address, plus shipping details when the cart has physical products. */
public record OrderRequest(String email, ShippingAddress shipping) {
}
