package nz.ac.auckland.se206;

import java.util.HashMap;
import java.util.Random;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.SharedElements;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Represents the state of the game. */
public class GameState {

  public enum Items {
    BREAD_TOASTED,
    BREAD_UNTOASTED,
    PAPER,
    USB
  }

  private static GameState instance;

  private GameState() {
    Random rng = new Random();
    firstDigits = rng.nextInt(100);
    secondDigits = rng.nextInt(100);
    thirdDigits = rng.nextInt(100);
    System.out.println(firstDigits);
    System.out.println(secondDigits);
    System.out.println(thirdDigits);
  }

  public static void newGame() {
    instance = new GameState();
  }

  public static GameState getInstance() {
    return instance;
  }

  private static HashMap<Items, ImageView[]> inventoryMap = new HashMap<Items, ImageView[]>();

  private static void inventoryMapAdd(Items item, ImageView[] itemImageView) {
    inventoryMap.put(item, itemImageView);
  }

  @FXML protected HBox inventoryHBox;
  @FXML protected TextArea chatBox;
  @FXML protected TextField messageBox;
  @FXML protected Button sendMessage;
  @FXML private ProgressBar timerProgressBar;
  @FXML public Label timerLabel;

  private static int chosenDifficulty = 0;
  private static int chosenTime = 0;
  private static int windowWidth = 1920;
  private static int windowHeight = 1080;
  private static int width = 1920;
  private static int height = 1080;

  private static ChatCompletionRequest chatCompletionRequest;
  private static int firstDigits;
  private static int secondDigits;
  private static int thirdDigits;

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

  public static void setDifficulty(int difficulty) {
    GameState.chosenDifficulty = difficulty;
  }

  public static int getDifficulty() {
    return chosenDifficulty;
  }

  public static void setTime(int time) {
    GameState.chosenTime = time;
  }

  public static void setWidth(int width) {
    GameState.width = width;
  }

  public static void setHeight(int height) {
    GameState.height = height;
  }

  public static void setWindowWidth(int width) {
    GameState.windowWidth = width;
  }

  public static void setWindowHeight(int height) {
    GameState.windowHeight = height;
  }

  public static void setChatCompletionRequest(ChatCompletionRequest chatCompletionRequest) {
    GameState.chatCompletionRequest = chatCompletionRequest;
  }

  public static String getSecondDigits() {
    return String.valueOf(secondDigits);
  }

