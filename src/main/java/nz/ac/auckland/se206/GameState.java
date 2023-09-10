package nz.ac.auckland.se206;

/** Represents the state of the game. */
public class GameState {

  private static int chosenDifficulty = 0;
  private static int chosenTime = 0;
  private static int windowWidth = 1920;
  private static int windowHeight = 1080;
  private static int width = 1920;
  private static int height = 1080;

  // create timer task to run in background persistently
  public static javafx.concurrent.Task<Void> timerTask =
      new javafx.concurrent.Task<>() {
        @Override
        protected Void call() throws Exception {

          // set time to chosen
          Integer timer = chosenTime;
          Integer maxTime = chosenTime;
          updateMessage(chosenTime / 60 + ":00");
          updateProgress(timer, maxTime);

          // add && to this while loop to end timer early for anything

          while (timer != 0) {
            Thread.sleep(1000);
            timer--;
            int minutes = timer / 60;
            int seconds = timer % 60;
            updateMessage(minutes + ":" + String.format("%02d", seconds));
            updateProgress(timer, maxTime);
          }

          // add code here if you want something to happen when the timer ends
          // this is a background thread so use Platform.runLater() for anything that happens in the
          // UI

          return null;
        }
      };

  // TODO SET RIDDLES ACCORDING TO DIFFICULTY

  /** Indicates whether the riddle has been resolved. */
  public static boolean isRiddleResolved = false;

  /** Indicates whether the key has been found. */
  public static boolean isKeyFound = false;

  public static void setDifficulty(int difficulty) {
    GameState.chosenDifficulty = difficulty;
  }

  public static int getDifficulty() {
    return chosenDifficulty;
  }

  public static void setTime(int time) {
    GameState.chosenTime = time;
  }

  public static int getTime() {
    return chosenTime;
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
