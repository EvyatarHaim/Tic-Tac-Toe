module com.tictactoe {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    // Export packages
    exports com.tictactoe.client;
    exports com.tictactoe.game;
    exports com.tictactoe.server;
    exports com.tictactoe.ui;

    // Open packages to FXML
    opens com.tictactoe.ui to javafx.fxml;
    opens com.tictactoe.client to javafx.fxml;
}