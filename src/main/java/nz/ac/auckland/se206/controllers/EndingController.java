package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

/** Controller class for the ending scene. */
public class EndingController {
  @FXML private AnchorPane contentPane;
  @FXML private ImageView shadowFrame;
  @FXML private TextArea textArea;
  @FXML private Button nextButton;

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
    textArea.clear();
    if (chatCount == 0) {
      Image newImage = new Image("/images/Ending/neutral-frame.png");
      shadowFrame.setImage(newImage);
      textArea.appendText("AI: " + GameState.endingCongrats);
    } else if (chatCount == 1) {
      Image newImage = new Image("/images/Ending/evil-frame.png");
      shadowFrame.setImage(newImage);
      textArea.appendText("AI: " + GameState.endingReveal);
    }

    chatCount++;
  }
}
