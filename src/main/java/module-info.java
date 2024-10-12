module com.example.paint24 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens com.example.paint24 to javafx.fxml;
    exports com.example.paint24;
}