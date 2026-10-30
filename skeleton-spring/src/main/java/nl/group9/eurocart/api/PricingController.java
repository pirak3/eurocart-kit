package nl.group9.eurocart.api;

import java.util.Map;

import nl.group9.eurocart.cart.CartService;
import nl.group9.eurocart.pricing.PricedCart;
import nl.group9.eurocart.pricing.Price;
import nl.group9.eurocart.pricing.PricingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping("/products")
	public Map<String, Price> products() {
		return pricing.productPrices();
	}

	@GetMapping("/cart")
	public PricedCart cart() {
		return pricing.price(cart.view());
	}

}
