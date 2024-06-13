package app.cashierposapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AppController {

    private static final String SALE_HISTORY_CSV = "sale_history.csv";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // FXML components in the "Cashier App" tab
    @FXML private ListView<String> cartListView;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button checkoutButton;
    @FXML private ListView<String> SaleHistoryList;
    @FXML private TextField overalltotal;
    @FXML private DatePicker SetCartpurchaseDate;
    @FXML private TextField itemquantity;
    @FXML private Button resetCart;
    @FXML private ListView<String> itemselection;

    // FXML components in the "Statistics" tab
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private Button showButton;
    @FXML private ListView<String> productListView;
    @FXML private PieChart productPieChart;
    @FXML private ListView<String> Profit_list_view;
    @FXML private BarChart<String, Number> Profitgraph;

    // Data storage
    private ObservableList<String> cartItems;
    private ObservableList<String> saleHistory;
    private ObservableList<String> itemSelection;
    private Map<String, Integer> itemQuantities;
    private ProductList productList;  // Updated to use ProductList

    @FXML
    private void initialize() {
        cartItems = FXCollections.observableArrayList();
        saleHistory = FXCollections.observableArrayList();
        itemSelection = FXCollections.observableArrayList();
        itemQuantities = new HashMap<>();
        productList = new ProductList();

        cartListView.setItems(cartItems);
        SaleHistoryList.setItems(saleHistory);
        itemselection.setItems(itemSelection);

        // Populate item selection from ProductList with ID and Price
        populateItemSelection();

        // Load sale history from CSV
        loadSaleHistoryFromCSV();
    }

    private void populateItemSelection() {
        for (Product product : productList.getAllProducts()) {
            itemSelection.add(String.format("%s - %s (₱%.2f)", product.getId(), product.getName(), product.getPrice()));
        }
    }

    @FXML
    private void addToCart() {
        // Get the selected item from the item selection ListView
        String selectedItemName = itemselection.getSelectionModel().getSelectedItem();
        // Extract the product name from the selected item string (assuming it has a fixed format)
        if (selectedItemName != null && !selectedItemName.isEmpty()) {
            String productName = selectedItemName.split(" - ")[1].split(" ")[0];  // Extract the product name

            // Get the corresponding product object
            Product product = productList.getProductByName(productName);

            // Ensure the product is not null and the quantity is not empty
            if (product != null && !itemquantity.getText().isEmpty()) {
                String quantityInput = itemquantity.getText();

                // Check if the input is a valid positive integer
                if (quantityInput.matches("[1-9]\\d*")) { // Regex for positive integers
                    int quantityToAdd = Integer.parseInt(quantityInput);
                    String itemKey = product.getName();

                    // Check if the item is already in the cart
                    boolean itemFound = false;
                    for (int i = 0; i < cartItems.size(); i++) {
                        String cartItem = cartItems.get(i);
                        if (cartItem.startsWith(itemKey + " x ")) {
                            int currentQuantity = Integer.parseInt(cartItem.split(" x ")[1].split(" @ ")[0]);
                            int newQuantity = currentQuantity + quantityToAdd;
                            double totalPrice = newQuantity * product.getPrice();
                            cartItems.set(i, String.format("%s x %d @ ₱%.2f", itemKey, newQuantity, totalPrice));
                            itemQuantities.put(itemKey, newQuantity);
                            itemFound = true;
                            break;
                        }
                    }

                    // If the item is not found in the cart, add it
                    if (!itemFound) {
                        double totalPrice = quantityToAdd * product.getPrice();
                        cartItems.add(String.format("%s x %d @ ₱%.2f", itemKey, quantityToAdd, totalPrice));
                        itemQuantities.put(itemKey, quantityToAdd);
                    }

                    // Update the total price
                    updateTotal();
                    System.out.println("Item added to cart: " + itemKey + " x " + quantityToAdd);
                } else {
                    // Display an error message for invalid quantity input
                    showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Please enter a valid positive integer for quantity.");
                }
            } else {
                // Display an error message for no product selected or quantity is empty
                showAlert(Alert.AlertType.ERROR, "Selection Error", "Please select a product and enter a quantity.");
            }
        } else {
            // Display an error message for no product selected
            showAlert(Alert.AlertType.ERROR, "Selection Error", "Please select a product from the list.");
        }
    }

    // Utility method to show alert dialogs
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void editQuantity() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !itemquantity.getText().isEmpty()) {
            String itemName = selectedItem.split(" x ")[0];
            int newQuantity = Integer.parseInt(itemquantity.getText());
            Product product = productList.getProductByName(itemName);
            if (product != null) {
                double totalPrice = newQuantity * product.getPrice();
                cartItems.set(cartListView.getSelectionModel().getSelectedIndex(), String.format("%s x %d @ ₱%.2f", itemName, newQuantity, totalPrice));
                itemQuantities.put(itemName, newQuantity);
                updateTotal();
                System.out.println("Item quantity edited: " + itemName + " x " + newQuantity);
            }
        }
    }


    @FXML
    private void deleteFromCart() {
        String selectedItem = cartListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String itemName = selectedItem.split(" x ")[0];
            cartItems.remove(selectedItem);
            itemQuantities.remove(itemName);
            updateTotal();
            System.out.println("Item deleted from cart: " + itemName);
        }
    }

    @FXML
    private void checkOut() {
        if (!cartItems.isEmpty()) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Payment");
            dialog.setHeaderText("Enter the payment amount:");
            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                try {
                    double payment = Double.parseDouble(result.get());
                    double total = Double.parseDouble(overalltotal.getText());
                    double change = payment - total;

                    if (change >= 0) {
                        LocalDateTime now = LocalDateTime.now();
                        String timestamp = DATE_TIME_FORMATTER.format(now);

                        StringBuilder saleRecord = new StringBuilder(timestamp);

                        List<String> saleItems = new ArrayList<>();
                        for (String item : cartItems) {
                            String[] parts = item.split(" x ");
                            String itemName = parts[0];
                            int quantity = Integer.parseInt(parts[1].split(" @ ")[0]); // Extract quantity
                            double price = productList.getProductByName(itemName).getPrice();
                            saleItems.add(String.format("%s (%d x %.2f)", itemName, quantity, price));
                        }

                        saleRecord.append(", ").append(String.join(", ", saleItems));
                        saleRecord.append(String.format(", Total: %.2f", total));
                        saleHistory.add(saleRecord.toString());

                        saveSaleHistoryToCSV();

                        cartItems.clear();
                        itemQuantities.clear();
                        overalltotal.clear();

                        // Show confirmation dialog with change
                        Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
                        confirmation.setTitle("Checkout Complete");
                        confirmation.setHeaderText("Payment Successful");
                        confirmation.setContentText(String.format("Total: ₱%.2f\nPayment: ₱%.2f\nChange: ₱%.2f", total, payment, change));
                        confirmation.showAndWait();

                        System.out.println("Checkout process completed. Change: " + change);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Insufficient Payment", "The payment amount is less than the total cost.");
                    }
                } catch (NumberFormatException e) {
                    // Handle invalid number format in payment input
                    showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid payment amount.");
                }
            }
        }
    }


    @FXML
    private void resetCart() {
        cartItems.clear();
        itemQuantities.clear();
        overalltotal.clear();
        System.out.println("Cart reset.");
    }

    @FXML
    private void showStatistics() {
        LocalDate from = fromDate.getValue();
        LocalDate to = toDate.getValue();
        if (from != null && to != null && !from.isAfter(to)) {
            System.out.println("Showing statistics from " + from + " to " + to);
            List<String> filteredSales = filterSalesByDate(from, to);
            updateProductPieChart(filteredSales);
            updateProfitGraph(filteredSales);
            updateProductListView(filteredSales);
            updateProfitListView(filteredSales);
        } else {
            System.out.println("Invalid date range.");
        }
    }


    private List<String> filterSalesByDate(LocalDate from, LocalDate to) {
        List<String> filteredSales = new ArrayList<>();
        for (String sale : saleHistory) {
            String dateStr = sale.split(",")[0];
            LocalDate saleDate = LocalDate.parse(dateStr, DATE_TIME_FORMATTER);
            if ((saleDate.isEqual(from) || saleDate.isAfter(from)) && (saleDate.isEqual(to) || saleDate.isBefore(to))) {
                filteredSales.add(sale);
            }
        }
        return filteredSales;
    }

    private void updateTotal() {
        double total = itemQuantities.entrySet().stream()
                .mapToDouble(entry -> productList.getProductByName(entry.getKey()).getPrice() * entry.getValue())
                .sum();
        overalltotal.setText(String.format("%.2f", total));
    }

    private void updateProductPieChart(List<String> filteredSales) {
        productPieChart.getData().clear();
        Map<String, Integer> productCounts = new HashMap<>();
        int totalItems = 0;
        for (String sale : filteredSales) {
            String[] saleDetails = sale.split(", ");
            for (String item : saleDetails) {
                if (item.contains("(") && item.contains(")")) {
                    String[] parts = item.split(" \\(");
                    String itemName = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].split(" ")[0].replaceAll("\\D+", ""));
                    productCounts.put(itemName, productCounts.getOrDefault(itemName, 0) + quantity);
                    totalItems += quantity;
                }
            }
        }
        for (Map.Entry<String, Integer> entry : productCounts.entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            productPieChart.getData().add(data);
        }
        // Update legend with percentages
        for (PieChart.Data data : productPieChart.getData()) {
            double percentage = (data.getPieValue() / totalItems) * 100;
            data.setName(String.format("%s (%.2f%%)", data.getName(), percentage));
        }
    }


    private void updateProfitGraph(List<String> filteredSales) {
        Profitgraph.getData().clear();
        Map<String, Double> profitData = new TreeMap<>(); // Use TreeMap to sort by date
        for (String sale : filteredSales) {
            String date = sale.split(",")[0].split(" ")[0];
            double dailyTotal = 0.0;
            for (String item : sale.split(", ")) {
                if (item.contains("(") && item.contains(")")) {
                    String[] parts = item.split(" \\(")[1].split(" x ");
                    int quantity = Integer.parseInt(parts[0].split(" ")[0]);
                    double price = Double.parseDouble(parts[1].split("\\)")[0]);
                    dailyTotal += quantity * price;
                }
            }
            profitData.put(date, profitData.getOrDefault(date, 0.0) + dailyTotal);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Profit");

        for (Map.Entry<String, Double> entry : profitData.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        Profitgraph.getData().add(series);

        // Set different colors for different data points
        int i = 0;
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.getNode().setStyle("-fx-bar-fill: " + getColor(i) + ";");
            i++;
        }
    }

    // Helper method to return a color for a given index
    private String getColor(int index) {
        String[] colors = {
                "#4caf50", "#2196f3", "#ffeb3b", "#ff5722", "#9c27b0", "#00bcd4", "#ffc107", "#e91e63", "#3f51b5", "#8bc34a"
        };
        return colors[index % colors.length];
    }


    private void updateProductListView(List<String> filteredSales) {
        productListView.getItems().clear();
        for (String sale : filteredSales) {
            productListView.getItems().add(sale);
        }
    }

    private void updateProfitListView(List<String> filteredSales) {
        Profit_list_view.getItems().clear();
        Map<String, Double> dailyProfits = new TreeMap<>(); // Use TreeMap to sort by date
        for (String sale : filteredSales) {
            String date = sale.split(",")[0].split(" ")[0];
            double dailyTotal = 0.0;
            for (String item : sale.split(", ")) {
                if (item.contains("(") && item.contains(")")) {
                    String[] parts = item.split(" \\(")[1].split(" x ");
                    int quantity = Integer.parseInt(parts[0].split(" ")[0]);
                    double price = Double.parseDouble(parts[1].split("\\)")[0]);
                    dailyTotal += quantity * price;
                }
            }
            dailyProfits.put(date, dailyProfits.getOrDefault(date, 0.0) + dailyTotal);
        }
        for (Map.Entry<String, Double> entry : dailyProfits.entrySet()) {
            Profit_list_view.getItems().add(String.format("%s: ₱%.2f", entry.getKey(), entry.getValue()));
        }
    }

    private void saveSaleHistoryToCSV() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(SALE_HISTORY_CSV))) {
            for (String sale : saleHistory) {
                writer.write(sale);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSaleHistoryFromCSV() {
        if (Files.exists(Paths.get(SALE_HISTORY_CSV))) {
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(SALE_HISTORY_CSV))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    saleHistory.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


