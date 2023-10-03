package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class LabController {
  @FXML private AnchorPane contentPane;
  @FXML private Rectangle printer;
  @FXML private ImageView rightArrow;
  @FXML private ImageView rightGlowArrow;
  @FXML private ImageView printerGlow;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private Pane room;
  @FXML private HBox dialogueHorizontalBox;
  @FXML private VBox bottomVerticalBox;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private ImageView doorGlow;
  @FXML private ImageView usbGlow;
  @FXML private ImageView usb;

  private boolean moving = false;
  private double startX = 1300;
  private double startY = 630;

  public void initialize() {
    // Initialization code goes here
    dialogueHorizontalBox.getChildren().add(SharedElements.getDialogueBox());
    bottomVerticalBox.getChildren().add(SharedElements.getTaskBarBox());
    SharedElements.incremnetLoadedScenes();
    GameState.scaleToScreen(contentPane);

    GameState.goToInstant(startX, startY, character, running);
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
        double movementDelay = GameState.goTo(360, 680, character, running);
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
                  GameState.runGpt(new ChatMessage("user", GptPromptEngineering.printPaper()));
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
              try {
                App.setRoot(AppUi.CONTROL_ROOM);
              } catch (IOException e) {
                e.printStackTrace();
              }
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
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
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
    usbGlow.setOpacity(0);
    usb.setOpacity(0);
    GameState.foundUSB();
  }
}
