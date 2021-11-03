module com.example.zookeeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires java.desktop;
    requires javafx.swing;
    requires opencv;
    requires org.bytedeco.javacpp;
    requires jnativehook;

    opens com.example.zookeeper to javafx.fxml;
    exports com.example.zookeeper;
}