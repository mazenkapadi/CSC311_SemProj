module com.example.csc311_db_ui_semesterlongproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires com.opencsv;
    requires org.apache.pdfbox;


    opens viewmodel;
    exports viewmodel;
    opens dao;
    exports dao;
    opens model;
    exports model;
}