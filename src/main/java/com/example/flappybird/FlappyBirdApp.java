//package com.example.flappybird;
//
//import com.example.flappybird.controller.GameController;
//import com.example.flappybird.view.GameView;
//import com.example.flappybird.model.GameModel;
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.input.KeyCode;
//import javafx.stage.Stage;
//
//public class FlappyBirdApp extends Application {
//
//    public static final int WIDTH  = 288;
//    public static final int HEIGHT = 512;
//
//    @Override
//    public void start(Stage stage) {
//        GameModel     model      = new GameModel();
//        GameView      view       = new GameView(WIDTH, HEIGHT);
//        GameController controller = new GameController(model, view, stage);
//
//        Scene scene = new Scene(view.getRoot(), WIDTH, HEIGHT);
//
//        // Space / Up / Mouse → flap
//        scene.setOnKeyPressed(e -> {
//            if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.UP) {
//                controller.handleInput();
//            }
//        });
//        scene.setOnMousePressed(e -> controller.handleInput());
//
//        stage.setTitle("Flappy Bird");
//        stage.setScene(scene);
//        stage.setResizable(false);
//        stage.show();
//
//        controller.start();
//    }
//}

package com.example.flappybird;

import com.example.flappybird.controller.GameController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX Application entry point.
 *
 * Bootstraps the MVC triad:
 *   Model  → GameModel
 *   View   → GameView + GameRenderer
 *   Controller → GameController (owns the AnimationTimer game-loop)
 */
public class FlappyBirdApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Controller wires everything together
        new GameController(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
