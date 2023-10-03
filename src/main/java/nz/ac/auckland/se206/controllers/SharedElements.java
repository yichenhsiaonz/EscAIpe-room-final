package nz.ac.auckland.se206.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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
  private static VBox[] inventoryVBoxList = new VBox[3];
  private static VBox[] dialogueBoxList = new VBox[3];
  private static Button[] hintButtonList = new Button[3];

  public static void newGame() throws ApiProxyException {
    // clear all previous instances
    taskBarHBoxList = new HBox[3];
    inventoryVBoxList = new VBox[3];
    dialogueBoxList = new VBox[3];
    hintButtonList = new Button[3];
    // create new instances
    instance = new SharedElements();
  }

  public static VBox getDialogueBox() {
    System.out.println("getDialogueBox");
    // allocate a dialogue box instance to each scene
    return dialogueBoxList[instance.loadedScenes];
  }

  public static HBox getTaskBarBox() {
    System.out.println("getTaskBarBox");
    // allocate a task bar instance to each scene
    return taskBarHBoxList[instance.loadedScenes];
  }

  public static VBox getInventoryBox() {
    System.out.println("getInventoryBox");
    // allocate an inventory instance to each scene
    return inventoryVBoxList[instance.loadedScenes];
  }

  public static Button getHintButton() {
    return hintButtonList[instance.loadedScenes];
  }

  public static void incremnetLoadedScenes() {
    instance.loadedScenes++;
  }

  public static TextField getMessageBox() {
    return instance.messageBox;
  }

  public static void addInventoryItem(ImageView[] item) {
    // add item to each inventory instance
    for (int i = 0; i < 3; i++) {
      inventoryVBoxList[i].getChildren().add(item[i]);
      item[i].setFitWidth(100);
      item[i].setFitHeight(100);
    }
  }

  public static void removeInventoryItem(ImageView[] item) {
    // remove item from each inventory instance
    for (int i = 0; i < 3; i++) {
      inventoryVBoxList[i].getChildren().remove(item[i]);
    }
  }

  public static void appendChat(String message) {
    // append message to master chat box with a newline below
    instance.chatBox.appendText(message + "\n\n");
    instance.chatBox.setScrollTop(Double.MAX_VALUE);
  }

  public static void setHintsText(int hints) {
    // set master hint button text
    if (hints == 0) {
      // disable hints button if no hints left
      instance.hintButton.setText("No Hints Left");
      instance.hintButton.setDisable(true);
    } else if (hints < 0) {
      // set hints to unlimited if on easy mode
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
    // create new master timer label and progress bar
    timerLabel = new Label();
    timerProgressBar = new ProgressBar();

    // bind master timer label and progress bar to game state timer
    Task<Void> timerTask = GameState.getTimer();

    timerLabel.textProperty().bind(timerTask.messageProperty());
    timerProgressBar.progressProperty().bind(timerTask.progressProperty());

    // create new master message box
    messageBox = new TextField();
    // create new master chat box
    chatBox = new TextArea();
    // create new master hint button
    hintButton = new Button();
    // assign script to hint button
    hintButton.setOnAction(
        event -> {
          try {

            GameState.getPuzzleHint();
          } catch (ApiProxyException e) {
            e.printStackTrace();
            System.out.println("setting up error");
          }
        });
    // create new master send button
    sendMessage = new Button();
    // assign script to send button
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
      // create new child timer label and progress bar
      ProgressBar timerProgressBarChild = new ProgressBar();
      timerProgressBarChild.setPrefWidth(609);
      timerProgressBarChild.setPrefHeight(35);
      Label timerLabelChild = new Label();
      timerLabelChild.setFont(new javafx.scene.text.Font("System", 24));
      Label powerLabelChild = new Label();
      powerLabelChild.setText("Power Left:");
      powerLabelChild.setFont(new javafx.scene.text.Font("System", 24));

      // use HBox to place elements side by side
      // use VBox to place elements on top of each other
      VBox inventoryVerticalBoxChild = new VBox();
      inventoryVerticalBoxChild.setPrefWidth(180);
      inventoryVerticalBoxChild.setPrefHeight(400);
      inventoryVerticalBoxChild.setSpacing(30);
      inventoryVerticalBoxChild.setAlignment(Pos.TOP_CENTER);
      inventoryVerticalBoxChild.setLayoutY(5);
      HBox taskBarHorizontalBoxChild = new HBox();
      HBox timerHorizontalBoxChild = new HBox();
      VBox timerVerticalBoxChild = new VBox();

      // place progress bar and label side by side
      timerHorizontalBoxChild.getChildren().addAll(timerProgressBarChild, timerLabelChild);
      // place header above progress bar and label
      timerVerticalBoxChild.getChildren().addAll(powerLabelChild, timerHorizontalBoxChild);
      // place timer and inventory side by side
      taskBarHorizontalBoxChild.getChildren().addAll(timerVerticalBoxChild);

      // add new instance to list
      taskBarHBoxList[i] = taskBarHorizontalBoxChild;
      inventoryVBoxList[i] = inventoryVerticalBoxChild;

      // bind child timer label and progress bar to master timer label and progress bar
      timerLabelChild.textProperty().bind(timerLabel.textProperty());
      timerProgressBarChild.progressProperty().bind(timerProgressBar.progressProperty());

      // create new child message box
      // bidirectionally bind child message box text to master message box
      TextField messageBoxChild = new TextField();
      messageBoxChild.textProperty().bindBidirectional(messageBox.textProperty());
      messageBoxChild.setPrefWidth(270);
      messageBoxChild.setPrefHeight(25);

      // create new child chat box
      // bind child chat box text to master chat box
      TextArea chatBoxChild = new TextArea();
      chatBoxChild.setPrefWidth(330);
      chatBoxChild.setPrefHeight(910);
      chatBoxChild.setEditable(false);
      chatBoxChild.setWrapText(true);
      chatBoxChild.setFocusTraversable(false);
      chatBoxChild.textProperty().bind(chatBox.textProperty());

      // create new child send button
      // bind child send button click behaviour to master send button
      // bind child send button disable property to master send button
      Button sendMessageChild = new Button();
      sendMessageChild.setText("Submit");
      sendMessageChild.setPrefWidth(60);
      sendMessageChild.setPrefHeight(25);
      sendMessageChild.onActionProperty().bind(sendMessage.onActionProperty());
      sendMessageChild.disableProperty().bind(sendMessage.disableProperty());

      // create new child hint button
      // bind child hint button click behaviour to master hint button
      // bind child hint button disable property to master hint button
      Button hintButtonChild = new Button();
      hintButtonChild.setPrefWidth(330);
      hintButtonChild.setPrefHeight(25);
      hintButtonChild.textProperty().bind(hintButton.textProperty());
      hintButtonChild.onActionProperty().bind(hintButton.onActionProperty());
      hintButtonChild.disableProperty().bind(hintButton.disableProperty());

      // use HBox to place elements side by side
      // use VBox to place elements on top of each other
      VBox dialogueBoxChild = new VBox();
      HBox sendBoxChild = new HBox();

      // place chat box and send button side by side
      sendBoxChild.getChildren().addAll(messageBoxChild, sendMessageChild);
      // place chat box and send button below chat box
      dialogueBoxChild.getChildren().addAll(chatBoxChild, sendBoxChild);

      // add new instance to list
      hintButtonList[i] = hintButtonChild;
      dialogueBoxList[i] = dialogueBoxChild;
    }
  }
}
