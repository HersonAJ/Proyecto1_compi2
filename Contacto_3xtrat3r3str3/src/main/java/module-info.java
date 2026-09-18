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
    requires org.antlr.antlr4.runtime;

    opens com.example.contacto_3xtrat3r3str3.ui to javafx.graphics, javafx.fxml;

    exports com.example.contacto_3xtrat3r3str3.y.ast;
    exports com.example.contacto_3xtrat3r3str3.y.semantica;
    exports com.example.contacto_3xtrat3r3str3.y.semantica.error;
}