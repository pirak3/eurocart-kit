package nl.group9.eurocart.order;

import java.util.Optional;

public interface OrderRepository {

	/** Saves the order, replacing an earlier version with the same id. */
	void save(Order order);

	Optional<Order> find(String id);

}
