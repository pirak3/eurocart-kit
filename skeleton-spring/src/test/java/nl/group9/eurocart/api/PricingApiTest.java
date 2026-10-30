package nl.group9.eurocart.api;

import nl.group9.eurocart.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PricingApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartService cartService;

	@BeforeEach
	void emptyCart() {
		cartService.clear();
	}

	@Test
	void pricesEveryProductInPln() throws Exception {
		mockMvc.perform(get("/api/pricing/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(51))
				.andExpect(jsonPath("$['LAP-001'].vatPercent").value(23))
				.andExpect(jsonPath("$['LAP-001'].net").value(3908.70))
				.andExpect(jsonPath("$['LAP-001'].vat").value(899.00))
				.andExpect(jsonPath("$['LAP-001'].gross").value(4807.70));
	}

	@Test
	void pricesTheCart() throws Exception {
		cartService.add("LAP-001", 1);
		cartService.add("BOK-001", 2);

		mockMvc.perform(get("/api/pricing/cart"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.lines.length()").value(2))
				.andExpect(jsonPath("$.lines[1].price.gross").value(114.14))
				.andExpect(jsonPath("$.shipping.net").value(21.74))
				.andExpect(jsonPath("$.shipping.gross").value(26.74))
				.andExpect(jsonPath("$.net").value(4039.14))
				.andExpect(jsonPath("$.vat").value(909.44))
				.andExpect(jsonPath("$.gross").value(4948.58));
	}

	@Test
	void listsAStorefrontPerCountry() throws Exception {
		mockMvc.perform(get("/api/pricing/storefronts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(6))
				.andExpect(jsonPath("$[0].country").value("NL"))
				.andExpect(jsonPath("$[0].currency").value("EUR"));
	}

	@Test
	void pricesForTheRequestedStorefront() throws Exception {
		cartService.add("LAP-001", 1);

		mockMvc.perform(get("/api/pricing/products").param("country", "NL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$['LAP-001'].vatPercent").value(21))
				.andExpect(jsonPath("$['LAP-001'].gross").value(1087.79));
		mockMvc.perform(get("/api/pricing/cart").param("country", "NL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currency").value("EUR"))
				.andExpect(jsonPath("$.eurPerUnit").value(1))
				.andExpect(jsonPath("$.shipping.gross").value(6.05))
				.andExpect(jsonPath("$.gross").value(1093.84));
	}

	@Test
	void rejectsAnUnknownStorefront() throws Exception {
		mockMvc.perform(get("/api/pricing/cart").param("country", "XX"))
				.andExpect(status().isBadRequest());
	}

}
