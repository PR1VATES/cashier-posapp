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
        addProduct(new Product("AP01", "Apple", 30));
        addProduct(new Product("BN01", "Banana", 18));
        addProduct(new Product("RP01", "Rice", 60));
        addProduct(new Product("EG01", "Eggs", 8));
        addProduct(new Product("CH01", "Chicken", 180));
        addProduct(new Product("BF01", "Beef", 350));
        addProduct(new Product("PK01", "Pork", 280));
        addProduct(new Product("TO01", "Tomato", 35));
        addProduct(new Product("CP01", "Carrot", 30));
        addProduct(new Product("PT01", "Potato", 45));
        addProduct(new Product("ON01", "Onion", 25));
        addProduct(new Product("CP01", "Cabbage", 40));
        addProduct(new Product("TS01", "T-Shirt", 300));
        addProduct(new Product("JN01", "Jeans", 700));
        addProduct(new Product("SK01", "Skirt", 500));
        addProduct(new Product("SH01", "Shoes", 1000));
        addProduct(new Product("DR01", "Dress", 900));

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
