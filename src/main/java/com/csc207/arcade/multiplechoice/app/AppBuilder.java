package com.csc207.arcade.multiplechoice.app;

import com.csc207.arcade.multiplechoice.data_access.JsonQuestionRepository;
import com.csc207.arcade.multiplechoice.interface_adapter.QuizController;
import com.csc207.arcade.multiplechoice.interface_adapter.QuizPresenter;
import com.csc207.arcade.multiplechoice.interface_adapter.QuizViewModel;
import com.csc207.arcade.multiplechoice.interface_adapter.ResultsViewModel;
import com.csc207.arcade.multiplechoice.use_case.QuestionRepository;
import com.csc207.arcade.multiplechoice.use_case.quiz.QuizInteractor;
import com.csc207.arcade.multiplechoice.use_case.submit.SubmitAnswerInteractor;
import com.csc207.arcade.multiplechoice.view.QuizView;
import com.csc207.arcade.multiplechoice.view.ResultsView;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Application builder that constructs and wires all components following Clean Architecture.
 * This class handles dependency injection and application setup.
 */
public class AppBuilder {
    private QuestionRepository repository;
    private QuizViewModel quizViewModel;
    private ResultsViewModel resultsViewModel;
    private QuizPresenter presenter;
    private QuizInteractor quizInteractor;
    private SubmitAnswerInteractor submitAnswerInteractor;
    private QuizController quizController;
    private QuizView quizView;
    private ResultsView resultsView;

    /**
     * Builds the application by constructing all layers from the inside out.
     * Following Clean Architecture, dependencies point inward:
     * Frameworks -> Interface Adapters -> Use Cases -> Entities
     */
    public AppBuilder build() {
        // Layer 1: Data Access (implements interface from use case layer)
        repository = new JsonQuestionRepository();
        
        // Layer 2: Interface Adapters - ViewModels
        quizViewModel = new QuizViewModel();
        resultsViewModel = new ResultsViewModel();
        
        // Layer 3: Interface Adapters - Presenter
        presenter = new QuizPresenter(quizViewModel, resultsViewModel);
        
        // Layer 4: Use Cases - Interactors
        quizInteractor = new QuizInteractor(repository, presenter);
        
        return this;
    }

    /**
     * Launches the user interface.
     * Must be called after build().
     */
    public void launch() {
            // Create views
            // Note: Controller will be set after session is available
            quizView = new QuizView(null, quizViewModel);
            resultsView = new ResultsView(resultsViewModel);
            
            // Setup results view listener
            setupResultsViewListener();
            
            // Show the quiz view
            quizView.setVisible(true);
            
            // Start the quiz to create the session
            quizInteractor.execute(new com.csc207.arcade.multiplechoice.use_case.quiz.QuizInputData());
            
            // Now that session exists, create the submit answer interactor
            submitAnswerInteractor = new SubmitAnswerInteractor(
                quizInteractor.getCurrentSession(),
                presenter,
                presenter
            );
            
            // Create the controller with both interactors
            quizController = new QuizController(quizInteractor, submitAnswerInteractor);
            
            // Set the controller in the view
            quizView.setController(quizController);

    }

    /**
     * Sets up the property change listener to show results view when quiz completes.
     */
    private void setupResultsViewListener() {
        resultsViewModel.addPropertyChangeListener(new PropertyChangeListener() {
            private boolean resultsShown = false;
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (!resultsShown && ("accuracy".equals(name) || "totalTimeMs".equals(name))) {
                    resultsShown = true;
                    SwingUtilities.invokeLater(() -> {
                        quizView.setVisible(false);
                        resultsView.setVisible(true);
                    });
                }
            }
        });
    }
}
