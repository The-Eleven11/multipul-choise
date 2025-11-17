# README: CSC207 Arcade - Multiple Choice Quiz Module

## 🚀 Getting Started

This project is now **fully implemented** and ready to run! To start the application:

```bash
# Quick start with Maven
mvn clean compile exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.Main"

# Or use the run script (Unix/Linux/Mac)
./run.sh
```

For detailed instructions, see [RUNNING.md](RUNNING.md).

> **📖 中文文档**: 如需详细的中文程序说明，请参阅 [PROGRAM_GUIDE.md](PROGRAM_GUIDE.md)（包含架构设计、依赖关系、数据流程和更新历史的完整说明）

## 1. 🎯 Project Overview

This document outlines the architecture and implementation plan for the **Multiple Choice Quiz Module**, a standalone component of the "CSC207 Arcade" project. This module is designed to be developed independently but easily integrated into the main Java Swing application.

This module fulfills **User Story 1**: "As a user, I want to take short multiple-choice quizzes on topics I choose so that I can quickly test and reinforce my understanding."

The primary goal is to create a quiz window that:
1.  Loads 15 questions from a predefined data source.
2.  Displays each question as an image.
3.  Allows the user to select one of four (A, B, C, D) button options.
4.  Provides **immediate visual feedback**:
    * **Incorrect Answer:** The selected button turns **red**.
    * **Correct Answer:** The selected button turns **green**.
5.  **Automatically advances** to the next question *only* after the correct answer is selected.
6.  Displays a **final results screen** with accuracy (percentage) and total time taken.

## 2. 🏛️ Core Architecture: Clean Architecture

To ensure modularity and future integration, we will strictly follow Clean Architecture principles.

* **Entities:** Core business objects (e.g., `QuizQuestion`, `QuizSession`). They know nothing about the rest of the application.
* **Use Cases (Interactors):** Application-specific logic (e.g., `SubmitAnswer`, `StartQuiz`). They orchestrate the flow of data between Entities and Interface Adapters.
* **Interface Adapters:** Connects Use Cases to the outside world (e.g., `QuizController`, `QuizPresenter`, `JsonQuestionRepository`).
* **Frameworks & Drivers:** The outermost layer (e.g., Java Swing UI, JSON file operations).

