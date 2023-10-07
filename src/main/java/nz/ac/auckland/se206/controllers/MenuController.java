package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class MenuController {

  @FXML private Label creditsLabel;
  @FXML private Label errorLabel;
  @FXML private AnchorPane contentPane;
  private boolean creditsVisible;
  private int difficultyValue;
  private int timeValue;

  public void initialize() {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);
    creditsVisible = false;
    difficultyValue = -5;
    timeValue = 0;
    System.out.println(difficultyValue);
    System.out.println(timeValue);
  }

  @FXML
  private void onEasySelected() {
    difficultyValue = -1;
    System.out.println(difficultyValue);
  }

  @FXML
  private void onMediumSelected(ActionEvent event) {
    difficultyValue = 5;
    System.out.println(difficultyValue);
  }

  @FXML
  private void onHardSelected(ActionEvent event) {
    difficultyValue = 0;
    System.out.println(difficultyValue);
  }

  @FXML
  private void onTwoSelected(ActionEvent event) {
    timeValue = 120;
    System.out.println(timeValue);
  }

  @FXML
  private void onFourSelected(ActionEvent event) {
    timeValue = 240;
    System.out.println(timeValue);
  }

  @FXML
  private void onSixSelected(ActionEvent event) {
    timeValue = 360;
    System.out.println(timeValue);
  }

  @FXML
  private void onCredits(ActionEvent event) {
    creditsLabel.setVisible(!creditsVisible);
    creditsVisible = !creditsVisible;
  }

  @FXML
  private void onStartGame(ActionEvent event) throws IOException, ApiProxyException {

    // show error message if level or time not selected
    if (difficultyValue == -5 || timeValue == 0) {
      errorLabel.setVisible(true);

      // hide error message
      Duration duration = Duration.seconds(3);
      KeyFrame keyFrame = new KeyFrame(duration, e -> errorLabel.setVisible(false));
      Timeline timeline = new Timeline(keyFrame);
      timeline.play();

      return;
    }

    System.out.println("time" + timeValue);
    GameState.newGame();
    SharedElements.newGame();
    GameState.setDifficulty(difficultyValue);
    GameState.setTime(timeValue);

    // get controller for control room
    FXMLLoader controlLoader = new FXMLLoader(getClass().getResource("/fxml/controlRoom.fxml"));
    Parent controlRoom = controlLoader.load();
    App.controlRoomController = controlLoader.getController();

    // get controller for lab
    FXMLLoader labLoader = new FXMLLoader(getClass().getResource("/fxml/lab.fxml"));
    Parent lab = labLoader.load();
    App.labController = labLoader.getController();

    // get controller for kitchen
    FXMLLoader kitchenLoader = new FXMLLoader(getClass().getResource("/fxml/kitchen.fxml"));
    Parent kitchen = kitchenLoader.load();
    App.kitchenController = kitchenLoader.getController();

    // load all other scenes
    SceneManager.addUi(AppUi.ENDING, App.loadFxml("ending"));
    SceneManager.addUi(AppUi.KITCHEN, kitchen);
    SceneManager.addUi(AppUi.CONTROL_ROOM, controlRoom);
    SceneManager.addUi(AppUi.LAB, lab);

    try {
      GameState.startTimer();

      // choose prompt according to difficulty
      if (difficultyValue == -1 || difficultyValue == 5) {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.introStringHints()), false);
      } else {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.introStringNoHints()), false);
      }

      App.setRoot(AppUi.CONTROL_ROOM);

    } catch (Exception e) {
      System.out.println("Error");
      e.printStackTrace();
    }
  }

  @FXML
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
  }
}
