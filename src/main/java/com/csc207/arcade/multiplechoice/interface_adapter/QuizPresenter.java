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
        quizViewModel.setFeedbackState("NONE");
    }

    @Override
    public void prepareSuccessView(SubmitAnswerOutputData data) {
        quizViewModel.setFeedbackState("CORRECT");
    }

    @Override
    public void prepareFailView(SubmitAnswerOutputData data) {
        quizViewModel.setFeedbackState("INCORRECT");
        quizViewModel.setIncorrectButton(data.getSelectedAnswer());
    }

    @Override
    public void prepareResultsView(double accuracy, long totalTimeMs) {
        resultsViewModel.setAccuracy(accuracy);
        resultsViewModel.setTotalTimeMs(totalTimeMs);
    }
}
