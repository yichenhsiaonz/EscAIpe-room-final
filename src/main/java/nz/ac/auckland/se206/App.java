package nz.ac.auckland.se206;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.controllers.ControlRoomController;
import nz.ac.auckland.se206.controllers.KitchenController;
import nz.ac.auckland.se206.controllers.LabController;
import nz.ac.auckland.se206.controllers.SharedElements;
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

  /**
   * This method is invoked when the application starts. It loads and shows the "Canvas" scene.
   *
   * @param stage The primary stage of the application.
   * @throws IOException If "src/main/resources/fxml/canvas.fxml" is not found.
   */
  @Override
  public void start(final Stage stage) throws IOException, ApiProxyException {
    GameState.newGame();
    SharedElements.newGame();
    DisplayMode mode =
        GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    double width = mode.getWidth();
    double height = mode.getHeight();
    GameState.setWindowWidth((int) width);
    GameState.setWindowHeight((int) height);
    if (height / 9 > width / 16) {
      height = width / 1920 * 1080;
    } else {
      width = height / 1080 * 1920;
    }
    GameState.setWidth((int) width);
    GameState.setHeight((int) height);
    SceneManager.addUi(AppUi.MENU, loadFxml("menu"));
    scene = new Scene(SceneManager.getUiRoot(AppUi.MENU));
    stage.setScene(scene);
    stage.setFullScreen(true);
    stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
    stage.show();
    SceneManager.getUiRoot(AppUi.MENU).requestFocus();

    stage.onCloseRequestProperty().setValue(e -> System.exit(0));

    // get controller for control room
    FXMLLoader controlLoader = new FXMLLoader(getClass().getResource("/fxml/controlRoom.fxml"));
    Parent controlRoom = controlLoader.load();
    controlRoomController = controlLoader.getController();

    // get controller for lab
    FXMLLoader labLoader = new FXMLLoader(getClass().getResource("/fxml/lab.fxml"));
    Parent lab = labLoader.load();
    labController = labLoader.getController();

    // get controller for kitchen
    FXMLLoader kitchenLoader = new FXMLLoader(getClass().getResource("/fxml/kitchen.fxml"));
    Parent kitchen = kitchenLoader.load();
    kitchenController = kitchenLoader.getController();

    // TODO TEMP REMOVE LATER
    SceneManager.addUi(AppUi.ENDING, loadFxml("ending"));
    SceneManager.addUi(AppUi.KITCHEN, kitchen);
    SceneManager.addUi(AppUi.CONTROL_ROOM, controlRoom);
    SceneManager.addUi(AppUi.COMPUTER, loadFxml("computer"));
    SceneManager.addUi(AppUi.KEYPAD, loadFxml("keypad"));
    SceneManager.addUi(AppUi.LAB, lab);
  }
}
