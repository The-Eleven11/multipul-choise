package com.csc207.arcade.multiplechoice.interface_adapter;

import com.csc207.arcade.multiplechoice.use_case.quiz.QuizOutputBoundary;
import com.csc207.arcade.multiplechoice.use_case.quiz.QuizOutputData;
import com.csc207.arcade.multiplechoice.use_case.submit.SubmitAnswerOutputBoundary;
import com.csc207.arcade.multiplechoice.use_case.submit.SubmitAnswerOutputData;

/**
 * Presenter that formats data from interactors for the ViewModels.
 */
public class QuizPresenter implements QuizOutputBoundary, SubmitAnswerOutputBoundary {
    private final QuizViewModel quizViewModel;
    private final ResultsViewModel resultsViewModel;

    public QuizPresenter(QuizViewModel quizViewModel, ResultsViewModel resultsViewModel) {
        this.quizViewModel = quizViewModel;
        this.resultsViewModel = resultsViewModel;
    }

    @Override
    public void prepareQuizView(QuizOutputData data) {
        quizViewModel.setCurrentImagePath(data.getImagePath());
        quizViewModel.setQuestionProgressLabel(data.getQuestionProgress());
        quizViewModel.setIncorrectButton(null); // Clear previous button selection
        quizViewModel.setFeedbackState("NONE");
    }

    @Override
    public void prepareSuccessView(SubmitAnswerOutputData data) {
        quizViewModel.setIncorrectButton(data.getSelectedAnswer()); // Reuse this to track selected button
        quizViewModel.setFeedbackState("CORRECT");
    }

    @Override
    public void prepareFailView(SubmitAnswerOutputData data) {
        quizViewModel.setIncorrectButton(data.getSelectedAnswer());
        quizViewModel.setFeedbackState("INCORRECT");
    }

    @Override
    public void prepareResultsView(double accuracy, long totalTimeMs) {
        resultsViewModel.setAccuracy(accuracy);
        resultsViewModel.setTotalTimeMs(totalTimeMs);
    }
}
