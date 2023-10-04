package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Controller class for the keypad. */
public class KeypadController {
  @FXML private AnchorPane contentPane;
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

  private String code = "";
  private ChatCompletionRequest chatCompletionRequest;

  public void initialize() {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);
  }

  // Implement keypad functionality
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

  /** Deletes the last entered digit of the code. */
  @FXML
  private void onDeleteClicked() {
    if (code.length() > 0) {
      code = code.substring(0, code.length() - 1);
      codeText.setText(code);
    }
  }

  /**
   * Checks if the code is correct.
   *
   * @throws ApiProxyException
   */
  @FXML
  private void onEnterClicked() throws ApiProxyException {
    System.out.println(code);
    System.out.println("Enter clicked");

    // Check if the code is correct
    if (code.equals(GameState.code)) {
      // load the ai text for ending
      chatCompletionRequest =
          new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
      endingGpt(new ChatMessage("user", GptPromptEngineering.endingCongrats()));

      codeText.setText("Unlocked");
      GameState.isExitUnlocked = true;
    } else {
      codeText.setText("Incorrect");

      Timeline timeline =
          new Timeline(
              new KeyFrame(
                  Duration.seconds(1.5),
                  event -> {
                    codeText.setText(""); // reset the text
                    codeText.clear(); // clear the input field
                  }));

      timeline.setCycleCount(1);
      timeline.play();

      // reset code
      code = "";
    }
  }

  /** Returns to the control room screen when exit button clicked. */
  @FXML
  private void onExitClicked() throws IOException {
    System.out.println("Exit clicked");

    App.setRoot(AppUi.CONTROL_ROOM);
  }

  /**
   * Appends the input number to the text field if the code is less than 3 digits long.
   *
   * @param number the number to be appended
   */
  private void appendNumber(String number) {
    if (code.length() < 6 && !codeText.getText().equals("Incorrect")) {
      codeText.appendText(number);
      code += number;
    }
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  public ChatMessage endingGpt(ChatMessage msg) throws ApiProxyException {

    if (!GameState.isUsbEnding) {
      chatCompletionRequest.addMessage(msg);

      // task for gpt chat generation
      Task<ChatMessage> gptTask =
          new Task<ChatMessage>() {

            @Override
            protected ChatMessage call() throws Exception {
              System.out.println("Get message: " + Thread.currentThread().getName());

              try {
                // get response from gpt
                ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

                Choice result = chatCompletionResult.getChoices().iterator().next();
                chatCompletionRequest.addMessage(result.getChatMessage());

                if (GameState.endingCongrats.equals("") && GameState.endingReveal.equals("")) {
                  GameState.endingCongrats = result.getChatMessage().getContent();
                  System.out.println(GameState.endingCongrats);
                } else if (GameState.endingReveal.equals("")
                    && !GameState.endingCongrats.equals("")) {
                  GameState.endingReveal = result.getChatMessage().getContent();
                  System.out.println(GameState.endingReveal);
                }

                return result.getChatMessage();
              } catch (ApiProxyException e) {
                System.out.println("Error with GPT");
                return null;
              }
            }
          };

      gptTask.setOnSucceeded(
          event -> {
            // get next message
            if (GameState.endingReveal.equals("")) {
              try {
                endingGpt(new ChatMessage("user", GptPromptEngineering.endingReveal()));
              } catch (ApiProxyException e) {
                System.out.println("Error with GPT");
              }
            }
          });

      // starts the task on a separate thread
      Thread gptThread = new Thread(gptTask, "Ending Thread");
      gptThread.start();

      return gptTask.getValue();
    } else {
      chatCompletionRequest.addMessage(msg);

      // task for gpt chat generation
      Task<ChatMessage> gptTask =
          new Task<ChatMessage>() {

            @Override
            protected ChatMessage call() throws Exception {
              System.out.println("Get message: " + Thread.currentThread().getName());

              try {
                // get response from gpt
                ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

                Choice result = chatCompletionResult.getChoices().iterator().next();
                chatCompletionRequest.addMessage(result.getChatMessage());

                if (GameState.endingCongrats.equals("") && GameState.usbEndingReveal.equals("")) {
                  GameState.endingCongrats = result.getChatMessage().getContent();
                  System.out.println(GameState.endingCongrats);
                } else if (GameState.usbEndingReveal.equals("")
                    && !GameState.endingCongrats.equals("")) {
                  GameState.usbEndingReveal = result.getChatMessage().getContent();
                  System.out.println(GameState.usbEndingReveal);
                }

                return result.getChatMessage();
              } catch (ApiProxyException e) {
                System.out.println("Error with GPT");
                return null;
              }
            }
          };

      gptTask.setOnSucceeded(
          event -> {
            // get next message
            if (GameState.usbEndingReveal.equals("")) {
              try {
                endingGpt(new ChatMessage("user", GptPromptEngineering.usbEndingReveal()));
              } catch (ApiProxyException e) {
                System.out.println("Error with GPT");
              }
            }
          });

      // starts the task on a separate thread
      Thread gptThread = new Thread(gptTask, "Ending Thread");
      gptThread.start();

      return gptTask.getValue();
    }
  }
}
