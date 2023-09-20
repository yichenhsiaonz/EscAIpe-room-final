package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;

public class GameOverController {
  public static GameOverController instance;
  @FXML private AnchorPane contentPane;
  @FXML private Button menuButton;
  @FXML private Label gameOverLabel;

  public void initialize() {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);
    instance = this;
  }

  @FXML
  private void onMenuButtonClicked() throws IOException {
    App.setRoot(AppUi.MENU);
  }

  public void showGameOver() {
    gameOverLabel.setOpacity(0);
    menuButton.setOpacity(0);
    Timeline timeline1 = new Timeline();
    KeyFrame key1 =
        new KeyFrame(Duration.millis(2000), new KeyValue(gameOverLabel.opacityProperty(), 1));
    timeline1.getKeyFrames().add(key1);
    Timeline timeline2 = new Timeline();
    KeyFrame key2 =
        new KeyFrame(Duration.millis(2000), new KeyValue(menuButton.opacityProperty(), 1));
    timeline1.getKeyFrames().add(key2);
    timeline1.setOnFinished(
        e -> {
          timeline2.play();
        });

    timeline1.play();
  }
}
