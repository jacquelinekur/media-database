module com.example.ibcsia {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens mediaDatabase to javafx.fxml;
    exports mediaDatabase;
}