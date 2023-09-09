package nz.ac.auckland.se206;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nz.ac.auckland.se206.SceneManager.AppUi;

/**
 * This is the entry point of the JavaFX application, while you can change this class, it should
 * remain as the class that runs the JavaFX application.
 */
public class App extends Application {

  private static Scene scene;

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
  public void start(final Stage stage) throws IOException {
    SceneManager.addUi(AppUi.MENU, loadFxml("menu"));
    scene = new Scene(SceneManager.getUiRoot(AppUi.MENU), 600, 650);
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
    SceneManager.getUiRoot(AppUi.MENU).requestFocus();
    // TODO TEMP REMOVE LATER
    SceneManager.addUi(AppUi.CONTROL_ROOM, loadFxml("controlRoom"));
    SceneManager.addUi(AppUi.COMPUTER, loadFxml("computer"));
    SceneManager.addUi(AppUi.KEYPAD, loadFxml("keypad"));

    SceneManager.addUi(AppUi.CHAT, loadFxml("chat"));
    SceneManager.addUi(AppUi.ROOM, loadFxml("room"));
  }
}
