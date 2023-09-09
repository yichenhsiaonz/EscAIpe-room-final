package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class ControlRoomController {
  @FXML private Rectangle computer;
  @FXML private Rectangle keypad;
  @FXML private Rectangle exitDoor;

  public void initialize() {
    // Initialization code goes here
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   * @throws IOException if there is an error loading the chat view
   */
  @FXML
  public void clickComputer(MouseEvent event) throws IOException {
    System.out.println("computer clicked");

    App.setRoot(AppUi.COMPUTER);
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickExit(MouseEvent event) {
    System.out.println("exit door clicked");
  }

  /**
   * Handles the click event on the computer.
   *
   * @param event the mouse event
   */
  @FXML
  public void clickKeypad(MouseEvent event) {
    System.out.println("keypad clicked");
  }
}
