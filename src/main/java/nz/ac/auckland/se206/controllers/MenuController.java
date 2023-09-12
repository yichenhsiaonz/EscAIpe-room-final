package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;

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
      // TODO LOAD THE RIGHT ROOM
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
