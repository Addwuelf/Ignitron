module org.example.ignitron {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens org.example.ignitron to javafx.fxml;
    exports org.example.ignitron;
}