package nz.ac.auckland.se206;

/** Represents the state of the game. */
public class GameState {

  private static int difficulty = 0;

  private static int time = 0;

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
}
