package nz.ac.auckland.se206;

import java.util.HashMap;
import javafx.scene.Parent;

/** Manages the different scenes in the game. */
public class SceneManager {

  /** Lists all the scenes in an enum for convenience. */
  public enum AppUi {
    MENU,
    CONTROL_ROOM,
    ENDING,
    KITCHEN,
    LAB,
    GAMEOVER
  }

  private static HashMap<AppUi, Parent> sceneMap = new HashMap<AppUi, Parent>();

  /**
   * Adds a scene to the scene map.
   *
   * @param appUi the scene to add
   * @param uiRoot the root of the scene
   */
  public static void addUi(AppUi appUi, Parent uiRoot) {
    sceneMap.put(appUi, uiRoot);
  }

  /**
   * Gets the root of the scene.
   *
   * @param appUi the scene to get
   * @return the root of the scene
   */
  public static Parent getUiRoot(AppUi appUi) {
    return sceneMap.get(appUi);
  }
}
