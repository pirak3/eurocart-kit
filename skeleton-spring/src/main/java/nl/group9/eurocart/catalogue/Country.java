package nl.group9.eurocart.catalogue;

/** A country we sell to, with the currency its storefront charges in. */
public enum Country {
	NL("EUR"),
	DE("EUR"),
	FR("EUR"),
	PL("PLN"),
	CH("CHF"),
	GB("GBP");

	private final String currency;

	Country(String currency) {
		this.currency = currency;
	}

	public String currency() {
		return currency;
	}

}
