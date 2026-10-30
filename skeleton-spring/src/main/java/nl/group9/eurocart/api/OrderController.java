package nl.group9.eurocart.api;

import nl.group9.eurocart.order.Order;
import nl.group9.eurocart.order.OrderRequest;
import nl.group9.eurocart.order.OrderService;
import nl.group9.eurocart.order.PaymentRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService orders;

	public OrderController(OrderService orders) {
		this.orders = orders;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Order place(@RequestBody OrderRequest request) {
		return orders.place(request);
	}

	@GetMapping("/{id}")
	public Order get(@PathVariable String id) {
		return orders.get(id);
	}

	@PostMapping("/{id}/payment")
	public Order pay(@PathVariable String id, @RequestBody PaymentRequest request) {
		return orders.pay(id, request);
	}

}
