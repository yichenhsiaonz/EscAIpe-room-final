package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.TextToSpeechManager;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for the ending scene. */
public class EndingController {
  @FXML private AnchorPane contentPane;
  @FXML private ImageView shadowFrame;
  @FXML private TextArea textArea;
  @FXML private Button nextButton;
  @FXML private ImageView winImage;
  @FXML private Button menuButton;

  private int chatCount = 0;

  /**
   * Initializes the ending scene, loading the initial text.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  public void initialize() throws ApiProxyException {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);
    textArea.appendText("You: Finally I've unlocked the exit! But, what is that?");
  }

  /**
   * Loads the next text in the ending scene.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  private void onNextClicked() throws ApiProxyException {
    if (!GameState.isUsbEnding) {
      TextToSpeechManager.cutOff();
      if (chatCount == 0) { // show neutral AI
        textArea.clear();
        Image newImage = new Image("/images/Ending/neutral-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + GameState.endingCongrats);
        TextToSpeechManager.speak(GameState.endingCongrats);
      } else if (chatCount == 1) { // show evil AI
        textArea.clear();
        Image newImage = new Image("/images/Ending/evil-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + GameState.endingReveal);
        TextToSpeechManager.speak(GameState.endingReveal);
      } else if (chatCount == 2) { // show win screen
        // create black rectangle that covers the entire AnchorPane
        AnchorPane anchorPane = (AnchorPane) nextButton.getParent();
        AnchorPane blackRectangle = new AnchorPane();
        blackRectangle.setStyle("-fx-background-color: black;");
        blackRectangle.setOpacity(0.0);
        AnchorPane.setTopAnchor(blackRectangle, 0.0);
        AnchorPane.setBottomAnchor(blackRectangle, 0.0);
        AnchorPane.setLeftAnchor(blackRectangle, 0.0);
        AnchorPane.setRightAnchor(blackRectangle, 0.0);

        anchorPane.getChildren().add(blackRectangle);

        // create fade transition
        FadeTransition fadeToBlack = new FadeTransition(Duration.seconds(5), blackRectangle);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        // show win screen when the fade animation is complete
        fadeToBlack.setOnFinished(
            event -> {
              // remove rectangle
              anchorPane.getChildren().remove(blackRectangle);

              // clear current screen
              shadowFrame.setVisible(false);
              textArea.setVisible(false);
              nextButton.setVisible(false);

              // show win screen
              winImage.setVisible(true);
              menuButton.setVisible(true);
            });

        fadeToBlack.play();
      }

      chatCount++;
    } else {
      TextToSpeechManager.cutOff();
      if (chatCount == 0) { // show neutral AI
        textArea.clear();
        Image newImage = new Image("/images/Ending/neutral-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + GameState.endingCongrats);
        TextToSpeechManager.speak(GameState.endingCongrats);
      } else if (chatCount == 1) { // show evil AI
        textArea.clear();
        Image newImage = new Image("/images/Ending/evil-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + GameState.endingReveal);
        TextToSpeechManager.speak(GameState.endingReveal);
      } else if (chatCount == 2) {
        textArea.clear();
        Image newImage = new Image("/images/Ending/confused-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + "Wait, what is that you're holding?");
        TextToSpeechManager.speak("Wait, what is that you're holding?");
      } else if (chatCount == 3) {
        textArea.clear();
        Image newImage = new Image("/images/Ending/angry-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + GameState.usbEndingReveal);
        TextToSpeechManager.speak(GameState.usbEndingReveal);
      } else if (chatCount == 4) {
        textArea.clear();
        Image newImage = new Image("/images/Ending/dead-frame.png");
        shadowFrame.setImage(newImage);
        textArea.appendText("AI: " + "*Robot dying noises*");
        TextToSpeechManager.speak("Beep boop beep boop, MALFUNCTION, MALFUNCTION, oof");
      } else if (chatCount == 5) { // show win screen
        // create black rectangle that covers the entire AnchorPane
        AnchorPane anchorPane = (AnchorPane) nextButton.getParent();
        AnchorPane blackRectangle = new AnchorPane();
        blackRectangle.setStyle("-fx-background-color: black;");
        blackRectangle.setOpacity(0.0);
        AnchorPane.setTopAnchor(blackRectangle, 0.0);
        AnchorPane.setBottomAnchor(blackRectangle, 0.0);
        AnchorPane.setLeftAnchor(blackRectangle, 0.0);
        AnchorPane.setRightAnchor(blackRectangle, 0.0);

        anchorPane.getChildren().add(blackRectangle);

        // create fade transition
        FadeTransition fadeToBlack = new FadeTransition(Duration.seconds(5), blackRectangle);
        fadeToBlack.setFromValue(0.0);
        fadeToBlack.setToValue(1.0);

        // show win screen when the fade animation is complete
        fadeToBlack.setOnFinished(
            event -> {
              // remove rectangle
              anchorPane.getChildren().remove(blackRectangle);

              // clear current screen
              shadowFrame.setVisible(false);
              textArea.setVisible(false);
              nextButton.setVisible(false);

              // show win screen
              winImage.setVisible(true);
              menuButton.setVisible(true);
            });

        fadeToBlack.play();
      }

      chatCount++;
    }
  }

  /** Returns to the main menu. */
  @FXML
  private void onMenuClicked() throws IOException {
    System.out.println("return to menu");
    App.setRoot(AppUi.MENU);
  }
}
