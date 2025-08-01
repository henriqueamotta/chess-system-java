# Chess System in Java

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

A fully functional chess game developed in Java to run in the console. This project was created as a practical study of Object-Oriented Programming (OOP) principles, including encapsulation, inheritance, polymorphism, and layered system design.

## 📖 Overview

The goal of this project is to demonstrate the logic behind a board game using a well-defined architecture that separates the system's responsibilities:

1.  **Board Layer:** Contains the generic entities and rules for any board game, such as the board itself, pieces, and positions.
2.  **Chess Layer:** Implements the specific rules and pieces for chess, building upon the board layer's foundation.
3.  **Application Layer:** Responsible for rendering the board in the console, capturing user input, and orchestrating the game flow.

## ✨ Features

* An 8x8 chessboard rendered in the console.
* Piece movement according to official chess rules.
* Calculation of possible moves for each piece, highlighted on the board.
* Logic for capturing opponent's pieces.
* Implementation of the "check" rule.

## 🛠️ Tech Stack

* **Java:** The primary language for the project.
* **Eclipse IDE:** The project is configured with Eclipse metadata files (`.project`, `.classpath`).

## ☝️ Getting Started

You can run this project in two ways: through a Java IDE or directly from the command line.

### 1. Running in an IDE (Eclipse / IntelliJ)

1.  Clone the repository:
    ```bash
    git clone [https://github.com/henriqueamotta/chess-system-java.git](https://github.com/henriqueamotta/chess-system-java.git)
    ```
2.  Open your preferred IDE.
3.  Import the project:
    * In **Eclipse**: `File` -> `Import` -> `General` -> `Existing Projects into Workspace` and select the cloned directory.
    * In **IntelliJ IDEA**: `File` -> `Open` and select the cloned directory.
4.  Locate the `Program.java` class inside the `application` package.
5.  Right-click on the file and select `Run As` -> `Java Application`.

### 2. Running from the Command Line

1.  Clone the repository and navigate into it:
    ```bash
    git clone [https://github.com/henriqueamotta/chess-system-java.git](https://github.com/henriqueamotta/chess-system-java.git)
    cd chess-system-java
    ```
2.  Compile all `.java` files from the `src` folder:
    ```bash
    javac -d bin -sourcepath src src/application/Program.java
    ```
    *This command compiles all necessary source files and places the resulting `.class` files into the `bin` directory.*

3.  Run the application:
    ```bash
    java -cp bin application.Program
    ```
    *This command runs the `Program` class, which is located in the `bin` directory.*

## 🏛️ Project Structure

```
chess-system-java/
├── .settings/      # Eclipse IDE settings
├── bin/            # (Optional) Where the compiled .class files are placed
├── src/
│   ├── application/  # Application layer (UI and Main)
│   ├── boardgame/    # Generic board game layer
│   └── chess/        # Layer with chess rules and pieces
├── .classpath      # Eclipse build path configuration
├── .gitignore      # Files ignored by Git
└── .project        # Eclipse project configuration file
```
