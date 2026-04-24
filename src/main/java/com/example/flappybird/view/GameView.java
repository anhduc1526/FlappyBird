package com.example.flappybird.view;

import com.example.flappybird.util.GameConstants;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * VIEW — Sets up the JavaFX Stage, Scene, and Canvas.
 *
 * Provides the GraphicsContext to the renderer and exposes the Scene
 * for event-listener attachment by the Controller.
 *
 * Design Pattern: Passive View — owns no state, just wires the window.
 */
public class GameView {

    private final Canvas          canvas;
    private final Scene           scene;
    private final GameRenderer    renderer;

    public GameView(Stage stage) {
        canvas = new Canvas(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        Pane root = new Pane(canvas);
        scene = new Scene(root, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);

        stage.setTitle(GameConstants.WINDOW_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        renderer = new GameRenderer(canvas.getGraphicsContext2D());
    }

    public Scene        getScene()    { return scene; }
    public Canvas       getCanvas()   { return canvas; }
    public GameRenderer getRenderer() { return renderer; }
    public GraphicsContext getGC()    { return canvas.getGraphicsContext2D(); }
}
