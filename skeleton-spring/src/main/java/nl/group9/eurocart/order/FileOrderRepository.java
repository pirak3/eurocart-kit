package nl.group9.eurocart.order;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import tools.jackson.databind.ObjectMapper;

/** Stores each order as one pretty-printed JSON file, {@code <id>.json}, in a directory. */
@Repository
public class FileOrderRepository implements OrderRepository {

	private final Path directory;

	private final ObjectMapper objectMapper;

	public FileOrderRepository(@Value("${eurocart.orders.dir:data/orders}") Path directory, ObjectMapper objectMapper) {
		this.directory = directory;
		this.objectMapper = objectMapper;
	}

	@Override
	public synchronized void save(Order order) {
		try {
			Files.createDirectories(directory);
			Path target = fileFor(order.id());
			Path temp = Files.createTempFile(directory, "order-", ".tmp");
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), order);
			Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Could not save order " + order.id(), ex);
		}
	}

	@Override
	public synchronized Optional<Order> find(String id) {
		Path file;
		try {
			file = fileFor(id);
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
		if (!Files.exists(file)) {
			return Optional.empty();
		}
		return Optional.of(objectMapper.readValue(file.toFile(), Order.class));
	}

	/** Only well-formed UUIDs map to a file, so an id can never point outside the directory. */
	private Path fileFor(String id) {
		return directory.resolve(UUID.fromString(id) + ".json");
	}

}
