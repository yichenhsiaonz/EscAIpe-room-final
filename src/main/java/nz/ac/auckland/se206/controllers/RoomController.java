package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;

/** Controller class for the room view. */
public class RoomController {

  @FXML private Rectangle door;
  @FXML private Rectangle window;
  @FXML private Rectangle vase;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private Pane room;

  /** Initializes the room view, it is called when the room loads. */
  public void initialize() {
    // Initialization code goes here

    // Set the initial position of the character within the Pane
    character.setLayoutX(0); // Initial X position
    character.setLayoutY(0); // Initial Y position

    // Set the dimensions of the character
    character.setFitWidth(50); // Width of character image
    character.setFitHeight(50); // Height of character image

    // Set the initial position of the character within the Pane
    running.setLayoutX(0); // Initial X position
    running.setLayoutY(0); // Initial Y position

    // Set the dimensions of the character
    running.setFitWidth(50); // Width of character image
    running.setFitHeight(50); // Height of character image
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

  @FXML
  private void consumeMouseEvent(MouseEvent event) {
    event.consume();
  }

  @FXML
  public void moveCharacter(MouseEvent event) {
    System.out.println("character moved");

    double mouseX = event.getX();
    double mouseY = event.getY();

    System.out.println("Mouse X: " + mouseX + " Mouse Y: " + mouseY);

    // Create a circle for the click animation
    Circle clickCircle = new Circle(5); // Adjust the radius as needed
    clickCircle.setFill(Color.BLUE); // Set the color of the circle
    clickCircle.setCenterX(mouseX);
    clickCircle.setCenterY(mouseY);

    // Add the circle to your root pane (replace 'yourRootPane' with your actual pane)
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

    // Retrieve the character's width and height using fitWidth and fitHeight
    double characterWidth = character.getFitWidth();
    double characterHeight = character.getFitHeight();

    // Calculate the character's new position relative to the Pane
    double characterX = mouseX - characterWidth / 2; // Adjust for character's width
    double characterY = mouseY - characterHeight / 2; // Adjust for character's height

    System.out.println("Character X: " + characterX + " Character Y: " + characterY);

    // Calculate the distance the character needs to move
    double distanceToMove =
        Math.sqrt(
            Math.pow(characterX - character.getTranslateX(), 2)
                + Math.pow(characterY - character.getTranslateY(), 2));

    // Define a constant speed
    double constantSpeed = 300; // Adjust this value as needed

    // Calculate the duration based on constant speed and distance
    double durationSeconds = distanceToMove / constantSpeed;

    // Create a TranslateTransition to smoothly move the character
    TranslateTransition transition =
        new TranslateTransition(Duration.seconds(durationSeconds), character);
    transition.setToX(characterX);
    transition.setToY(characterY);

    // Play the animation
    transition.play();

    // Create a TranslateTransition to smoothly move the "running" element
    TranslateTransition transition2 =
        new TranslateTransition(Duration.seconds(durationSeconds), running);
    transition2.setToX(characterX);
    transition2.setToY(characterY);

    running.setOpacity(1);
    // Play the animation
    transition2.play();

    transition2.setOnFinished(
        e -> {
          // Remove the "running" element from the pane when the animation is done
          running.setOpacity(0);
        });
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
      App.setRoot("chat");
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
