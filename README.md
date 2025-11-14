📘 CSC207 Arcade — Multiple Choice Quiz Module
Clean Architecture Implementation · Standalone + Future Integration Ready
1. Overview

This module implements the Multiple-Choice Quiz component of the CSC207 Arcade desktop application.

The final full project is a Java/Swing–based Duolingo-style learning platform for CSC207 material.
This module handles only the Multiple-Choice game, but:

it must be fully standalone runnable,

it must follow Clean Architecture,

it must be easy to integrate into the main system later.

The Multiple-Choice game flow:

User clicks “Multiple Choice Quiz” from main menu (handled by main project).

This module launches a separate Swing window.

Each quiz question displays:

a resized screenshot at the top

four answer buttons (A/B/C/D) below

User selects an option → it is validated immediately

wrong → button becomes red

correct → button becomes green, then automatically loads next question

After 15 questions, the result screen shows:

accuracy

total time spent

Question data are stored as screenshots in a folder, named like:

id001_level1_answerA.png
id002_level3_answerC.png
...


On startup, a question importer scans that folder and generates a single questions.json file.

2. Directory Structure (Clean Architecture)
src/
 ├── entities/
 │    └── Question.java
 │    └── QuizResult.java
 │
 ├── usecases/
 │    ├── generatejson/
 │    │      └── QuestionGenerationInteractor.java
 │    │      └── QuestionGenerationInputBoundary.java
 │    │      └── QuestionGenerationOutputBoundary.java
 │    │      └── QuestionMetadataExtractor.java
 │    │
 │    ├── runquiz/
 │    │      └── QuizInteractor.java
 │    │      └── QuizInputBoundary.java
 │    │      └── QuizOutputBoundary.java
 │    │      └── QuizTimer.java
 │
 ├── interface_adapters/
 │    ├── controllers/
 │    │       └── QuestionGenerationController.java
 │    │       └── QuizController.java
 │    │
 │    ├── presenters/
 │    │       └── QuestionGenerationPresenter.java
 │    │       └── QuizPresenter.java
 │    │
 │    ├── gateways/
 │    │       └── QuestionJsonGateway.java
 │    │       └── ScreenshotFolderGateway.java
 │
 ├── frameworks/
 │    ├── ui/
 │    │      └── QuizWindow.java
 │    │      └── ResultWindow.java
 │    │
 │    └── data/
 │           └── JsonFileWriter.java
 │           └── JsonFileReader.java
 │
 └── Main.java

3. Data Format
questions.json

Generated at startup:

{
  "questions": [
    {
      "id": "001",
      "level": 1,
      "correctAnswer": "A",
      "imagePath": "questions/id001_level1_answerA.png"
    },
    {
      "id": "002",
      "level": 3,
      "correctAnswer": "C",
      "imagePath": "questions/id002_level3_answerC.png"
    }
  ]
}

🧠 4. Entities Layer

Entities contain only business rules.

4.1 Question Entity
Responsibilities

represents a single MC question

immutable

contains no UI logic

Fields
private final String id;
private final int level;
private final String correctAnswer; // "A" | "B" | "C" | "D"
private final String imagePath;

4.2 QuizResult Entity

Tracks result summary:

private final int totalQuestions;
private final int correctCount;
private final long timeMillis;

⚙ 5. Use Cases Layer
5.1 Use Case 1 — Generate JSON from Screenshot Folder
Purpose

Scan a folder at startup

Detect file patterns like id012_level2_answerC.png

Convert to Question entities

Write all of them into questions.json

File naming parser logic

Filename format:

idXXX_levelY_answerZ.png


Extraction rules:

XXX → question id

Y → difficulty level

Z → correct answer

5.1.1 QuestionGenerationInteractor
Responsibilities

orchestrates scanning, parsing, creating Questions, writing JSON

Key Methods
public void generate(String folderPath, String outputJsonPath);

Steps Performed

Read all PNG files from folder

For each filename:

extract id, level, correctAnswer

Create Question entity

Collect into a list

Output a single JSON object via QuestionJsonGateway

Presenter notifies UI (console or dialog)

5.1.2 QuestionMetadataExtractor

