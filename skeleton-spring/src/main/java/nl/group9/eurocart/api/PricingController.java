package nl.group9.eurocart.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nl.group9.eurocart.cart.CartService;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.pricing.PricedCart;
import nl.group9.eurocart.pricing.Price;
import nl.group9.eurocart.pricing.PricingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

	private final PricingService pricing;

	private final CartService cart;

	public PricingController(PricingService pricing, CartService cart) {
		this.pricing = pricing;
		this.cart = cart;
	}

	@GetMapping("/storefronts")
	public List<Storefront> storefronts() {
		return Arrays.stream(Country.values()).map(country -> new Storefront(country, country.currency())).toList();
	}

	@GetMapping("/products")
	public Map<String, Price> products(@RequestParam(defaultValue = "PL") Country country) {
		return pricing.productPrices(country);
	}

	@GetMapping("/shipping")
	public Price shipping(@RequestParam(defaultValue = "PL") Country country) {
		return pricing.shipping(country);
	}

	@GetMapping("/cart")
	public PricedCart cart(@RequestParam(defaultValue = "PL") Country country) {
		return pricing.price(cart.view(), country);
	}

	public record Storefront(Country country, String currency) {
	}

}