![Clean Architecture Diagram](https://blog.cleancoder.com/uncle-bob/images/2012-08-13-the-clean-architecture/CleanArchitecture.jpg)

## 3. 🗂️ Proposed Directory Structure

```

/src
├── main
│   ├── java
│   │   └── com
│   │       └── csc207
│   │           └── arcade
│   │               └── multiplechoice
│   │                   ├── app/                \# Main entry point & Dependency Injection
│   │                   │   └── Main.java
│   │                   │   └── DataInitializer.java
│   │                   ├── data\_access/        \# Data implementation
│   │                   │   └── JsonQuestionRepository.java
│   │                   ├── entities/           \# Core business logic
│   │                   │   └── QuizQuestion.java
│   │                   │   └── QuizSession.java
│   │                   ├── interface\_adapter/  \# Controllers, Presenters, ViewModels
│   │                   │   └── QuizController.java
│   │                   │   └── QuizPresenter.java
│   │                   │   └── QuizViewModel.java
│   │                   │   └── ResultsController.java
│   │                   │   └── ResultsPresenter.java
│   │                   │   └── ResultsViewModel.java
│   │                   ├── use\_case/           \# Application business logic
│   │                   │   ├── QuestionRepository.java   (Interface)
│   │                   │   ├── quiz/
│   │                   │   │   ├── QuizInputBoundary.java
│   │                   │   │   ├── QuizInputData.java
│   │                   │   │   ├── QuizInteractor.java
│   │                   │   │   ├── QuizOutputBoundary.java
│   │                   │   │   └── QuizOutputData.java
│   │                   │   └── submit/
│   │                   │       ├── SubmitAnswerInputBoundary.java
│   │                   │       ├── SubmitAnswerInputData.java
│   │                   │       ├── SubmitAnswerInteractor.java
│   │                   │       ├── SubmitAnswerOutputBoundary.java
│   │                   │       └── SubmitAnswerOutputData.java
│   │                   └── view/               \# Java Swing components
│   │                       ├── QuizView.java
│   │                       ├── ResultsView.java
│   │                       └── ScaledImagePanel.java   (Custom component)
│   └── resources
│       └── data
│           ├── questions.json                  \# Generated database
│           └── images/                         \# Raw question images
│               ├── id001\_level1\_answerB.png
│               ├── id002\_level2\_answerA.png
│               └── ...
└── test
└── java

````

## 4. ⚙️ Data Pipeline: Image Files to JSON

A critical requirement is to load quiz data from image filenames. We will use a startup utility for this.

### Step 1: `DataInitializer.java` (in `app` package)

This utility class will be run *once* at application startup (called from `Main.java`).

* **Action:** Scans the `src/main/resources/data/images/` directory.
* **Parsing:** Uses a regular expression to parse each filename.
    * **Pattern:** `(id\d+)_level(\d+)_answer([ABCD])\.png`
    * **Example:** `id001_level1_answerB.png`
        * Group 1 (ID): `id001`
        * Group 2 (Level): `1`
        * Group 3 (Answer): `B`
* **Output:** Generates a list of `QuizQuestion` objects and serializes them into `src/main/resources/data/questions.json`.

### Step 2: `questions.json` (in `resources/data` package)

This file is the "database" for the application. It will be read by the `JsonQuestionRepository`.

**JSON Structure:**
```json
[
  {
    "questionId": "id001",
    "imagePath": "data/images/id001_level1_answerB.png",
    "level": 1,
    "correctAnswer": "B"
  },
  {
    "questionId": "id002",
    "imagePath": "data/images/id002_level2_answerA.png",
    "level": 2,
    "correctAnswer": "A"
  }
]
````

> **Note:** The `imagePath` must be relative to the `resources` root so it can be loaded by `class.getResource()`.

## 5\. 🧱 Component Implementation Details

### Entities (`entities` package)

  * **`QuizQuestion.java`**

      * A simple Plain Old Java Object (POJO).
      * Fields: `String questionId`, `String imagePath`, `int level`, `String correctAnswer`.
      * Getters and setters (or a constructor).

  * **`QuizSession.java`**

      * Manages the state of a single quiz playthrough.
      * Fields:
          * `List<QuizQuestion> questions`: The 15 questions for this session.
          * `int currentQuestionIndex`: Tracks the current question (0-14).
          * `int correctAnswersCount`: Number of correct answers.
          * `long startTime`: `System.currentTimeMillis()` captured at the start.
          * `long endTime`: Captured at the end.
      * Methods:
          * `QuizQuestion getCurrentQuestion()`: Returns `questions.get(currentQuestionIndex)`.
          * `void recordAnswer(boolean isCorrect)`: If `isCorrect`, increments `correctAnswersCount`.
          * `boolean advanceToNextQuestion()`: Increments `currentQuestionIndex`. Returns `true` if there are more questions, `false` if the quiz is over.
          * `double getAccuracy()`: Calculates `(double)correctAnswersCount / questions.size()`.
          * `long getTotalTime()`: Calculates `endTime - startTime`.
          * `boolean isQuizOver()`: Checks if `currentQuestionIndex` is at the end.

### Use Cases (`use_case` package)

  * **`QuestionRepository.java` (Interface)**

      * Defines the contract for data access.
      * Methods:
          * `void loadData()`: (Optional) Triggers loading from the data source.
          * `List<QuizQuestion> getQuestions(int count)`: Gets `count` random questions.

  * **`QuizInteractor.java` (Implements `QuizInputBoundary`)**

      * **Purpose:** To start the quiz and load the first question.
      * **Dependencies:** `QuestionRepository`, `QuizOutputBoundary` (the Presenter).
      * **`execute(QuizInputData data)`:**
        1.  Calls `questionRepository.getQuestions(15)`.
        2.  Creates a new `QuizSession` entity with these questions and starts the timer.
        3.  Gets the *first* question (`session.getCurrentQuestion()`).
        4.  Creates `QuizOutputData` (with image path, question number "1/15").
        5.  Calls `quizPresenter.prepareQuizView(outputData)`.

  * **`SubmitAnswerInteractor.java` (Implements `SubmitAnswerInputBoundary`)**

      * **Purpose:** To check an answer and decide the outcome.
      * **Dependencies:** `QuizSession` (needs access to the *current* session), `SubmitAnswerOutputBoundary` (the Presenter).
      * **`execute(SubmitAnswerInputData data)`:**
        1.  Gets the `selectedAnswer` (e.g., "A") from `inputData`.
        2.  Gets the `currentQuestion` from the `QuizSession`.
        3.  Compares `selectedAnswer` with `currentQuestion.getCorrectAnswer()`.
        4.  **If CORRECT:**
            a.  Calls `quizSession.recordAnswer(true)`.
            b.  Creates `SubmitAnswerOutputData` (isCorrect: `true`, correctAnswer: `currentQuestion.getCorrectAnswer()`.
            c.  Calls `submitAnswerPresenter.prepareSuccessView(outputData)`.
            d.  **Crucially:** Calls `quizSession.advanceToNextQuestion()`.
            e.  Checks `quizSession.isQuizOver()`.
            \* If **NOT over**: Gets the *next* question and calls `quizPresenter.prepareQuizView(...)` to display it.
            \* If **OVER**: Triggers the `Results` use case (calculates time, accuracy, and calls `resultsPresenter.prepareResultsView(...)`).
        5.  **If INCORRECT:**
            a.  Calls `quizSession.recordAnswer(false)`.
            b.  Creates `SubmitAnswerOutputData` (isCorrect: `false`, selectedAnswer: `selectedAnswer`).
            c.  Calls `submitAnswerPresenter.prepareFailView(outputData)`. (Does *not* advance).

### Interface Adapters (`interface_adapter` package)

  * **`QuizController.java`**

      * The "adapter" for the View.
      * Dependencies: `QuizInputBoundary`, `SubmitAnswerInputBoundary`.
      * Methods:
          * `void startQuiz()`: Calls `quizInteractor.execute(...)`.
          * `void submitAnswer(String answer)`: Creates `SubmitAnswerInputData` and calls `submitAnswerInteractor.execute(...)`.

  * **`QuizViewModel.java`**

      * A state container for the `QuizView`. Uses `PropertyChangeSupport` to be observable.
      * Fields:
          * `String currentImagePath`
          * `String questionProgressLabel` (e.g., "Question 5/15")
          * `String feedbackState` (e.g., "CORRECT", "INCORRECT", "NONE")
          * `String incorrectButton` (e.g., "A", "B", "C", or "D")
      * `firePropertyChange(...)` notifies the View to update.

  * **`QuizPresenter.java` (Implements `QuizOutputBoundary`, `SubmitAnswerOutputBoundary`)**

      * **Purpose:** To format data from Interactors for the ViewModel.
      * Dependency: `QuizViewModel`.
      * `prepareQuizView(QuizOutputData data)`: Updates `QuizViewModel` with the new `imagePath` and `questionProgressLabel`. Sets feedback state to "NONE".
      * `prepareSuccessView(SubmitAnswerOutputData data)`: Updates `QuizViewModel` `feedbackState` to "CORRECT".
      * `prepareFailView(SubmitAnswerOutputData data)`: Updates `QuizViewModel` `feedbackState` to "INCORRECT" and sets `incorrectButton` to the failed answer.

  * **(Similarly for `ResultsController`, `ResultsPresenter`, `ResultsViewModel`)**

### Frameworks & Drivers (`view` & `data_access` packages)

  * **`JsonQuestionRepository.java` (Implements `QuestionRepository`)**

      * Uses a library (like **GSON** or **Jackson**) to read `questions.json`.
      * Loads the resource stream: `InputStream is = getClass().getClassLoader().getResourceAsStream("data/questions.json");`
      * Deserializes the JSON array into a `List<QuizQuestion>`.
      * `getQuestions(int count)`: Shuffles the list and returns a sublist of size `count`.

  * **`ScaledImagePanel.java` (Custom Swing Component)**

      * This is essential for the scaling requirement.
      * `extends JPanel`
      * Field: `private BufferedImage image;`
      * Method: `public void setImage(String imagePath)`
        1.  Loads the image: `InputStream is = getClass().getClassLoader().getResourceAsStream(imagePath);`
        2.  `this.image = ImageIO.read(is);`
        3.  `repaint();`
      * Override `paintComponent(Graphics g)`:
        1.  `super.paintComponent(g);`
        2.  `if (image != null)`:
        3.  `g.drawImage(image, 0, 0, getWidth(), getHeight(), null);` (This line performs the scaling).

  * **`QuizView.java` (extends `JFrame`, Implements `PropertyChangeListener`)**

      * **Purpose:** The main quiz window.
      * **Components:**
          * `ScaledImagePanel` (for the top half).
          * `JPanel` (for the bottom half) with a `GridLayout` (2x2) or `FlowLayout`.
          * `JButton buttonA`, `buttonB`, `buttonC`, `buttonD`.
          * `JLabel progressLabel`.
      * **Connections:**
          * Holds a `QuizController` and `QuizViewModel`.
          * Adds itself as a listener: `viewModel.addPropertyChangeListener(this)`.
          * `ActionListener` on `buttonA`: `quizController.submitAnswer("A");` (and so on for B, C, D).
      * **`propertyChange(PropertyChangeEvent evt)` (The Core UI Logic):**
        1.  Checks `evt.getPropertyName()`.
        2.  If `imagePath` changed: `scaledImagePanel.setImage(viewModel.getImagePath())`.
        3.  If `progressLabel` changed: `progressLabel.setText(viewModel.getLabel())`.
        4.  If `feedbackState` changed:
              * `resetButtonColors();` (set all to default).
              * `if (viewModel.getFeedbackState().equals("INCORRECT"))`:
                  * Get the `incorrectButton` (e.g., "A") and set its background to `Color.RED`.
              * `if (viewModel.getFeedbackState().equals("CORRECT"))`:
                  * Find the correct button and set its background to `Color.GREEN`.
                  * **Auto-Advance:** Use a `javax.swing.Timer` to pause for \~1 second, then trigger the *next* question (which the `SubmitAnswerInteractor` already pushed to the `QuizPresenter`).

  * **`ResultsView.java` (extends `JFrame`)**

      * A simple window with `JLabel`s to show accuracy and total time.
      * Receives data from its `ResultsViewModel`.

### Entry Point (`app` package)

  * **`Main.java`**
      * This makes the module runnable.
      * `public static void main(String[] args)`
      * **Step 1: Data Init**
          * `DataInitializer.run();` (or `new DataInitializer().run();`)
      * **Step 2: Dependency Injection (Pure Java "Wiring")**
          * `QuestionRepository repository = new JsonQuestionRepository();`
          * `QuizViewModel quizVM = new QuizViewModel();`
          * `ResultsViewModel resultsVM = new ResultsViewModel();`
          * `QuizPresenter quizPresenter = new QuizPresenter(quizVM);`
          * `// ... create all interactors, presenters, controllers...`
          * `QuizController quizController = new QuizController(...);`
      * **Step 3: Launch UI**
          * `SwingUtilities.invokeLater(() -> {`
          * `QuizView view = new QuizView(quizController, quizVM);`
          * `view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);`
          * `view.setSize(800, 600);`
          * `view.setVisible(true);`
          * `quizController.startQuiz();` // Kick off the first question
          * `});`

## 6\. 🚀 Next Steps (for Copilot)

1.  **Stub Entities:** Create `QuizQuestion.java` and `QuizSession.java`.
2.  **Stub Data Pipeline:** Create `DataInitializer.java` to scan the image folder and `JsonQuestionRepository.java` to read the (not-yet-existing) JSON.
3.  **Implement `ScaledImagePanel.java`:** This is a crucial, custom UI component.
4.  **Stub View:** Create `QuizView.java` with the panel and 4 buttons.
5.  **Wire `Main.java`:** Connect the components to launch the window.
6.  **Implement Use Cases:** Build the `QuizInteractor` and `SubmitAnswerInteractor` logic.
7.  **Implement Presenters:** Build the `QuizPresenter` to update the `QuizViewModel`.
8.  **Implement View Logic:** Write the `propertyChange` method in `QuizView` to handle UI updates (colors, new images).
9.  **Build Results Flow:** Add the `ResultsView` and the logic to transition to it.

<!-- end list -->

```
```
