module com.example.prueba {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens com.example.prueba to javafx.fxml;
    exports com.example.prueba;
}