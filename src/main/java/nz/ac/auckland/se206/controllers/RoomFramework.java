package nz.ac.auckland.se206.controllers;

import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import nz.ac.auckland.se206.GameState;

public class RoomFramework {

  public static void scaleToScreen(AnchorPane contentPane) {

    double scale = (double) GameState.getWidth() / 1920;
    contentPane.setScaleX(scale);
    contentPane.setScaleY(scale);
    int verticalMargin =
        (GameState.getHeight() - 1080) / 2
            + (GameState.getWindowHeight() - GameState.getHeight()) / 2;
    int horizontalMargin =
        (GameState.getWidth() - 1920) / 2 + (GameState.getWindowWidth() - GameState.getWidth()) / 2;

    System.out.println(verticalMargin);
    System.out.println(horizontalMargin);
    BorderPane.setMargin(
        contentPane,
        new javafx.geometry.Insets(
            verticalMargin, horizontalMargin, verticalMargin, horizontalMargin));
    System.out.println(scale);
  }

  public static void goTo(
      double destinationX, double destinationY, ImageView character, ImageView running) {
    // Retrieve the character's width and height using fitWidth and fitHeight
    double characterWidth = character.getFitWidth();
    double characterHeight = character.getFitHeight();

    // Calculate the character's new position relative to the room
    double characterX = destinationX - characterWidth / 2; // Adjust for character's width
    double characterY = destinationY - characterHeight / 2; // Adjust for character's height

    // Calculate the distance the character needs to move
    double distanceToMove =
        Math.sqrt(
            Math.pow(characterX - character.getTranslateX(), 2)
                + Math.pow(characterY - character.getTranslateY(), 2));

    // Define a constant speed
    double constantSpeed = 300;

    // Calculate the duration based on constant speed and distance
    double durationSeconds = distanceToMove / constantSpeed;

    // Create a TranslateTransition to smoothly move the character
    TranslateTransition transition =
        new TranslateTransition(Duration.seconds(durationSeconds), character);
    transition.setToX(characterX);
    transition.setToY(characterY);

    // Play the animation
    transition.play();

    // Create a TranslateTransition to smoothly move the "running" element
    TranslateTransition transition2 =
        new TranslateTransition(Duration.seconds(durationSeconds), running);
    transition2.setToX(characterX);
    transition2.setToY(characterY);

    // flip the character and running gif if needed
    if (characterX > character.getTranslateX()) {
      running.setScaleX(1);
      character.setScaleX(1);
    } else {
      running.setScaleX(-1);
      character.setScaleX(-1);
    }

    running.setOpacity(1);
    // Play the animation
    transition2.play();

    transition2.setOnFinished(
        e -> {
          // Remove the "running" element from the pane when the animation is done
          running.setOpacity(0);
        });
  }
}
