package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class ControlRoomController {
  @FXML private AnchorPane contentPane;
  @FXML private Rectangle computer;
  @FXML private Rectangle keypad;
  @FXML private Rectangle exitDoor;
  @FXML private Label timerLabel;
  @FXML private ProgressBar timerProgressBar;
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
  @FXML private TextArea chatBox;
  @FXML private TextField messageBox;
  @FXML private Button sendMessage;

  private boolean moving = false;

  public void initialize() throws ApiProxyException {
    // Initialization code goes here
    RoomFramework.scaleToScreen(contentPane);

    timerProgressBar.progressProperty().bind(GameState.timerTask.progressProperty());
    timerLabel.textProperty().bind(GameState.timerTask.messageProperty());

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

    RoomFramework.goToInstant(780, 480, character, running);

    chatBox.textProperty().bind(GameState.chatTextProperty());
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

    App.setRoot(AppUi.COMPUTER);
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
    System.out.println("exit door clicked");
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

    App.setRoot(AppUi.KEYPAD);
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
    try {
      App.setRoot(AppUi.KITCHEN);
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
    try {
      App.setRoot(AppUi.LAB);
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
    System.out.println("left arrow clicked");
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

      double mouseX = event.getX();
      double mouseY = event.getY();

      System.out.println("MouseX: " + mouseX + ", MouseY: " + mouseY);

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
      double movementDelay = RoomFramework.goTo(mouseX, mouseY, character, running);
      Runnable resumeMoving =
          () -> {
            moving = false;
          };
      RoomFramework.delayRun(resumeMoving, movementDelay);
    }
  }
  
  /**
   * Sends the typed message by the user to gpt.
   *
   * @param event the mouse event
   */
  @FXML
  public void onMessageSent(ActionEvent event) throws ApiProxyException {
    String message = messageBox.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    messageBox.clear();
    ChatMessage msg = new ChatMessage("user", message);
    GameState.appendChatMessage(msg);

    GameState.runGpt(msg);
  }
}
