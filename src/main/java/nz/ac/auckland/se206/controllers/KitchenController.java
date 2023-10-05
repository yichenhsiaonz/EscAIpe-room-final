package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
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
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

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

  private boolean moving = false;

  /** Initializes the room view, it is called when the room loads. */
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
    hintVerticalBox.getChildren().add(SharedElements.getHintButton());
    SharedElements.incremnetLoadedScenes();
    // scale the room to the screen size
    GameState.scaleToScreen(contentPane);

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
    // check if character is already moving to prevent multiple movements
    if (!moving) {
      // create click indicator
      GameState.onCharacterMovementClick(event, room);
      // get mouse position
      double mouseX = event.getX();
      double mouseY = event.getY();

      moving = true;
      // move character to mouse position
      double movementDelay = GameState.goTo(mouseX, mouseY, character, running);
      // allow character to move again after movement animation is finished
      Runnable resumeMoving =
          () -> {
            moving = false;
          };
      GameState.delayRun(resumeMoving, movementDelay);
    }
  }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void onDoorClicked(MouseEvent event) throws IOException {
    // check if character is already moving to prevent multiple movements
    if (!moving) {
      moving = true;

      // move character to door marker position
      double movementDelay =
          GameState.goTo(doorMarker.getLayoutX(), doorMarker.getLayoutY(), character, running);
      // load the control room scene after movement animation is finished
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
    // check if character is already moving to prevent multiple movements
    if (!moving) {
      moving = true;
      // move character to toaster marker position
      double movementDelay =
          GameState.goTo(
              toasterMarker.getLayoutX(), toasterMarker.getLayoutY(), character, running);
      // set active puzzle to toaster puzzle
      GameState.setPuzzleToast();
      if (GameState.hasBread && !GameState.hasToast) {
        // run if the user has bread and doesn't have toast
        Runnable putInToast =
            () -> {
              System.out.println("toaster clicked");
              // remove bread from inventory
              GameState.removeItem(GameState.Items.BREAD_UNTOASTED);
              // put notification in chat box
              SharedElements.appendChat("You put a slice of bread in the toaster");
              // flag that the user has no bread
              GameState.hasBread = false;
            };
        Runnable waitForToast =
            () -> {
              SharedElements.appendChat("Sparks fly out of the toaster as it toasts the bread");
            };
        Runnable toastFinish =
            () -> {
              System.out.println("toaster clicked");
              // add toasted bread to inventory
              GameState.addItem(GameState.Items.BREAD_TOASTED);
              // put notification in chat box
              SharedElements.appendChat("A charred slice of toast pops out of the toaster");
              // Load prompt to congratulate user on toasting bread
              try {
                GameState.runGpt(new ChatMessage("user", GptPromptEngineering.toastBread()), false);
              } catch (ApiProxyException e) {
                e.printStackTrace();
              }
              // flag that the user has toast
              GameState.hasToast = true;
              // flag that the toaster puzzle has been completed
              // so that no more hints are given for this puzzle
              GameState.toasterPuzzleHints = false;
              // allow character to move again
              moving = false;
            };

        GameState.delayRun(putInToast, movementDelay);
        GameState.delayRun(waitForToast, 2);
        GameState.delayRun(toastFinish, 4);
      } else if (GameState.hasToast) {
        // run if the user has toast already
        Runnable toasterRunnable =
            () -> {
              System.out.println("toaster clicked");
              // put notification in chat box
              SharedElements.appendChat("Looks like the toaster is toast.");
              // allow character to move again
              moving = false;
            };
        GameState.delayRun(toasterRunnable, movementDelay);
      } else {
        Runnable toasterRunnable =
            () -> {
              System.out.println("toaster clicked");
              // put notification in chat box
              SharedElements.appendChat("This toaster looks like it's been tampered with");
              // allow character to move again
              moving = false;
            };
        GameState.delayRun(toasterRunnable, movementDelay);
      }
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
    // check if character is already moving to prevent multiple movements
    if (!moving) {
      moving = true;
      // move character to fridge marker position
      double movementDelay =
          GameState.goTo(fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);

      Runnable openFridgeRunnable =
          () -> {
            System.out.println("open fridge clicked");
            // put notification in chat box
            SharedElements.appendChat("There's nothing left in the fridge.");
            // allow character to move again
            moving = false;
          };
      GameState.delayRun(openFridgeRunnable, movementDelay);
    }
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
      // check if character is already moving to prevent multiple movements
      if (!moving) {
        moving = true;
        // move character to fridge marker position
        double movementDelay =
            GameState.goTo(
                fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);
        // flag that the current puzzle is the toast puzzle
        GameState.setPuzzleToast();
        Runnable closedFridgeRunnable =
            () -> {
              // change fridge image to open fridge
              fridgeClosed.setVisible(false);
              fridgeOpen.setVisible(true);
              // change floor hitbox to account for open fridge
              floor.setImage(new Image("images/Kitchen/floorfridgeopen.png"));
              // flag that the user has bread
              GameState.hasBread = true;
              // add bread to inventory
              GameState.addItem(GameState.Items.BREAD_UNTOASTED);
              // put notification in chat box
              SharedElements.appendChat("You find a stale loaf of bread in the fridge.");
              System.out.println("closed fridge clicked");
              // allow character to move again
              moving = false;
            };
        GameState.delayRun(closedFridgeRunnable, movementDelay);
      }
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
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
  }

  public void fadeIn() {
    GameState.fadeIn(room);
  }
}
