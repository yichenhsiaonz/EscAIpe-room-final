package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class MenuController {

  @FXML private Label creditsLabel;
  @FXML private AnchorPane contentPane;
  @FXML private boolean creditsVisible;
  @FXML private int difficulty;
  @FXML private int time;

  public void initialize() {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);
    creditsVisible = false;
    difficulty = 5;
    time = 240;
    System.out.println(difficulty);
    System.out.println(time);
  }

  @FXML
  private void onEasySelected() {
    difficulty = -1;
    System.out.println(difficulty);
  }

  @FXML
  private void onMediumSelected(ActionEvent event) {
    difficulty = 5;
    System.out.println(difficulty);
  }

  @FXML
  private void onHardSelected(ActionEvent event) {
    difficulty = 0;
    System.out.println(difficulty);
  }

  @FXML
  private void onTwoSelected(ActionEvent event) {
    time = 120;
    System.out.println(time);
  }

  @FXML
  private void onFourSelected(ActionEvent event) {
    time = 240;
    System.out.println(time);
  }

  @FXML
  private void onSixSelected(ActionEvent event) {
    time = 360;
    System.out.println(time);
  }

  @FXML
  private void onCredits(ActionEvent event) {
    creditsLabel.setVisible(!creditsVisible);
    creditsVisible = !creditsVisible;
  }

  @FXML
  private void onStartGame(ActionEvent event) throws IOException, ApiProxyException {
    System.out.println("time" + time);
    GameState.newGame();
    SharedElements.newGame();
    GameState.setDifficulty(difficulty);
    GameState.setTime(time);

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
    SceneManager.addUi(AppUi.COMPUTER, App.loadFxml("computer"));
    SceneManager.addUi(AppUi.KEYPAD, App.loadFxml("keypad"));
    SceneManager.addUi(AppUi.LAB, lab);

    try {
      GameState.startTimer();

      // choose prompt according to difficulty
      if (difficulty == -1 || difficulty == 5) {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.introStringHints()));
      } else {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.introStringNoHints()));
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
