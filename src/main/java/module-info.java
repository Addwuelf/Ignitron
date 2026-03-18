module org.example.ignitron {
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.sun.jna.platform;
    requires javafx.swing;
    requires com.sun.jna;
    requires com.google.gson;

    opens org.example.ignitron;
    opens org.example.ignitron.controllers to javafx.fxml;

    exports org.example.ignitron;
    exports org.example.ignitron.controllers;
}