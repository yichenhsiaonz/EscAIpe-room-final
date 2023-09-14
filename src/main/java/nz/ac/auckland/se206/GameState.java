package nz.ac.auckland.se206;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Represents the state of the game. */
public class GameState {

  private static int chosenDifficulty = 0;
  private static int chosenTime = 0;
  private static int windowWidth = 1920;
  private static int windowHeight = 1080;
  private static int width = 1920;
  private static int height = 1080;

  private static StringProperty chatText = new SimpleStringProperty("");
  private static ChatCompletionRequest chatCompletionRequest;

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

  public static int hints = 0;

  /** Indicates whether the riddle has been resolved. */
  public static boolean isRiddleResolved = false;

  /** Indicates whether the bread has been toasted. */
  public static boolean isBreadToast = false;

  /** Indicates whether the paper has been printed. */
  public static boolean isPaperPrinted = false;

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

  public static void setChatCompletionRequest(ChatCompletionRequest request) {
    GameState.chatCompletionRequest = request;
  }

  public static StringProperty chatTextProperty() {
    return chatText;
  }

  public static String getChatText() {
    return chatText.get();
  }

  public static void setChatText(String text) {
    chatText.set(text);
  }

  // update the chat message in the UI
  public static void appendChatMessage(ChatMessage msg) {
    String updateText = chatText.get() + msg.getRole() + ": " + msg.getContent() + "\n\n";
    setChatText(updateText);
  }

  // get gpt response
  public static ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());

              // update UI when thread is done
              Platform.runLater(
                  () -> {
                    appendChatMessage(result.getChatMessage());
                  });

              return result.getChatMessage();
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
          }
        };

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Chat Thread");
    gptThread.start();

    return gptTask.getValue();
  }
}
