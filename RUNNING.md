# Running the Multiple Choice Quiz Application

## Prerequisites

- Java 11 or higher
- Maven (for building from source)

## Quick Start

### Option 1: Using Maven (Recommended)

```bash
# Run the application
mvn clean compile exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.Main"
```

### Option 2: Using the JAR file

```bash
# Build the project
mvn clean package

# Run the JAR with dependencies
mvn exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.Main"
```

## Application Features

The application demonstrates a fully functional multiple choice quiz with:

- **15 sample questions** with placeholder images
- **Clean Architecture** implementation following SOLID principles
- **Immediate visual feedback** (red for incorrect, green for correct)
- **Automatic progression** to next question after correct answer
- **Results screen** showing accuracy and total time

## Project Structure

The project follows Clean Architecture principles with clear separation of concerns:

```
src/main/java/com/csc207/arcade/multiplechoice/
├── app/                    # Main entry point & utilities
├── data_access/            # Data layer (JSON repository)
├── entities/               # Business objects
├── interface_adapter/      # Controllers, Presenters, ViewModels
├── use_case/               # Application business logic
└── view/                   # UI components (Swing)
```

## Architecture

This module strictly follows **Clean Architecture** principles:

- **Entities**: `QuizQuestion`, `QuizSession` - core business objects
- **Use Cases**: `QuizInteractor`, `SubmitAnswerInteractor` - application logic
- **Interface Adapters**: `QuizController`, `QuizPresenter`, `QuizViewModel` - connecting layers
- **Frameworks & Drivers**: `QuizView`, `JsonQuestionRepository` - external interfaces

## Adding Your Own Questions

1. Place PNG images in `src/main/resources/data/images/` following this naming pattern:
   - `{id}\_level{level}\_answer{answer}.png`
   - Example: `id001_level1_answerB.png`

2. Run the data initializer to regenerate `questions.json`:
   ```bash
   mvn exec:java -Dexec.mainClass="com.csc207.arcade.multiplechoice.app.DataInitializer"
   ```

3. Run the application normally

## Development

### Building

```bash
mvn clean compile
```

### Running Tests

```bash
mvn test
```

### Packaging

```bash
mvn clean package
```

The JAR will be created in the `target/` directory.
