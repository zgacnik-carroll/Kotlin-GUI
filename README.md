# Maze Escape (Kotlin GUI Game)

---

## Description
Maze Escape is a simple GUI maze navigation game written in Kotlin. The player moves through a maze using keyboard input and attempts to reach the exit while avoiding walls.

---

## Requirements

- Kotlin 2.3.10
- Java 21.0.7
- A terminal that supports ANSI color codes

---

## How to Play

When the program is running the user will first be greeted with a
title screen that gives a quick set of instructions on how to play the game:

### Controls
- **W** — Move up
- **A** — Move left
- **S** — Move down
- **D** — Move right

*Along with the WASD keys, the user can also move using the arrow keys.*

---

## How to Run

1. Clone this GitHub repository into your desired directory.
2. Navigate to your desired directory, then navigate to the source code folder:
   ```bash
   cd src/
   ```
3. Next, compile and run the program using the following command:
    ```bash
    kotlinc MazeGUI.kt -include-runtime -d temp.jar && java -jar temp.jar && rm temp.jar
    ```
   This command compiles the Kotlin file into a temporary JAR, runs the program, and then deletes the JAR to keep the project directory clean.

Now the program will be up and running! Be sure to follow the instructions on the screen

---

## Screens

### *Title Screen*

As stated above, when the program is first run the user will be
greeted with a basic title screen with simple instructions and a button labeled "Start."
Clicking this button will take you to the next screen.

### *Level Select*

The Level Select Screen displays the four different levels in the game, as well
as an "Exit Game" button that closes the window and stops the program. Select which level
you would like to play or close the program!

### *Current Level Screen*

Once you have selected a level from the Level Select screen, you will be prompted with
a maze where you can move the player (green circle) through the maze all the way to the
exit (red square). Again, you can use the WASD keys or the arrow keys, either will work.
In addition to the maze, you have the option to restart the level by clicking the "Restart"
button in the top left of the screen, or click the "Quit" button to quit the level entirely
and be taken back to the Level Select screen.

### *Victory Screen*

Once you have reached the end of the maze, you will be prompted with a Victory screen. This
screen displays a simple "Victory!" message along with the amount of time (in seconds) it took you
to finish the maze. You are given a star rating on a scale from one to three based on how fast
you completed the level:
- 3 stars = less than 30 seconds
- 2 stars = less than 60 seconds
- 1 star = more than 60 seconds

In addition to these star ratings, you have the option of returning to the Level Select screen by
clicking the "Level Select" button, or quit and close the program entirely by clicking the "Exit" button.

---

## Closing Remarks

This project was created to strengthen my understanding of Kotlin programming concepts through the development of an interactive console application. It provides a solid foundation for expanding into more advanced game logic and future Kotlin projects like a more interactive GUI or a Web Application.
Otherwise, have fun playing the Maze Escape GUI game!