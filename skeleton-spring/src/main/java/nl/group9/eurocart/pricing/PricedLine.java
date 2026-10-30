package nl.group9.eurocart.pricing;

import nl.group9.eurocart.catalogue.Product;

public record PricedLine(Product product, int quantity, Price price) {
}
