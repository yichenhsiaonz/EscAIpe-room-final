package nz.ac.auckland.se206;

import javafx.concurrent.Task;
import nz.ac.auckland.se206.speech.TextToSpeech;

public class TextToSpeechManager {
  private static TextToSpeech textToSpeech = new TextToSpeech();
  private static Task<Void> task;

  public static void speak(String... sentences) {
    cutOff();
    // Run in a separate thread to avoid blocking the UI thread
    task =
        new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException {
            textToSpeech.speak(sentences);
            return null;
          }
        };
    Thread thread = new Thread(task);
    thread.start();
  }

  public static void cutOff() {
    if (task != null) {
      task.setOnSucceeded(null);
    }
    textToSpeech.stop();
  }

  public static void setCompletedRunnable(Runnable runnable) {
    task.setOnSucceeded(e -> runnable.run());
  }
}
