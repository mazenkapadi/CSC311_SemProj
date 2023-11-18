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
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Predicate;

public class SignUpController {
    public TextField firstNameField;
    public TextField lastNameField;
    public TextField emailField;
    public TextField dobField;
    public TextField zipCodeField;
    public Button newAccountBtn;
    public Label savedLabel;
    public Label firstNameCheck;
    public Label lastNameCheck;
    public Label emailCheck;
    public Label dobCheck;
    public Label zipCodeCheck;

    private final BooleanProperty firstNameValid = new SimpleBooleanProperty(false);
    private final BooleanProperty lastNameValid = new SimpleBooleanProperty(false);
    private final BooleanProperty emailValid = new SimpleBooleanProperty(false);
    private final BooleanProperty dobValid = new SimpleBooleanProperty(false);
    private final BooleanProperty zipCodeValid = new SimpleBooleanProperty(false);



    public void initialize() {
        addValidationListener(firstNameField, firstNameValid, "^[A-Za-z]{2,25}$");
        addValidationListener(lastNameField, lastNameValid, "^[A-Za-z]{2,25}$");
        addValidationListener(emailField, emailValid, this::isValidEmail);
        addValidationListener(dobField, dobValid, "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/(19|20)\\d{2}$");
        addValidationListener(zipCodeField, zipCodeValid, "^\\d{5}$");

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

    private boolean isValidEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@farmingdale.edu");
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

    private void setFieldBorderColor(TextField field, Color color) {
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
                        firstNameValid.and(lastNameValid).and(emailValid).and(dobValid).and(zipCodeValid)
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
    private void handleAddButtonClick() {
        if (allFieldsAreValid()) {
            savedLabel.setText("Data Saved");
            savedLabel.setVisible(true);
            clearFieldsWithDelay();
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        dobField.clear();
        zipCodeField.clear();
        savedLabel.setVisible(false);
        setFieldBorderColor(firstNameField, Color.TRANSPARENT);
        setFieldBorderColor(lastNameField, Color.TRANSPARENT);
        setFieldBorderColor(emailField, Color.TRANSPARENT);
        setFieldBorderColor(dobField, Color.TRANSPARENT);
        setFieldBorderColor(zipCodeField, Color.TRANSPARENT);
        hideCheckmark(firstNameField);
        hideCheckmark(lastNameField);
        hideCheckmark(emailField);
        hideCheckmark(dobField);
        hideCheckmark(zipCodeField);
    }

    private boolean allFieldsAreValid() {
        return firstNameValid.get() && lastNameValid.get() && emailValid.get() && dobValid.get() && zipCodeValid.get();
    }

    private void showCheckmark(TextField field) {
        if (field == firstNameField) {
            firstNameCheck.setText("✓"); // Unicode checkmark
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
        }
    }

    public void createNewAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Info for the user. Message goes here");
        alert.showAndWait();
    }

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