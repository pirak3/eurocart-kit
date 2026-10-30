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
				.andExpect(jsonPath("$['LAP-001'].net").value(3950.03))
				.andExpect(jsonPath("$['LAP-001'].vat").value(908.51))
				.andExpect(jsonPath("$['LAP-001'].gross").value(4858.54));
	}

	@Test
	void pricesTheCart() throws Exception {
		cartService.add("LAP-001", 1);
		cartService.add("BOK-001", 2);

		mockMvc.perform(get("/api/pricing/cart"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.lines.length()").value(2))
				.andExpect(jsonPath("$.lines[1].price.gross").value(115.34))
				.andExpect(jsonPath("$.net").value(4059.88))
				.andExpect(jsonPath("$.vat").value(914.00))
				.andExpect(jsonPath("$.gross").value(4973.88));
	}

}
