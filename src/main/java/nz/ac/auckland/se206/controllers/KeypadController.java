package nz.ac.auckland.se206.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class KeypadController {
  @FXML private TextField codeText;
  @FXML private Button oneButton;
  @FXML private Button twoButton;
  @FXML private Button threeButton;
  @FXML private Button fourButton;
  @FXML private Button fiveButton;
  @FXML private Button sixButton;
  @FXML private Button sevenButton;
  @FXML private Button eightButton;
  @FXML private Button nineButton;
  @FXML private Button zeroButton;
  @FXML private Button deleteButton;
  @FXML private Button enterButton;
  @FXML private Button exitButton;

  String code = "";

  public void initialize() {
    // Initialization code goes here
  }

  @FXML
  private void onOneClicked() {
    appendNumber("1");
  }

  @FXML
  private void onTwoClicked() {
    appendNumber("2");
  }

  @FXML
  private void onThreeClicked() {
    appendNumber("3");
  }

  @FXML
  private void onFourClicked() {
    appendNumber("4");
  }

  @FXML
  private void onFiveClicked() {
    appendNumber("5");
  }

  @FXML
  private void onSixClicked() {
    appendNumber("6");
  }

  @FXML
  private void onSevenClicked() {
    appendNumber("7");
  }

  @FXML
  private void onEightClicked() {
    appendNumber("8");
  }

  @FXML
  private void onNineClicked() {
    appendNumber("9");
  }

  @FXML
  private void onZeroClicked() {
    appendNumber("0");
  }

  @FXML
  private void onDeleteClicked() {
    if (code.length() > 0) {
      code = code.substring(0, code.length() - 1);
      codeText.setText(code);
    }
  }

  @FXML
  private void onEnterClicked() {
    System.out.println("Enter clicked");
    code = "";
    codeText.clear();
  }

  @FXML
  private void onExitClicked() throws IOException {
    System.out.println("Exit clicked");

    App.setRoot(AppUi.CONTROL_ROOM);
  }

  private void appendNumber(String number) {
    if (code.length() >= 3) {
      return;
    } else {
      codeText.appendText(number);
      code += number;
    }
  }
}
