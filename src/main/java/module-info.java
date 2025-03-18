module com.tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires java.sql;  // Add this for database connectivity

    // Export packages
    exports com.tictactoe.client;
    exports com.tictactoe.game;
    exports com.tictactoe.server;
    exports com.tictactoe.ui;
    exports com.tictactoe.db;       // Add this for database access
    exports com.tictactoe.db.model; // Add this for database models

    // Open packages to FXML
    opens com.tictactoe.ui to javafx.fxml;
    opens com.tictactoe.client to javafx.fxml;
}