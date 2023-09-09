package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class ComputerController {
  @FXML private TextArea riddleTextArea;
  @FXML private TextField inputText;
  @FXML private Button enterButton;
  @FXML private Button exitButton;

  public void initialize() {
    // Initialization code goes here
  }

  /** Returns to the control room screen when exit button clicked. */
  @FXML
  private void onExitClicked() {
    try {
      App.setRoot(AppUi.CONTROL_ROOM);
    } catch (IOException e) {
      System.out.println("Error");
    }
  }
}
