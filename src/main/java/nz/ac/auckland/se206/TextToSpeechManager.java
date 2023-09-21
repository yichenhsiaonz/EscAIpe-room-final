package nz.ac.auckland.se206;

import javafx.concurrent.Task;
import nz.ac.auckland.se206.speech.TextToSpeech;

public class TextToSpeechManager {
  private static TextToSpeech textToSpeech = new TextToSpeech();

  public static void speak(String... sentences) {
    // Run in a separate thread to avoid blocking the UI thread
    Task<Void> task =
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
    textToSpeech.stop();
  }
}
