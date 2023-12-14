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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import model.Student;
import service.MyLogger;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML
    TextField first_name, last_name, major, email, year;
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
    private TableView<Student> tv;
    @FXML
    private TableColumn<Student, Integer> tv_id;
    @FXML
    private TableColumn<Student, String> tv_fn, tv_ln, tv_year, tv_major, tv_email;
    @FXML
    private ComboBox<Major> majorComboBox;
    @FXML
    private Label statusLabel;
    @FXML
    private MenuItem exportPdfItem;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Student> data = cnUtil.getData();

    private final BooleanProperty isEditDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty isDeleteDisabled = new SimpleBooleanProperty(true);
    private final BooleanProperty isAddDisabled = new SimpleBooleanProperty(true);

    @Override
    public void initialize(URL ul, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_year.setCellValueFactory(new PropertyValueFactory<>("year"));
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
                                    majorComboBox.getValue() == null ||
                                    email.getText().isEmpty() ||
                                    year.getText().isEmpty(),
                    first_name.textProperty(),
                    last_name.textProperty(),
                    majorComboBox.valueProperty(),
                    email.textProperty(),
                    year.textProperty())
            );

            editItem.disableProperty().bind(isEditDisabled);
            deleteItem.disableProperty().bind(isDeleteDisabled);
            addItem.disableProperty().bind(isAddDisabled);
            majorComboBox.setItems(FXCollections.observableArrayList(Major.values()));
            majorComboBox.getSelectionModel().selectFirst();

            tv.setRowFactory(tv -> {
                TableRow<Student> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 1 && !row.isEmpty()) {
                        addNewRowOnClick(row);
                    }
                });
                return row;
            });

            enableEditingWithinRow();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void addNewRecord() {
        if (validateForm()) {
            Student p = new Student(
                    null,  // You can set the ID to null, and it will be generated automatically
                    first_name.getText(),
                    last_name.getText(),
                    majorComboBox.getValue().getDisplayName(),
                    email.getText(),
                    year.getText()
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
                majorComboBox.getValue() == null ||
                email.getText().isEmpty() || year.getText().isEmpty()) {
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

    private void addNewRowOnClick(TableRow<Student> row) {
        if (row.isEmpty()) {
            // The row is empty, add a new record
            clearForm(); // Clear the form before adding a new record
            isEditDisabled.set(true);
            isDeleteDisabled.set(true);
        } else {
            // The row is not empty, populate the fields with the selected row's data
            Student selectedStudent = row.getItem();
            first_name.setText(selectedStudent.getFirstName());
            last_name.setText(selectedStudent.getLastName());
            year.setText(selectedStudent.getYear());
            email.setText(selectedStudent.getEmail());

            isEditDisabled.set(false);
            isDeleteDisabled.set(false);
        }
    }

    private void enableEditingWithinRow() {
        tv.setEditable(true);

        tv_id.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        tv_fn.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_fn.setOnEditCommit(this::updateCell);

        tv_ln.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_ln.setOnEditCommit(this::updateCell);

        tv_year.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_year.setOnEditCommit(this::updateCell);

        tv_email.setCellFactory(TextFieldTableCell.forTableColumn());
        tv_email.setOnEditCommit(this::updateCell);
    }

    private void updateCell(TableColumn.CellEditEvent<Student, String> event) {
        Student student = tv.getSelectionModel().getSelectedItem();
        if (student != null) {
            // Update the corresponding property based on the column
            if (event.getTableColumn() == tv_fn) {
                student.setFirstName(event.getNewValue());
            } else if (event.getTableColumn() == tv_ln) {
                student.setLastName(event.getNewValue());
            }  else if (event.getTableColumn() == tv_major) {
                student.setMajor(event.getNewValue());
            } else if (event.getTableColumn() == tv_email) {
                student.setEmail(event.getNewValue());
            }

            // Save changes to the database
            cnUtil.editUser(student.getId(), student);

            // You may need to adjust your data model or update the database accordingly
            // For simplicity, assume the Person class has appropriate setters
            // and update the corresponding property based on the edited column
        }
    }


    @FXML
    private void importCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                List<Student> importedData = new ArrayList<>();

                // Read CSV header (if present)
                String[] header = reader.readNext();

                // Assuming CSV format: ID,FirstName,LastName,Year,Major,Email
                if (header != null && header.length == 6) {
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        Student student = new Student(
                                null,  // You can set the ID to null, and it will be generated automatically
                                nextLine[1],
                                nextLine[2],
                                nextLine[3],
                                nextLine[4],
                                nextLine[5]);
                        importedData.add(student);
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
                String[] header = {"ID", "FirstName", "LastName", "Year", "Major", "Email"};
                writer.writeNext(header);

                // Write data to CSV
                for (Student student : data) {
                    String[] rowData = {
                            String.valueOf(student.getId()),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getMajor(),
                            student.getEmail()
                    };
                    writer.writeNext(rowData);
                }

                setStatusMessage("CSV Export Successful");

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error exporting CSV file.");
            }
        }
    }@FXML
    private void exportPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                generatePdf(file);
                setStatusMessage("PDF Export Successful");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error exporting PDF file.");
            }
        }
    }


    private void generatePdf(File file) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Course: " + DbConnectivityClass.getDbName()); // Display course name
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Student List for " + DbConnectivityClass.getDbName());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("Total Students: " + data.size());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("--------------------------------------------------");


            // Define column headers
            String[] headers = {"ID", "FirstName", "LastName", "Year", "Major", "Email"};
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            addRowToPDF(contentStream, headers);

            // Reset font for student data
            contentStream.setFont(PDType1Font.HELVETICA, 10);

            // Add student data
            for (Student student : data) {
                String[] studentData = {
                        String.valueOf(student.getId()),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getYear(),
                        student.getMajor(),
                        student.getEmail()
                };
                addRowToPDF(contentStream, studentData);
            }

            contentStream.endText();
        }

        document.save(file);
        document.close();
    }

    private void addRowToPDF(PDPageContentStream contentStream, String[] data) throws IOException {
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText(String.join(" | ", data));
    }



    private Map<String, Integer> countStudentsByMajor() {
        Map<String, Integer> majorCounts = new HashMap<>();

        for (Student student : data) {
            String major = student.getMajor();
            majorCounts.put(major, majorCounts.getOrDefault(major, 0) + 1);
        }

        return majorCounts;
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
        majorComboBox.setValue(null);
        email.setText("");
        year.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        DbConnectivityClass.setDbName(null);
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
    protected void editRecord() {
        Student p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            int index = data.indexOf(p);

            Student p2 = new Student(
                    p.getId(),
                    first_name.getText(),
                    last_name.getText(),
                    majorComboBox.getValue().getDisplayName(),  // Use ComboBox instead of TextField
                    email.getText(),
                    year.getText()
            );

            cnUtil.editUser(p.getId(), p2);
            data.remove(p);
            data.add(index, p2);
            tv.getSelectionModel().select(index);
        }
    }




    @FXML
    protected void deleteRecord() {
        Student p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        if(index<0){return;}
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
        Student p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        major.setText(p.getMajor());
        email.setText(p.getEmail());
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
        ENGLISH("English"),
        BIO("Biology"),
        CHEM("Chemistry"),
        PSYCH("Psychology"),
        MATH("Mathematics"),
        PHYS("Physics"),
        ECON("Economics"),
        POLISCI("Political Science"),
        HIST("History"),
        SOC("Sociology"),
        PHIL("Philosophy"),
        MUSIC("Music"),
        ART("Art"),
        ENG("Engineering"),
        NURS("Nursing"),
        EDU("Education"),
        BUS("Business"),
        COMM("Communications"),
        ENVSCI("Environmental Science"),
        ANTHRO("Anthropology"),
        THEATRE("Theatre"),
        FILM("Film Studies"),
        LANG("Languages"),
        ARCH("Architecture"),
        STAT("Statistics"),
        PHARM("Pharmacy"),
        KINE("Kinesiology"),
        ACCT("Accounting");

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