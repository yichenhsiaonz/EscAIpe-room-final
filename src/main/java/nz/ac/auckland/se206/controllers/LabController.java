package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
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
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;

public class LabController {
  @FXML private AnchorPane contentPane;
  @FXML private Rectangle printer;
  @FXML private Label timerLabel;
  @FXML private ProgressBar timerProgressBar;
  @FXML private ImageView rightArrow;
  @FXML private ImageView rightGlowArrow;
  @FXML private ImageView printerGlow;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private Pane room;
  @FXML private TextArea chatBox;
  @FXML private TextField messageBox;
  @FXML private Button sendMessage;

  private boolean moving = false;
  double startX = 1400;
  double startY = 900;

  public void initialize() {
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

    running.setScaleX(-1);
    character.setScaleX(-1);
    RoomFramework.goToInstant(startX, startY, character, running);

    chatBox.textProperty().bind(GameState.chatTextProperty());
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

    consumeMouseEvent(event);

    try {
      if (!moving) {
        double movementDelay = RoomFramework.goTo(660, 900, character, running);
        Runnable goToPrinter =
            () -> {
              System.out.println("Printer clicked");
              moving = false;
            };

        RoomFramework.delayRun(goToPrinter, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }

    GameState.isPaperPrinted = true;

    // Load prompt to congratulate user on printing paper
    GameState.setChatCompletionRequest(
      new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100));  
    try {
      GameState.runGpt(new ChatMessage("user", GptPromptEngineering.printPaper()));
    } catch (ApiProxyException e) {
      // TODO Auto-generated catch block
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
      App.setRoot(AppUi.CONTROL_ROOM);
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
