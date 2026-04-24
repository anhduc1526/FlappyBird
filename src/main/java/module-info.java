module com.example.flappybird {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example.flappybird to javafx.fxml;
    exports com.example.flappybird;
}