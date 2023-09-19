package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
  private static int windowWidth = 1920;
  private static int windowHeight = 1080;
  public static boolean hasBread = false;
  public static boolean hasToast = false;
  public static boolean toasterPuzzleHints = true;
  public static boolean paperPuzzleHints = true;
  public static boolean computerPuzzleHints = true;

  private GameState() {
    Random rng = new Random();
    List<String> answerList =
        List.of("Planet", "Earth", "Galaxy", "Universe", "Space", "Sun", "Star");
    riddleAnswer = answerList.get(rng.nextInt(answerList.size()));
    System.out.println(riddleAnswer);
    firstDigits = String.format("%02d", rng.nextInt(100));
    secondDigits = String.format("%02d", rng.nextInt(100));
    thirdDigits = String.format("%02d", rng.nextInt(100));
    System.out.println(firstDigits);
    System.out.println(secondDigits);
    System.out.println(thirdDigits);
  }

  public static void newGame() {
    hasBread = false;
    hasToast = false;
    toasterPuzzleHints = true;
    paperPuzzleHints = true;
    computerPuzzleHints = true;
    instance = new GameState();
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

  private String riddleAnswer;
  private String riddle;
  private int hints;
  private int riddleHints = 0;
  private int currentPuzzle = 0;
  private int chosenDifficulty = 0;
  private int chosenTime = 0;
  private int width = 1920;
  private int height = 1080;
  private Thread timerThread;
  private ArrayList<Thread> runningThreads = new ArrayList<Thread>();

  private ChatCompletionRequest chatBoxChatCompletionRequest =
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
  private ChatCompletionRequest riddleHintChatCompletionRequest =
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
  private String firstDigits;
  private String secondDigits;
  private String thirdDigits;

  // create timer task to run in background persistently
  public javafx.concurrent.Task<Void> timerTask =
      new javafx.concurrent.Task<>() {
        @Override
        protected Void call() throws InterruptedException, IOException {
          try {
            // set time to chosen
            Integer timer = instance.chosenTime;
            Integer maxTime = instance.chosenTime;
            updateMessage(instance.chosenTime / 60 + ":00");
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
            // this is a background thread so use Platform.runLater() for anything that happens in
            // the
            // UI
            Runnable menuTask =
                () -> {
                  System.out.println("Timer ended");
                  stopAllThreads();
                  App.gameOver();
                };

            Platform.runLater(menuTask);
            return null;
          } catch (InterruptedException e) {
            System.out.println("Timer stopped");
            return null;
          }
        }
      };

  public static void startTimer() {
    instance.timerThread = new Thread(instance.timerTask);
    instance.timerThread.start();
  }

  public static Task<Void> getTimer() {
    return instance.timerTask;
  }

  public static void stopTimer() {
    instance.timerThread.interrupt();
  }

  public static void setDifficulty(int difficulty) {
    instance.chosenDifficulty = difficulty;
    instance.hints = difficulty;
    SharedElements.setHintsText(instance.hints);
  }

  public static int getDifficulty() {
    return instance.chosenDifficulty;
  }

  public static void setTime(int time) {
    instance.chosenTime = time;
  }

  public static void setWidth(int width) {
    instance.width = width;
  }

  public static void setHeight(int height) {
    instance.height = height;
  }

  public static void setWindowWidth(int width) {
    windowWidth = width;
  }

  public static void setWindowHeight(int height) {
    windowHeight = height;
  }

  public static int getWindowHeight() {
    return instance.height;
  }

  public static int getWindowWidth() {
    return instance.width;
  }

  public static void setChatCompletionRequest(ChatCompletionRequest chatCompletionRequest) {}

  public static String getSecondDigits() {
    return String.valueOf(instance.secondDigits);
  }

  // get gpt response
  public static ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    // get loading image in each room
    ImageView loadingAiCon = App.controlRoomController.getLoadingAi();
    ImageView loadingAiLab = App.labController.getLoadingAi();
    ImageView loadingAiKit = App.kitchenController.getLoadingAi();
    List<ImageView> loadingImages = Arrays.asList(loadingAiCon, loadingAiKit, loadingAiLab);

    // get talking image in each room
    ImageView talkingAiCon = App.controlRoomController.getTalkingAi();
    ImageView talkingAiLab = App.labController.getTalkingAi();
    ImageView talkingAiKit = App.kitchenController.getTalkingAi();
    List<ImageView> talkingImages = Arrays.asList(talkingAiCon, talkingAiKit, talkingAiLab);

    // show loading image
    for (ImageView imageView : loadingImages) {
      imageView.setVisible(true);
    }

    instance.chatBoxChatCompletionRequest.addMessage(msg);
    SharedElements.disableSendButton();
    SharedElements.disableHintsButton();

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult =
                  instance.chatBoxChatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              instance.chatBoxChatCompletionRequest.addMessage(result.getChatMessage());

              // update UI when thread is done
              Platform.runLater(
                  () -> {
                    SharedElements.appendChat("AI: " + result.getChatMessage().getContent());
                    if (instance.hints != 0) {
                      SharedElements.enableHintsButton();
                    }
                    SharedElements.enableSendButton();
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
          // remove loading image
          for (ImageView imageView : loadingImages) {
            imageView.setVisible(false);
          }

          // show talking image for 3 seconds
          for (ImageView imageView : talkingImages) {
            imageView.setVisible(true);
          }

          Timeline imageVisibilityTimeline =
              new Timeline(
                  new KeyFrame(
                      Duration.seconds(3),
                      e -> {
                        // Set all images to invisible
                        for (ImageView imageView : talkingImages) {
                          imageView.setVisible(false);
                        }
                      }));

          imageVisibilityTimeline.play();
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
    int verticalMargin = (instance.height - 1080) / 2 + (windowHeight - instance.height) / 2;
    int horizontalMargin = (instance.width - 1920) / 2 + (windowWidth - instance.width) / 2;

    BorderPane.setMargin(
        contentPane,
        new javafx.geometry.Insets(
            verticalMargin, horizontalMargin, verticalMargin, horizontalMargin));
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
    if (characterX == character.getTranslateX() && characterY == character.getTranslateY()) {
      return 0;
    }

    character.setOpacity(0);

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
      running.setScaleX(-1);
      character.setScaleX(-1);
    } else {
      running.setScaleX(1);
      character.setScaleX(1);
    }

    running.setOpacity(1);
    // Play the animation
    transition2.play();

    transition2.setOnFinished(
        e -> {
          // Remove the "running" element from the pane when the animation is done
          running.setOpacity(0);
          character.setOpacity(1);
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
    Task<Void> threadTask =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            try {
              Thread.sleep((int) (delay * 1000));
              Platform.runLater(runnable);
              return null;
            } catch (InterruptedException e) {
              System.out.println("Thread interrupted");
              return null;
            }
          }
        };
    Thread thread = new Thread(threadTask);
    instance.runningThreads.add(thread);
    threadTask.setOnSucceeded(
        e -> {
          instance.runningThreads.remove(thread);
        });
    thread.start();
  }

  public static void stopAllThreads() {
    for (Thread thread : instance.runningThreads) {
      thread.interrupt();
    }
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
                      + instance.thirdDigits
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
                      + instance.firstDigits
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

  public static void setRiddle(String riddle) {
    instance.riddleAnswer = riddle;
  }

  public static String getRiddleAnswer() {
    return instance.riddleAnswer;
  }

  public static void setPuzzleToast() {
    instance.currentPuzzle = 1;
  }

  public static void setPuzzlePaper() {
    instance.currentPuzzle = 2;
  }

  public static void setPuzzleComputer() {
    instance.currentPuzzle = 3;
  }

  public static void getPuzzleHint() throws ApiProxyException {
    try {
      SharedElements.disableHintsButton();
      if (instance.hints != 0) {
        String hint =
            "The user used the hint button, but you have no more hints to give. Tell them this in"
                + " one sentence.";
        if (instance.currentPuzzle == 1 && toasterPuzzleHints) {
          if (hasBread) {
            hint =
                "The user used the hint button. The user found a toaster that looks like it has"
                    + " been modified and the user has a slice of bread. Write a two sentence hint"
                    + " that the user should put the bread in the toaster";
          } else {
            hint =
                "The user used the hint button. The user has found a toaster that looks like it has"
                    + " been modified. The user needs toast to use it, but doesn't have any. Write"
                    + " a two sentence hint that there is toast in the fridge";
            toasterPuzzleHints = false;
          }

          instance.hints--;
          runGpt(new ChatMessage("user", hint));
        } else if (instance.currentPuzzle == 2 && paperPuzzleHints) {
          hint = "";
          instance.hints--;
          runGpt(new ChatMessage("user", hint));
          System.out.println("I NEED A PRINTER PUZZLE");
        } else if (instance.currentPuzzle == 3 && computerPuzzleHints) {
          if (instance.riddleHints == 0) {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution is \""
                    + instance.riddleAnswer
                    + "\". Write a very vague two sentence hint without giving away the answer";
            instance.hints--;
            instance.riddleHints++;
          } else if (instance.riddleHints == 1) {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution to the riddle is \""
                    + instance.riddleAnswer
                    + "\". Write a two sentence hint without giving away the answer";
            instance.hints--;
            instance.riddleHints++;
          } else if (instance.riddleHints >= 2) {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution to the riddle is \""
                    + instance.riddleAnswer
                    + "\". Write another obvious two sentence hint without giving away the answer";
            instance.hints--;
            instance.riddleHints++;
          } else {
            System.out.println("No more hints");
          }
          instance.riddleHintChatCompletionRequest.addMessage("user", hint);

          // get loading image in each room
          ImageView loadingAiCon = App.controlRoomController.getLoadingAi();
          ImageView loadingAiLab = App.labController.getLoadingAi();
          ImageView loadingAiKit = App.kitchenController.getLoadingAi();
          List<ImageView> loadingImages = Arrays.asList(loadingAiCon, loadingAiKit, loadingAiLab);

          // get talking image in each room
          ImageView talkingAiCon = App.controlRoomController.getTalkingAi();
          ImageView talkingAiLab = App.labController.getTalkingAi();
          ImageView talkingAiKit = App.kitchenController.getTalkingAi();
          List<ImageView> talkingImages = Arrays.asList(talkingAiCon, talkingAiKit, talkingAiLab);

          // show loading image
          for (ImageView imageView : loadingImages) {
            imageView.setVisible(true);
          }

          SharedElements.disableSendButton();
          SharedElements.disableHintsButton();

          Task<Void> gptTask =
              new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                  try {

                    // get response from gpt
                    ChatCompletionResult chatCompletionResult =
                        instance.riddleHintChatCompletionRequest.execute();

                    Choice result = chatCompletionResult.getChoices().iterator().next();
                    instance.riddleHintChatCompletionRequest.addMessage(result.getChatMessage());

                    // update UI when thread is done
                    Platform.runLater(
                        () -> {
                          if (instance.hints != 0) {
                            SharedElements.enableHintsButton();
                          }
                          SharedElements.enableSendButton();
                          SharedElements.appendChat("AI: " + result.getChatMessage().getContent());
                        });
                    return null;
                  } catch (Exception e) {
                    System.out.println("API error");
                    return null;
                  }
                }
              };

          gptTask.setOnSucceeded(
              event -> {
                // remove loading image
                for (ImageView imageView : loadingImages) {
                  imageView.setVisible(false);
                }

                // show talking image for 3 seconds
                for (ImageView imageView : talkingImages) {
                  imageView.setVisible(true);
                }

                Timeline imageVisibilityTimeline =
                    new Timeline(
                        new KeyFrame(
                            Duration.seconds(3),
                            e -> {
                              // Set all images to invisible
                              for (ImageView imageView : talkingImages) {
                                imageView.setVisible(false);
                              }
                            }));

                imageVisibilityTimeline.play();
              });

          // starts the task on a separate thread
          Thread gptThread = new Thread(gptTask);
          gptThread.start();
        } else {
          System.out.println("No more hints");
          runGpt(new ChatMessage("user", hint));
        }

        System.out.println(instance.currentPuzzle);
        SharedElements.setHintsText(instance.hints);
      }
    } catch (Exception e) {
      System.out.println("API error");
    }
  }
}
