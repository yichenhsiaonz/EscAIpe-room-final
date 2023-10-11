package nz.ac.auckland.se206;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.se206.controllers.SharedElements;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
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
  private static int width = 1920;
  private static int height = 1080;
  public static boolean hasBread = false;
  public static boolean hasToast = false;
  public static boolean toasterPuzzleHints = true;
  public static boolean paperPuzzleHints = true;
  public static boolean computerPuzzleHints = true;
  public static boolean isExitUnlocked = false;
  public static boolean isUsbEnding = false;
  public static String code;
  public static String endingCongrats = "";
  public static String endingReveal = "";
  public static String usbEndingReveal = "";
  private static HashMap<Items, ImageView[]> inventoryMap = new HashMap<Items, ImageView[]>();
  private static MediaPlayer mediaPlayer;
  private static boolean moving = false;

  public static void newGame() {
    // reset flags
    endingCongrats = "";
    endingReveal = "";
    usbEndingReveal = "";
    hasBread = false;
    hasToast = false;
    toasterPuzzleHints = true;
    paperPuzzleHints = true;
    computerPuzzleHints = true;
    isExitUnlocked = false;
    isUsbEnding = false;
    moving = false;
    // create new instance
    instance = new GameState();
  }

  public static void foundUSB() {
    isUsbEnding = true;
  }

  private static void inventoryMapAdd(Items item, ImageView[] itemImageView) {
    // add list of instances of identical items to the map
    inventoryMap.put(item, itemImageView);
  }

  public static void startTimer() {
    // allocate a thread to the timer
    System.out.println(instance.chosenTime);
    instance.timerThread = new Thread(instance.timerTask);
    instance.timerThread.start();
  }

  public static Task<Void> getTimer() {
    // return the currently running timer
    return instance.timerTask;
  }

  public static void stopTimer() {
    // stop the timer
    instance.timerThread.interrupt();
  }

  public static void setDifficulty(int difficulty) {
    // set the difficulty and update the hints button accordingly
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

  public static void setWidth(int newWidth) {
    width = newWidth;
  }

  public static void setHeight(int newHeight) {
    height = newHeight;
  }

  public static void setWindowWidth(int width) {
    windowWidth = width;
  }

  public static void setWindowHeight(int height) {
    windowHeight = height;
  }

  public static int getWindowHeight() {
    return height;
  }

  public static int getWindowWidth() {
    return width;
  }

  public static String getSecondDigits() {
    return String.valueOf(instance.secondDigits);
  }

  public static boolean getMuted() {
    return instance.muted;
  }

  public static void toggleMuted() {
    SharedElements.toggleMuteText();
    instance.muted = !instance.muted;
    if (instance.muted) {
      TextToSpeechManager.cutOff();
      stopSound();
    }
  }

  // get gpt response
  public static ChatMessage runGpt(ChatMessage msg, boolean hintFlag) throws ApiProxyException {
    // get loading image in each room
    List<ImageView> loadingImages = getLoadingIcons();

    // get talking image in each room
    List<ImageView> talkingImages = getTalkingIcons();

    // show loading image
    for (ImageView imageView : loadingImages) {
      imageView.setVisible(true);
    }
    // add message to gpt request
    instance.chatBoxChatCompletionRequest.addMessage(msg);
    // disable gpt related buttons
    SharedElements.disableSendButton();
    SharedElements.disableHintsButton();
    // task for gpt chat generation
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
                    // append message to chat box
                    SharedElements.appendChat("AI: " + result.getChatMessage().getContent());
                    SharedElements.chatBubbleSpeak(result.getChatMessage().getContent());

                    // if the message is a hint, add it to the hint text
                    if (hintFlag) {
                      SharedElements.appendHint(result.getChatMessage().getContent());
                    }

                    // enable gpt related buttons
                    if (instance.hints != 0) {
                      SharedElements.enableHintsButton();
                    }
                    SharedElements.enableSendButton();
                  });
              return result.getChatMessage();

            } catch (Exception e) {
              System.out.println("API error");
              e.printStackTrace();
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        event -> {
          // hide loading image and show talking image
          setTalkingIcons(talkingImages, loadingImages);
        });

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Chat Thread");
    gptThread.start();

    return gptTask.getValue();
  }

  public static void scaleToScreen(AnchorPane contentPane) {
    // calculate the scale factor
    double scale = (double) windowWidth / 1920;
    // scale the content pane accordingly
    contentPane.setScaleX(scale);
    contentPane.setScaleY(scale);
    // calculate the margins
    int verticalMargin = (height - 1080) / 2 + (windowHeight - height) / 2;
    int horizontalMargin = (width - 1920) / 2 + (windowWidth - width) / 2;
    // set the margins accordingly
    BorderPane.setMargin(
        contentPane,
        new javafx.geometry.Insets(
            verticalMargin, horizontalMargin, verticalMargin, horizontalMargin));
  }

  // this method runs a translate transition to move the character to a new position
  // and returns the duration of the animation in seconds.
  // use the setOnMovementComplete method to run code after the animation is complete.
  public static void goTo(
      double destinationX, double destinationY, ImageView character, ImageView running) {

    double durationSeconds;

    // stop moving if already moving
    if (moving) {
      stopMoving();
    }
    moving = true;
    // Retrieve the character's width and height using fitWidth and fitHeight
    double characterWidth = character.getFitWidth();
    double characterHeight = character.getFitHeight();

    // Calculate the character's new position relative to the room
    double characterX = destinationX - characterWidth / 2; // Adjust for character's width
    double characterY = destinationY - characterHeight; // Adjust for character's height
    if (characterX == character.getTranslateX() && characterY == character.getTranslateY()) {
      durationSeconds = 0;
    } else {
      // Calculate the distance the character needs to move
      double distanceToMove =
          Math.sqrt(
              Math.pow(characterX - character.getTranslateX(), 2)
                  + Math.pow(characterY - character.getTranslateY(), 2));

      // Define a constant speed
      double constantSpeed = 300;

      // Calculate the duration based on constant speed and distance
      durationSeconds = distanceToMove / constantSpeed;
    }

    character.setOpacity(0);

    // Create a TranslateTransition to smoothly move the character
    instance.transition = new TranslateTransition(Duration.seconds(durationSeconds), character);
    instance.transition.setToX(characterX);
    instance.transition.setToY(characterY);

    // Create a TranslateTransition to smoothly move the "running" element
    instance.transition2 = new TranslateTransition(Duration.seconds(durationSeconds), running);
    instance.transition2.setToX(characterX);
    instance.transition2.setToY(characterY);

    // flip the character and running gif if needed
    if (characterX > character.getTranslateX()) {
      running.setScaleX(-1);
      character.setScaleX(-1);
    } else {
      running.setScaleX(1);
      character.setScaleX(1);
    }

    running.setOpacity(1);

    instance.transition2.setOnFinished(
        e -> {
          // Remove the "running" element from the pane when the animation is done
          running.setOpacity(0);
          character.setOpacity(1);
          moving = false;
        });
  }

  public static void setOnMovementComplete(Runnable runnable) {
    if (instance.transition != null) {
      instance.transition.setOnFinished(
          e -> {
            runnable.run();
          });
    }
  }

  public static void stopMoving() {
    setOnMovementComplete(null);
    instance.transition.stop();
    instance.transition2.stop();
  }

  public static void startMoving() {
    instance.transition.play();
    instance.transition2.play();
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
              // wait for delay seconds
              Thread.sleep((int) (delay * 1000));
              // run the code on the main thread
              Platform.runLater(runnable);
              return null;
            } catch (InterruptedException e) {
              System.out.println("Thread interrupted");
              return null;
            }
          }
        };
    // starts the task on a separate thread
    Thread thread = new Thread(threadTask);
    // add thread to list of running threads
    instance.runningThreads.add(thread);
    // remove thread from list of running threads when it is done
    threadTask.setOnSucceeded(
        e -> {
          instance.runningThreads.remove(thread);
        });
    thread.start();
  }

  public static void stopAllThreads() {
    // interrupt all running threads
    for (Thread thread : instance.runningThreads) {
      thread.interrupt();
    }
  }

  public static void addItem(Items item) {
    String itemToAdd;
    String itemToAddGlow;
    String readOut;
    EventHandler<MouseEvent> clickBehaviour;
    // read flag and adjust image and click behaviour accordingly
    switch (item) {
      case BREAD_TOASTED:
        itemToAdd = "/images/Items/breadToasted.png";
        itemToAddGlow = "/images/Items/breadToastedGlow.png";
        readOut =
            "It appears that the numbers ____"
                + instance.thirdDigits
                + " have been burnt into the toast";
        clickBehaviour =
            (event) -> {
              System.out.println("bread toasted clicked");
              String aiRespone =
                  "The numbers ____"
                      + instance.thirdDigits
                      + " are burnt into it. It is still unfit for consumption";
              SharedElements.appendChat(aiRespone);
              SharedElements.chatBubbleSpeak(aiRespone);
            };
        break;
      case BREAD_UNTOASTED:
        itemToAdd = "/images/Items/breadUntoasted.png";
        itemToAddGlow = "/images/Items/breadUntoastedGlow.png";
        readOut = "I do not recommend eating that";
        clickBehaviour =
            (event) -> {
              System.out.println("bread untoasted clicked");
              String aiRespone = "It is unfit for consumption";
              SharedElements.appendChat(aiRespone);
              SharedElements.chatBubbleSpeak(aiRespone);
            };
        break;
      case PAPER:
        itemToAdd = "/images/Items/paper.png";
        itemToAddGlow = "/images/Items/paperGlow.png";
        readOut = "The numbers " + instance.firstDigits + "____ appear to be of importance";
        clickBehaviour =
            (event) -> {
              System.out.println("paper clicked");
              String aiRespone =
                  "It reads "
                      + instance.firstDigits
                      + "____. The rest of the text is irrelevant to our escape";
              SharedElements.appendChat(aiRespone);
              SharedElements.chatBubbleSpeak(aiRespone);
            };
        break;
      case USB:
        itemToAdd = "/images/Items/usb.png";
        itemToAddGlow = "/images/Items/usbGlow.png";
        readOut = "Please refrain from hoarding junk that will not assist in our escape";
        clickBehaviour =
            (event) -> {
              System.out.println("usb clicked");
              String aiRespone = "Why are you still in possesion of that? It is of no use to us";
              SharedElements.appendChat(aiRespone);
              SharedElements.chatBubbleSpeak(aiRespone);
            };
        break;
      default:
        itemToAdd = null;
        itemToAddGlow = null;
        readOut = null;
        clickBehaviour = null;
        break;
    }

    try {
      // create one instance for each room
      ImageView[] itemCopies = new ImageView[3];
      for (int i = 0; i < 3; i++) {
        ImageView itemImage = new ImageView(itemToAdd);
        itemImage.setOnMouseClicked(clickBehaviour);
        // set glow behaviour
        itemImage.setOnMouseEntered(
            event -> {
              itemImage.setImage(new Image(itemToAddGlow));
            });
        itemImage.setOnMouseExited(
            event -> {
              itemImage.setImage(new Image(itemToAdd));
            });
        itemImage.setPreserveRatio(true);
        itemCopies[i] = itemImage;
      }
      // add item to each inventory instance
      SharedElements.addInventoryItem(itemCopies);
      // add list of instances of identical items to the map
      inventoryMapAdd(item, itemCopies);

      // read out the readout and add it to history and hints
      SharedElements.appendChat(readOut);
      SharedElements.appendHint(readOut);
      SharedElements.chatBubbleSpeak(readOut);
      switch (item) {
        case BREAD_TOASTED:
          // Load prompt to congratulate user on toasting bread
          TextToSpeechManager.hideOnComplete = false;
          TextToSpeechManager.setCompletedRunnable(
              () -> {
                try {
                  GameState.runGpt(
                      new ChatMessage("user", GptPromptEngineering.toastBread()), false);
                } catch (ApiProxyException e) {
                  e.printStackTrace();
                }
              });
          break;
        case PAPER:
          // Load prompt to congratulate user on printing paper
          TextToSpeechManager.hideOnComplete = false;
          TextToSpeechManager.setCompletedRunnable(
              () -> {
                try {
                  GameState.runGpt(
                      new ChatMessage("user", GptPromptEngineering.printPaper()), false);
                } catch (ApiProxyException e) {
                  e.printStackTrace();
                }
              });
          break;
        default:
          break;
      }

    } catch (Exception e) {
      System.out.println("Error: item not found");
      e.printStackTrace();
    }
  }

  public static void removeItem(Items item) {
    try {
      // retrieve list of instances of identical items from the map
      // remove item from each inventory instance
      SharedElements.removeInventoryItem(inventoryMap.get(item));
    } catch (Exception e) {
      System.out.println("Error: item not found");
      e.printStackTrace();
    }
  }

  public static void onMessageSent() throws ApiProxyException {
    // get message from text field
    TextField messageBox = SharedElements.getMessageBox();
    String message = messageBox.getText();
    // end method if message is empty
    if (message.trim().isEmpty()) {
      return;
    }
    // append message to chat box
    SharedElements.appendChat("You: " + message);
    // clear message box
    messageBox.clear();
    // send message to gpt
    ChatMessage msg = new ChatMessage("user", message);
    GameState.runGpt(msg, false);
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
      System.out.println("current puzzle: " + instance.currentPuzzle);
      System.out.println("toaster puzzle hints: " + toasterPuzzleHints);
      System.out.println("paper puzzle hints: " + paperPuzzleHints);
      System.out.println("computer puzzle hints: " + computerPuzzleHints);
      System.out.println("Toaster Location Shown: " + instance.toasterLocationShown);
      System.out.println("Paper Location Shown: " + instance.paperLocationShown);
      System.out.println("Computer Location Shown: " + instance.computerLocationShown);
      // run as long as hints not equal to 0 (negative means unlimited)
      if (instance.hints != 0) {
        // default prompt for when user hasn't interacted with any puzzles
        String hint;
        if (instance.currentPuzzle == 1 && toasterPuzzleHints) {
          // prompt for when user has interacted with toaster puzzle
          if (hasBread) {
            hint =
                "The user used the hint button. The user found a toaster that looks like it has"
                    + " been modified and the user has a slice of bread. Write a two sentence hint"
                    + " that the user should put the bread in the toaster";
          } else {
            hint =
                "The user used the hint button. The user has found a toaster that looks like it has"
                    + " been modified. The user needs bread to use it, but doesn't have any. Write"
                    + " a two sentence hint that there is bread in the fridge";
            // flag to prevent hint from being given again
            toasterPuzzleHints = false;
          }
          instance.hints--;
          runGpt(new ChatMessage("user", hint), true);
        } else if (instance.currentPuzzle == 2 && paperPuzzleHints) {
          // prompt for when user has interacted with paper puzzle
          hint =
              "The user used the hint button. The user has found a printer with something queued"
                  + " up. The user needs to print it out. Write a two sentence hint that there is a"
                  + " computer connected to the printer in the control room";
          instance.hints--;
          // flag to prevent hint from being given again
          paperPuzzleHints = false;
          runGpt(new ChatMessage("user", hint), true);
        } else if (instance.currentPuzzle == 3 && computerPuzzleHints) {
          // prompt for when user has interacted with computer puzzle
          // uses a new gpt request for riddle hints
          // as the hints are more specific than the others
          // and may break the game
          if (instance.riddleHints == 0) {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution is \""
                    + instance.riddleAnswer
                    + "\". Write a very vague two sentence hint with no flowery language without"
                    + " giving away the answer";
            instance.hints--;
            instance.riddleHints++;
          } else if (instance.riddleHints == 1) {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution to the riddle is \""
                    + instance.riddleAnswer
                    + "\". Write a very clear two sentence hint with no flowery language without"
                    + " giving away the answer";
            instance.hints--;
            instance.riddleHints++;
          } else {
            hint =
                "I have found a computer with a password. The password is the solution to the"
                    + " riddle \""
                    + instance.riddle
                    + "\" The solution to the riddle is \""
                    + instance.riddleAnswer
                    + "\". Write another very clear two sentence hint with no flowery language and"
                    + " without giving away the answer";
            instance.hints--;
            instance.riddleHints++;
            computerPuzzleHints = false;
          }
          instance.riddleHintChatCompletionRequest.addMessage("user", hint);

          // set loading image in each room

          // get loading image in each room
          List<ImageView> loadingImages = getLoadingIcons();

          // get talking image in each room
          List<ImageView> talkingImages = getTalkingIcons();

          // show loading image
          for (ImageView imageView : loadingImages) {
            imageView.setVisible(true);
          }

          // disable gpt related buttons
          SharedElements.disableSendButton();
          SharedElements.disableHintsButton();

          // task for gpt chat generation
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
                          // enable gpt related buttons
                          if (instance.hints != 0) {
                            SharedElements.enableHintsButton();
                          }
                          SharedElements.enableSendButton();
                          // append hint to chat box
                          SharedElements.appendChat("AI: " + result.getChatMessage().getContent());
                          SharedElements.appendHint(result.getChatMessage().getContent());
                          SharedElements.chatBubbleSpeak(result.getChatMessage().getContent());
                        });
                    return null;
                  } catch (Exception e) {
                    System.out.println("API error");
                    e.printStackTrace();
                    return null;
                  }
                }
              };

          // hide loading image and show talking image
          gptTask.setOnSucceeded(
              event -> {
                setTalkingIcons(talkingImages, loadingImages);
              });

          // starts the task on a separate thread
          Thread gptThread = new Thread(gptTask);
          gptThread.start();
        } else {
          Boolean hintFlag = true;

          if (toasterPuzzleHints && !instance.toasterLocationShown) {
            hint =
                "The user used the hint button. There is a modified toaster in the kitchen. Tell"
                    + " them this in one sentence.";
            instance.hints--;
            instance.toasterLocationShown = true;
          } else if (paperPuzzleHints && !instance.paperLocationShown) {
            hint =
                "The user used the hint button. Something appeared queued on the printer in the"
                    + " lab. Tell them this in one sentence.";
            instance.hints--;
            instance.paperLocationShown = true;
          } else if (computerPuzzleHints && !instance.computerLocationShown) {
            hint =
                "The user used the hint button. There is a scientist's computer in the control"
                    + " room. Tell them this in one sentence.";
            instance.hints--;
            instance.computerLocationShown = true;
          } else {
            hint =
                "The user used the hint button, but you have no more hints to give. Tell them this"
                    + " in one sentence.";

            // ensure response is not added to hint box
            hintFlag = false;
          }
          runGpt(new ChatMessage("user", hint), hintFlag);
        }

        System.out.println(instance.currentPuzzle);
        // update hints button text
        SharedElements.setHintsText(instance.hints);
      }
    } catch (Exception e) {
      System.out.println("API error");
      e.printStackTrace();
    }
  }

  private static List<ImageView> getLoadingIcons() {
    // get loading image in each room
    ImageView loadingAiCon = App.controlRoomController.getLoadingAi();
    ImageView loadingAiLab = App.labController.getLoadingAi();
    ImageView loadingAiKit = App.kitchenController.getLoadingAi();
    return Arrays.asList(loadingAiCon, loadingAiKit, loadingAiLab);
  }

  private static List<ImageView> getTalkingIcons() {
    // get talking image in each room
    ImageView talkingAiCon = App.controlRoomController.getTalkingAi();
    ImageView talkingAiLab = App.labController.getTalkingAi();
    ImageView talkingAiKit = App.kitchenController.getTalkingAi();
    return Arrays.asList(talkingAiCon, talkingAiKit, talkingAiLab);
  }

  private static void setTalkingIcons(
      List<ImageView> talkingImages, List<ImageView> loadingImages) {
    // hide loading images
    for (ImageView imageView : loadingImages) {
      imageView.setVisible(false);
    }

    // show talking images for 3 seconds
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
  }

  public static void onCharacterMovementClick(MouseEvent event, AnchorPane room) {

    double mouseX = event.getX();
    double mouseY = event.getY();
    // Create a circle for the click animation
    Circle clickCircle = new Circle(5); // Adjust the radius as needed
    clickCircle.setFill(Color.BLUE); // Set the color of the circle
    clickCircle.setCenterX(mouseX);
    clickCircle.setCenterY(mouseY);

    // Add the circle to the room
    room.getChildren().add(clickCircle);

    // Create a fade transition for the circle
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.4), clickCircle);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    // Create a scale transition for the circle
    ScaleTransition scale = new ScaleTransition(Duration.seconds(0.4), clickCircle);
    scale.setToX(3.0); // Adjust the scale factor as needed
    scale.setToY(3.0); // Adjust the scale factor as needed

    // Play both the fade and scale transitions in parallel
    ParallelTransition parallelTransition = new ParallelTransition(fadeOut, scale);
    parallelTransition.setOnFinished(
        e -> {
          // Remove the circle from the pane when the animation is done
          room.getChildren().remove(clickCircle);
        });

    parallelTransition.play();
  }

  public static void fadeOut(AnchorPane room) {
    room.setMouseTransparent(true);
    Timeline timeline = new Timeline();
    KeyFrame key = new KeyFrame(Duration.millis(1000), new KeyValue(room.opacityProperty(), 0));
    timeline.getKeyFrames().add(key);
    timeline.play();
  }

  public static void fadeIn(AnchorPane room) {
    room.setMouseTransparent(false);
    Timeline timeline = new Timeline();
    KeyFrame key = new KeyFrame(Duration.millis(1000), new KeyValue(room.opacityProperty(), 1));
    timeline.getKeyFrames().add(key);
    timeline.play();
  }

  private boolean muted = false;
  private String riddleAnswer;
  private String riddle;
  private boolean toasterLocationShown = false;
  private boolean paperLocationShown = false;
  private boolean computerLocationShown = false;
  private int hints;
  private int riddleHints = 0;
  private int currentPuzzle = 0;
  private int chosenDifficulty = 5;
  private int chosenTime = 240;
  private Thread timerThread;
  private ArrayList<Thread> runningThreads = new ArrayList<Thread>();
  private Thread movingThread;
  private TranslateTransition transition;
  private TranslateTransition transition2;

  private ChatCompletionRequest chatBoxChatCompletionRequest =
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
  private ChatCompletionRequest riddleHintChatCompletionRequest =
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
  private String firstDigits;
  private String secondDigits;
  private String thirdDigits;
  private Task<Void> timerTask =
      new Task<>() {
        @Override
        protected Void call() throws InterruptedException, IOException {
          try {
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
            // this is a background thread so use Platform.runLater() for anything that happens in
            // the
            // UI
            Runnable menuTask =
                () -> {
                  // stop all running threads
                  // show game over screen
                  System.out.println("Timer ended");
                  TextToSpeechManager.cutOff();
                  stopSound();
                  stopAllThreads();
                  App.gameOver();
                };

            Platform.runLater(menuTask);
            return null;
          } catch (InterruptedException e) {
            System.out.println("Timer stopped");
            Runnable interruptTask =
                () -> {
                  // stop all running threads
                  stopAllThreads();
                };

            Platform.runLater(interruptTask);
            return null;
          }
        }
      };

  private GameState() {
    // pick random riddle solution
    Random rng = new Random();
    List<String> answerList =
        List.of("Planet", "Earth", "Galaxy", "Universe", "Space", "Sun", "Star");
    riddleAnswer = answerList.get(rng.nextInt(answerList.size()));
    System.out.println(riddleAnswer);
    // generate 6 digit code
    firstDigits = String.format("%02d", rng.nextInt(100));
    secondDigits = String.format("%02d", rng.nextInt(100));
    thirdDigits = String.format("%02d", rng.nextInt(100));
    code = firstDigits + secondDigits + thirdDigits;
    System.out.println(firstDigits);
    System.out.println(secondDigits);
    System.out.println(thirdDigits);
    System.out.println(code);
  }

  public static void playSound(String soundFile) {
    try {
      // Stop any currently playing sound
      stopSound();
      if (!instance.muted) {
        Media sound = new Media(GameState.class.getResource(soundFile).toExternalForm());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void stopSound() {
    if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
      mediaPlayer.stop();
    }
  }
}
