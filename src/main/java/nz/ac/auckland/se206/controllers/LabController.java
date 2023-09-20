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
  @FXML private HBox dialogueHBox;
  @FXML private VBox bottomVBox;
  @FXML private ImageView neutralAi;
  @FXML private ImageView loadingAi;
  @FXML private ImageView talkingAi;
  @FXML private ImageView doorGlow;

  private boolean moving = false;
  private double startX = 1300;
  private double startY = 630;

  public void initialize() {
    // Initialization code goes here
    dialogueHBox.getChildren().add(SharedElements.getDialogueBox());
    bottomVBox.getChildren().add(SharedElements.getTaskBarBox());
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
    if (!moving) {
      GameState.movementEvent(event, room);
      double mouseX = event.getX();
      double mouseY = event.getY();
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

    try {
      if (!moving) {
        moving = true;
        double movementDelay = GameState.goTo(360, 680, character, running);
        Runnable goToPrinter =
            () -> {
              System.out.println("Printer clicked");

              if (SharedElements.isPaperPrinted() == false) {
                SharedElements.appendChat("Printer is empty!");
              } else {
                SharedElements.appendChat("There is a printed piece of paper, you take it.");
                GameState.addItem(GameState.Items.PAPER);
                SharedElements.takePaper();
                // Load prompt to congratulate user on printing paper
                try {
                  GameState.runGpt(new ChatMessage("user", GptPromptEngineering.printPaper()));
                } catch (ApiProxyException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
              }

              moving = false;
            };

        GameState.delayRun(goToPrinter, movementDelay);
      }
    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
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
}
