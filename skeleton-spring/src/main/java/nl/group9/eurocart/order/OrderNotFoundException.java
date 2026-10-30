package nl.group9.eurocart.order;

public class OrderNotFoundException extends RuntimeException {

	public OrderNotFoundException(String id) {
		super("Unknown order: " + id);
	}

}