  // get gpt response
  public static ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    App.controlRoomController.getLoadingAi().setVisible(true);
    App.labController.getLoadingAi().setVisible(true);
    App.kitchenController.getLoadingAi().setVisible(true);
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
                    SharedElements.appendChat("AI: " + result.getChatMessage().getContent());
                  });
              return result.getChatMessage();

            } catch (Exception e) {
              System.out.println("API error");
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        event -> {
          App.controlRoomController.getLoadingAi().setVisible(false);
          App.labController.getLoadingAi().setVisible(false);
          App.kitchenController.getLoadingAi().setVisible(false);
        });

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Chat Thread");
    gptThread.start();

    return gptTask.getValue();
  }

  public static void scaleToScreen(AnchorPane contentPane) {

    double scale = (double) windowWidth / 1920;
    contentPane.setScaleX(scale);
    contentPane.setScaleY(scale);
    int verticalMargin = (height - 1080) / 2 + (windowHeight - height) / 2;
    int horizontalMargin = (width - 1920) / 2 + (windowWidth - width) / 2;

    System.out.println(verticalMargin);
    System.out.println(horizontalMargin);
    BorderPane.setMargin(
        contentPane,
        new javafx.geometry.Insets(
            verticalMargin, horizontalMargin, verticalMargin, horizontalMargin));
    System.out.println(scale);
  }

  // this method runs a translate transition to move the character to a new position
  // and returns the duration of the animation in seconds.
  // use the delayRun method to run code after the animation is complete.
  public static double goTo(
      double destinationX, double destinationY, ImageView character, ImageView running) {

    // Retrieve the character's width and height using fitWidth and fitHeight
    double characterWidth = character.getFitWidth();
    double characterHeight = character.getFitHeight();

    // Calculate the character's new position relative to the room
    double characterX = destinationX - characterWidth / 2; // Adjust for character's width
    double characterY = destinationY - characterHeight; // Adjust for character's height

    // Calculate the distance the character needs to move
    double distanceToMove =
        Math.sqrt(
            Math.pow(characterX - character.getTranslateX(), 2)
                + Math.pow(characterY - character.getTranslateY(), 2));

    // Define a constant speed
    double constantSpeed = 300;

    // Calculate the duration based on constant speed and distance
    double durationSeconds = distanceToMove / constantSpeed;

    // Create a TranslateTransition to smoothly move the character
    TranslateTransition transition =
        new TranslateTransition(Duration.seconds(durationSeconds), character);
    transition.setToX(characterX);
    transition.setToY(characterY);

    // Play the animation
    transition.play();

    // Create a TranslateTransition to smoothly move the "running" element
    TranslateTransition transition2 =
        new TranslateTransition(Duration.seconds(durationSeconds), running);
    transition2.setToX(characterX);
    transition2.setToY(characterY);

    // flip the character and running gif if needed
    if (characterX > character.getTranslateX()) {
      running.setScaleX(1);
      character.setScaleX(1);
    } else {
      running.setScaleX(-1);
      character.setScaleX(-1);
    }

    running.setOpacity(1);
    // Play the animation
    transition2.play();

    transition2.setOnFinished(
        e -> {
          // Remove the "running" element from the pane when the animation is done
          running.setOpacity(0);
        });
    return durationSeconds;
  }

  // This method is used to move the character instantly to a new position without changing
  // direction.
  // call this method the same way as goTo.
  public static void goToInstant(
      double destinationX, double destinationY, ImageView character, ImageView running) {
    // Retrieve the character's width and height using fitWidth and fitHeight
    double characterWidth = character.getFitWidth();
    double characterHeight = character.getFitHeight();

    // Calculate the character's new position relative to the room
    double characterX = destinationX - characterWidth / 2; // Adjust for character's width
    double characterY = destinationY - characterHeight; // Adjust for character's height

    // Create a TranslateTransition to smoothly move the character
    character.setTranslateX(characterX);
    character.setTranslateY(characterY);
    running.setTranslateX(characterX);
    running.setTranslateY(characterY);
  }

  // put code you want to run after delay in a Runnable and pass it to this method along with the
  // delay
  // in seconds.
  public static void delayRun(Runnable runnable, double delay) {
    Thread thread =
        new Thread(
            () -> {
              try {
                Thread.sleep((int) (delay * 1000));
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              Platform.runLater(runnable);
            });
    thread.start();
  }

  public static void addItem(Items item) {
    String itemToAdd;
    EventHandler<MouseEvent> clickBehaviour;
    switch (item) {
      case BREAD_TOASTED:
        itemToAdd = "/images/Items/breadToasted.png";
        clickBehaviour =
            (event) -> {
              System.out.println("bread toasted clicked");
              SharedElements.appendChat(
                  "it's hard to read, but the numbers "
                      + thirdDigits
                      + " are burnt into the toast");
            };
        break;
      case BREAD_UNTOASTED:
        itemToAdd = "/images/Items/breadUntoasted.png";
        clickBehaviour =
            (event) -> {
              System.out.println("bread untoasted clicked");
              SharedElements.appendChat("It looks rather unappetising");
            };
        break;
      case PAPER:
        itemToAdd = "/images/Items/paper.png";
        clickBehaviour =
            (event) -> {
              System.out.println("paper clicked");
              SharedElements.appendChat(
                  "Within the blocks of text, you can make out the numbers "
                      + firstDigits
                      + " in bold");
            };
        break;
      case USB:
        itemToAdd = "/images/Items/usb.png";
        clickBehaviour =
            (event) -> {
              System.out.println("usb clicked");
              SharedElements.appendChat(
                  "You're reminded of the times they told you not to stick random USBs into your"
                      + " computer");
            };
        break;
      default:
        itemToAdd = null;
        clickBehaviour = null;
        break;
    }

    try {
      ImageView[] itemCopies = new ImageView[3];
      for (int i = 0; i < 3; i++) {
        ImageView itemImage = new ImageView(itemToAdd);
        itemImage.setOnMouseClicked(clickBehaviour);
        itemCopies[i] = itemImage;
      }
      SharedElements.addInventoryItem(itemCopies);
      inventoryMapAdd(item, itemCopies);

    } catch (Exception e) {
      System.out.println("Error: item not found");
      System.out.println(e);
    }
  }

  public static void removeItem(Items item) {
    try {
      SharedElements.removeInventoryItem(inventoryMap.get(item));
    } catch (Exception e) {
      System.out.println("Error: item not found");
      System.out.println(e);
    }
  }

  public static void onMessageSent() throws ApiProxyException {
    TextField messageBox = SharedElements.getMessageBox();
    String message = messageBox.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    SharedElements.appendChat("You: " + message + "\n");
    messageBox.clear();
    ChatMessage msg = new ChatMessage("user", message);
    GameState.runGpt(msg);
  }
}
