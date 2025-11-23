package com.csc207.arcade.multiplechoice.use_case.submit;

/**
 * Input boundary for submitting an answer.
 */
public interface SubmitAnswerInputBoundary {
    /**
     * Executes the submit answer use case.
     *
     * @param inputData Input data containing the selected answer
     */
    void execute(SubmitAnswerInputData inputData);
    
    /**
     * Advances to the next question after a correct answer.
     * Should be called by the controller after a delay to show feedback.
     */
    void advanceToNextQuestion();
}
