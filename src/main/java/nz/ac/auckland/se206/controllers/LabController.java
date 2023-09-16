package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
  @FXML private ImageView rightArrow;
  @FXML private ImageView rightGlowArrow;
  @FXML private ImageView printerGlow;
  @FXML private ImageView character;
  @FXML private ImageView running;
  @FXML private Pane room;
  @FXML private HBox dialogueHBox;
  @FXML private VBox bottomVBox;
  private boolean isPaperPrinted = false;
  private boolean moving = false;
  double startX = 1400;
  double startY = 900;

  public void initialize() {
    // Initialization code goes here
    dialogueHBox.getChildren().add(SharedElements.getDialogueBox());
    bottomVBox.getChildren().add(SharedElements.getTaskBarBox());
    SharedElements.incremnetLoadedScenes();
    GameState.scaleToScreen(contentPane);

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
    GameState.goToInstant(startX, startY, character, running);
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
      double movementDelay = GameState.goTo(mouseX, mouseY, character, running);
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

    consumeMouseEvent(event);

    try {
      if (!moving) {
        moving = true;
        double movementDelay = GameState.goTo(660, 900, character, running);
        Runnable goToPrinter =
            () -> {
              System.out.println("Printer clicked");

              if (isPaperPrinted) {
                SharedElements.appendChat("You already printed the paper.");
              } else {
                SharedElements.appendChat("A print queued up.");
                SharedElements.appendChat("You allow the print to go through.");
                GameState.addItem(GameState.Items.PAPER);
                isPaperPrinted = true;
              }

              moving = false;
            };

        GameState.delayRun(goToPrinter, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }

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

    consumeMouseEvent(event);

    try {
      if (!moving) {
        moving = true;
        double movementDelay = GameState.goTo(startX, startY, character, running);
        Runnable leaveRoom =
            () -> {
              try {
                character.setScaleX(-1);
                running.setScaleX(-1);
                running.setOpacity(0);
                App.setRoot(AppUi.CONTROL_ROOM);
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

  @FXML
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
  }
}
