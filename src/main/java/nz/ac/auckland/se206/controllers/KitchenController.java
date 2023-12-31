package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.TextToSpeechManager;

/** Controller class for the room view. */
public class KitchenController {

  public static KitchenController instance;
  @FXML private AnchorPane contentPane;
  @FXML private ImageView floor;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private ImageView doorGlow;
  @FXML private ImageView toasterGlow;
  @FXML private ImageView fridgeClosedGlow;
  @FXML private ImageView fridgeOpenGlow;
  @FXML private ImageView fridgeClosed;
  @FXML private ImageView fridgeOpen;
  @FXML private HBox dialogueHorizontalBox;
  @FXML private VBox bottomVerticalBox;
  @FXML private VBox hintVerticalBox;
  @FXML private Pane inventoryPane;
  @FXML private Circle doorMarker;
  @FXML private Circle toasterMarker;
  @FXML private Circle fridgeMarker;
  @FXML private AnchorPane room;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private Button muteButton;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    SharedElements.initialize(
        room, bottomVerticalBox, inventoryPane, dialogueHorizontalBox, muteButton, contentPane);

    // get door marker position
    int doorMarkerX = (int) doorMarker.getLayoutX();
    int doorMarkerY = (int) doorMarker.getLayoutY();

    // move character to door marker position
    GameState.goToInstant(doorMarkerX, doorMarkerY, character, running);
    room.setOpacity(0);
    instance = this;
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyPressed(KeyEvent event) {
    System.out.println("key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  public void onKeyReleased(KeyEvent event) {
    System.out.println("key " + event.getCode() + " released");
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
   * Handles the mouse click event on the room, moving the character to the clicked location.
   *
   * @param event the mouse event
   */
  @FXML
  public void onMoveCharacter(MouseEvent event) {
    // create click indicator
    GameState.onCharacterMovementClick(event, room);
    // get mouse position
    double mouseX = event.getX();
    double mouseY = event.getY();

    // move character to mouse position
    GameState.goTo(mouseX, mouseY, character, running);
    GameState.startMoving();
  }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void onDoorClicked(MouseEvent event) throws IOException {

    // move character to door marker position
    GameState.goTo(doorMarker.getLayoutX(), doorMarker.getLayoutY(), character, running);
    // load the control room scene after movement animation is finished
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
  }

  // add glow highlight to door when hover
  @FXML
  public void onDoorHovered(MouseEvent event) {
    doorGlow.setVisible(true);
  }

  @FXML
  public void onDoorUnhovered(MouseEvent event) {
    doorGlow.setVisible(false);
  }

  /**
   * Handles the click event on the toaster.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void onToasterClicked(MouseEvent event) {
    // move character to toaster marker position
    GameState.goTo(toasterMarker.getLayoutX(), toasterMarker.getLayoutY(), character, running);
    // set active puzzle to toaster puzzle
    GameState.setPuzzleToast();
    if (GameState.hasBread && !GameState.hasToast) {
      // run if the user has bread and doesn't have toast
      Runnable toastFinish =
          () -> {
            GameState.playSound("/sounds/toaster.mp3");
            System.out.println("toaster clicked");
            // put notification in chat box
            SharedElements.appendChat("A charred slice of toast pops out of the toaster");
            // add toasted bread to inventory
            GameState.addItem(GameState.Items.BREAD_TOASTED);
            // flag that the user has toast
            GameState.hasToast = true;
            // flag that the toaster puzzle has been completed
            // so that no more hints are given for this puzzle
            GameState.toasterPuzzleHints = false;
          };
      Runnable waitForToast =
          () -> {
            GameState.playSound("/sounds/timer.mp3");
            SharedElements.appendChat("Sparks fly out of the toaster as it toasts the bread");
            GameState.delayRun(toastFinish, 4);
          };
      Runnable putInToast =
          () -> {
            GameState.playSound("/sounds/timer.mp3");
            System.out.println("toaster clicked");
            // remove bread from inventory
            GameState.removeItem(GameState.Items.BREAD_UNTOASTED);
            // put notification in chat box
            SharedElements.appendChat("You put a slice of bread in the toaster");
            // flag that the user has no bread
            GameState.hasBread = false;
            GameState.delayRun(waitForToast, 2);
          };
      GameState.setOnMovementComplete(putInToast);
      GameState.startMoving();

    } else if (GameState.hasToast) {
      // run if the user has toast already
      Runnable toasterRunnable =
          () -> {
            System.out.println("toaster clicked");
            // put notification in chat box
            String message = "Looks like the toaster is toast.";
            SharedElements.appendChat(message);
            SharedElements.chatBubbleSpeak(message);
          };
      GameState.setOnMovementComplete(toasterRunnable);
      GameState.startMoving();
    } else {
      Runnable toasterRunnable =
          () -> {
            System.out.println("toaster clicked");
            // put notification in chat box
            String message = "This toaster looks like it has been modified";
            SharedElements.appendChat(message);
            SharedElements.chatBubbleSpeak(message);
          };
      GameState.setOnMovementComplete(toasterRunnable);
      GameState.startMoving();
    }
  }

  @FXML
  public void onToasterHovered(MouseEvent event) {
    toasterGlow.setVisible(true);
  }

  @FXML
  public void onToasterUnhovered(MouseEvent event) {
    toasterGlow.setVisible(false);
  }

  /**
   * Handles the click event on the open fridge.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void onFridgeOpenClicked(MouseEvent event) {
    // move character to fridge marker position
    GameState.goTo(fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);

    Runnable openFridgeRunnable =
        () -> {
          System.out.println("open fridge clicked");
          // put notification in chat box
          SharedElements.appendChat("There's nothing left in the fridge.");
        };
    GameState.setOnMovementComplete(openFridgeRunnable);
    GameState.startMoving();
  }

  @FXML
  public void onFridgeOpenHovered(MouseEvent event) {
    fridgeOpenGlow.setVisible(true);
  }

  @FXML
  public void onFridgeOpenUnhovered(MouseEvent event) {
    fridgeOpenGlow.setVisible(false);
  }

  /**
   * Handles the click event on the closed fridge.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void onFridgeClosedClicked(MouseEvent event) {
    try {
      // move character to fridge marker position
      GameState.goTo(fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);
      // flag that the current puzzle is the toast puzzle
      GameState.setPuzzleToast();
      Runnable closedFridgeRunnable =
          () -> {
            GameState.playSound("/sounds/pick-up-item.m4a");
            // change fridge image to open fridge
            fridgeClosed.setVisible(false);
            fridgeOpen.setVisible(true);
            // change floor hitbox to account for open fridge
            floor.setImage(new Image("images/Kitchen/floorfridgeopen.png"));
            // put notification in chat box
            SharedElements.appendChat("You find a stale loaf of bread in the fridge.");
            // flag that the user has bread
            GameState.hasBread = true;
            // add bread to inventory
            GameState.addItem(GameState.Items.BREAD_UNTOASTED);
            System.out.println("closed fridge clicked");
          };
      GameState.setOnMovementComplete(closedFridgeRunnable);
      GameState.startMoving();
    } catch (Exception e) {
      System.out.println("get bread error");
      e.printStackTrace();
    }
  }

  @FXML
  public void onFridgeClosedHovered(MouseEvent event) {
    fridgeClosedGlow.setVisible(true);
  }

  @FXML
  public void onFridgeClosedUnhovered(MouseEvent event) {
    fridgeClosedGlow.setVisible(false);
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
}
