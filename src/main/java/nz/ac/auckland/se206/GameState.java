package nz.ac.auckland.se206;

/** Represents the state of the game. */
public class GameState {

  private static int difficulty = 0;
  private static int time = 0;
  private static int windowWidth = 1920;
  private static int windowHeight = 1080;
  private static int width = 1920;
  private static int height = 1080;

  // TODO SET RIDDLES ACCORDING TO DIFFICULTY

  /** Indicates whether the riddle has been resolved. */
  public static boolean isRiddleResolved = false;

  /** Indicates whether the key has been found. */
  public static boolean isKeyFound = false;

  public static void setDifficulty(int difficulty) {
    GameState.difficulty = difficulty;
  }

  public static int getDifficulty() {
    return difficulty;
  }

  public static void setTime(int time) {
    GameState.time = time;
  }

  public static int getTime() {
    return time;
  }

  public static int getWidth() {
    return width;
  }

  public static int getHeight() {
    return height;
  }

  public static void setWidth(int width) {
    GameState.width = width;
  }

  public static void setHeight(int height) {
    GameState.height = height;
  }

  public static int getWindowWidth() {
    return windowWidth;
  }

  public static int getWindowHeight() {
    return windowHeight;
  }

  public static void setWindowWidth(int width) {
    GameState.windowWidth = width;
  }

  public static void setWindowHeight(int height) {
    GameState.windowHeight = height;
  }
}
