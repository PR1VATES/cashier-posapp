package app.cashierposapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProductList {
    private final Map<String, Product> products;
    private final ObservableList<Product> productObservableList;
    private final Lock lock = new ReentrantLock();

    public ProductList() {
        products = new HashMap<>();
        productObservableList = FXCollections.observableArrayList();
        initializeProducts();
    }

    private void initializeProducts() {
        addProduct(new Product("AP01", "Apple", 30, 100));
        addProduct(new Product("BN01", "Banana", 18, 150));
        addProduct(new Product("RP01", "Rice", 60, 100));
        addProduct(new Product("EG01", "Eggs", 8, 100));
        addProduct(new Product("CH01", "Chicken", 180, 50));
        addProduct(new Product("BF01", "Beef", 350, 60));
        addProduct(new Product("PK01", "Pork", 280, 60));
        addProduct(new Product("TO01", "Tomato", 35, 30));
        addProduct(new Product("CP01", "Carrot", 30, 100));
        addProduct(new Product("PT01", "Potato", 45, 100));
        addProduct(new Product("ON01", "Onion", 25, 100));
        addProduct(new Product("CB01", "Cabbage", 40, 87));
        addProduct(new Product("TS01", "T-Shirt", 300, 86));
        addProduct(new Product("JN01", "Jeans", 700, 100));
        addProduct(new Product("SK01", "Skirt", 500, 123));
        addProduct(new Product("SH01", "Shoes", 1000, 231));
        addProduct(new Product("DR01", "Dress", 900, 322));
    }

    public void addProduct(Product product) {
        lock.lock();
        try {
            products.put(product.getId(), product);
            productObservableList.add(product);
        } finally {
            lock.unlock();
        }
    }

    public Product getProductById(String id) {
        lock.lock();
        try {
            return products.get(id);
        } finally {
            lock.unlock();
        }
    }

    public Product getProductByName(String name) {
        lock.lock();
        try {
            for (Product product : products.values()) {
                if (product.getName().equals(name)) {
                    return product;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public Collection<Product> getAllProducts() {
        lock.lock();
        try {
            return products.values();
        } finally {
            lock.unlock();
        }
    }

    public ObservableList<Product> getProductObservableList() {
        lock.lock();
        try {
            return FXCollections.unmodifiableObservableList(productObservableList);
        } finally {
            lock.unlock();
        }
    }

    public void updateProductPrice(String id, double newPrice) {
        lock.lock();
        try {
            Product product = products.get(id);
            if (product != null) {
                product.setPrice(newPrice);
                productObservableList.set(productObservableList.indexOf(product), product);
            }
        } finally {
            lock.unlock();
        }
    }

    public void updateProductStock(String id, int newStock) {
        lock.lock();
        try {
            Product product = products.get(id);
            if (product != null) {
                product.setStock(newStock);
                productObservableList.set(productObservableList.indexOf(product), product);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeProduct(String id) {
        lock.lock();
        try {
            Product product = products.remove(id);
            if (product != null) {
                productObservableList.remove(product);
            }
        } finally {
            lock.unlock();
        }
    }

    // Additional utility methods

    public boolean productExists(String id) {
        lock.lock();
        try {
            return products.containsKey(id);
        } finally {
            lock.unlock();
        }
    }

    public int getTotalProductCount() {
        lock.lock();
        try {
            return products.size();
        } finally {
            lock.unlock();
        }
    }
}
