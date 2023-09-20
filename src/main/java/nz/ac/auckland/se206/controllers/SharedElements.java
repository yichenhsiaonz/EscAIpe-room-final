package nz.ac.auckland.se206.controllers;

import javafx.concurrent.Task;
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

  public static void newGame() throws ApiProxyException {
    taskBarHBoxList = new HBox[3];
    inventoryHBoxList = new HBox[3];
    dialogueBoxList = new VBox[3];
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
    instance.chatBox.appendText(message + "\n\n");
    instance.chatBox.setScrollTop(Double.MAX_VALUE);
  }

  public static void setHintsText(int hints) {
    if (hints == 0) {
      instance.hintButton.setText("No Hints Left");
      instance.hintButton.setDisable(true);
    } else if (hints < 0) {
      instance.hintButton.setText("Hints Left: Unlimited");
    } else {
      instance.hintButton.setText("Hints Left: " + hints);
    }
  }

  public static void disableHintsButton() {
    instance.hintButton.setDisable(true);
  }

  public static void enableHintsButton() {
    instance.hintButton.setDisable(false);
  }

  public static void disableSendButton() {
    instance.sendMessage.setDisable(true);
  }

  public static void enableSendButton() {
    instance.sendMessage.setDisable(false);
  }

  public static void printPaper() {
    instance.isPaperPrinted = true;
  }

  public static void takePaper() {
    instance.isPaperPrinted = false;
  }

  public static boolean isPaperPrinted() {
    return instance.isPaperPrinted;
  }

  @FXML private VBox dialogueBox;
  @FXML private HBox sendBox;
  @FXML private Label timerLabel;
  @FXML private Label powerLabel;
  @FXML private ProgressBar timerProgressBar;
  @FXML private TextField messageBox;
  @FXML private TextArea chatBox;
  @FXML private Button sendMessage;
  @FXML private Button hintButton;

  private int loadedScenes;
  private boolean isPaperPrinted = false;

  private SharedElements() throws ApiProxyException {
    timerLabel = new Label();
    timerLabel.setFont(new javafx.scene.text.Font("System", 24));
    timerProgressBar = new ProgressBar();
    timerProgressBar.setPrefWidth(609);
    timerProgressBar.setPrefHeight(35);

    Task<Void> timerTask = GameState.getTimer();

    timerLabel.textProperty().bind(timerTask.messageProperty());
    timerProgressBar.progressProperty().bind(timerTask.progressProperty());

    messageBox = new TextField();
    messageBox.setPrefWidth(270);
    messageBox.setPrefHeight(25);
    chatBox = new TextArea();
    chatBox.setPrefWidth(330);
    chatBox.setPrefHeight(935);
    chatBox.setEditable(false);
    chatBox.setWrapText(true);
    chatBox.setFocusTraversable(false);
    hintButton = new Button();
    hintButton.setText("Hint");
    hintButton.setOnAction(
        event -> {
          try {

            GameState.getPuzzleHint();
          } catch (ApiProxyException e) {
            e.printStackTrace();
            System.out.println("setting up error");
          }
        });
    sendMessage = new Button();
    sendMessage.setOnAction(
        event -> {
          try {
            GameState.onMessageSent();
          } catch (ApiProxyException e) {
            e.printStackTrace();
            System.out.println("setting up error");
          }
        });

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

      timerLabelChild.textProperty().bind(timerLabel.textProperty());
      timerProgressBarChild.progressProperty().bind(timerProgressBar.progressProperty());

      TextField messageBoxChild = new TextField();
      messageBoxChild.textProperty().bindBidirectional(messageBox.textProperty());
      messageBoxChild.setPrefWidth(270);
      messageBoxChild.setPrefHeight(25);
      TextArea chatBoxChild = new TextArea();
      chatBoxChild.setPrefWidth(330);
      chatBoxChild.setPrefHeight(910);
      chatBoxChild.setEditable(false);
      chatBoxChild.setWrapText(true);
      chatBoxChild.setFocusTraversable(false);
      chatBoxChild.textProperty().bind(chatBox.textProperty());
      Button sendMessageChild = new Button();
      sendMessageChild.setText("Submit");
      sendMessageChild.setPrefWidth(60);
      sendMessageChild.setPrefHeight(25);
      sendMessageChild.onActionProperty().bind(sendMessage.onActionProperty());
      sendMessageChild.disableProperty().bind(sendMessage.disableProperty());
      Button hintButtonChild = new Button();
      hintButtonChild.setPrefWidth(330);
      hintButtonChild.setPrefHeight(25);
      hintButtonChild.textProperty().bind(hintButton.textProperty());
      hintButtonChild.onActionProperty().bind(hintButton.onActionProperty());
      hintButtonChild.disableProperty().bind(hintButton.disableProperty());
      VBox dialogueBoxChild = new VBox();
      HBox sendBoxChild = new HBox();

      // place chat box and send button side by side
      sendBoxChild.getChildren().addAll(messageBoxChild, sendMessageChild);
      // place chat box and send button below chat box
      dialogueBoxChild.getChildren().addAll(chatBoxChild, hintButtonChild, sendBoxChild);

      dialogueBoxList[i] = dialogueBoxChild;
    }
  }
}
