package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;

/** Controller class for the room view. */
public class RoomController {

  @FXML private AnchorPane contentPane;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private Pane room;
  @FXML private Label timerLabel;
  @FXML private ProgressBar timerProgressBar;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {

    RoomFramework.scaleToScreen(contentPane);

    timerProgressBar.progressProperty().bind(GameState.timerTask.progressProperty());
    timerLabel.textProperty().bind(GameState.timerTask.messageProperty());

    // Set the initial position of the character within the Pane
    character.setLayoutX(0); // Initial X position
    character.setLayoutY(0); // Initial Y position

    // Set the dimensions of the character
    character.setFitWidth(50); // Width of character image
    character.setFitHeight(50); // Height of character image

    // Set the initial position of the running gif within the Pane
    running.setLayoutX(0); // Initial X position
    running.setLayoutY(0); // Initial Y position

    // Set the dimensions of the running gif
    running.setFitWidth(50); // Width of running gif
    running.setFitHeight(50); // Height of running gif
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
  private void consumeMouseEvent(MouseEvent event) {
    event.consume();
  }

  /**
   * Handles the mouse click event on the room, moving the character to the clicked location.
   *
   * @param event the mouse event
   */
  @FXML
  public void onMoveCharacter(MouseEvent event) {

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
    RoomFramework.goTo(mouseX, mouseY, character, running);
  }

  /**
   * Displays a dialog box with the given title, header text, and message.
   *
   * @param title the title of the dialog box
   * @param headerText the header text of the dialog box
   * @param message the message content of the dialog box
   */
  private void showDialog(String title, String headerText, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Handles the click event on the door.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickDoor(MouseEvent event) throws IOException {
    System.out.println("door clicked");

    if (!GameState.isRiddleResolved) {
      showDialog("Info", "Riddle", "You need to resolve the riddle!");
      App.setRoot(AppUi.CHAT);
      return;
    }

    if (!GameState.isKeyFound) {
      showDialog(
          "Info", "Find the key!", "You resolved the riddle, now you know where the key is.");
    } else {
      showDialog("Info", "You Won!", "Good Job!");
    }
  }

  /**
   * Handles the click event on the vase.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickVase(MouseEvent event) {
    System.out.println("vase clicked");
    if (GameState.isRiddleResolved && !GameState.isKeyFound) {
      showDialog("Info", "Key Found", "You found a key under the vase!");
      GameState.isKeyFound = true;
    }
  }

  /**
   * Handles the click event on the window.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickWindow(MouseEvent event) {
    System.out.println("window clicked");
  }
}
