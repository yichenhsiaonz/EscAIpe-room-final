package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.TextToSpeechManager;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class LabController {
  public static LabController instance;
  @FXML private AnchorPane contentPane;
  @FXML private Rectangle printer;
  @FXML private ImageView rightArrow;
  @FXML private ImageView rightGlowArrow;
  @FXML private ImageView printerGlow;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private AnchorPane room;
  @FXML private HBox dialogueHorizontalBox;
  @FXML private VBox bottomVerticalBox;
  @FXML private Pane inventoryPane;
  @FXML private VBox hintVerticalBox;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private ImageView doorGlow;
  @FXML private ImageView usbGlow;
  @FXML private ImageView usb;

  private boolean moving = false;
  private double startX = 1512;
  private double startY = 814;

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
    GameState.scaleToScreen(contentPane);

    GameState.goToInstant(startX, startY, character, running);
    room.setOpacity(0);
    instance = this;
  }

  /**
   * Handles the mouse click event on the room, moving the character to the clicked location.
   *
   * @param event the mouse event
   */
  @FXML
  public void onMoveCharacter(MouseEvent event) {
    // check if the character is already moving to prevent multiple clicks
    if (!moving) {
      moving = true;
      // show click animation
      GameState.onCharacterMovementClick(event, room);
      // move character to clicked location
      double mouseX = event.getX();
      double mouseY = event.getY();
      double movementDelay = GameState.goTo(mouseX, mouseY, character, running);
      // allow character to move again after movement delay
      Runnable resumeMoving =
          () -> {
            moving = false;
          };
      GameState.delayRun(resumeMoving, movementDelay);
    }
  }

  /**
   * 'Consumes' the mouse event, preventing it from being registered.
   *
   * @param event the mouse event
   */
  @FXML
  public void consumeMouseEvent(MouseEvent event) {
    System.out.println("mouse event consumed");
    System.out.println(event.getSource());
    event.consume();
  }

  @FXML
  public void onPrinterClicked(MouseEvent event) throws IOException {

    try {
      if (!moving) {
        moving = true;
        double movementDelay = GameState.goTo(619, 834, character, running);
        // flag that the current puzzle is the paper puzzle
        GameState.setPuzzlePaper();
        Runnable goToPrinter =
            () -> {
              System.out.println("Printer clicked");

              if (SharedElements.isPaperPrinted() == false) {
                SharedElements.appendChat("Printer is empty!");
              } else {
                // append notification to chat box
                SharedElements.appendChat("There is a printed piece of paper, you take it.");
                // add paper to inventory
                GameState.addItem(GameState.Items.PAPER);
                SharedElements.takePaper();
                // flag that the paper puzzle has been completed to prevent hints from being shown
                GameState.paperPuzzleHints = false;
                // Load prompt to congratulate user on printing paper
                try {
                  GameState.runGpt(
                      new ChatMessage("user", GptPromptEngineering.printPaper()), false);
                } catch (ApiProxyException e) {
                  e.printStackTrace();
                }
              }

              moving = false;
            };

        GameState.delayRun(goToPrinter, movementDelay);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void onPrinterHovered(MouseEvent event) {
    printerGlow.setVisible(true);
  }

  @FXML
  public void onPrinterUnhovered(MouseEvent event) {
    printerGlow.setVisible(false);
  }

  /**
   * Handles the click event on the right arrow.
   *
   * @param event the mouse event
   */
  @FXML
  public void onRightClicked(MouseEvent event) throws IOException {

    try {
      // check if the character is already moving to prevent multiple clicks
      if (!moving) {
        moving = true;
        // move character to door position
        double movementDelay = GameState.goTo(startX, startY, character, running);
        // set root to control room and allow character to move again after movement delay
        Runnable leaveRoom =
            () -> {
              GameState.fadeOut(room);
              Runnable loadControlRoom =
                  () -> {
                    try {
                      App.setRoot(AppUi.CONTROL_ROOM);
                      ControlRoomController.instance.fadeIn();
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  };
              GameState.delayRun(loadControlRoom, 1);
              moving = false;
            };

        GameState.delayRun(leaveRoom, movementDelay);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // add glow highlight to right arrow when hover
  @FXML
  public void onRightHovered(MouseEvent event) {
    doorGlow.setVisible(true);
  }

  @FXML
  public void onRightUnhovered(MouseEvent event) {
    doorGlow.setVisible(false);
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
    App.setRoot(AppUi.MENU);
  }

  @FXML
  private void onMute(ActionEvent event) {
    TextToSpeechManager.cutOff();
    GameState.toggleMuted();
  }

  @FXML
  public void onUSBhovered(MouseEvent event) {
    usbGlow.setVisible(true);
  }

  @FXML
  public void onUSBunhovered(MouseEvent event) {
    usbGlow.setVisible(false);
  }

  @FXML
  public void onUSBclicked(MouseEvent event) {

    try {
      // check if the character is already moving to prevent multiple clicks
      if (!moving) {
        moving = true;
        // move character to usb position
        double movementDelay =
            GameState.goTo(usb.getLayoutX(), usb.getLayoutY(), character, running);
        Runnable goToUsb =
            () -> {
              usbGlow.setOpacity(0);
              usb.setOpacity(0);
              GameState.addItem(GameState.Items.USB);
              GameState.foundUSB();
              moving = false;
            };

        GameState.delayRun(goToUsb, movementDelay);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void fadeIn() {
    GameState.fadeIn(room);
  }
}
