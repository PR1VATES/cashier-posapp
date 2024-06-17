package app.cashierposapp;

import javafx.beans.property.*;

public class Product {
    private final StringProperty id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty stock;

    public Product(String id, String name, double price, int stock) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleIntegerProperty(stock);
    }

    public StringProperty idProperty() {
        return id;
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    public int getStock() {
        return stock.get();
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public void reduceStock(int quantity) {
        if (this.stock.get() >= quantity) {
            this.stock.set(this.stock.get() - quantity);
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }

    public void increaseStock(int quantity) {
        this.stock.set(this.stock.get() + quantity);
    }

    @Override
    public String toString() {
        return String.format("%s - %s (₱%.2f)", getId(), getName(), getPrice());
    }
}
