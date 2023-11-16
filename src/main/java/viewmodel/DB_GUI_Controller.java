package viewmodel;

import com.opencsv.exceptions.CsvValidationException;
import dao.DbConnectivityClass;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Person;
import service.MyLogger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML
    TextField first_name, last_name, department, major, email, imageURL;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private MenuItem editItem;
    @FXML
    private MenuItem deleteItem;
    @FXML
    private MenuItem addItem;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private Label statusLabel;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private final BooleanProperty isEditDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty isDeleteDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty isAddDisabled = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);
            tv.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection == null) {
                    isEditDisabled.set(true);
                    isDeleteDisabled.set(true);
                } else {
                    isEditDisabled.set(false);
                    isDeleteDisabled.set(false);
                }
            });

            tv.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    tv.getSelectionModel().clearSelection();
                }
            });

            isAddDisabled.bind(Bindings.createBooleanBinding(() ->
                            first_name.getText().isEmpty() ||
                                    last_name.getText().isEmpty() ||
                                    department.getText().isEmpty() ||
                                    majorComboBox.getValue() == null ||
                                    email.getText().isEmpty() ||
                                    imageURL.getText().isEmpty(),
                    first_name.textProperty(),
                    last_name.textProperty(),
                    department.textProperty(),
                    majorComboBox.valueProperty(),
                    email.textProperty(),
                    imageURL.textProperty())
            );

            editItem.disableProperty().bind(isEditDisabled);
            deleteItem.disableProperty().bind(isDeleteDisabled);
            addItem.disableProperty().bind(isAddDisabled);
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
            majorComboBox.getSelectionModel().selectFirst();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void addNewRecord() {
        if (validateForm()) {
            Person p = new Person(
                    null,  // You can set the ID to null, and it will be generated automatically
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorComboBox.getValue().getDisplayName(),
                    email.getText(),
                    imageURL.getText()
            );
            cnUtil.insertUser(p);
            // Retrieve ID after insertion
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();
            setStatusMessage("New Record Successfully Added");
        }
    }




    private boolean validateForm() {
        if (first_name.getText().isEmpty() || last_name.getText().isEmpty() ||
                department.getText().isEmpty() || majorComboBox.getValue() == null ||
                email.getText().isEmpty() || imageURL.getText().isEmpty()) {
            // Display an alert or handle validation error as needed
            showAlert("Please fill in all fields.");
            return false;
        }

        if (!first_name.getText().matches("^[A-Za-z]{2,25}$")) {
            showAlert("Invalid first name. It should contain 2 to 25 alphabetical characters.");
            return false;
        }

        if (!last_name.getText().matches("^[A-Za-z]{2,25}$")) {
            showAlert("Invalid last name. It should contain 2 to 25 alphabetical characters.");
            return false;
        }

        if (!email.getText().endsWith("@farmingdale.edu")) {
            showAlert("Invalid email. It should end with @farmingdale.edu.");
            return false;
        }

        // Additional validation checks if needed

        return true;
    }

    private void setStatusMessage(String message) {
        statusLabel.setText(message);
        HBox.setHgrow(statusLabel, Priority.ALWAYS); // Use the whole width
        HBox.setMargin(statusLabel, new Insets(0, 0, 0, 10)); // Adjust margin if needed

        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(5),
                event -> statusLabel.setText("")));

        timeline.play();
    }
    @FXML
    private void importCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                List<Person> importedData = new ArrayList<>();

                // Read CSV header (if present)
                String[] header = reader.readNext();

                // Assuming CSV format: ID,FirstName,LastName,Department,Major,Email
                if (header != null && header.length == 6) {
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        Person person = new Person(
                                null,  // You can set the ID to null, and it will be generated automatically
                                nextLine[1],
                                nextLine[2],
                                nextLine[3],
                                nextLine[4],
                                nextLine[5],
                                ""
                        );
                        importedData.add(person);
                    }

                    // Update TableView with imported data
                    data.clear();
                    data.addAll(importedData);
                    setStatusMessage("CSV Import Successful");
                } else {
                    showAlert("Invalid CSV file format. Please make sure the file has the correct header.");
                }

            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
                showAlert("Error importing CSV file.");
            } catch (CsvValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @FXML
    private void exportCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                // Write CSV header
                String[] header = {"ID", "FirstName", "LastName", "Department", "Major", "Email"};
                writer.writeNext(header);

                // Write data to CSV
                for (Person person : data) {
                    String[] rowData = {
                            String.valueOf(person.getId()),
                            person.getFirstName(),
                            person.getLastName(),
                            person.getDepartment(),
                            person.getMajor(),
                            person.getEmail()
                    };
                    writer.writeNext(rowData);
                }

                setStatusMessage("CSV Export Successful");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error exporting CSV file.");
            }
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Form Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorComboBox.setValue(null);
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            int index = data.indexOf(p);

            Person p2 = new Person(
                    p.getId(),
                    first_name.getText(),
                    last_name.getText(),
                    department.getText(),
                    majorComboBox.getValue().getDisplayName(),  // Use ComboBox instead of TextField
                    email.getText(),
                    imageURL.getText()
            );

            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
        }
    }


    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        major.setText(p.getMajor());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    public enum Major {
        CS("Computer Science"),
        CPIS("Computer Information Systems"),
        ENGLISH("English");

        private final String displayName;

        Major(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }



    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}