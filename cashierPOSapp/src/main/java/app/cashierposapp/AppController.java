package app.cashierposapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AppController {

    public Button showButton,checkoutButton,Showsalehistory,addButton,editButton,deleteButton;
    @FXML
    private DatePicker SalesHistoryDate, SetCartpurchaseDate, fromDate, toDate;
    @FXML
    private ListView<String> cartListView, SaleHistoryList, productListView;
    @FXML
    private ComboBox<String> ItemsComboBox;
    @FXML
    private TextField temporarytotal, Taxtotal, overalltotal, Indooritemsquantity;
    @FXML
    private PieChart productPieChart;
    @FXML
    private BarChart<String, Number> Profitgraph;
    @FXML
    private ListView<String> Profit_list_view;

    private final ObservableList<String> cartItems = FXCollections.observableArrayList();
    private final ObservableList<String> salesHistory = FXCollections.observableArrayList();
    private final ObservableList<String> availableItems = FXCollections.observableArrayList(
            "Apple", "Banana", "Orange", "Milk", "Eggs", "Bread", "Butter",
            "Cheese", "Chicken Breast", "Ground Beef", "Rice", "Pasta",
            "Tomato Sauce", "Potato Chips", "Cookies", "Ice Cream",
            "Cereal", "Toilet Paper", "Shampoo", "Toothpaste"
    );
    private final Map<String, Integer> itemPrices = new HashMap<>();

    public void initialize() {
        ItemsComboBox.setItems(availableItems);
        cartListView.setItems(cartItems);
        SaleHistoryList.setItems(salesHistory);
        productListView.setItems(FXCollections.observableArrayList());

        itemPrices.put("Apple", 5);
        itemPrices.put("Banana", 3);
        itemPrices.put("Orange", 4);
        itemPrices.put("Milk", 20);
        itemPrices.put("Eggs", 15);
        itemPrices.put("Bread", 10);
        itemPrices.put("Butter", 25);
        itemPrices.put("Cheese", 30);
        itemPrices.put("Chicken Breast", 50);
        itemPrices.put("Ground Beef", 60);
        itemPrices.put("Rice", 40);
        itemPrices.put("Pasta", 25);
        itemPrices.put("Tomato Sauce", 15);
        itemPrices.put("Potato Chips", 10);
        itemPrices.put("Cookies", 12);
        itemPrices.put("Ice Cream", 35);
        itemPrices.put("Cereal", 28);
        itemPrices.put("Toilet Paper", 18);
        itemPrices.put("Shampoo", 45);
        itemPrices.put("Toothpaste", 22);


        // Make ItemsComboBox searchable
        makeComboBoxSearchable(ItemsComboBox);

        // Custom cell factory for productListView
        productListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Do not set the initial value for SetCartpurchaseDate
        SetCartpurchaseDate.setValue(null);
    }

    private void makeComboBoxSearchable(ComboBox<String> comboBox) {
        comboBox.setEditable(true);
        comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> filteredItems = FXCollections.observableArrayList();
            for (String item : availableItems) {
                if (item.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            comboBox.setItems(filteredItems);
            comboBox.show();
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            comboBox.getEditor().setText(newValue);
        });
    }

    @FXML
    private void addToCart() {
        if (SetCartpurchaseDate.getValue() == null) {
            showAlert("Set purchase date before adding items.");
            return;
        }

        String item = ItemsComboBox.getValue();
        String quantityStr = Indooritemsquantity.getText();
        if (item != null && !quantityStr.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                cartItems.add(item + " x " + quantity);
                updateTotals();
                ItemsComboBox.getEditor().clear();
                ItemsComboBox.setItems(availableItems);
                ItemsComboBox.getSelectionModel().clearSelection(); // Clear the selection in the ComboBox
            } catch (NumberFormatException e) {
                showAlert("Invalid quantity. Please enter a number.");
            }
        } else {
            showAlert("Select an item and enter quantity.");
        }
    }

    @FXML
    private void editQuantity() {
        if (SetCartpurchaseDate.getValue() == null) {
            showAlert("Set purchase date before editing items.");
            return;
        }

        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String newQuantityStr = Indooritemsquantity.getText();
            try {
                int newQuantity = Integer.parseInt(newQuantityStr);
                cartItems.set(cartListView.getSelectionModel().getSelectedIndex(), selectedItem.split(" x ")[0] + " x " + newQuantity);
                updateTotals();
            } catch (NumberFormatException e) {
                showAlert("Invalid quantity. Please enter a number.");
            }
        } else {
            showAlert("Select an item to edit.");
        }
    }

    @FXML
    private void deleteFromCart() {
        if (SetCartpurchaseDate.getValue() == null) {
            showAlert("Set purchase date before deleting items.");
            return;
        }

        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartItems.remove(selectedItem);
            updateTotals();
        } else {
            showAlert("Select an item to delete.");
        }
    }

    @FXML
    private void resetCart() {
        cartItems.clear();
        temporarytotal.clear();
        Taxtotal.clear();
        overalltotal.clear();
    }

    @FXML
    private void checkOut() {
        if (cartItems.isEmpty()) {
            showAlert("Cart is empty.");
            return;
        }

        double total = Double.parseDouble(overalltotal.getText());
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Checkout");
        dialog.setHeaderText("Total: ₱" + total);
        dialog.setContentText("Enter amount received:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount >= total) {
                    double change = amount - total;
                    showAlert("Change: ₱" + change);
                    generateReceipt();
                    saveSalesToCSV();
                    resetCart();
                } else {
                    showAlert("Insufficient amount. Please enter a valid amount.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid amount. Please enter a number.");
            }
        });
    }

    @FXML
    private void showSaleHistory() {
        salesHistory.clear();
        LocalDate date = SalesHistoryDate.getValue();
        if (date != null) {
            File file = new File("sales_history.csv");
            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    int transactionTotal = 0;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(",");
                        if (LocalDate.parse(parts[0]).isEqual(date)) {
                            int quantity = Integer.parseInt(parts[2]);
                            int price = Integer.parseInt(parts[3]);
                            int total = quantity * price;
                            salesHistory.add(parts[1] + " x " + parts[2] + " - ₱" + total);
                            transactionTotal += total;
                        }
                    }
                    salesHistory.add("Total for the day: ₱" + transactionTotal);
                } catch (IOException e) {
                    showAlert("Error reading sales history.");
                }
            } else {
                showAlert("No sales history found.");
            }
        } else {
            showAlert("Select a date to view sales history.");
        }
    }

    @FXML
    private void showStatistics() {
        productListView.getItems().clear();
        productPieChart.getData().clear();
        Profit_list_view.getItems().clear(); // Clear previous profit list view entries

        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();

        if (from != null && to != null) {
            Map<String, Integer> productSales = new HashMap<>();
            Map<LocalDate, Integer> dailyProfit = new HashMap<>();
            File file = new File("sales_history.csv");

            if (file.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(",");
                        LocalDate date = LocalDate.parse(parts[0]);
                        if (!date.isBefore(from) && !date.isAfter(to)) {
                            String item = parts[1];
                            int quantity = Integer.parseInt(parts[2]);
                            int price = Integer.parseInt(parts[3]);
                            int totalPrice = Integer.parseInt(parts[4]); // Parse total price
                            productSales.put(item, productSales.getOrDefault(item, 0) + quantity);

                            dailyProfit.put(date, dailyProfit.getOrDefault(date, 0) + totalPrice);

                            String row = String.format("Date: %s | Item: %s | Quantity: %d | Retail Price: %d | Total: %d",
                                    parts[0], item, quantity, price, totalPrice);
                            productListView.getItems().add(row);
                        }
                    }
                } catch (IOException e) {
                    showAlert("Error reading sales history.");
                }

                // Calculate total sales for percentage calculation
                int totalSales = productSales.values().stream().mapToInt(Integer::intValue).sum();

                for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
                    String item = entry.getKey();
                    int quantity = entry.getValue();
                    double percentage = (quantity / (double) totalSales) * 100;
                    productPieChart.getData().add(new PieChart.Data(
                            String.format("%s (%.2f%%)", item, percentage), quantity));
                }

                updateProfitGraph(dailyProfit);

                // Add total profit per day to Profit_list_view
                for (Map.Entry<LocalDate, Integer> entry : dailyProfit.entrySet()) {
                    String profitEntry = String.format("Date: %s | Total Profit: ₱%d", entry.getKey(), entry.getValue());
                    Profit_list_view.getItems().add(profitEntry);
                }
            } else {
                showAlert("No sales history found.");
            }
        } else {
            showAlert("Select a valid date range.");
        }
    }

    private void updateProfitGraph(Map<LocalDate, Integer> dailyProfit) {
        Profitgraph.getData().clear(); // Clear existing data

        for (Map.Entry<LocalDate, Integer> entry : dailyProfit.entrySet()) {
            LocalDate date = entry.getKey();
            int profit = entry.getValue();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(date.toString());
            series.getData().add(new XYChart.Data<>(date.toString(), profit));

            Profitgraph.getData().add(series);
        }
    }


    private void updateTotals() {
        double subtotal = 0;
        for (String item : cartItems) {
            String[] parts = item.split(" x ");
            String itemName = parts[0];
            int quantity = Integer.parseInt(parts[1]);
            subtotal += itemPrices.get(itemName) * quantity;
        }

        double taxRate = 0.10;
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        temporarytotal.setText(String.valueOf(subtotal));
        Taxtotal.setText(String.valueOf(tax));
        overalltotal.setText(String.valueOf(total));
    }


    private void generateReceipt() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(java.time.LocalDateTime.now());
        String receiptFileName = "receipt_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(receiptFileName))) {
            // Header
            writer.write("**************************\n");
            writer.write("       STORE NAME        \n");
            writer.write("    ADDRESS LINE 1       \n");
            writer.write("    ADDRESS LINE 2       \n");
            writer.write("    PHONE NUMBER         \n");
            writer.write("**************************\n\n");

            writer.write("Receipt:\n");
            writer.write("Date: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(java.time.LocalDateTime.now()) + "\n\n");

            writer.write("Item               Qty   Price\n");
            writer.write("--------------------------------\n");
            for (String item : cartItems) {
                writer.write(formatReceiptItem(item) + "\n");
            }

            writer.write("\n");
            writer.write("--------------------------------\n");
            writer.write(String.format("%-20s %8s\n", "Subtotal:", "₱" + temporarytotal.getText()));
            writer.write(String.format("%-20s %8s\n", "Tax:", "₱" + Taxtotal.getText()));
            writer.write(String.format("%-20s %8s\n", "Total:", "₱" + overalltotal.getText()));
            writer.write("--------------------------------\n");

            writer.write("\nThank you for shopping with us!\n");
            writer.write("Please come again.\n");
        } catch (IOException e) {
            showAlert("Error generating receipt.");
        }
    }

    private String formatReceiptItem(String item) {
        String[] parts = item.split(" x ");
        String itemName = parts[0];
        String quantity = parts[1];
        return String.format("%-20s %5s", itemName, quantity);
    }

    private void saveSalesToCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("sales_history.csv", true))) {
            for (String item : cartItems) {
                String[] parts = item.split(" x ");
                String itemName = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                int price = itemPrices.get(itemName);
                int totalPrice = quantity * price;
                writer.write(SetCartpurchaseDate.getValue() + "," + itemName + "," + quantity + "," + price + "," + totalPrice);
                writer.newLine();
            }
        } catch (IOException e) {
            showAlert("Error saving sales history.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
