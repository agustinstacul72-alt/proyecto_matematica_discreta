module com.medset {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.medset;
    exports com.medset.ui to javafx.fxml;

    opens com.medset.controller to javafx.fxml;
}
