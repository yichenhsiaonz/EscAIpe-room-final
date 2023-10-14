package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.TextToSpeechManager;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Controller class for the Control Room. */
public class ControlRoomController {
  static ControlRoomController instance;
  @FXML private AnchorPane contentPane;
  @FXML private ImageView computer;
  @FXML private ImageView keypad;
  @FXML private ImageView exitDoor;
  @FXML private ImageView rightArrow;
  @FXML private ImageView rightGlowArrow;
  @FXML private ImageView leftArrow;
  @FXML private ImageView leftGlowArrow;
  @FXML private ImageView computerGlow;
  @FXML private ImageView exitGlow;
  @FXML private ImageView keypadGlow;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private AnchorPane room;
  @FXML private HBox dialogueHorizontalBox;
  @FXML private Pane inventoryPane;
  @FXML private VBox hintVerticalBox;
  @FXML private VBox bottomVerticalBox;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private Circle rightDoorMarker;
  @FXML private Circle leftDoorMarker;
  @FXML private Circle computerMarker;
  @FXML private Circle keypadMarker;
  @FXML private Circle centerDoorMarker;
  @FXML private Button muteButton;

  // elements of keypad
  @FXML private AnchorPane keyPadAnchorPane;
  @FXML private TextField codeText;

  // elements of computer
  @FXML private AnchorPane computerAnchorPane;
  @FXML private AnchorPane computerLoginAnchorPane;
  @FXML private Label gptLabel;
  @FXML private Button enterButton;
  @FXML private TextField inputText;
  @FXML private AnchorPane computerSignedInAnchorPane;
  @FXML private ImageView computerDocumentIcon;
  @FXML private ImageView computerPrintIcon;
  @FXML private ImageView computerCatsIcon;
  @FXML private Image document = new Image("images/Computer/document.png");
  @FXML private Image print = new Image("images/Computer/printer.png");
  @FXML private Image cats = new Image("images/Computer/image.png");
  @FXML private Image documentHover = new Image("images/Computer/document_hover.png");
  @FXML private Image printHover = new Image("images/Computer/printer_hover.png");
  @FXML private Image catsHover = new Image("images/Computer/image_hover.png");
  @FXML private AnchorPane computerPrintWindowAnchorPane;
  @FXML private ImageView printIcon;
  @FXML private ImageView printIconComplete;
  @FXML private Button printButton;
  @FXML private Label printLabel;
  @FXML private AnchorPane computerTextWindowAnchorPane;
  @FXML private TextArea computerDoorCodeTextArea;
  @FXML private AnchorPane computerImageWindowAnchorPane;
  @FXML private Label riddleLabel;

  private String code = "";
  private ChatCompletionRequest endingChatCompletionRequest;
  private ChatCompletionRequest computerChatCompletionRequest;
  private boolean firstOpeningTextFile;

