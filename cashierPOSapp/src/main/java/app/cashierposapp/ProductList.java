package app.cashierposapp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProductList {
    private Map<String, Product> products;

    public ProductList() {
        products = new HashMap<>();
        initializeProducts();
    }

    private void initializeProducts() {
        addProduct(new Product("AP01", "Apple", 0.5));
        addProduct(new Product("BN01", "Banana", 0.3));
        addProduct(new Product("OR01", "Orange", 0.7));
        addProduct(new Product("ML01", "Milk", 1.2));
        addProduct(new Product("BR01", "Bread", 1.0));
    }

    public void addProduct(Product product) {
        products.put(product.getId(), product);
    }

    public Product getProductById(String id) {
        return products.get(id);
    }

    public Product getProductByName(String name) {
        for (Product product : products.values()) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        return null;
    }

    public Collection<Product> getAllProducts() {
        return products.values();
    }

    public void updateProductPrice(String id, double newPrice) {
        Product product = products.get(id);
        if (product != null) {
            product.setPrice(newPrice);
        }
    }

    public void removeProduct(String id) {
        products.remove(id);
    }

    public void deleteProduct(String productId) {
    }
}
