module com.example.contacto_3xtrat3r3str3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.example.contacto_3xtrat3r3str3 to javafx.fxml;
    exports com.example.contacto_3xtrat3r3str3;
}