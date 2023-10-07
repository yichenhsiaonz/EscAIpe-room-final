package nz.ac.auckland.se206;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.PrintStream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.ControlRoomController;
import nz.ac.auckland.se206.controllers.GameOverController;
import nz.ac.auckland.se206.controllers.KitchenController;
import nz.ac.auckland.se206.controllers.LabController;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {

  private static Scene scene;
  public static ControlRoomController controlRoomController;
  public static LabController labController;
  public static KitchenController kitchenController;

  public static void main(final String[] args) {
    launch();
  }

  public static void setRoot(AppUi window) throws IOException {
    scene.setRoot(SceneManager.getUiRoot(window));
  }

  /**
   * Returns the node associated to the input file. The method expects that the file is located in
   * "src/main/resources/fxml".
   *
   * @param fxml The name of the FXML file (without extension).
   * @return The node of the input file.
   * @throws IOException If the file is not found.
   */
  public static Parent loadFxml(String fxml) throws IOException {
    try {
      return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml")).load();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void gameOver() {
    // get the scene root's node and fade it to transparent
    Timeline timeline = new Timeline();
    KeyFrame key =
        new KeyFrame(Duration.millis(2000), new KeyValue(scene.getRoot().opacityProperty(), 0));
    timeline.getKeyFrames().add(key);
    // at the end of the animation, set the root to the game over scene
    timeline.setOnFinished(
        e -> {
          try {
            setRoot(AppUi.GAMEOVER);
            // begin the game over scene's transition animation
            GameOverController.instance.showGameOver();
          } catch (IOException ex) {
            System.out.println("Error");
          }
        });
    timeline.play();
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "Canvas" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException, ApiProxyException {
    PrintStream ps = new PrintStream("error.txt");
    System.setErr(ps);
    // get the window size of the primary screen
    DisplayMode mode =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    // save the window size
    double width = mode.getWidth();
    double height = mode.getHeight();
    GameState.setWindowWidth((int) width);
    GameState.setWindowHeight((int) height);
    // check if the window is wider than 16:9
    if (height / 9 > width / 16) {
      // if so, set the width to 16:9 and calculate the height
      height = width / 1920 * 1080;
    } else {
      // otherwise, set the height to 16:9 and calculate the width
      width = height / 1080 * 1920;
    }
    // save the new window size
    GameState.setWidth((int) width);
    GameState.setHeight((int) height);

    // load the menu scene and the game over scene
    SceneManager.addUi(AppUi.MENU, loadFxml("menu"));
    SceneManager.addUi(AppUi.GAMEOVER, loadFxml("gameover"));

    // set the scene to the menu scene
    scene = new Scene(SceneManager.getUiRoot(AppUi.MENU));

    // add css file
    String cssFile = getClass().getResource("/css/styles.css").toExternalForm();
    scene.getStylesheets().add(cssFile);

    // load fonts
    Font.loadFont(getClass().getResource("/fonts/PressStart2P-Regular.ttf").toExternalForm(), 12);
    Font.loadFont(getClass().getResource("/fonts/SpaceGrotesk-Regular.ttf").toExternalForm(), 12);

    // set the background color to black
    scene.setFill(Color.BLACK);
    // place the scene on the stage
    stage.setScene(scene);
    // set the stage to full screen
    stage.setFullScreen(true);
    // disable the full screen exit key
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    stage.show();
    SceneManager.getUiRoot(AppUi.MENU).requestFocus();
    // fully close the application when the window is closed
    stage.onCloseRequestProperty().setValue(e -> System.exit(0));
  }
}
