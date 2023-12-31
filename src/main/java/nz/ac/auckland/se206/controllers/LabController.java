package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

/**
 * Controller for the lab scene. This class controls the items in the lab and the functionality of
 * them.
 */
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
  @FXML private Button muteButton;

  private double startX = 1512;
  private double startY = 814;

  /** Initalizes the lab scene. This method is called when the game starts. */
  public void initialize() {
    SharedElements.initialize(
        room, bottomVerticalBox, inventoryPane, dialogueHorizontalBox, muteButton, contentPane);

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
    // show click animation
    GameState.onCharacterMovementClick(event, room);
    // move character to clicked location
    double mouseX = event.getX();
    double mouseY = event.getY();
    GameState.goTo(mouseX, mouseY, character, running);
    GameState.startMoving();
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

  /**
   * Handles the click event on the printer.
   *
   * @param event the mouse event
   */
  @FXML
  public void onPrinterClicked(MouseEvent event) throws IOException {

    try {
      GameState.goTo(619, 834, character, running);
      // flag that the current puzzle is the paper puzzle
      GameState.setPuzzlePaper();
      Runnable goToPrinter =
          () -> {
            System.out.println("Printer clicked");

            if (SharedElements.isPaperPrinted() == false) {
              String message = "The printer is empty";
              SharedElements.appendChat(message);
              SharedElements.chatBubbleSpeak(message);
            } else {
              GameState.playSound("/sounds/pick-up-item.m4a");
              // append notification to chat box
              SharedElements.appendChat("There is a printed piece of paper, you take it.");
              // add paper to inventory
              GameState.addItem(GameState.Items.PAPER);
              SharedElements.takePaper();
              // flag that the paper puzzle has been completed to prevent hints from being shown
              GameState.paperPuzzleHints = false;
            }
          };

      GameState.setOnMovementComplete(goToPrinter);
      GameState.startMoving();
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
      // move character to door position
      GameState.goTo(startX, startY, character, running);
      // set root to control room and allow character to move again after movement delay
      Runnable leaveRoom =
          () -> {
            GameState.playSound("/sounds/door-opening.m4a");
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
          };

      GameState.setOnMovementComplete(leaveRoom);
      GameState.startMoving();
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
    GameState.stopSound();
    App.setRoot(AppUi.MENU);
  }

  @FXML
  private void onMute(ActionEvent event) {
    GameState.toggleMuted();
  }

  @FXML
  public void onUsbHovered(MouseEvent event) {
    usbGlow.setVisible(true);
  }

  @FXML
  public void onUsbUnhovered(MouseEvent event) {
    usbGlow.setVisible(false);
  }

  /**
   * Handles the click event on the usb.
   *
   * @param event the mouse event
   */
  @FXML
  public void onUsbClicked(MouseEvent event) {

    try {
      // move character to usb position
      GameState.goTo(usb.getLayoutX(), usb.getLayoutY(), character, running);
      Runnable goToUsb =
          () -> {
            GameState.playSound("/sounds/pick-up-item.m4a");
            room.getChildren().remove(usbGlow);
            room.getChildren().remove(usb);
            GameState.addItem(GameState.Items.USB);
            GameState.foundUsb();
          };

      GameState.setOnMovementComplete(goToUsb);
      GameState.startMoving();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void fadeIn() {
    GameState.fadeIn(room);
  }
}
