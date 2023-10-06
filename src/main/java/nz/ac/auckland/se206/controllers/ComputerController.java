package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
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
  @FXML private ImageView printButton;
  @FXML private ImageView printHighlight;
  @FXML private Text printingMessage;
  @FXML private Text finishedPrinting;

  private ChatCompletionRequest chatCompletionRequest;

  public void initialize() throws ApiProxyException {
    // Initialization code goes here
    GameState.scaleToScreen(contentPane);

    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(100);
    runGpt(
        new ChatMessage(
            "user",
            GptPromptEngineering.getRiddle(
                GameState.getRiddleAnswer(), GameState.getSecondDigits())));
  }

  @FXML
  private void print() {
    GameState.playSound("/sounds/printer.m4a");
    // flag that the paper has been printed from the computer
    SharedElements.printPaper();

    // show the print button
    printButton.setOpacity(1);
    // disable the print hightlight
    printHighlight.disableProperty().set(true);
    // show the printing message below
    printingMessage.setOpacity(1);

    // hide the printing text after 2 seconds
    // show the finished printing icon
    Runnable printing =
        () -> {
          printingMessage.setOpacity(0);
          finishedPrinting.setOpacity(1);
        };

    GameState.delayRun(printing, 2);
  }

  @FXML
  private void hoverPrinter() {
    printHighlight.setOpacity(1);
  }

  @FXML
  private void unhoverPrinter() {
    printHighlight.setOpacity(0);
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
    enterButton.setDisable(true);

    Task<ChatMessage> gptTask =
        new Task<ChatMessage>() {
          @Override
          protected ChatMessage call() throws Exception {
            try {
              // get response from gpt
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();

              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());
              GameState.setRiddle(result.getChatMessage().getContent());

              // update UI when thread is done
              Platform.runLater(
                  () -> {
                    enterButton.setDisable(false);
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
            System.out.println("Riddle solved");
            // flag that riddle has been solved and hints are no longer needed
            GameState.computerPuzzleHints = false;

            // Load prompt to congratulate user on solving riddle
            try {
              GameState.runGpt(new ChatMessage("user", GptPromptEngineering.solveRiddle()), false);
            } catch (ApiProxyException e) {
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
    // get message from text field
    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      // end method if message is empty
      return;
    }
    // clear text field
    inputText.clear();
    // create chat message and append it to the text area
    ChatMessage msg = new ChatMessage("user", message);
    appendChatMessage(msg);
    // send message to gpt
    runGpt(msg);
  }
}