Handles regex logic:

Pattern pattern = Pattern.compile("id(\\d+)_level(\\d+)_answer([A-D])\\.png");

5.1.3 Gateways
● ScreenshotFolderGateway

Reads files from folder.

● QuestionJsonGateway

Writes the completed JSON file.

5.2 Use Case 2 — Run the Quiz

Handled by QuizInteractor.

Responsibilities

manage state of current question index

validate user answer

track score + timer

notify presenter for UI updates

Quiz Flow (Use Case Layer)
Step 1 — Load Questions

QuizInteractor.loadQuestions() uses:

JsonFileReader

converts JSON → List<Question>

Step 2 — Start Quiz
QuizInteractor.startQuiz(questions);
QuizTimer.start();

Step 3 — User selects answer

Flow:

Controller calls:

quizInteractor.submitAnswer("B");


Interactor checks correctness

Presenter is triggered:

showCorrect()

showIncorrect()

If correct → move to next question

Step 4 — Quiz ends

Presenter receives final result:

accuracy

time spent

Displayed in a ResultWindow.

🎨 6. Interface Adapters Layer
6.1 Controllers
QuizController

Connects UI → use case:

public void onAnswerSelected(String answer);
public void onStartQuiz();

QuestionGenerationController

Initiates the JSON creation process:

public void generateQuestions();

6.2 Presenters
QuizPresenter

Outputs pure data for UI (no Swing code):

public void presentCorrect();
public void presentIncorrect();
public void presentNextQuestion(Question question);
public void presentResult(QuizResult result);

6.3 Gateways
JsonFileReader

Loads JSON into plain Java Maps or entity factories.

JsonFileWriter

Serializes entities → JSON.

🖥 7. Frameworks Layer (Swing UI)

Contains Swing-specific classes.
No business logic.

7.1 QuizWindow
Layout
-----------------------------------------
|           [Image JLabel]              |
|         auto-resized to fit           |
-----------------------------------------
|  [A Button]   [B Button]   [C Button]  |
|               [D Button]              |
-----------------------------------------

Responsibilities

render question image

resize image based on window size

link button clicks to QuizController

dynamically recolor answer button (green/red)

load next question

Key Methods
public void displayQuestion(Question q);
private void updateImage(String imagePath);
public void highlightCorrect(String answer);
public void highlightIncorrect(String answer);

7.2 Image Auto-Resizing Specification

When loading an image:

get window’s available width/height

scale image proportionally

set into JLabel

Use:

Image scaled = original.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);

7.3 ResultWindow

Displays summary:

correct count

accuracy %

time elapsed

🚀 8. Program Startup Flow
Startup Sequence
Main
 ├── run QuestionGenerationController.generate()
 ├── load questions.json via QuizInteractor
 └── open QuizWindow


Full Flow:

Scan folder → generate JSON

Load all questions

Start quiz

Show UI

User answers 15 questions

Show result window

🔌 9. Integration Into CSC207 Full Platform

To integrate later:

Add a button in the main menu:
new MultipleChoiceLauncher().launch();

MultipleChoiceLauncher:
public class MultipleChoiceLauncher {
    public void launch() {
       // initialize interactor + presenter + ui
       QuizWindow window = new QuizWindow(controller);
       window.setVisible(true);
    }
}

💡 10. Copilot Startup Prompt Tips

To make Copilot write correct code:

You are working in a Clean Architecture Java/Swing project.
You must NOT put UI code in use cases or entities.
All business logic goes into interactors.
All Swing logic goes into QuizWindow and ResultWindow.
The module scans a screenshot folder, extracts id/level/answer from filenames, and generates questions.json.
QuizInteractor loads Question entities, handles correctness checking, updates score, and moves to next question.
QuizPresenter exposes pure data and delegates UI rendering to QuizWindow.
Images must auto-scale to the window size.
Do not use external libraries other than standard Java + javax.swing + org.json.

🏁 11. Summary

This README provides:

Clean Architecture structure

Detailed module responsibilities

Full JSON generation pipeline

Quiz run pipeline

UI resizing behavior

Class responsibilities & method signatures

Clear integration strategy

Copilot startup rules