  /** Initializes the control room. */
  public void initialize() {
    // get shared elements from the SharedElements class
    HBox bottom = SharedElements.getTaskBarBox();
    TextArea chatBox = SharedElements.getChatBox();
    TextArea hintBox = SharedElements.getHintBox();
    VBox inventory = SharedElements.getInventoryBox();
    HBox chatBubble = SharedElements.getChatBubble();

    // add shared elements to the correct places
    room.getChildren().addAll(chatBox, hintBox);
    AnchorPane.setBottomAnchor(chatBox, 0.0);
    AnchorPane.setLeftAnchor(chatBox, 0.0);
    AnchorPane.setBottomAnchor(hintBox, 0.0);
    AnchorPane.setLeftAnchor(hintBox, 0.0);
    bottomVerticalBox.getChildren().add(bottom);
    inventoryPane.getChildren().add(inventory);
    dialogueHorizontalBox.getChildren().add(chatBubble);
    SharedElements.incremnetLoadedScenes();

    // bind the mute button to the GameState muted property
    muteButton.textProperty().bind(SharedElements.getMuteText().textProperty());

    // computer initialization

    firstOpeningTextFile = true;

    try {
      computerChatCompletionRequest =
          new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
      runGpt(
          new ChatMessage("user", GptPromptEngineering.getRiddle(GameState.getRiddleAnswer())),
          true);
      computerDoorCodeTextArea.setText(
          "TOP SECRET DOOR CODE: \n\n__"
              + GameState.getSecondDigits()
              + "__\n\n\n\n"
              + "(If only I could remember the other four digits)");
    } catch (Exception e) {
      e.printStackTrace();
    }

    GameState.scaleToScreen(contentPane);

    GameState.goToInstant(
        centerDoorMarker.getLayoutX(), centerDoorMarker.getLayoutY(), character, running);
    instance = this;
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  private void clickComputer(MouseEvent event) throws IOException {
    System.out.println("computer clicked");
    try {
      // move character to clicked location
      GameState.goTo(computerMarker.getLayoutX(), computerMarker.getLayoutY(), character, running);
      // flag the current puzzle as the computer puzzle for hints
      // set root to the computer
      // enable movement after delay
      Runnable accessComputer =
          () -> {
            GameState.setPuzzleComputer();
            computerAnchorPane.setVisible(true);
          };

      GameState.setOnMovementComplete(accessComputer);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to computer when hover
  @FXML
  private void onComputerHovered(MouseEvent event) {
    computerGlow.setVisible(true);
  }

  @FXML
  private void onComputerUnhovered(MouseEvent event) {
    computerGlow.setVisible(false);
  }

  /**
   * Handles the click event on the exit door.
   *
   * @param event the mouse event
   */
  @FXML
  private void clickExit(MouseEvent event) {

    try {
      // move character to center door marker position
      GameState.goTo(
          centerDoorMarker.getLayoutX(), centerDoorMarker.getLayoutY(), character, running);

      Runnable leaveRoom =
          () -> {
            System.out.println("exit door clicked");
            if (GameState.isExitUnlocked) {
              // if the exit is unlocked, fade to black for ending scene
              TextToSpeechManager.cutOff();
              GameState.stopAllThreads();
              GameState.stopSound();
              GameState.playSound("/sounds/gate-open.m4a");
              fadeBlack();
            } else {
              String message = "The exit is locked and will not budge";
              // otherwise, display notification in chat
              SharedElements.appendChat(message);
              SharedElements.chatBubbleSpeak(message);
            }
          };

      GameState.setOnMovementComplete(leaveRoom);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to exit door when hover
  @FXML
  private void onExitHovered(MouseEvent event) {
    exitGlow.setVisible(true);
  }

  @FXML
  private void onExitUnhovered(MouseEvent event) {
    exitGlow.setVisible(false);
  }

  /**
   * Handles the click event on the keypad.
   *
   * @param event the mouse event
   * @throws IOException
   */
  @FXML
  private void clickKeypad(MouseEvent event) throws IOException {
    System.out.println("keypad clicked");
    try {
      // check if the character is already moving to prevent multiple clicks
      // move character to clicked location
      GameState.goTo(keypadMarker.getLayoutX(), keypadMarker.getLayoutY(), character, running);
      // set root to the keypad after delay and enable movement
      Runnable accessKeypad =
          () -> {
            keyPadAnchorPane.setVisible(true);
          };

      GameState.setOnMovementComplete(accessKeypad);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to keypad when hover
  @FXML
  private void onKeypadHovered(MouseEvent event) {
    keypadGlow.setVisible(true);
  }

  @FXML
  private void onKeypadUnhovered(MouseEvent event) {
    keypadGlow.setVisible(false);
  }

  /**
   * Handles the click event on the right arrow.
   *
   * @param event the mouse event
   */
  @FXML
  private void onRightClicked(MouseEvent event) throws IOException {
    try {
      // move character to clicked location
      GameState.goTo(
          rightDoorMarker.getLayoutX(), rightDoorMarker.getLayoutY(), character, running);
      // set root to the kitchen after delay and enable movement
      Runnable leaveRoom =
          () -> {
            GameState.playSound("/sounds/door-opening.m4a");
            GameState.fadeOut(room);
            Runnable loadKitchen =
                () -> {
                  try {
                    App.setRoot(AppUi.KITCHEN);
                    KitchenController.instance.fadeIn();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                };
            GameState.delayRun(loadKitchen, 1);
          };

      GameState.setOnMovementComplete(leaveRoom);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to right arrow when hover
  @FXML
  private void onRightHovered(MouseEvent event) {
    rightGlowArrow.setVisible(true);
  }

  @FXML
  private void onRightUnhovered(MouseEvent event) {
    rightGlowArrow.setVisible(false);
  }

  /**
   * Handles the click event on the left arrow.
   *
   * @param event the mouse event
   */
  @FXML
  private void onLeftClicked(MouseEvent event) {

    try {
      // move character to clicked location
      GameState.goTo(leftDoorMarker.getLayoutX(), leftDoorMarker.getLayoutY(), character, running);
      // set root to the lab after delay and enable movement
      Runnable leaveRoom =
          () -> {
            GameState.playSound("/sounds/door-opening.m4a");
            GameState.fadeOut(room);
            Runnable loadLab =
                () -> {
                  try {
                    App.setRoot(AppUi.LAB);
                    LabController.instance.fadeIn();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                };
            GameState.delayRun(loadLab, 1);
          };
      GameState.setOnMovementComplete(leaveRoom);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to left arrow when hover
  @FXML
  private void onLeftHovered(MouseEvent event) {
    leftGlowArrow.setVisible(true);
  }

  @FXML
  private void onLeftUnhovered(MouseEvent event) {
    leftGlowArrow.setVisible(false);
  }

  /**
   * 'Consumes' the mouse event, preventing it from being registered.
   *
   * @param event the mouse event
   */
  @FXML
  private void consumeMouseEvent(MouseEvent event) {
    System.out.println("mouse event consumed");
    System.out.println(event.getSource());
    event.consume();
  }

  /**
   * Handles the mouse click event on the room, moving the character to the clicked location.
   *
   * @param event the mouse event
   */
  @FXML
  private void onMoveCharacter(MouseEvent event) {
    // check if the character is already moving to prevent multiple clicks
    // play click animation
    GameState.onCharacterMovementClick(event, room);
    // move character to clicked location
    double mouseX = event.getX();
    double mouseY = event.getY();

    GameState.goTo(mouseX, mouseY, character, running);
    GameState.startMoving();
  }

  public void fadeBlack() {
    // stop timer
    GameState.stopTimer();
    // Create a black rectangle that covers the entire AnchorPane
    AnchorPane anchorPane = contentPane;
    AnchorPane blackRectangle = new AnchorPane();
    blackRectangle.setStyle("-fx-background-color: black;");
    blackRectangle.setOpacity(0.0);
    AnchorPane.setTopAnchor(blackRectangle, 0.0);
    AnchorPane.setBottomAnchor(blackRectangle, 0.0);
    AnchorPane.setLeftAnchor(blackRectangle, 0.0);
    AnchorPane.setRightAnchor(blackRectangle, 0.0);

    // Add the black rectangle to AnchorPane
    anchorPane.getChildren().add(blackRectangle);

    // Create a fade transition
    FadeTransition fadeToBlack = new FadeTransition(Duration.seconds(5), blackRectangle);
    fadeToBlack.setFromValue(0.0);
    fadeToBlack.setToValue(1.0);

    // Change to end scene when the fade animation is complete
    fadeToBlack.setOnFinished(
        event -> {
          try {
            App.setRoot(AppUi.ENDING);
          } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error changing to ending scene");
          }
        });

    fadeToBlack.play();
  }

  // get image of loading AI
  public ImageView getLoadingAi() {
    return loadingAi;
  }

  // get image of talking AI
  public ImageView getTalkingAi() {
    return talkingAi;
  }

  @FXML
  private void onBackToMenu(ActionEvent event) throws IOException {
    TextToSpeechManager.cutOff();
    GameState.stopAllThreads();
    GameState.stopSound();
    App.setRoot(AppUi.MENU);
  }

  @FXML
  private void onMute(ActionEvent event) {
    GameState.toggleMuted();
  }

  public void fadeIn() {
    GameState.fadeIn(room);
  }

  // keypad methods
  // Implement keypad functionality
  @FXML
  private void onOneClicked() {
    appendNumber("1");
  }

  @FXML
  private void onTwoClicked() {
    appendNumber("2");
  }

  @FXML
  private void onThreeClicked() {
    appendNumber("3");
  }

  @FXML
  private void onFourClicked() {
    appendNumber("4");
  }

  @FXML
  private void onFiveClicked() {
    appendNumber("5");
  }

  @FXML
  private void onSixClicked() {
    appendNumber("6");
  }

  @FXML
  private void onSevenClicked() {
    appendNumber("7");
  }

  @FXML
  private void onEightClicked() {
    appendNumber("8");
  }

  @FXML
  private void onNineClicked() {
    appendNumber("9");
  }

  @FXML
  private void onZeroClicked() {
    appendNumber("0");
  }

  /** Deletes the last entered digit of the code. */
  @FXML
  private void onDeleteClicked() {
    if (code.length() > 0) {
      code = code.substring(0, code.length() - 1);
      codeText.setText(code);
    }
  }

  /**
   * Checks if the code is correct.
   *
   * @throws ApiProxyException
   */
  @FXML
  private void onEnterClicked() throws ApiProxyException {
    System.out.println(code);
    System.out.println("Enter clicked");

    // Check if the code is correct
    if (code.equals(GameState.code)) {
      GameState.playSound("/sounds/unlock.m4a");
      // load the ai text for ending
      endingChatCompletionRequest =
          new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
      endingGpt(new ChatMessage("user", GptPromptEngineering.endingCongrats()), 1);

      codeText.setText("Unlocked");
      GameState.isExitUnlocked = true;
    } else {
      codeText.setText("Incorrect");

      Timeline timeline =
          new Timeline(
              new KeyFrame(
                  Duration.seconds(1.5),
                  event -> {
                    codeText.setText(""); // reset the text
                    codeText.clear(); // clear the input field
                  }));

      timeline.setCycleCount(1);
      timeline.play();

      // reset code
      code = "";
    }
  }

  /** Returns to the control room screen when exit button clicked. */
  @FXML
  private void onCloseKeypadClicked() throws IOException {
    System.out.println("Exit clicked");

    keyPadAnchorPane.setVisible(false);
  }

  /**
   * Appends the input number to the text field if the code is less than 3 digits long.
   *
   * @param number the number to be appended
   */
  private void appendNumber(String number) {
    if (code.length() < 6 && !codeText.getText().equals("Incorrect")) {
      codeText.appendText(number);
      code += number;
    }
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  public ChatMessage endingGpt(ChatMessage msg, int iteration) throws ApiProxyException {

    endingChatCompletionRequest.addMessage(msg);

    // task for gpt chat generation
    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {

          @Override
          protected ChatMessage call() throws Exception {
            System.out.println("Get message: " + Thread.currentThread().getName());

            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult = endingChatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              endingChatCompletionRequest.addMessage(result.getChatMessage());

              if (iteration == 1) {
                GameState.endingCongrats = result.getChatMessage().getContent();
                System.out.println(GameState.endingCongrats);
              } else if (iteration == 2) {
                GameState.endingReveal = result.getChatMessage().getContent();
                System.out.println(GameState.endingReveal);
              } else if (iteration == 3) {
                GameState.usbEndingReveal = result.getChatMessage().getContent();
                System.out.println(GameState.usbEndingReveal);
              }

              return result.getChatMessage();
            } catch (ApiProxyException e) {
              System.out.println("Error with GPT");
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        event -> {
          // get next message
          if (iteration == 1) {
            try {
              endingGpt(new ChatMessage("user", GptPromptEngineering.endingReveal()), 2);
            } catch (ApiProxyException e) {
              System.out.println("Error with GPT");
            }
          } else if (iteration == 2) {
            try {
              endingGpt(new ChatMessage("user", GptPromptEngineering.usbEndingReveal()), 3);
            } catch (ApiProxyException e) {
              System.out.println("Error with GPT");
            }
          }
        });

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Ending Thread");
    gptThread.start();

    return gptTask.getValue();
  }

  // computer methods start here

  private void runGpt(ChatMessage msg, Boolean generatingRiddle) throws ApiProxyException {
    computerChatCompletionRequest.addMessage(msg);
    enterButton.setDisable(true);

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult = computerChatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              computerChatCompletionRequest.addMessage(result.getChatMessage());
              GameState.setRiddle(result.getChatMessage().getContent());

              // update UI when thread is done
              Platform.runLater(
                  () -> {
                    enterButton.setDisable(false);
                    if (generatingRiddle) {
                      riddleLabel.setText(result.getChatMessage().getContent());
                    } else {
                      gptLabel.setText(result.getChatMessage().getContent());
                    }
                  });

              return result.getChatMessage();
            } catch (ApiProxyException e) {
              System.out.println("API error");
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        event -> {
          ChatMessage lastMsg = gptTask.getValue();

          if (lastMsg.getRole().equals("assistant") && lastMsg.getContent().startsWith("Correct")) {
            System.out.println("Riddle solved");
            // flag that riddle has been solved and hints are no longer needed
            GameState.computerPuzzleHints = false;
            computerSignedInAnchorPane.setVisible(true);
            computerLoginAnchorPane.setVisible(false);
          }
        });

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Chat Thread");
    gptThread.start();
  }

  /**
   * Sends the typed message by the user to gpt.
   *
   * @param event the mouse event
   */
  @FXML
  private void onMessageSent(ActionEvent event) throws ApiProxyException, IOException {
    // get message from text field
    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      // end method if message is empty
      return;
    }
    // clear text field
    inputText.clear();
    // create chat message and append it to the text area
    ChatMessage msg = new ChatMessage("user", message);
    // send message to gpt
    runGpt(msg, false);
  }

  /**
   * Sends the typed message by the user to gpt when user uses enter key.
   *
   * @param event the key event
   */
  @FXML
  private void onEnter(KeyEvent event) throws ApiProxyException, IOException {
    if (event.getCode().equals(KeyCode.ENTER)) {
      onMessageSent(null);
    }
  }

  @FXML
  private void onDocumentIconHovered() {
    computerDocumentIcon.setImage(documentHover);
  }

  @FXML
  private void onDocumentIconUnhovered() {
    computerDocumentIcon.setImage(document);
  }

  @FXML
  private void onPrintIconHovered() {
    computerPrintIcon.setImage(printHover);
  }

  @FXML
  private void onPrintIconUnhovered() {
    computerPrintIcon.setImage(print);
  }

  @FXML
  private void onCatIconHovered() {
    computerCatsIcon.setImage(catsHover);
  }

  @FXML
  private void onCatIconUnhovered() {
    computerCatsIcon.setImage(cats);
  }

  @FXML
  private void onFocusTextWindowClicked() {
    computerTextWindowAnchorPane.setVisible(true);
    computerTextWindowAnchorPane.toFront();
    if (firstOpeningTextFile) {
      String readout = "The code in the text file reads __" + GameState.getSecondDigits() + "__";
      SharedElements.appendHint(readout);
      SharedElements.appendChat(readout);
      SharedElements.chatBubbleSpeak(readout);
      TextToSpeechManager.setCompletedRunnable(
          () -> {
            try {
              GameState.runGpt(new ChatMessage("user", GptPromptEngineering.solveRiddle()), false);
            } catch (ApiProxyException e) {
              e.printStackTrace();
            }
          });
      // Load prompt to congratulate user on solving riddle

      firstOpeningTextFile = false;
    }
  }

  @FXML
  private void onFocusPrintWindowClicked() {
    computerPrintWindowAnchorPane.setVisible(true);
    computerPrintWindowAnchorPane.toFront();
  }

  @FXML
  private void onFocusCatWindowClicked() {
    computerImageWindowAnchorPane.setVisible(true);
    computerImageWindowAnchorPane.toFront();
  }

  @FXML
  private void onCloseTextWindowClicked() {
    computerTextWindowAnchorPane.setVisible(false);
  }

  @FXML
  private void onClosePrintWindowClicked() {
    computerPrintWindowAnchorPane.setVisible(false);
  }

  @FXML
  private void onCloseImageWindowClicked() {
    computerImageWindowAnchorPane.setVisible(false);
  }

  @FXML
  private void onPrintButtonClicked() {
    // disable the print hightlight
    printButton.disableProperty().set(true);
    // show the printing message below
    printLabel.setText("Printing...");
    GameState.playSound("/sounds/printer.m4a");

    // hide the printing text after 2 seconds
    // show the finished printing icon
    Runnable printing =
        () -> {
          // flag that the paper has been printed from the computer
          printIcon.setVisible(false);
          printIconComplete.setVisible(true);
          SharedElements.printPaper();
          printLabel.setText("No Files in Queue");
        };

    GameState.delayRun(printing, 2);
  }

  @FXML
  private void onCloseComputer() {
    computerAnchorPane.setVisible(false);
  }
}
