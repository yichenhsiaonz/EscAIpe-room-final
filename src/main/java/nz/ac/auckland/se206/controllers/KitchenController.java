package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for the room view. */
public class KitchenController {

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
  @FXML private HBox dialogueHBox;
  @FXML private VBox bottomVBox;
  @FXML private Circle doorMarker;
  @FXML private Circle toasterMarker;
  @FXML private Circle fridgeMarker;
  @FXML private Pane room;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;

  private boolean moving = false;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    HBox bottom = SharedElements.getTaskBarBox();
    VBox dialogue = SharedElements.getDialogueBox();
    SharedElements.incremnetLoadedScenes();
    dialogueHBox.getChildren().addAll(dialogue);
    bottomVBox.getChildren().addAll(bottom);
    bottom.toFront();
    dialogue.toFront();

    GameState.scaleToScreen(contentPane);

    // get door marker position
    int doorMarkerX = (int) doorMarker.getLayoutX();
    int doorMarkerY = (int) doorMarker.getLayoutY();

    // Set the initial position of the character within the Pane
    character.setLayoutX(0); // Initial X position
    character.setLayoutY(0); // Initial Y position

    // Set the dimensions of the character
    character.setFitWidth(150); // Width of character image
    character.setFitHeight(150); // Height of character image

    // Set the initial position of the running gif within the Pane
    running.setLayoutX(0); // Initial X position
    running.setLayoutY(0); // Initial Y position

    // Set the dimensions of the running gif
    running.setFitWidth(150); // Width of running gif
    running.setFitHeight(150); // Height of running gif

    GameState.goToInstant(doorMarkerX, doorMarkerY, character, running);
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
    if (!moving) {

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
      moving = true;
      double movementDelay = GameState.goTo(mouseX, mouseY, character, running);
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
    if (!moving) {
      moving = true;
      double movementDelay =
          GameState.goTo(doorMarker.getLayoutX(), doorMarker.getLayoutY(), character, running);
      Runnable leaveRoom =
          () -> {
            try {
              running.setOpacity(0);
              App.setRoot(AppUi.CONTROL_ROOM);
            } catch (IOException e) {
              e.printStackTrace();
            }
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
    if (!moving) {
      moving = true;
      double movementDelay =
          GameState.goTo(
              toasterMarker.getLayoutX(), toasterMarker.getLayoutY(), character, running);
      GameState.setPuzzleToast();
      if (GameState.hasBread && !GameState.hasToast) {
        Runnable putInToast =
            () -> {
              System.out.println("toaster clicked");
              GameState.removeItem(GameState.Items.BREAD_UNTOASTED);
              SharedElements.appendChat("You put a slice of bread in the toaster");
              GameState.hasBread = false;
            };
        Runnable waitForToast =
            () -> {
              SharedElements.appendChat("Sparks fly out of the toaster as it toasts the bread");
            };
        Runnable ToastFinish =
            () -> {
              System.out.println("toaster clicked");
              GameState.addItem(GameState.Items.BREAD_TOASTED);
              SharedElements.appendChat("A charred slice of toast pops out of the toaster");
              // Load prompt to congratulate user on toasting bread
              try {
                GameState.runGpt(new ChatMessage("user", GptPromptEngineering.toastBread()));
              } catch (ApiProxyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              GameState.hasToast = true;
              GameState.toasterPuzzleHints = false;
              moving = false;
            };

        GameState.delayRun(putInToast, movementDelay);
        GameState.delayRun(waitForToast, 2);
        GameState.delayRun(ToastFinish, 4);
      } else if (GameState.hasToast) {
        Runnable toasterRunnable =
            () -> {
              System.out.println("toaster clicked");
              SharedElements.appendChat("Looks like the toaster is toast.");
              moving = false;
            };
        GameState.delayRun(toasterRunnable, movementDelay);
      } else {
        Runnable toasterRunnable =
            () -> {
              System.out.println("toaster clicked");
              SharedElements.appendChat("This toaster looks like it's been tampered with");
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
    if (!moving) {
      moving = true;
      double movementDelay =
          GameState.goTo(fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);

      Runnable openFridgeRunnable =
          () -> {
            running.setOpacity(0);
            System.out.println("open fridge clicked");
            SharedElements.appendChat("There's nothing left in the fridge.");
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
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                fridgeMarker.getLayoutX(), fridgeMarker.getLayoutY(), character, running);
        Runnable closedFridgeRunnable =
            () -> {
              fridgeClosed.setVisible(false);
              fridgeOpen.setVisible(true);
              GameState.hasBread = true;
              GameState.addItem(GameState.Items.BREAD_UNTOASTED);
              SharedElements.appendChat("You find a stale loaf of bread in the fridge.");
              System.out.println("closed fridge clicked");
              moving = false;
            };
        GameState.delayRun(closedFridgeRunnable, movementDelay);
      }
    } catch (Exception e) {
      // TODO: handle exception
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
}
