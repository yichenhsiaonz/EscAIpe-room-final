package nz.ac.auckland.se206;

import javafx.concurrent.Task;
import nz.ac.auckland.se206.controllers.SharedElements;
import nz.ac.auckland.se206.speech.TextToSpeech;

/** This class manages the text to speech that plays for the user. */
public class TextToSpeechManager {
  private static TextToSpeech textToSpeech = new TextToSpeech();
  private static Task<Void> task;
  public static boolean hideOnComplete = false;

  /**
   * Speaks the given sentences, and hides the chat bubble when complete.
   *
   * @param sentences sentences to speak
   */
  public static void speak(String... sentences) {
    hideOnComplete = false;
    cutOff();
    hideOnComplete = true;
    // Run in a separate thread to avoid blocking the UI thread
    task =
        new Task<Void>() {
          @Override
          protected Void call() throws InterruptedException {
            textToSpeech.speak(sentences);
            if (hideOnComplete) {
              SharedElements.hideChatBubble();
            }
            return null;
          }
        };
    Thread thread = new Thread(task);
    thread.start();
  }

  public static void cutOff() {
    textToSpeech.stop();
  }

  public static void setCompletedRunnable(Runnable runnable) {
    task.setOnSucceeded(e -> runnable.run());
  }
}
