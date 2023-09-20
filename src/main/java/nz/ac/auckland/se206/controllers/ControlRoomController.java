package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
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

/** Controller class for the Control Room. */
public class ControlRoomController {
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
  @FXML private Pane room;
  @FXML private HBox dialogueHorizontalBox;
  @FXML private VBox bottomVerticalBox;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private Circle rightDoorMarker;
  @FXML private Circle leftDoorMarker;
  @FXML private Circle computerMarker;
  @FXML private Circle keypadMarker;
  @FXML private Circle centerDoorMarker;

  private boolean moving = false;

  /** Initializes the control room. */
  public void initialize() {
    // Initialization code goes here
    dialogueHorizontalBox.getChildren().add(SharedElements.getDialogueBox());
    bottomVerticalBox.getChildren().add(SharedElements.getTaskBarBox());
    SharedElements.incremnetLoadedScenes();
    GameState.scaleToScreen(contentPane);

    GameState.goToInstant(
        centerDoorMarker.getLayoutX(), centerDoorMarker.getLayoutY(), character, running);
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickComputer(MouseEvent event) throws IOException {
    System.out.println("computer clicked");
    try {
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                computerMarker.getLayoutX(), computerMarker.getLayoutY(), character, running);
        Runnable accessComputer =
            () -> {
              try {
                running.setOpacity(0);
                character.setOpacity(1);
                GameState.setPuzzleComputer();
                App.setRoot(AppUi.COMPUTER);
              } catch (IOException e) {
                e.printStackTrace();
              }
              moving = false;
            };

        GameState.delayRun(accessComputer, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  // add glow highlight to computer when hover
  @FXML
  public void onComputerHovered(MouseEvent event) {
    computerGlow.setVisible(true);
  }

  @FXML
  public void onComputerUnhovered(MouseEvent event) {
    computerGlow.setVisible(false);
  }

  /**
   * Handles the click event on the exit door.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickExit(MouseEvent event) {
    consumeMouseEvent(event);

    try {
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                centerDoorMarker.getLayoutX(), centerDoorMarker.getLayoutY(), character, running);
        Runnable leaveRoom =
            () -> {
              running.setOpacity(0);
              character.setOpacity(1);
              System.out.println("exit door clicked");
              if (GameState.isExitUnlocked) {
                fadeBlack();
              } else {
                SharedElements.appendChat("The exit is locked and will not budge.");
              }
              moving = false;
            };

        GameState.delayRun(leaveRoom, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  // add glow highlight to exit door when hover
  @FXML
  public void onExitHovered(MouseEvent event) {
    exitGlow.setVisible(true);
  }

  @FXML
  public void onExitUnhovered(MouseEvent event) {
    exitGlow.setVisible(false);
  }

  /**
   * Handles the click event on the keypad.
   *
   * @param event the mouse event
   * @throws IOException
   */
  @FXML
  public void clickKeypad(MouseEvent event) throws IOException {
    System.out.println("keypad clicked");
    try {
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                keypadMarker.getLayoutX(), keypadMarker.getLayoutY(), character, running);
        Runnable accessKeypad =
            () -> {
              try {
                running.setOpacity(0);
                character.setOpacity(1);
                App.setRoot(AppUi.KEYPAD);
              } catch (IOException e) {
                e.printStackTrace();
              }
              moving = false;
            };

        GameState.delayRun(accessKeypad, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  // add glow highlight to keypad when hover
  @FXML
  public void onKeypadHovered(MouseEvent event) {
    keypadGlow.setVisible(true);
  }

  @FXML
  public void onKeypadUnhovered(MouseEvent event) {
    keypadGlow.setVisible(false);
  }

  /**
   * Handles the click event on the right arrow.
   *
   * @param event the mouse event
   */
  @FXML
  public void onRightClicked(MouseEvent event) throws IOException {
    consumeMouseEvent(event);

    try {
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                rightDoorMarker.getLayoutX(), rightDoorMarker.getLayoutY(), character, running);
        Runnable leaveRoom =
            () -> {
              try {
                running.setOpacity(0);
                character.setOpacity(1);
                App.setRoot(AppUi.KITCHEN);
              } catch (IOException e) {
                e.printStackTrace();
              }
              moving = false;
            };

        GameState.delayRun(leaveRoom, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  // add glow highlight to right arrow when hover
  @FXML
  public void onRightHovered(MouseEvent event) {
    rightGlowArrow.setVisible(true);
  }

  @FXML
  public void onRightUnhovered(MouseEvent event) {
    rightGlowArrow.setVisible(false);
  }

  /**
   * Handles the click event on the left arrow.
   *
   * @param event the mouse event
   */
  @FXML
  public void onLeftClicked(MouseEvent event) {
    consumeMouseEvent(event);

    try {
      if (!moving) {
        moving = true;
        double movementDelay =
            GameState.goTo(
                leftDoorMarker.getLayoutX(), leftDoorMarker.getLayoutY(), character, running);
        Runnable leaveRoom =
            () -> {
              try {
                running.setOpacity(0);
                character.setOpacity(1);
                App.setRoot(AppUi.LAB);
              } catch (IOException e) {
                e.printStackTrace();
              }
              moving = false;
            };

        GameState.delayRun(leaveRoom, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  // add glow highlight to left arrow when hover
  @FXML
  public void onLeftHovered(MouseEvent event) {
    leftGlowArrow.setVisible(true);
  }

  @FXML
  public void onLeftUnhovered(MouseEvent event) {
    leftGlowArrow.setVisible(false);
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
      moving = true;
      GameState.onCharacterMovementClick(event, room);
      double mouseX = event.getX();
      double mouseY = event.getY();

      double movementDelay = GameState.goTo(mouseX, mouseY, character, running);
      Runnable resumeMoving =
          () -> {
            moving = false;
          };
      GameState.delayRun(resumeMoving, movementDelay);
    }
  }

  public void fadeBlack() {
    // Create a black rectangle that covers the entire AnchorPane
    AnchorPane anchorPane = (AnchorPane) dialogueHorizontalBox.getParent();
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
    App.setRoot(AppUi.MENU);
  }

  @FXML
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
  }
}
