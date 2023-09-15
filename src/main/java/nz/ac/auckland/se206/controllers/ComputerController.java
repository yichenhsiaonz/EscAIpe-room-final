package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.SceneManager.AppUi;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

public class ComputerController {
  @FXML private AnchorPane contentPane;
  @FXML private TextArea riddleTextArea;
  @FXML private TextField inputText;
  @FXML private Button enterButton;
  @FXML private Button exitButton;

  private ChatCompletionRequest chatCompletionRequest;

  public void initialize() throws ApiProxyException {
    // Initialization code goes here
    RoomFramework.scaleToScreen(contentPane);

    // get random number between 0 and 9
    int randomNum = (int) (Math.random() * 10);

    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
    runGpt(new ChatMessage("user", GptPromptEngineering.getRiddle(Integer.toString(randomNum))));
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

  private void appendChatMessage(ChatMessage msg) {
    riddleTextArea.appendText(msg.getRole() + ": " + msg.getContent() + "\n\n");
  }

  private ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());

              // update UI when thread is done
              Platform.runLater(
                  () -> {
                    appendChatMessage(result.getChatMessage());
                  });

              return result.getChatMessage();
            } catch (ApiProxyException e) {
              System.out.println("API error");
              return null;
            }
          }
        };

    gptTask.setOnSucceeded(
        event -> {
          ChatMessage lastMsg = gptTask.getValue();

          if (lastMsg.getRole().equals("assistant") && lastMsg.getContent().startsWith("Correct")) {
            GameState.isRiddleResolved = true;
            System.out.println("Riddle solved");

            // Load prompt to congratulate user on solving riddle
            GameState.setChatCompletionRequest(
                new ChatCompletionRequest()
                    .setN(1)
                    .setTemperature(0.2)
                    .setTopP(0.5)
                    .setMaxTokens(100));
            try {
              GameState.runGpt(new ChatMessage("user", GptPromptEngineering.solveRiddle()));
            } catch (ApiProxyException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }
        });

    // starts the task on a separate thread
    Thread gptThread = new Thread(gptTask, "Chat Thread");
    gptThread.start();

    return gptTask.getValue();
  }

  /**
   * Sends the typed message by the user to gpt.
   *
   * @param event the mouse event
   */
  @FXML
  private void onMessageSent(ActionEvent event) throws ApiProxyException, IOException {
    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      return;
    }
    inputText.clear();
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    runGpt(msg);
  }
}
