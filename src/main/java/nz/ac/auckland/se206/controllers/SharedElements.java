package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;

public class SharedElements {

  private static SharedElements instance;
  private static HBox[] taskBarHBoxList = new HBox[3];
  private static HBox[] inventoryHBoxList = new HBox[3];
  private static VBox[] dialogueBoxList = new VBox[3];

  @FXML private VBox dialogueBox;
  @FXML private HBox sendBox;
  @FXML private Label timerLabel;
  @FXML private Label powerLabel;
  @FXML private ProgressBar timerProgressBar;
  @FXML private TextField messageBox;
  @FXML private TextArea chatBox;
  @FXML private Button sendMessage;

  private int loadedScenes;

  private SharedElements() throws ApiProxyException {

    timerLabel = new Label();
    timerLabel.setFont(new javafx.scene.text.Font("System", 24));
    timerProgressBar = new ProgressBar();
    timerProgressBar.setPrefWidth(609);
    timerProgressBar.setPrefHeight(35);

    timerLabel.textProperty().bind(GameState.timerTask.messageProperty());
    timerProgressBar.progressProperty().bind(GameState.timerTask.progressProperty());

    messageBox = new TextField();
    messageBox.setPrefWidth(270);
    messageBox.setPrefHeight(25);
    chatBox = new TextArea();
    chatBox.setPrefWidth(330);
    chatBox.setPrefHeight(935);
    chatBox.setEditable(false);
    chatBox.setWrapText(true);
    chatBox.setFocusTraversable(false);

    loadedScenes = 0;
    for (int i = 0; i < 3; i++) {
      ProgressBar timerProgressBarChild = new ProgressBar();
      timerProgressBarChild.setPrefWidth(609);
      timerProgressBarChild.setPrefHeight(35);
      Label timerLabelChild = new Label();
      timerLabelChild.setFont(new javafx.scene.text.Font("System", 24));
      Label powerLabelChild = new Label();
      powerLabelChild.setText("Power Left:");
      powerLabelChild.setFont(new javafx.scene.text.Font("System", 24));

      HBox inventoryHBoxChild = new HBox();
      HBox taskBarHBoxChild = new HBox();
      HBox timerHBoxChild = new HBox();
      VBox timerVBoxChild = new VBox();

      // place progress bar and label side by side
      timerHBoxChild.getChildren().addAll(timerProgressBarChild, timerLabelChild);
      // place header above progress bar and label
      timerVBoxChild.getChildren().addAll(powerLabelChild, timerHBoxChild);
      // place timer and inventory side by side
      taskBarHBoxChild.getChildren().addAll(timerVBoxChild, inventoryHBoxChild);

      taskBarHBoxList[i] = taskBarHBoxChild;
      inventoryHBoxList[i] = inventoryHBoxChild;

      timerLabelChild.textProperty().bind(GameState.timerTask.messageProperty());
      timerProgressBarChild.progressProperty().bind(GameState.timerTask.progressProperty());

      TextField messageBoxChild = new TextField();
      messageBoxChild.setPrefWidth(270);
      messageBoxChild.setPrefHeight(25);
      TextArea chatBoxChild = new TextArea();
      chatBoxChild.setPrefWidth(330);
      chatBoxChild.setPrefHeight(935);
      chatBoxChild.setEditable(false);
      chatBoxChild.setWrapText(true);
      chatBoxChild.setFocusTraversable(false);
      chatBoxChild.textProperty().bind(chatBox.textProperty());
      Button sendMessageChild = new Button();
      sendMessageChild.setText("Submit");
      sendMessageChild.setPrefWidth(60);
      sendMessageChild.setPrefHeight(25);
      sendMessageChild.setOnAction(
          event -> {
            String message = messageBox.getText();
            chatBox.appendText("You: " + message + "\n");
            messageBox.clear();
            try {
              GameState.onMessageSent();
            } catch (ApiProxyException e) {
              e.printStackTrace();
              System.out.println("setting up error");
            }
          });

      VBox dialogueBoxChild = new VBox();
      HBox sendBoxChild = new HBox();

      // place chat box and send button side by side
      sendBoxChild.getChildren().addAll(messageBoxChild, sendMessageChild);
      // place chat box and send button below chat box
      dialogueBoxChild.getChildren().addAll(chatBoxChild, sendBoxChild);

      dialogueBoxList[i] = dialogueBoxChild;
    }
  }

  public static void newGame() throws ApiProxyException {
    instance = new SharedElements();
  }

  public static VBox getDialogueBox() {
    System.out.println("getDialogueBox");

    return dialogueBoxList[instance.loadedScenes];
  }

  public static HBox getTaskBarBox() {
    System.out.println("getTaskBarBox");
    return taskBarHBoxList[instance.loadedScenes];
  }

  public static void incremnetLoadedScenes() {
    instance.loadedScenes++;
  }

  public static TextField getMessageBox() {
    return instance.messageBox;
  }

  public static void addInventoryItem(ImageView[] item) {
    for (int i = 0; i < 3; i++) {
      inventoryHBoxList[i].getChildren().add(item[i]);
    }
  }

  public static void removeInventoryItem(ImageView[] item) {
    for (int i = 0; i < 3; i++) {
      inventoryHBoxList[i].getChildren().remove(item[i]);
    }
  }

  public static void appendChat(String message) {
    instance.chatBox.appendText(message + "\n");
  }
}
