package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;

public class MenuController {

  @FXML private Label creditsLabel;
  @FXML private AnchorPane contentPane;
  private boolean creditsVisible = false;
  private int difficulty = 2;
  private int time = 240;

  public void initialize() {
    // Initialization code goes here
    RoomFramework.scaleToScreen(contentPane);
    System.out.println(difficulty);
    System.out.println(time);
  }

  @FXML
  private void onEasySelected() {
    difficulty = 1;
    System.out.println(difficulty);
  }

  @FXML
  private void onMediumSelected(ActionEvent event) {
    difficulty = 2;
    System.out.println(difficulty);
  }

  @FXML
  private void onHardSelected(ActionEvent event) {
    difficulty = 3;
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
  private void onStartGame(ActionEvent event) throws IOException {
    GameState.setDifficulty(difficulty);
    GameState.setTime(time);

    try {
      new Thread(GameState.timerTask).start();

      GameState.setChatCompletionRequest(new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(10));

      // Load prompt according to difficulty
      if (GameState.getDifficulty() == 1) {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.easyDifficulty()));
      } else if (GameState.getDifficulty() == 2) {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.mediumDifficulty()));
      } else if (GameState.getDifficulty() == 3) {
        GameState.runGpt(new ChatMessage("user", GptPromptEngineering.hardDifficulty()));
      }
      App.setRoot(AppUi.CONTROL_ROOM);

    } catch (Exception e) {
      // TODO handle exception appropriately
      System.out.println("Error");
    }
  }

  @FXML
  private void onQuitGame(ActionEvent event) {
    System.exit(0);
  }
}
