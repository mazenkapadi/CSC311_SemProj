package viewmodel;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Predicate;

public class SignUpController {

    @FXML
    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public TextField dobField;
    public TextField zipCodeField;
    public PasswordField passwordField;
    public PasswordField confirmPasswordField;
    public Button newAccountBtn;
    public Label savedLabel;
    public Label firstNameCheck;
    public Label lastNameCheck;
    public Label emailCheck;
    public Label dobCheck;
    public Label zipCodeCheck;
    public Label passwordCheck;
    public Label confirmPasswordCheck;

    private final BooleanProperty firstNameValid = new SimpleBooleanProperty(false);
    private final BooleanProperty lastNameValid = new SimpleBooleanProperty(false);
    private final BooleanProperty emailValid = new SimpleBooleanProperty(false);
    private final BooleanProperty dobValid = new SimpleBooleanProperty(false);
    private final BooleanProperty zipCodeValid = new SimpleBooleanProperty(false);
    private final BooleanProperty passwordValid = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmPasswordValid = new SimpleBooleanProperty(false);

    public void initialize() {
        addValidationListener(firstNameField, firstNameValid, "^[A-Za-z]{2,25}$");
        addValidationListener(lastNameField, lastNameValid, "^[A-Za-z]{2,25}$");
        addValidationListener(emailField, emailValid, this::isValidEmail);
        addValidationListener(dobField, dobValid, "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d{2}$");
        addValidationListener(zipCodeField, zipCodeValid, "^\\d{5}$");
        addValidationListener(passwordField, passwordValid, this::isValidPassword);

        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.equals(passwordField.getText());
            setFieldStyle(confirmPasswordField, isValid);
            confirmPasswordValid.set(isValid);
            enableAddButtonIfValid();
        });

        enableAddButtonIfValid();
    }

    private void addValidationListener(TextField field, BooleanProperty property, String regex) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = newValue.matches(regex);
            setFieldStyle(field, isValid);
            property.set(isValid);
            enableAddButtonIfValid();
        });
    }

    private void addValidationListener(TextField field, BooleanProperty property, Predicate<String> validationFunction) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validationFunction.test(newValue);
            setFieldStyle(field, isValid);
            property.set(isValid);
            enableAddButtonIfValid();
        });
    }

    private void addValidationListener(PasswordField field, BooleanProperty property, Predicate<String> validationFunction) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = validationFunction.test(newValue);
            setFieldStyle(field, isValid);
            property.set(isValid);
            enableAddButtonIfValid();
        });
    }

    private boolean isValidEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@farmingdale.edu");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    }

    private void setFieldStyle(TextField field, boolean isValid) {
        if (isValid) {
            setFieldBorderColor(field, Color.GREEN);
            showCheckmark(field);
        } else {
            setFieldBorderColor(field, Color.RED);
            hideCheckmark(field);
        }
    }

    private void setFieldStyle(PasswordField field, boolean isValid) {
        if (isValid) {
            setFieldBorderColor(field, Color.GREEN);
            showCheckmark(field);
        } else {
            setFieldBorderColor(field, Color.RED);
            hideCheckmark(field);
        }
    }

    private void setFieldBorderColor(TextField field, Color color) {
        field.setStyle("-fx-border-color: " + toHexColor(color) + ";");
    }

    private void setFieldBorderColor(PasswordField field, Color color) {
        field.setStyle("-fx-border-color: " + toHexColor(color) + ";");
    }

    private String toHexColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void enableAddButtonIfValid() {
        newAccountBtn.disableProperty().bind(
                Bindings.not(
                        firstNameValid.and(lastNameValid).and(emailValid)
                                .and(dobValid).and(zipCodeValid).and(passwordValid).and(confirmPasswordValid)
                )
        );
    }

    private void clearFieldsWithDelay() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(5), event -> clearFields())
        );
        timeline.play();
    }

    @FXML
    private void createNewAccount(ActionEvent event) {
        if (allFieldsAreValid() && passwordMatches()) {
            saveUserData();
            clearFields();
            savedLabel.setText("Account Created Successfully!");
            savedLabel.setVisible(true);
            clearFieldsWithDelay();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please check the form for errors.");
            alert.showAndWait();
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        dobField.clear();
        zipCodeField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        savedLabel.setVisible(false);
        setFieldBorderColor(firstNameField, Color.TRANSPARENT);
        setFieldBorderColor(lastNameField, Color.TRANSPARENT);
        setFieldBorderColor(emailField, Color.TRANSPARENT);
        setFieldBorderColor(dobField, Color.TRANSPARENT);
        setFieldBorderColor(zipCodeField, Color.TRANSPARENT);
        setFieldBorderColor(passwordField, Color.TRANSPARENT);
        setFieldBorderColor(confirmPasswordField, Color.TRANSPARENT);
        hideCheckmark(firstNameField);
        hideCheckmark(lastNameField);
        hideCheckmark(emailField);
        hideCheckmark(dobField);
        hideCheckmark(zipCodeField);
        hideCheckmark(passwordField);
        hideCheckmark(confirmPasswordField);
    }

    private boolean allFieldsAreValid() {
        return firstNameValid.get() && lastNameValid.get() && emailValid.get()
                && dobValid.get() && zipCodeValid.get() && passwordValid.get() && confirmPasswordValid.get();
    }

    private boolean passwordMatches() {
        return passwordField.getText().equals(confirmPasswordField.getText());
    }

    private void showCheckmark(TextField field) {
        if (field == firstNameField) {
            firstNameCheck.setText("✓");
            firstNameCheck.setVisible(true);
        } else if (field == lastNameField) {
            lastNameCheck.setText("✓");
            lastNameCheck.setVisible(true);
        } else if (field == emailField) {
            emailCheck.setText("✓");
            emailCheck.setVisible(true);
        } else if (field == dobField) {
            dobCheck.setText("✓");
            dobCheck.setVisible(true);
        } else if (field == zipCodeField) {
            zipCodeCheck.setText("✓");
            zipCodeCheck.setVisible(true);
        } else if (field == passwordField) {
            passwordCheck.setText("✓");
            passwordCheck.setVisible(true);
        }
    }

    private void hideCheckmark(TextField field) {
        if (field == firstNameField) {
            firstNameCheck.setText("");
            firstNameCheck.setVisible(false);
        } else if (field == lastNameField) {
            lastNameCheck.setText("");
            lastNameCheck.setVisible(false);
        } else if (field == emailField) {
            emailCheck.setText("");
            emailCheck.setVisible(false);
        } else if (field == dobField) {
            dobCheck.setText("");
            dobCheck.setVisible(false);
        } else if (field == zipCodeField) {
            zipCodeCheck.setText("");
            zipCodeCheck.setVisible(false);
        } else if (field == passwordField) {
            passwordCheck.setText("");
            passwordCheck.setVisible(false);
        }
    }

    private void showCheckmark(PasswordField field) {
        if (field == passwordField) {
            passwordCheck.setText("✓");
            passwordCheck.setVisible(true);
        } else if (field == confirmPasswordField) {
            confirmPasswordCheck.setText("✓");
            confirmPasswordCheck.setVisible(true);
        }
    }

    private void hideCheckmark(PasswordField field) {
        if (field == passwordField) {
            passwordCheck.setText("");
            passwordCheck.setVisible(false);
        } else if (field == confirmPasswordField) {
            confirmPasswordCheck.setText("");
            confirmPasswordCheck.setVisible(false);
        }
    }

    private void saveUserData() {
        if (allFieldsAreValid() && passwordMatches()) {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String dob = dobField.getText();
            String zipCode = zipCodeField.getText();
            String password = passwordField.getText();

            // Save user data to Preferences
            saveUserDataToPreferences(firstName, lastName, email, dob, zipCode, password);

            // Optionally, you can clear the fields and show a success message
            clearFields();
            savedLabel.setText("Account Created Successfully!");
            savedLabel.setVisible(true);
            clearFieldsWithDelay();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please check the form for errors.");
            alert.showAndWait();
        }
    }

    private void saveUserDataToPreferences(String firstName, String lastName, String email, String dob, String zipCode, String password) {
        java.util.prefs.Preferences preferences = java.util.prefs.Preferences.userRoot().node(this.getClass().getName());

        preferences.put("firstName", firstName);
        preferences.put("lastName", lastName);
        preferences.put("email", email);
        preferences.put("dob", dob);
        preferences.put("zipCode", zipCode);
        preferences.put("password", password);
    }

    @FXML
    public void goBack(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
