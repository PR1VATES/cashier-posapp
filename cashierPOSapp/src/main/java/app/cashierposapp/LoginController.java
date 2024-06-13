package app.cashierposapp;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateCredentials(username, password)) {
            stage.close();
            openCashierApp(stage); // Pass the existing stage
        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    private boolean validateCredentials(String username, String password) {
        // Simple validation logic for demonstration purposes
        return "admin".equals(username) && "password".equals(password);
    }

    private void openCashierApp(Stage stage) {
        MainApp mainApp = new MainApp();
        mainApp.showCashierApp(stage); // Pass the existing stage
    }
}
