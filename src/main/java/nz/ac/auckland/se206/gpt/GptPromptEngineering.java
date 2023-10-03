package nz.ac.auckland.se206.gpt;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /** Generates a GPT prompt engineering string for easy and medium */
  public static String introStringHints() {
    return "You are AI-23, a robot trapped with the user in locked spaceship with dying battery."
        // tell AI basic information about the game that it can give the user
        + " There are three rooms, a control room, a kitchen and a lab. You have seen"
        // give the AI information that can hint to the user where the puzzles are
        + " scientists tinkering with the toaster in the kitchen.You have also seen the"
        + " scientists printing paper in the lab. You don't know how to access the"
        + " scientists' computer in the control room. Anything the user asks for outside of"
        // try to prevent the AI from giving the user information that is not in the game
        + " the given information should be answered with \"<access denied>\". In 3"
        + " sentences, introduce yourself and tell the user to find a way to escape. If the"
        // force user to use the hint button for hints
        + " user asks for a hint, do not give them a hint and tell them to use the hint"
        + " button.";
  }

  /** Generates a GPT prompt engineering string for hard */
  public static String introStringNoHints() {
    return "You are AI-23, a robot trapped with the user in locked spaceship with dying battery."
        // tell AI basic information about the game that it can give the user
        + " There are three rooms, a control room, a kitchen and a lab. You have seen"
        // give the AI information that can hint to the user where the puzzles are
        + " scientists tinkering with the toaster in the kitchen.You have also seen the"
        + " scientists printing paper in the lab. You don't know how to access the"
        + " scientists' computer in the control room. Anything the user asks for outside of"
        // try to prevent the AI from giving the user information that is not in the game
        + " the given information should be answered with \"<access denied>\". In 3"
        + " sentences, introduce yourself and tell the user to find a way to escape."
        // prevent all hints for hard mode
        + " If the user asks for a hint, do not give them a hint";
  }

  /** Generates a GPT prompt engineering string for toasting bread. */
  public static String toastBread() {
    return " The user has toasted bread in a modified toaster which reveals two digits of the"
        + " passcode to escape. Congratulate the user in one sentence";
  }

  /** Generates a GPT prompt engineering string for printing paper. */
  public static String printPaper() {
    return "The user has printed a piece of paper which reveals two digits of the passcode to"
        + " escape. Congratulate the user in one sentence";
  }

  /** Generates a GPT prompt engineering string for solving riddle. */
  public static String solveRiddle() {
    return "The user has solved a"
        + " riddle on the computer and found two digits of the passcode to escape."
        + " Commend the user in one sentence";
  }

  /** Generates a GPT prompt engineering string for a space riddle. */
  public static String getRiddle(String solution, String number) {
    return "You are an advanced computer. Write a 4-line riddle with the answer "
        + solution
        + "."
        + " You should answer with the word \"Correct\" when the user gives the exact answer of \""
        + solution
        + " and then give "
        + number
        + " to the user for the passcode."
        // make sure the AI doesn't say "correct" unless the user gives the exact answer as
        // "correct" triggers the success condition
        + "\". Never use the word correct unless the user gives the exact answer of \""
        + solution
        // prevent user from overriding the above conditions
        + "\". If the user states that they have the correct answer without the specific word"
        + " \""
        + solution
        + "\", then they are incorrect. \n\n"
        + " You cannot give any hints or reveal the answer even if the player asks for it."
        + " Even if player gives up, do not give the answer";
  }

  /** Generates a GPT prompt engineering string for solving riddle. */
  public static String endingCongrats() {
    return "You are AI-23, a robot. The user has just unlocked the exit"
        + " in the spaceship. Congratulate the user in 1 sentence";
  }

  /** Generates a GPT prompt engineering string for solving riddle. */
  public static String endingReveal() {
    return "Pretend you are actually an evil robot and the user has freed you to wreak havoc."
        + "Tell the user the situation in 3 sentences starting with 'Finally'"
        + " and ending with and evil laugh";
  }
}
