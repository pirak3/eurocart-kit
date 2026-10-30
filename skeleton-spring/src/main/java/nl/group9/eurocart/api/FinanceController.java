package nl.group9.eurocart.api;

import nl.group9.eurocart.finance.FinanceOverview;
import nl.group9.eurocart.finance.FinanceService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

	private final FinanceService finance;

	public FinanceController(FinanceService finance) {
		this.finance = finance;
	}

	@GetMapping("/orders")
	public FinanceOverview orders() {
		return finance.overview();
	}

}
