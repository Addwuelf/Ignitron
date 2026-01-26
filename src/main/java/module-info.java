module org.example.ignitron {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.sun.jna.platform;
    requires javafx.swing;
    requires com.sun.jna;
    requires java.logging;

    opens org.example.ignitron to javafx.fxml;
    exports org.example.ignitron;
    exports org.example.ignitron.controllers;
    opens org.example.ignitron.controllers to javafx.fxml;
}