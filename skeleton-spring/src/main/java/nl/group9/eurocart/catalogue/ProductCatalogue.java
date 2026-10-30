package nl.group9.eurocart.catalogue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
public class ProductCatalogue {

	private final Map<String, Product> productsById;

	public ProductCatalogue(ObjectMapper objectMapper) {
		this.productsById = load(objectMapper);
	}

	public List<Product> all() {
		return List.copyOf(productsById.values());
	}

	public Optional<Product> find(String id) {
		return Optional.ofNullable(productsById.get(id));
	}

	private static Map<String, Product> load(ObjectMapper objectMapper) {
		try (InputStream in = new ClassPathResource("catalogue.json").getInputStream()) {
			List<Product> products = objectMapper.readValue(in, new TypeReference<List<Product>>() {
			});
			Map<String, Product> byId = new LinkedHashMap<>();
			for (Product product : products) {
				if (product.vatPercent() == null || !product.vatPercent().keySet().containsAll(EnumSet.allOf(Country.class))) {
					throw new IllegalStateException("Missing VAT percentage per country in catalogue.json: " + product.id());
				}
				if (byId.put(product.id(), product) != null) {
					throw new IllegalStateException("Duplicate product id in catalogue.json: " + product.id());
				}
			}
			return byId;
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Could not read catalogue.json", ex);
		}
	}

}
